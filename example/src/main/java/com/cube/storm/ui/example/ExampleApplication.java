package com.cube.storm.ui.example;

import android.app.Application;
import android.util.Log;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Environment;
import com.cube.storm.content.lib.helper.BundleHelper;
import com.cube.storm.content.lib.listener.UpdateListener;
import com.cube.storm.content.lib.worker.BackgroundWorkerUpdateManager;

public class ExampleApplication extends Application
{
	@Override
	public void onCreate()
	{
		super.onCreate();

		new ContentSettings.Builder(this)
			.appId(BuildConfig.STORM_APP_NAME)
			.contentBaseUrl(BuildConfig.STORM_API_BASE + "/")
			.contentVersion(BuildConfig.STORM_API_VERSION)
			.contentEnvironment(Environment.LIVE)
			.updateManager(new BackgroundWorkerUpdateManager(this))
			.updateListener(new UpdateListener()
			{
				@Override public void onUpdateDownloaded()
				{
					Log.d("3SC", "onUpdateDownloaded " + BundleHelper.readContentTimestamp());
				}
			})
			.build();

		BundleHelper.clearCache();

		Log.d("3SC", "Update content " + BundleHelper.readContentTimestamp());
		ContentSettings.getInstance().getUpdateManager().scheduleBackgroundUpdates();
	}
}
