package com.cube.storm.content.lib.push;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Environment;
import com.cube.storm.content.lib.helper.BundleHelper;
import timber.log.Timber;

import java.util.Map;

/**
 * Provides functionality related to handling content available pushes from the Storm server
 */
public class ContentAvailablePushHandler
{
	public static final String CONTENT_AVAILABLE_PUSH_TYPE = "background";

	public static void handleContentAvailablePush(Map<String, String> data)
	{
		Timber.tag("storm_diagnostics").i("Handling content available push: " + data.toString());

		if (ContentSettings.getInstance().getContentEnvironment() != Environment.LIVE)
		{
			return;
		}

		String remoteEndpoint = data.get("filename");
		String remoteTimestampString = data.get("timestamp");
		String previousLandmarkTimestampString = data.get("latestLandmarkTimestamp");

		if (remoteEndpoint == null || remoteTimestampString == null)
		{
			Timber.tag("storm_diagnostics").i("Content available push invalid");
			return;
		}

		long remoteTimestamp = Long.parseLong(remoteTimestampString);
		long previousLandmarkTimestamp =
			previousLandmarkTimestampString != null ? Long.parseLong(previousLandmarkTimestampString) : 0L;

		Long localTimestamp = BundleHelper.readContentTimestamp();

		if (localTimestamp == null)
		{
			Timber.tag("storm_diagnostics").i("Local bundle timestamp not found");
			return;
		}


			Timber.tag("storm_diagnostics").i("Local bundle timestamp: " + localTimestamp);

		// If the remote bundle is earlier or the same as our local one then stop
		if (localTimestamp >= remoteTimestamp)
		{
			Timber.tag("storm_diagnostics").i("App already up-to-date");
			return;
		}

		// If the local bundle is not allowed to upgrade to the remote bundle because of a landmark publish then stop
		if (localTimestamp < previousLandmarkTimestamp)
		{
			Timber.tag("storm_diagnostics").i("App cannot update this far");
			return;
		}


		Timber.tag("storm_diagnostics").i("Downloading from " + remoteEndpoint);
		ContentSettings.getInstance().getUpdateManager().downloadUpdates(remoteEndpoint);
	}
}
