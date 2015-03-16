package com.cube.storm.content.lib.manager;

import android.text.TextUtils;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Constants;
import com.cube.storm.content.lib.Environment;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;

/**
 * This is the manager class responsible for checking for and downloading updates from the server
 * <p/>
 * Access this class via {@link com.cube.storm.ContentSettings#getApiManager()}. Do not instantiate this class directly
 * <p/>
 * You should not need to use this class directly for checking for updates, instead, use the {@link com.cube.storm.ContentSettings#getUpdateManager()} class.
 * <p/>
 * Example code for checking for deltas.
 * <pre>
 Manifest manifest = ContentSettings.getInstance().getBundleBuilder().buildManifest(Uri.parse("cache://manifest.json"));
 long lastUpdate = 0;

 if (manifest != null)
 {
 	lastUpdate = manifest.getTimestamp();
 }

 ContentSettings.getInstance().getUpdateManager().checkForUpdates(lastUpdate);
 * </pre>
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public abstract class APIManager
{
	/**
	 * Checks the API for any delta updates.
	 * <p/>
	 * Uses the URLs defined in {@link com.cube.storm.ContentSettings#contentBaseUrl} and {@link com.cube.storm.ContentSettings#contentVersion} to check for
	 * updates since {@param lastUpdate}
	 *
	 * @param lastUpdate The time of the last update. Usually found in the {@code manifest.json} file
	 * @param response The response to use for downloading the delta
	 */
	public void checkForDelta(long lastUpdate, JsonResponseHandler response)
	{
		String appId = "";

		try
		{
			appId = ContentSettings.getInstance().getAppId().split("-")[2];
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("App ID set in ContentSettings#appId is an incorrect format");
		}

		String urlPart = String.format(Constants.API_CONTENT_UPDATE, appId, lastUpdate, ContentSettings.getInstance().getContentEnvironment().getEnvironmentLabel());

		ArrayList<Header> headers = new ArrayList<Header>();

		if (ContentSettings.getInstance().getContentEnvironment() == Environment.TEST)
		{
			if (TextUtils.isEmpty(ContentSettings.getInstance().getAuthorizationToken()))
			{
				throw new Error("Authorization token is empty, you must set this in ContentSettings$Builder.authorizationToken(String)");
			}

			headers.add(new BasicHeader("Authorization", "" + ContentSettings.getInstance().getAuthorizationToken()));
		}

		AsyncHttpClient client = new AsyncHttpClient(ContentSettings.getInstance().getContentBaseUrl());
		client.setAllowRedirect(false);
		client.get(ContentSettings.getInstance().getContentVersion() + "/" + urlPart, null, headers, response);
	}

	/**
	 * Downloads a full bundle from the server
	 * <p/>
	 * Uses the URLs defined in {@link com.cube.storm.ContentSettings#contentBaseUrl} and {@link com.cube.storm.ContentSettings#contentVersion} to download a full
	 * bundle
	 *
	 * @param response The response to use for downloading the bundle
	 */
	public void checkForBundle(JsonResponseHandler response)
	{
		checkForBundle(-1, response);
	}

	/**
	 * Downloads a full bundle from the server
	 * <p/>
	 * Uses the URLs defined in {@link com.cube.storm.ContentSettings#contentBaseUrl} and {@link com.cube.storm.ContentSettings#contentVersion} to download a full
	 * bundle since {@param lastUpdate}
	 *
	 * @param lastUpdate The time of the last update. Usually found in the {@code manifest.json} file
	 * @param response The response to use for downloading the bundle
	 */
	public void checkForBundle(long lastUpdate, JsonResponseHandler response)
	{
		String appId = "";

		try
		{
			appId = ContentSettings.getInstance().getAppId().split("-")[2];
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new Error("App ID set in ContentSettings$appId is an incorrect format");
		}

		String urlPart = String.format(Constants.API_BUNDLE, appId, ContentSettings.getInstance().getContentEnvironment().getEnvironmentLabel());

		if (lastUpdate > -1)
		{
			urlPart += "&timestamp=" + lastUpdate;
		}

		ArrayList<Header> headers = new ArrayList<Header>();

		if (ContentSettings.getInstance().getContentEnvironment() == Environment.TEST)
		{
			if (TextUtils.isEmpty(ContentSettings.getInstance().getAuthorizationToken()))
			{
				throw new Error("Authorization token is empty, you must set this in ContentSettings$Builder.authorizationToken(String)");
			}

			headers.add(new BasicHeader("Authorization", "" + ContentSettings.getInstance().getAuthorizationToken()));
		}

		AsyncHttpClient client = new AsyncHttpClient(ContentSettings.getInstance().getContentBaseUrl());
		client.setAllowRedirect(false);
		client.get(ContentSettings.getInstance().getContentVersion() + "/" + urlPart, null, headers, response);
	}
}
