package com.cube.storm.content.lib.manager;

import android.net.Uri;

import com.cube.storm.ContentSettings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Identifiers manager class used for loading the identifiers structure
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public class IdentifiersManager
{
	@Getter private HashMap<String, String> apps;

	private static IdentifiersManager instance;

	public static IdentifiersManager getInstance()
	{
		if (instance == null)
		{
			synchronized (IdentifiersManager.class)
			{
				if (instance == null)
				{
					instance = new IdentifiersManager();
				}
			}
		}

		return instance;
	}

	public void loadApps(Uri path)
	{
		try
		{
			byte[] identifiersData = ContentSettings.getInstance().getFileFactory().loadFromUri(path);

			if (identifiersData != null)
			{
				String identifierStr = new String(identifiersData, "UTF-8");
				JsonObject appsObject = new JsonParser().parse(identifierStr).getAsJsonObject();
				apps = new HashMap<String, String>();

				for (Map.Entry<String, JsonElement> entry : appsObject.entrySet())
				{
					try
					{
						String appName = entry.getKey();
						String packageName = entry.getValue().getAsJsonObject().get("android").getAsJsonObject().get("packageName").getAsString();

						apps.put(appName, packageName);
					}
					catch (Exception e)
					{
					 	e.printStackTrace();
					}
				}
			}
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
	}

	public String getAppPackageName(String id)
	{
		return apps == null ? null : apps.get(id);
	}
}
