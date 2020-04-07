package com.cube.storm.content.lib.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import com.cube.storm.content.model.UpdateContentProgress;
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
	public Observable<UpdateContentProgress> checkForBundle()
	{
		if (!canDownload())
		{
			return Observable.error(new IllegalStateException("Not connected to wifi"));
		}
		return delegate.checkForBundle();
	}

	@Override
	public Observable<UpdateContentProgress> checkForUpdates(long lastUpdate)
	{
		if (!canDownload())
		{
			return Observable.error(new IllegalStateException("Not connected to wifi"));
		}
		return delegate.checkForUpdates(lastUpdate);
	}

	private boolean canDownload()
	{
		return !preferences.getBoolean("wifi_only", false) || isConnectedToWifi();
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
	public Observable<Observable<UpdateContentProgress>> updates()
	{
		return delegate.updates();
	}
}
