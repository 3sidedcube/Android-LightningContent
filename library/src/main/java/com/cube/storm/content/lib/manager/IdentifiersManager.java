package com.cube.storm.content.lib.manager;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.model.StormApp;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * Identifiers manager class used for loading the identifiers structure found in the {@code data/} folder in the bundle.
 * <p/>
 * The identifiers file is used to allow the use of inter-app linking between different CMS systems. Each "app" object
 * is identified by the key of the object, where the key is the app ID defined in the CMS.
 * <p/>
 * Identifiers structure
 * <pre>
 {
	 "ARC_STORM-1-1": {
		 "android": {
		 	"packageName": "com.cube.arc.fa"
		 },
		 "ios": {
		 "countryCode": "us",
			 "iTunesId": "529160691",
			 "launcher": "ARCFA://"
		 },
		 "name": {
			 "en": "First aid",
			 "es": ""
	 	}
 	}
 }
 * </pre>
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public class IdentifiersManager
{
	@Getter private HashMap<String, StormApp> apps;

	private static IdentifiersManager instance;

	public static IdentifiersManager getInstance()
	{
		if (instance == null)
		{
			instance = new IdentifiersManager();
		}

		return instance;
	}

	/**
	 * Loads the file from the {@param path}
	 *
	 * @param path The path to the {@code identifiers.json} file
	 */
	public void loadApps(@NonNull Uri path)
	{
		try
		{
			InputStream stream = ContentSettings.getInstance().getFileFactory().loadFromUri(path);

			if (stream != null)
			{
				JsonObject appsObject = new JsonParser().parse(new InputStreamReader(new BufferedInputStream(stream, 8192))).getAsJsonObject();
				apps = new HashMap<String, StormApp>();

				for (Map.Entry<String, JsonElement> entry : appsObject.entrySet())
				{
					try
					{
						String appName = entry.getKey();
						String packageName = entry.getValue().getAsJsonObject().get("android").getAsJsonObject().get("packageName").getAsString();

						StormApp app = new StormApp();
						app.setPackageName(packageName);
						app.setAppId(appName);

						if (entry.getValue() != null && !entry.getValue().isJsonNull())
						{
							app.setName(new Gson().fromJson(entry.getValue().getAsJsonObject().get("name"), Map.class));
						}

						apps.put(appName, app);
					}
					catch (Exception e)
					{
					 	e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Looks up the package name of an app from its storm ID
	 *
	 * @param id The storm ID
	 *
	 * @return The package name, or null if it was not found
	 */
	@Nullable
	public String getAppPackageName(@NonNull String id)
	{
		return apps == null ? null : apps.get(id).getPackageName();
	}
}
