package com.cube.storm.content.lib.listener;

/**
 * Listener interface for when a delta or bundle is being downloaded.
 * <p/>
 * Use this interface to display a progress whilst the files are being downloaded
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public interface DownloadListener
{
	/**
	 * Method called when a chunk has been downloaded
	 *
	 * @param downloaded the amount downloaded
	 * @param total the total amount to download
	 */
	public void onDownloadProgress(long downloaded, long total);
}
