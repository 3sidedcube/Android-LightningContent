package com.cube.storm.content.lib.worker;

import android.content.Context;
import androidx.annotation.NonNull;
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
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

import static java.util.concurrent.TimeUnit.HOURS;

/**
 * Implementation of {@link UpdateManager} that uses Android {@link WorkManager} to invoke and schedule content checks.
 * <p/>
 * Only one content check - whether it's periodic or manually invoked - is allowed at a time.
 */
public class BackgroundWorkerUpdateManager implements UpdateManager
{
	private Subject<Observable<UpdateContentProgress>> updates = PublishSubject.create();

	@NonNull
	private static Constraints createWorkConstraints()
	{
		return new Constraints.Builder().setRequiredNetworkType(NetworkType.UNMETERED).build();
	}

	@NonNull
	private static OneTimeWorkRequest createOneTimeWorkRequest()
	{
		Data inputData = new Data.Builder()
			                 .putString(ContentUpdateWorker.INPUT_KEY_UPDATE_MANAGER,
			                            ContentUpdateWorker.UPDATE_MANAGER_IMPL_DEFAULT)
			                 .build();
		return new OneTimeWorkRequest.Builder(ContentUpdateWorker.class)
			       .setConstraints(CONTENT_CHECK_WORK_CONSTRAINTS)
			       .setInputData(inputData)
			       .build();
	}

	@NonNull
	private static PeriodicWorkRequest createPeriodicWorkRequest()
	{
		Data inputData = new Data.Builder()
			                 .putString(ContentUpdateWorker.INPUT_KEY_UPDATE_MANAGER,
			                            ContentUpdateWorker.UPDATE_MANAGER_IMPL_WORKER)
			                 .build();
		return new PeriodicWorkRequest.Builder(ContentUpdateWorker.class, 6L, HOURS, 3L, HOURS)
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
	public Observable<UpdateContentProgress> checkForBundle()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Observable<UpdateContentProgress> checkForUpdates(long lastUpdate)
	{
		// TODO: Incorporate lastUpdate
		OneTimeWorkRequest workRequest = createOneTimeWorkRequest();
		workManager.enqueueUniqueWork(CONTENT_CHECK_WORK_NAME, ExistingWorkPolicy.APPEND, workRequest);

		LiveData<WorkInfo> workInfoLiveData = workManager.getWorkInfoByIdLiveData(workRequest.getId());
		BackgroundWorkerObserver workLiveDataObserver = new BackgroundWorkerObserver(workInfoLiveData);
		updates.onNext(workLiveDataObserver.getSubject());
		workInfoLiveData.observeForever(workLiveDataObserver);
		return workLiveDataObserver.getSubject();
	}

	@Override
	public void scheduleBackgroundUpdates()
	{
		PeriodicWorkRequest workRequest = createPeriodicWorkRequest();
		workManager.enqueueUniquePeriodicWork(CONTENT_CHECK_SCHEDULE_NAME,
		                                      ExistingPeriodicWorkPolicy.KEEP,
		                                      workRequest);
	}

	@Override
	public Observable<Observable<UpdateContentProgress>> updates()
	{
		return updates;
	}
}
