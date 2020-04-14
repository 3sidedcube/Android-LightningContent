package com.cube.storm.content.lib.push;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Environment;
import com.cube.storm.content.lib.helper.BundleHelper;
import timber.log.Timber;

import java.util.Map;

/**
 * Provides functionality related to handling content available pushes from the Storm server
 * <p/>
 * It is not hooked up by default. Apps should listen for pushes of type {@code CONTENT_AVAILABLE_PUSH_TYPE} and then
 * handle the push payload with {@link ContentAvailablePushHandler#handleContentAvailablePush}.
 */
public class ContentAvailablePushHandler
{
	/**
	 * Default notification type identifier for Storm content available pushes
	 */
	public static final String CONTENT_AVAILABLE_PUSH_TYPE = "background";

	/**
	 * Extracts data from a content-available push payload and decides whether to update or not
	 * <p/>
	 * The push payload contains a URI to a full bundle. This is to avoid receipients overloading the server by asking
	 * for a delta bundle. For this reason, content-available pushes should only be handled on devices with sufficient
	 * unmetered connectivity, battery, etc.
	 * <p/>
	 * The push payload also contains the timestamp of the most recent landmark publish before the update. This handler
	 * will not update past a landmark/
	 *
	 * @param data
	 */
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
