package com.cube.storm.content.lib.listener;

/**
 * Listener interface for when a delta or bundle has been downloaded and extracted.
 * <p/>
 * Use this interface to refresh your app when a new bundle has been downloaded
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public interface UpdateListener
{
	/**
	 * Method called when an update has been successfully downloaded and extracted
	 */
	public void onUpdateDownloaded();

	/**
	 * Called when the content check has been made
	 * @param hasUpdates If there are updates to download or not
	 */
	public void onUpdateCheckFinished(boolean hasUpdates);
}
