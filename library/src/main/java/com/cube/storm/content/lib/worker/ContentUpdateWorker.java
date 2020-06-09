package com.cube.storm.content.lib.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;
import com.cube.storm.content.lib.manager.DefaultUpdateManager;
import com.cube.storm.content.lib.manager.UpdateManager;
import com.cube.storm.content.model.UpdateContentRequest;
import io.reactivex.Single;
import timber.log.Timber;

/**
 * Background worker responsible for performing a task relating to content updates.
 * <p />
 * It is possible to tell this worker to do one of 4 different tasks:
 * <ol>
 *     <li>Full bundle check and download</li>
 *     <li>Delta bundle check and download from specified timestamp</li>
 *     <li>Delta bundle check and download using timestamp from local bundle</li>
 *     <li>Direct bundle download using specified URL</li>
 * </ol>
 * <p />
 * The worker will emit progress updates based on the progress of the underlying task.
 */
public class ContentUpdateWorker extends RxWorker
{
	public static final String INPUT_KEY_UPDATE_MANAGER = "update_manager_impl";
	public static final String INPUT_KEY_UPDATE_TYPE = "update_type";
	public static final String INPUT_KEY_UPDATE_ENDPOINT = "update_endpoint";
	public static final String INPUT_KEY_BUILD_TIMESTAMP = "build_timestamp";
	public static final String INPUT_KEY_UPDATE_TIMESTAMP = "update_timestamp";

	public static final String UPDATE_MANAGER_IMPL_DEFAULT = "update_manager_impl_default";
	public static final String UPDATE_MANAGER_IMPL_WORKER = "update_manager_impl_worker";

	private static final long TIMESTAMP_UNINITIALIZED = 0L;

	public enum UpdateType
	{
		FULL_BUNDLE,
		DELTA,
		DIRECT_DOWNLOAD
	}

	private UpdateManager updateManager;
	private UpdateType updateType;
	private long buildTimestamp;
	private long updateTimestamp;
	private String updateEndpoint;

	public ContentUpdateWorker(
		@NonNull Context context,
		@NonNull WorkerParameters workerParams
	)
	{
		super(context, workerParams);

		updateType =
			UpdateType.values()[workerParams.getInputData().getInt(INPUT_KEY_UPDATE_TYPE, UpdateType.DELTA.ordinal())];
		buildTimestamp = workerParams.getInputData().getLong(INPUT_KEY_BUILD_TIMESTAMP, TIMESTAMP_UNINITIALIZED);
		updateTimestamp = workerParams.getInputData().getLong(INPUT_KEY_UPDATE_TIMESTAMP, TIMESTAMP_UNINITIALIZED);
		updateEndpoint = workerParams.getInputData().getString(INPUT_KEY_UPDATE_ENDPOINT);

		String workerImpl = workerParams.getInputData().getString(INPUT_KEY_UPDATE_MANAGER);

		if (workerImpl == null)
		{
			workerImpl = UPDATE_MANAGER_IMPL_DEFAULT;
		}

		switch (workerImpl)
		{
			case UPDATE_MANAGER_IMPL_WORKER:
			{
				updateManager = new BackgroundWorkerUpdateManager(context);
				break;
			}
			case UPDATE_MANAGER_IMPL_DEFAULT:
			default:
			{
				updateManager = new DefaultUpdateManager();
				break;
			}
		}
	}

	@NonNull
	@Override
	public Single<Result> createWork()
	{
		try
		{
			return doCreateWork();
		}
		catch (Throwable err)
		{
			log(err);
			return Single.just(Result.failure());
		}
	}

	@NonNull
	private Single<Result> doCreateWork()
	{
		log("started with " + updateManager);

		UpdateContentRequest workJob = null;

		switch (updateType)
		{
			case FULL_BUNDLE:
			{
				if (buildTimestamp == TIMESTAMP_UNINITIALIZED)
				{
					workJob = updateManager.checkForBundle(null);
				}
				else
				{
					workJob = updateManager.checkForBundle(buildTimestamp);
				}
				break;
			}
			case DELTA:
			{
				if (updateTimestamp == TIMESTAMP_UNINITIALIZED)
				{
					if (buildTimestamp == TIMESTAMP_UNINITIALIZED)
					{
						workJob = updateManager.checkForUpdatesToLocalContent(null);
					}
					else
					{
						workJob = updateManager.checkForUpdatesToLocalContent(buildTimestamp);
					}
				}
				else
				{
					workJob = updateManager.checkForUpdates(updateTimestamp);
				}
				break;
			}
			case DIRECT_DOWNLOAD:
			{
				workJob = updateManager.downloadUpdates(updateEndpoint);
				break;
			}
		}

		if (workJob == null)
		{
			log("No job to perform");
			return Single.just(Result.failure());
		}

		return workJob
			       .getProgress()
			       .doOnNext(updateContentProgress -> this.setProgress(updateContentProgress.toWorkerData()))
			       .ignoreElements()
			       .toSingleDefault(Result.success())
			       .doOnSuccess(result -> log("Success"))
			       .doOnError(this::log)
			       .onErrorReturn(err -> Result.failure(new Data.Builder()
				                                            .putString("error", err.getMessage())
				                                            .build()));
	}

	@Override
	public void onStopped()
	{
		super.onStopped();
		log("stopped");
	}

	private void log(String s)
	{
		Timber.tag("storm_diagnostics").i("Background worker " + this.getId().toString() + " " + s);
	}

	private void log(Throwable err)
	{
		Timber.tag("storm_diagnostics").i(err, "Background worker " + this.getId().toString() + " error");
	}
}
