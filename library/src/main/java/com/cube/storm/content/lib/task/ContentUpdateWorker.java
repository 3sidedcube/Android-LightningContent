package com.cube.storm.content.lib.task;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.model.Manifest;

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
		Uri manifestUri = Uri.parse("cache://manifest.json");
		Manifest manifest = ContentSettings.getInstance().getBundleBuilder().buildManifest(manifestUri);

		if (manifest == null)
		{
			return Result.failure();
		}

		long lastUpdate = manifest.getTimestamp();
		ContentSettings.getInstance().getUpdateManager().checkForUpdates(lastUpdate);

		return null;
	}
}
