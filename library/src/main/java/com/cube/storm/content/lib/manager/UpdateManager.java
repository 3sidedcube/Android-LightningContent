package com.cube.storm.content.lib.manager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cube.storm.content.lib.helper.BundleHelper;
import com.cube.storm.content.model.UpdateContentRequest;
import io.reactivex.Observable;

/**
 * This is the manager class responsible for checking for and downloading updates from the server
 * <p/>
 * Access this class via {@link com.cube.storm.ContentSettings#getUpdateManager()}. Do not instantiate this class directly
 */
public interface UpdateManager
{
	void cancelPendingRequests();

	/**
	 * Downloads the latest full bundle from the server that is valid for an app built at the specified buildTime.
	 * <p>
	 * This method will respect landmark publishes, and won't download bundles incompatible with a binary. Send in null
	 * to retrieve the latest bundle, irrespective of landmark publishes.
	 * <p>
	 * Despite the name this method will also download updates, not just check for them.
	 */
	UpdateContentRequest checkForBundle(@Nullable Long buildTime);

	/**
	 * Checks for updates on the server and downloads any new files in the form of a delta bundle
	 * <p>
	 * Reads the timestamp from which to check for updates from the local manifest file
	 * <p>
	 * Despite the name this method will also download updates, not just check for them.
	 */
	default UpdateContentRequest checkForUpdatesToLocalContent()
	{
		Long bundleTimestamp = BundleHelper.readContentTimestamp();

		if (bundleTimestamp == null)
		{
			throw new IllegalStateException("No bundle to update");
		}
		else
		{
			return checkForUpdates(bundleTimestamp);
		}
	}

	/**
	 * Checks for updates on the server and downloads any new files in the form of a delta bundle
	 * <p>
	 * Despite the name this method will also download updates, not just check for them.
	 *
	 * @param lastUpdate The time of the last update. Usually found in the {@code manifest.json} file
	 */
	UpdateContentRequest checkForUpdates(final long lastUpdate);

	/**
	 * Downloads a tar.gz file from the given endpoint
	 *
	 * @param endpoint The endpoint to the tar.gz bundle/delta file
	 */
	UpdateContentRequest downloadUpdates(@NonNull String endpoint);

	/**
	 * Schedules periodic background content updates that should run even if the app is closed.
	 */
	void scheduleBackgroundUpdates();

	/**
	 * Returns an observable which emits further observables representing content updates.
	 */
	Observable<UpdateContentRequest> updates();
}
