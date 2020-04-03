package com.cube.storm.content.lib.task;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.cube.storm.ContentSettings;

import java.util.concurrent.TimeUnit;

public class ContentUpdateWorker extends Worker
{
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
			boolean isCompleted = ContentSettings.getInstance()
			                                     .getUpdateManager()
			                                     .checkForUpdates()
			                                     .blockingAwait(9L, TimeUnit.MINUTES);

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
