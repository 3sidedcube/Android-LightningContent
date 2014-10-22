package com.cube.storm.content.lib.manager;

import android.text.TextUtils;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Constants;
import com.cube.storm.content.lib.event.RefreshContentEvent;
import com.cube.storm.content.lib.handler.GZIPTarCacheResponseHandler;
import com.cube.storm.content.lib.helper.BusHelper;
import com.cube.storm.util.lib.debug.Debug;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the manager class responsible for checking for and downloading updates from the server
 * <p/>
 * This class should not be directly instantiated.
 *
 * @author Callum Taylor
 * @project StormContent
 */
public class UpdateManager
{
	private static UpdateManager instance;

	/**
	 * Gets the update manager singleton or creates one if its null
	 *
	 * @return The update manager singleton
	 */
	public static UpdateManager getInstance()
	{
		if (instance == null)
		{
			synchronized (UpdateManager.class)
			{
				if (instance == null)
				{
					instance = new UpdateManager();
				}
			}
		}

		return instance;
	}

	/**
	 * Default private constructor
	 */
	private UpdateManager(){}

	/**
	 * Downloads the latest full bundle from the server
	 */
	public void checkForBundle()
	{
		APIManager.getInstance().checkForBundle(new JsonResponseHandler()
		{
			@Override public void onFailure()
			{
				try
				{
					Debug.out(getConnectionInfo());
					Debug.out(getContent());
				}
				catch (Exception e)
				{
					Debug.out(e);
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

	/**
	 * Checks for updates on the server and downloads any new files in the form of a delta bundle
	 *
	 * @param lastUpdate The time of the last update. Usually found in the {@code manifest.json} file
	 */
	public void checkForUpdates(long lastUpdate)
	{
		APIManager.getInstance().checkForDelta(lastUpdate, new JsonResponseHandler()
		{
			@Override public void onFailure()
			{
				try
				{
					Debug.out(getConnectionInfo());
					Debug.out(getContent());
				}
				catch (Exception e)
				{
					Debug.out(e);
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

	/**
	 * Downloads a tar.gz file from the given endpoint
	 *
	 * @param endpoint The endpoint to the tar.gz bundle/delta file
	 */
	public void downloadUpdates(String endpoint)
	{
		Debug.out("Downloading from %s", endpoint);

		if (!TextUtils.isEmpty(ContentSettings.getInstance().getStoragePath()))
		{
			AsyncHttpClient client = new AsyncHttpClient(endpoint);
			client.get(new GZIPTarCacheResponseHandler(ContentSettings.getInstance().getStoragePath())
			{
				private boolean refresh = false;

				@Override public void onFailure()
				{
					try
					{
						Debug.out(getConnectionInfo());
						Debug.out(getContent());
					}
					catch (Exception e)
					{
						Debug.out(e);
					}
				}

				@Override public void onSuccess()
				{
					try
					{
						File contentPath = new File(getContent());
						File manifest = new File(contentPath, Constants.FILE_MANIFEST);

						JsonObject manifestJson = ContentSettings.getInstance().getFileManager().readFileAsJson(manifest).getAsJsonObject();

						// Create map of expected data
						Map<String, String[]> expectedContent = new HashMap<String, String[]>();
						expectedContent.put("pages", new File(contentPath.getAbsolutePath() + "/" + Constants.FOLDER_PAGES + "/").list());
						expectedContent.put("languages", new File(contentPath.getAbsolutePath() + "/" + Constants.FOLDER_LANGUAGES + "/").list());
						expectedContent.put("content", new File(contentPath.getAbsolutePath() + "/" + Constants.FOLDER_CONTENT + "/").list());
						expectedContent.put("data", new File(contentPath.getAbsolutePath() + "/" + Constants.FOLDER_DATA + "/").list());

						for (String key : expectedContent.keySet())
						{
							if (manifestJson.has(key))
							{
								JsonArray pagesList = manifestJson.get(key).getAsJsonArray();

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

								removeFiles(contentPath.getAbsolutePath() + "/" + key + "/", expectedContent.get(key));
							}
						}

						integrityCheck(contentPath);
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					refresh = true;
				}

				@Override public void onFinish(boolean failed)
				{
					if (!failed && refresh)
					{
						BusHelper.getInstance().post(new RefreshContentEvent());

						if (ContentSettings.getInstance().getUpdateListener() != null)
						{
							ContentSettings.getInstance().getUpdateListener().onUpdateDownloaded();
						}
					}
				}
			});
		}
	}

	/**
	 * Purges files in a folder that do not exist in the manifest
	 *
	 * @param folderPath The folder to check, can be either, {@link Constants#FOLDER_PAGES}, {@link Constants#FOLDER_LANGUAGES}, {@link Constants#FOLDER_CONTENT}, or {@link Constants#FOLDER_DATA}
	 * @param fileList The list of files to delete
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

					if (!deleted)
					{
						Debug.out("%s was not deleted successfully", s);
					}
				}
			}
		}
	}

	/**
	 * Checks the integrity of each file currently stored in cache
	 * <p/>
	 * This method compares the file hash with the hash in the manifest. If the hashes do not match, the file is discarded.
	 */
	public void integrityCheck(File contentPath)
	{
		JsonObject manifest = new JsonParser().parse(ContentSettings.getInstance().getFileManager().readFileAsString(new File(contentPath, Constants.FILE_MANIFEST))).getAsJsonObject();

		String[] sections = {"pages", "data", "content", "languages"};
		String[] folders = {Constants.FOLDER_PAGES, Constants.FOLDER_DATA, Constants.FOLDER_CONTENT, Constants.FOLDER_LANGUAGES};

		for (int index = 0; index < sections.length; index++)
		{
			JsonArray pages = manifest.get(sections[index]).getAsJsonArray();
			for (JsonElement p : pages)
			{
				JsonObject page = p.getAsJsonObject();
				String filename = page.get("src").getAsString();
				String requiredHash = page.get("hash").getAsString();
				String actualHash = ContentSettings.getInstance().getFileManager().getFileHash(contentPath.getAbsolutePath() + "/" + folders[index] + "/" + filename);

				if (actualHash != null && !requiredHash.equals(actualHash))
				{
					Debug.out("File %s has the wrong hash! Expected %s but got %s", filename, requiredHash, actualHash);
				}
			}
		}
	}
}
