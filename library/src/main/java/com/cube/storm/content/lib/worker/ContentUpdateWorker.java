package com.cube.storm.content.lib.worker;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.RxWorker;
import androidx.work.WorkerParameters;
import com.cube.storm.content.lib.manager.DefaultUpdateManager;
import com.cube.storm.content.lib.manager.UpdateManager;
import io.reactivex.Single;

public class ContentUpdateWorker extends RxWorker
{
	public static final String INPUT_KEY_UPDATE_MANAGER = "update_manager_impl";
	public static final String UPDATE_MANAGER_IMPL_DEFAULT = "update_manager_impl_default";
	public static final String UPDATE_MANAGER_IMPL_WORKER = "update_manager_impl_worker";

	private UpdateManager updateManager;

	public ContentUpdateWorker(
		@NonNull Context context, @NonNull WorkerParameters workerParams
	)
	{
		super(context, workerParams);
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
		return updateManager
			       .checkForUpdates()
			       .ignoreElements()
			       //.concatMapCompletable(updateContentProgress -> this.setProgress(updateContentProgress.toWorkerData()).ignoreElement())
			       .toSingleDefault(Result.success())
			       .onErrorReturn(err -> Result.failure(new Data.Builder().putString("error", err.getMessage()).build()));
	}
}
