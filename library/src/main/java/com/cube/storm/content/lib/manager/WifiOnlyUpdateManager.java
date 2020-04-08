package com.cube.storm.content.lib.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import com.cube.storm.content.lib.worker.ContentUpdateWorker;
import com.cube.storm.content.model.UpdateContentRequest;
import io.reactivex.Observable;

/**
 * {@link UpdateManager} which ensures updates only occur on a wi-fi connection, otherwise delegating to another
 * UpdateManager.
 */
public class WifiOnlyUpdateManager implements UpdateManager
{
	private ConnectivityManager connectivityManager;
	private SharedPreferences preferences;
	private UpdateManager delegate;

	public WifiOnlyUpdateManager(Context context, UpdateManager delegate)
	{
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		this.delegate = delegate;
	}

	@Override
	public void cancelPendingRequests()
	{
		delegate.cancelPendingRequests();
	}

	@Override
	public UpdateContentRequest checkForBundle()
	{
		if (!canDownload())
		{
			return new UpdateContentRequest(
				Long.toString(System.currentTimeMillis()),
				ContentUpdateWorker.UpdateType.FULL_BUNDLE,
				null,
				Observable.error(new IllegalStateException("Not connected to wifi"))
			);
		}
		return delegate.checkForBundle();
	}

	@Override
	public UpdateContentRequest checkForUpdates(long lastUpdate)
	{
		if (!canDownload())
		{
			return new UpdateContentRequest(
				Long.toString(System.currentTimeMillis()),
				ContentUpdateWorker.UpdateType.DELTA,
				lastUpdate,
				Observable.error(new IllegalStateException("Not connected to wifi"))
			);
		}
		return delegate.checkForUpdates(lastUpdate);
	}

	private boolean canDownload()
	{
		return !preferences.getBoolean("wifi_only", false) || isConnectedToWifi();
	}

	@Override
	public UpdateContentRequest downloadUpdates(@NonNull String endpoint)
	{
		if (!canDownload())
		{
			return new UpdateContentRequest(
				Long.toString(System.currentTimeMillis()),
				ContentUpdateWorker.UpdateType.DIRECT_DOWNLOAD,
				null,
				Observable.error(new IllegalStateException("Not connected to wifi"))
			);
		}
		return delegate.downloadUpdates(endpoint);
	}

	/**
	 * Checks if the user is connected to wifi or not. Ensure you provide the {@link android.Manifest.permission#ACCESS_WIFI_STATE} permission in
	 * your manifest.
	 *
	 * @return True if connected, or the check failed. False if not connected. May return true even if the user has no
	 * active internet connection with their selected wifi service
	 */
	private boolean isConnectedToWifi()
	{
		try
		{
			NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			return networkInfo.isConnected();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// Assume they are.
		return true;
	}

	@Override
	public void scheduleBackgroundUpdates()
	{
		delegate.scheduleBackgroundUpdates();
	}

	@Override
	public Observable<UpdateContentRequest> updates()
	{
		return delegate.updates();
	}
}
