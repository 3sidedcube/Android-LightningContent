package com.cube.storm.ui.example;

import android.app.Application;
import android.util.Log;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Environment;
import com.cube.storm.content.lib.listener.UpdateListener;

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
			.updateListener(new UpdateListener()
			{
				@Override public void onUpdateDownloaded()
				{
					Log.d("3SC", "onUpdateDownloaded");
				}
			})
			.build();
	}
}
