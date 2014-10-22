package com.cube.storm.content.lib.manager;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Constants;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

/**
 * This is the manager class responsible for checking for and downloading updates from the server
 * <p/>
 * This class should not be directly instantiated.
 *
 * @author Callum Taylor
 * @project StormContent
 */
public class APIManager
{
	private static APIManager instance;

	/**
	 * Gets the api manager singleton or creates one if its null
	 *
	 * @return The api manager singleton
	 */
	public static APIManager getInstance()
	{
		if (instance == null)
		{
			synchronized (APIManager.class)
			{
				if (instance == null)
				{
					instance = new APIManager();
				}
			}
		}

		return instance;
	}

	private APIManager(){}

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
			throw new Error("App ID set in ContentSettings$appId is an incorrect format");
		}

		String urlPart = String.format(Constants.API_CONTENT_UPDATE, appId, lastUpdate, ContentSettings.getInstance().getContentEnvironment().getEnvironmentLabel());

		AsyncHttpClient client = new AsyncHttpClient(ContentSettings.getInstance().getContentBaseUrl());
		client.setAllowRedirect(false);
		client.get(ContentSettings.getInstance().getContentVersion() + "/" + urlPart, response);
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
			appId = ContentSettings.getInstance().getAppId().split("_")[2];
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

		AsyncHttpClient client = new AsyncHttpClient(ContentSettings.getInstance().getContentBaseUrl());
		client.setAllowRedirect(false);
		client.get(ContentSettings.getInstance().getContentVersion() + "/" + urlPart, response);
	}
}
