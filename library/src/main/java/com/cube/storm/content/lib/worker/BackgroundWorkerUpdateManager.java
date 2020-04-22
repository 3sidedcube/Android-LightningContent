package com.cube.storm.content.lib.worker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import com.cube.storm.content.lib.manager.UpdateManager;
import com.cube.storm.content.model.UpdateContentProgress;
import com.cube.storm.content.model.UpdateContentRequest;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import timber.log.Timber;

import java.util.UUID;

import static com.cube.storm.content.lib.worker.ContentUpdateWorker.UpdateType.DELTA;
import static com.cube.storm.content.lib.worker.ContentUpdateWorker.UpdateType.DIRECT_DOWNLOAD;
import static com.cube.storm.content.lib.worker.ContentUpdateWorker.UpdateType.FULL_BUNDLE;
import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Implementation of {@link UpdateManager} that uses Android {@link WorkManager} to invoke and schedule content checks.
 * <p/>
 * Only one content check - whether it's periodic or manually invoked - is allowed at a time.
 */
public class BackgroundWorkerUpdateManager implements UpdateManager
{
	private Subject<UpdateContentRequest> updates = PublishSubject.create();

	/**
	 * Constraints that the background workers should abide by
	 */
	@NonNull
	private static Constraints createWorkConstraints()
	{
		return new Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build();
	}

	/**
	 * Creates a {@link OneTimeWorkRequest} that when invoked will perform the specified Storm update request.
	 *
	 * @param updateType      Type of background update to perform
	 * @param updateTimestamp Optional timestamp representing the current local Storm bundle
	 * @param updateEndpoint  Optional endpoint from which to download data. This is only relevant for the DIRECT_DOWNLOAD UpdateType.
	 * @return
	 */
	@NonNull
	private static OneTimeWorkRequest createOneTimeWorkRequest(
		@NonNull ContentUpdateWorker.UpdateType updateType,
		@Nullable Long updateTimestamp,
		@Nullable String updateEndpoint
	)
	{
		Data.Builder inputDataBuilder = new Data.Builder()
			                                // When invoked, the worker will delegate to DefaultUpdateManager
			                                .putString(
				                                ContentUpdateWorker.INPUT_KEY_UPDATE_MANAGER,
				                                ContentUpdateWorker.UPDATE_MANAGER_IMPL_DEFAULT
			                                ).putInt(ContentUpdateWorker.INPUT_KEY_UPDATE_TYPE, updateType.ordinal());

		if (updateTimestamp != null)
		{
			inputDataBuilder =
				inputDataBuilder.putLong(ContentUpdateWorker.INPUT_KEY_UPDATE_TIMESTAMP, updateTimestamp);
		}

		if (updateEndpoint != null)
		{
			inputDataBuilder =
				inputDataBuilder.putString(ContentUpdateWorker.INPUT_KEY_UPDATE_ENDPOINT, updateEndpoint);
		}

		Data inputData = inputDataBuilder.build();
		return new OneTimeWorkRequest.Builder(ContentUpdateWorker.class)
			       .setConstraints(CONTENT_CHECK_WORK_CONSTRAINTS)
			       .setInputData(inputData)
			       .build();
	}

	@NonNull
	private static PeriodicWorkRequest createPeriodicWorkRequest()
	{
		Data inputData = new Data.Builder()
			                 // When invoked, the worker will delegate to BackgroundWorkerUpdateManager
			                 // This way, we can ensure there is only one work job executed concurrently
			                 .putString(ContentUpdateWorker.INPUT_KEY_UPDATE_MANAGER,
			                            ContentUpdateWorker.UPDATE_MANAGER_IMPL_WORKER
			                 ).putInt(ContentUpdateWorker.INPUT_KEY_UPDATE_TYPE, DELTA.ordinal()).build();
		return new PeriodicWorkRequest.Builder(ContentUpdateWorker.class, 20L, MINUTES, 10L, MINUTES)
			       .setConstraints(CONTENT_CHECK_WORK_CONSTRAINTS)
			       .setInputData(inputData)
			       .build();
	}

	private static final String CONTENT_CHECK_WORK_NAME = "storm_content_check";
	private static final String CONTENT_CHECK_SCHEDULE_NAME = "storm_content_check_schedule";
	private static final Constraints CONTENT_CHECK_WORK_CONSTRAINTS = createWorkConstraints();

	private WorkManager workManager;

	public BackgroundWorkerUpdateManager(@NonNull Context context)
	{
		workManager = WorkManager.getInstance(context);
	}

	@Override
	public void cancelPendingRequests()
	{
		workManager.cancelUniqueWork(CONTENT_CHECK_WORK_NAME);
	}

	@Override
	public UpdateContentRequest checkForBundle()
	{
		OneTimeWorkRequest workRequest = createOneTimeWorkRequest(FULL_BUNDLE, null, null);
		log(String.format("Enqueuing bundle check (%s)", workRequest.getId().toString()));
		workManager.enqueueUniqueWork(CONTENT_CHECK_WORK_NAME, ExistingWorkPolicy.APPEND, workRequest);
		UpdateContentRequest updateContentRequest = new UpdateContentRequest(
			workRequest.getId().toString(),
			FULL_BUNDLE,
			null,
			createWorkObservable(workRequest.getId())
		);
		updates.onNext(updateContentRequest);
		return updateContentRequest;
	}

	@Override
	public UpdateContentRequest checkForUpdates()
	{
		OneTimeWorkRequest workRequest = createOneTimeWorkRequest(DELTA, null, null);
		log(String.format("Enqueuing update check (%s)", workRequest.getId().toString()));
		workManager.enqueueUniqueWork(CONTENT_CHECK_WORK_NAME, ExistingWorkPolicy.APPEND, workRequest);
		UpdateContentRequest updateContentRequest = new UpdateContentRequest(
			workRequest.getId().toString(),
			DELTA,
			null,
			createWorkObservable(workRequest.getId())
		);
		updates.onNext(updateContentRequest);
		return updateContentRequest;
	}

	@Override
	public UpdateContentRequest checkForUpdates(long lastUpdate)
	{
		OneTimeWorkRequest workRequest = createOneTimeWorkRequest(DELTA, lastUpdate, null);
		log(String.format("Enqueuing update check from %d (%s)", lastUpdate, workRequest.getId().toString()));
		workManager.enqueueUniqueWork(CONTENT_CHECK_WORK_NAME, ExistingWorkPolicy.APPEND, workRequest);
		UpdateContentRequest updateContentRequest = new UpdateContentRequest(
			workRequest.getId().toString(),
			DELTA,
			lastUpdate,
			createWorkObservable(workRequest.getId())
		);
		updates.onNext(updateContentRequest);
		return updateContentRequest;
	}

	private Observable<UpdateContentProgress> createWorkObservable(UUID workId)
	{
		LiveData<WorkInfo> workInfoLiveData = workManager.getWorkInfoByIdLiveData(workId);
		BackgroundWorkerObserver workLiveDataObserver = new BackgroundWorkerObserver(workInfoLiveData);
		new Handler(Looper.getMainLooper()).post(() -> {
			workInfoLiveData.observeForever(workLiveDataObserver);
		});
		return workLiveDataObserver.getSubject();
	}

	@Override
	public UpdateContentRequest downloadUpdates(@NonNull String endpoint)
	{
		OneTimeWorkRequest workRequest = createOneTimeWorkRequest(DIRECT_DOWNLOAD, null, endpoint);
		log(String.format("Enqueuing download from %s (%s)", endpoint, workRequest.getId().toString()));
		workManager.enqueueUniqueWork(CONTENT_CHECK_WORK_NAME, ExistingWorkPolicy.APPEND, workRequest);
		UpdateContentRequest updateContentRequest = new UpdateContentRequest(
			workRequest.getId().toString(),
			DIRECT_DOWNLOAD,
			null,
			createWorkObservable(workRequest.getId())
		);
		updates.onNext(updateContentRequest);
		return updateContentRequest;
	}

	private void log(String s)
	{
		Timber.tag("storm_diagnostics").i(s);
	}

	@Override
	public void scheduleBackgroundUpdates()
	{
		Timber.tag("storm_diagnostics").i("Scheduling background content updates");
		PeriodicWorkRequest workRequest = createPeriodicWorkRequest();
		workManager.enqueueUniquePeriodicWork(CONTENT_CHECK_SCHEDULE_NAME,
		                                      ExistingPeriodicWorkPolicy.REPLACE,
		                                      workRequest
		);
	}

	@Override
	public Observable<UpdateContentRequest> updates()
	{
		return updates;
	}
}
