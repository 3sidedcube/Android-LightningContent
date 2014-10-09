package com.cube.storm.content.lib.manager;

import android.content.Context;
import android.text.TextUtils;

import com.cube.storm.content.lib.Constants;
import com.cube.storm.content.lib.Constants.ContentDensity;
import com.cube.storm.content.lib.D;
import com.cube.storm.content.lib.event.RefreshContentEvent;
import com.cube.storm.content.lib.handler.GZIPTarCacheResponseHandler;
import com.cube.storm.content.lib.helper.BusHelper;
import com.cube.storm.content.lib.helper.ReadHelper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class UpdateManager
{
	private static UpdateManager instance;
	private final Context context;

	public static UpdateManager getInstance(Context context)
	{
		if (instance == null)
		{
			synchronized (UpdateManager.class)
			{
				if (instance == null)
				{
					instance = new UpdateManager(context);
				}
			}
		}

		return instance;
	}

	public UpdateManager(Context c)
	{
		context = c;
	}

	/**
	 * Checks for updates on the server and downloads any new files
	 */
	public void checkForUpdates(int appId, long lastUpdate)
	{
		ContentDensity cd = ContentDensity.getDensityForSize(context);

		APIManager.getInstance().checkForUpdate(appId, lastUpdate, cd.getDensity(), new JsonResponseHandler()
		{
			@Override public void onFailure()
			{
				try
				{
					D.out(getConnectionInfo());
					D.out(getContent());
				}
				catch (Exception e)
				{
					D.out(e);
				}
			}

			@Override public void onSuccess()
			{
				if (getConnectionInfo().responseCode < 300 && getConnectionInfo().responseCode >= 200)
				{
					try
					{
						if (getConnectionInfo().responseCode == 200)
						{
							JsonElement response = getContent();

							if (response != null && response.isJsonObject())
							{
								if (response.getAsJsonObject().has("file"))
								{
									String endpoint = response.getAsJsonObject().get("file").getAsString();
									downloadUpdates(endpoint);
								}
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (getConnectionInfo().responseCode == 303)
				{
					if (getConnectionInfo().responseHeaders.containsKey("Location"))
					{
						String location = getConnectionInfo().responseHeaders.get("Location");
						downloadUpdates(location);
					}
				}
			}
		});
	}

	public void downloadUpdates(String endpoint)
	{
		D.out("Downloading updates from %s", endpoint);
		if (!TextUtils.isEmpty(CacheManager.getCachePath()))
		{
			APIManager.getInstance().downloadUpdate(endpoint, new GZIPTarCacheResponseHandler(CacheManager.getCachePath())
			{
				boolean refresh = false;

				@Override public void onFailure()
				{
					D.out(getConnectionInfo());
					D.out(getContent());
				}

				@Override public void onSuccess()
				{
					try
					{
						D.out("Extracting files");
						File f = new File(getContent());
						File manifest = new File(f, Constants.FILE_MANIFEST);

						JsonObject manu = ReadHelper.readJsonFromFileStream(new FileInputStream(manifest)).getAsJsonObject();

						// Create map of expected data
						Map<String, String[]> expectedContent = new HashMap<String, String[]>();
						expectedContent.put("pages", new File(f.getAbsolutePath() + "/" + Constants.FOLDER_PAGES + "/").list());
						expectedContent.put("languages", new File(f.getAbsolutePath() + "/" + Constants.FOLDER_LANGUAGES + "/").list());
						expectedContent.put("content", new File(f.getAbsolutePath() + "/" + Constants.FOLDER_CONTENT + "/").list());
						expectedContent.put("data", new File(f.getAbsolutePath() + "/" + Constants.FOLDER_DATA + "/").list());

						for (String key : expectedContent.keySet())
						{
							if (manu.has(key))
							{
								JsonArray pagesList = manu.get(key).getAsJsonArray();

								for (JsonElement e : pagesList)
								{
									String filename = e.getAsJsonObject().get("src").getAsString();

									int size = expectedContent.get(key) == null ? 0 : expectedContent.get(key).length;
									for (int index = 0; index < size; index++)
									{
										if (expectedContent.get(key)[index] != null && expectedContent.get(key)[index].equals(filename))
										{
											expectedContent.get(key)[index] = null;
											break;
										}
									}
								}

								removeFiles(f.getAbsolutePath() + "/" + key + "/", expectedContent.get(key));
							}
						}

						// check files integrity
						integrityCheck();
					}
					catch (FileNotFoundException e)
					{
						// re-download manifest
						e.printStackTrace();
					}

					refresh = true;
					//D.out(f.list());
				}

				@Override public void onFinish(boolean failed)
				{
					if (!failed && refresh)
					{
						D.out("SENDING EVENT");
						BusHelper.getInstance().post(new RefreshContentEvent());
					}

					/*App insersion = ViewParser.buildGson(CacheManager.getInstance().readFileAsJson(Constants.FILE_ENTRY_POINT), App.class);
					StormApplication.setAppEntry(insersion);

					String pageClass = insersion.getPageDescriptor(insersion.getVector()).getType();
					Intent main = new Intent(context, ((StormApplication)context.getApplicationContext()).getIntentHelper().getActivityForPage(context, pageClass));
					main.putExtra(Constants.EXTRA_FILE_NAME, insersion.getVector());
					context.startActivity(main);*/
				}
			});
		}
	}

	/**
	 * Purges files in a folder that do not exist in the manifest
	 *
	 * @param folderPath
	 *            The folder to check, can be either,
	 *            {@link Constants#FOLDER_PAGES},
	 *            {@link Constants#FOLDER_LANGUAGES}, or
	 *            {@link Constants#FOLDER_CONTENT}
	 * @param fileList
	 *            The list of files to delete
	 */
	public void removeFiles(String folderPath, String[] fileList)
	{
		if (fileList != null)
		{
			for (String s : fileList)
			{
				if (!TextUtils.isEmpty(s))
				{
					boolean deleted = new File(folderPath, s).delete();
					//D.out("%s deleted? %s", s, deleted);
				}
			}
		}
	}

	/**
	 * Checks the integrety of each file currently stored in cache
	 */
	public void integrityCheck()
	{
		JsonObject manifest = new JsonParser().parse(CacheManager.getInstance(context).readFileAsString(Constants.FILE_MANIFEST)).getAsJsonObject();

		// check app.json
		//String appHash = manifest.get(null)

		// pages
		JsonArray pages = manifest.get("pages").getAsJsonArray();
		for (JsonElement p : pages)
		{
			JsonObject page = p.getAsJsonObject();
			String filename = page.get("src").getAsString();
			String requiredHash = page.get("hash").getAsString();
			String actualHash = CacheManager.getInstance(context).getFileHash(Constants.FOLDER_PAGES + "/" + filename);

			//D.out("%s - %s = %s? %s", filename, requiredHash, actualHash, requiredHash.equals(actualHash));
			if (!actualHash.equals(requiredHash))
			{
				// download file again
			}
		}

		// content
		JsonArray content = manifest.get("content").getAsJsonArray();
		for (JsonElement p : content)
		{
			JsonObject page = p.getAsJsonObject();
			String filename = page.get("src").getAsString();
			String requiredHash = page.get("hash").getAsString();
			String actualHash = CacheManager.getInstance(context).getFileHash(Constants.FOLDER_CONTENT + "/" + filename);

			//D.out("%s - %s = %s? %s", filename, requiredHash, actualHash, requiredHash.equals(actualHash));
			if (!actualHash.equals(requiredHash))
			{
				// download file again
			}
		}

		// pages
		JsonArray languages = manifest.get("languages").getAsJsonArray();
		for (JsonElement p : languages)
		{
			JsonObject page = p.getAsJsonObject();
			String filename = page.get("src").getAsString();
			String requiredHash = page.get("hash").getAsString();
			String actualHash = CacheManager.getInstance(context).getFileHash(Constants.FOLDER_LANGUAGES + "/" + filename);

			//D.out("%s - %s = %s? %s", filename, requiredHash, actualHash, requiredHash.equals(actualHash));
			if (!actualHash.equals(requiredHash))
			{
				// download file again
			}
		}
	}
}
