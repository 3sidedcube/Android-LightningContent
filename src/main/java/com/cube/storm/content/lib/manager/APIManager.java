package com.cube.storm.content.lib.manager;

import android.text.TextUtils;

import com.cube.storm.content.ModuleSettings;
import com.cube.storm.content.ModuleSettings.Environment;
import com.cube.storm.content.lib.Constants;
import com.cube.storm.content.lib.D;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.AsyncHttpResponseHandler;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import java.util.ArrayList;

public class APIManager
{
	private static APIManager instance;

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

	public void checkForUpdate(int appId, long lastUpdate, String density, JsonResponseHandler response)
	{
		checkForUpdate(appId, lastUpdate, false, density, response);
	}

	public void checkForUpdate(int appId, long lastUpdate, boolean inTest, String density, JsonResponseHandler response)
	{
		Environment environment = inTest ? Environment.TEST : ModuleSettings.CONTENT_ENVIRONMENT;
		String urlPart = String.format(Constants.API_CONTENT_UPDATE, appId, lastUpdate, density, environment.getEnvironmentLabel());
		D.out("Checking updates %s%s", Constants.API_CONTENT_URL, urlPart);

		ArrayList<Header> headers = new ArrayList<Header>();
		if (!TextUtils.isEmpty(ModuleSettings.CONTENT_AUTH_TOKEN))
		{
			headers.add(new BasicHeader("Authorization", ModuleSettings.CONTENT_AUTH_TOKEN));
		}

		AsyncHttpClient client = new AsyncHttpClient(Constants.API_CONTENT_URL);
		client.setAllowRedirect(false);
		client.get(urlPart, null, headers, response);
	}

	public void checkForBundle(int appId, long lastUpdate, boolean inTest, String density, JsonResponseHandler response)
	{
		Environment environment = inTest ? Environment.TEST : ModuleSettings.CONTENT_ENVIRONMENT;
		String urlPart = String.format(Constants.API_BUNDLE, appId, density, environment.getEnvironmentLabel());
		D.out("Checking updates %s%s", Constants.API_CONTENT_URL, urlPart);

		ArrayList<Header> headers = new ArrayList<Header>();
		if (!TextUtils.isEmpty(ModuleSettings.CONTENT_AUTH_TOKEN))
		{
			headers.add(new BasicHeader("Authorization", ModuleSettings.CONTENT_AUTH_TOKEN));
		}

		if (lastUpdate > -1)
		{
			urlPart += "&timestamp=" + lastUpdate;
		}

		AsyncHttpClient client = new AsyncHttpClient(Constants.API_CONTENT_URL);
		client.setAllowRedirect(false);
		client.get(urlPart, null, headers, response);
	}

	public void downloadUpdate(String endpoint, AsyncHttpResponseHandler response)
	{
		AsyncHttpClient client = new AsyncHttpClient(endpoint);
		client.get(response);
	}
}
