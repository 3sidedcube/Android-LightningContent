package com.cube.storm.content.lib.listener;

/**
 * Listener interface for when a delta or bundle has been downloaded and extracted.
 * <p/>
 * Use this interface to refresh your app when a new bundle has been downloaded
 *
 * @author Callum Taylor
 * @project Storm
 */
public interface UpdateListener
{
	/**
	 * Method called when an update has been successfully downloaded and extracted
	 */
	public void onUpdateDownloaded();
}