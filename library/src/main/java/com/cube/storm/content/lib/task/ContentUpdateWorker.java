package com.cube.storm.content.lib.task;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.cube.storm.content.lib.manager.DefaultUpdateManager;
import com.cube.storm.content.lib.manager.UpdateManager;

import java.util.concurrent.TimeUnit;

public class ContentUpdateWorker extends Worker
{
	private UpdateManager updateManager = new DefaultUpdateManager();

	public ContentUpdateWorker(
		@NonNull Context context,
		@NonNull WorkerParameters workerParams
	)
	{
		super(context, workerParams);
	}

	@NonNull
	@Override
	public Result doWork()
	{
		try
		{
			boolean isCompleted = updateManager.checkForUpdates().blockingAwait(9L, TimeUnit.MINUTES);

			if (!isCompleted)
			{
				return Result.retry();
			}

			return Result.success();
		}
		catch (Exception ex)
		{
			return Result.failure();
		}
	}
}
