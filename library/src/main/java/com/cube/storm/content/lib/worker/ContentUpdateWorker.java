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

public class ContentUpdateWorker extends RxWorker
{
	public static final String INPUT_KEY_UPDATE_MANAGER = "update_manager_impl";
	public static final String INPUT_KEY_UPDATE_TYPE = "update_type";
	public static final String INPUT_KEY_UPDATE_ENDPOINT = "update_endpoint";
	public static final String INPUT_KEY_UPDATE_TIMESTAMP = "update_timestamp";

	public static final String UPDATE_MANAGER_IMPL_DEFAULT = "update_manager_impl_default";
	public static final String UPDATE_MANAGER_IMPL_WORKER = "update_manager_impl_worker";

	private static final long UPDATE_TIMESTAMP_UNINITIALIZED = 0L;

	public enum UpdateType
	{
		FULL_BUNDLE,
		DELTA,
		DIRECT_DOWNLOAD
	}

	private UpdateManager updateManager;
	private UpdateType updateType;
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
		updateTimestamp =
			workerParams.getInputData().getLong(INPUT_KEY_UPDATE_TIMESTAMP, UPDATE_TIMESTAMP_UNINITIALIZED);
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
				workJob = updateManager.checkForBundle();
				break;
			}
			case DELTA:
			{
				if (updateTimestamp == UPDATE_TIMESTAMP_UNINITIALIZED)
				{
					workJob = updateManager.checkForUpdates();
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
