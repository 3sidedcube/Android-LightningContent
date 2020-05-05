package com.cube.storm.content.lib.policy;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;

public class SharedPreferencesPolicyManager implements PolicyManager
{
	public static final String PREF_KEY_WIFI_ONLY = "wifi_only";

	private ConnectivityManager connectivityManager;
	private SharedPreferences preferences;

	public SharedPreferencesPolicyManager(@NonNull Context context)
	{
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public boolean canUpdate()
	{
		return isConnectedToWifi() || isCellularDownloadPermitted();
	}

	@Override
	public boolean isCellularDownloadPermitted()
	{
		return !preferences.getBoolean(PREF_KEY_WIFI_ONLY, false);
	}

	/**
	 * Checks if the user is connected to wifi or not. Ensure you provide the {@link android.Manifest.permission#ACCESS_WIFI_STATE}
	 * permission in your manifest.
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

}
