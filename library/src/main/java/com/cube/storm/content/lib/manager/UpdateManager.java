package com.cube.storm.content.lib.manager;

import android.text.TextUtils;
import android.util.Log;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Constants;
import com.cube.storm.content.lib.handler.GZIPTarCacheResponseHandler;
import com.cube.storm.util.lib.debug.Debug;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the manager class responsible for checking for and downloading updates from the server
 * <p/>
 * Access this class via {@link com.cube.storm.ContentSettings#getUpdateManager()}. Do not instantiate this class directly
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public abstract class UpdateManager
{
	/**
	 * Downloads the latest full bundle from the server
	 */
	public void checkForBundle()
	{
		ContentSettings.getInstance().getApiManager().checkForBundle(new JsonResponseHandler()
		{
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
					if (!TextUtils.isEmpty(getConnectionInfo().responseHeaders.get("Location")))
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
		ContentSettings.getInstance().getApiManager().checkForDelta(lastUpdate, new JsonResponseHandler()
		{
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
					if (!TextUtils.isEmpty(getConnectionInfo().responseHeaders.get("Location")))
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
		if (!TextUtils.isEmpty(ContentSettings.getInstance().getStoragePath()))
		{
			// download to temp file
			new File(ContentSettings.getInstance().getStoragePath() + "/delta").mkdir();

			AsyncHttpClient client = new AsyncHttpClient(endpoint);
			client.get(new GZIPTarCacheResponseHandler(ContentSettings.getInstance().getStoragePath() + "/delta")
			{
				@Override public void onByteChunkReceivedProcessed(long totalProcessed, long totalLength)
				{
					super.onByteChunkReceivedProcessed(totalProcessed, totalLength);

					if (ContentSettings.getInstance().getDownloadListener() != null)
					{
						ContentSettings.getInstance().getDownloadListener().onDownloadProgress(totalProcessed, totalLength);
					}
				}

				@Override public void onSuccess()
				{
					super.onSuccess();

					try
					{
						// delete the bundle
						new File(getFilePath() + "/bundle.tar").delete();

						File path = new File(ContentSettings.getInstance().getStoragePath());

						if (integrityCheck(getFilePath()))
						{
							// Move files from /delta to ../
							copyDirectory(new File(getFilePath()), new File(ContentSettings.getInstance().getStoragePath()));
							deleteRecursive(new File(getFilePath()));

							File manifest = new File(path, Constants.FILE_MANIFEST);
							JsonObject manifestJson = ContentSettings.getInstance().getFileManager().readFileAsJson(manifest).getAsJsonObject();

							// Create map of expected data
							Map<String, String[]> expectedContent = new HashMap<String, String[]>();
							expectedContent.put("pages", new File(path + "/" + Constants.FOLDER_PAGES + "/").list());
							expectedContent.put("languages", new File(path + "/" + Constants.FOLDER_LANGUAGES + "/").list());
							expectedContent.put("content", new File(path + "/" + Constants.FOLDER_CONTENT + "/").list());
							expectedContent.put("data", new File(path + "/" + Constants.FOLDER_DATA + "/").list());

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

									removeFiles(path + "/" + key + "/", expectedContent.get(key));
								}
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}

				@Override public void onFinish()
				{
					if (getConnectionInfo().responseCode >= 200 && getConnectionInfo().responseCode < 300)
					{
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
	 * This method compares the file hash with the hash in the manifest. If the hashes do not match, the bundle is discarded.
	 *
	 * @return true if the bundle has the correct integrity, false if it was discarded
	 */
	public boolean integrityCheck(String contentPath)
	{
		boolean correct = true;

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
				String actualHash = ContentSettings.getInstance().getFileManager().getFileHash(contentPath + "/" + folders[index] + "/" + filename);

				if (actualHash != null && !requiredHash.equals(actualHash))
				{
					correct = false;
					Log.w("LightningContent", String.format("File %s has the wrong hash! Expected %s but got %s", filename, requiredHash, actualHash));
					break;
				}
			}
		}

		if (!correct)
		{
			deleteRecursive(new File(contentPath));
		}

		return correct;
	}

	private void deleteRecursive(File fileOrDirectory)
	{
		if (fileOrDirectory.isDirectory())
		{
			File[] files = fileOrDirectory.listFiles();

			if (files != null)
			{
				for (File child : files)
				{
					deleteRecursive(child);
				}
			}
		}

		fileOrDirectory.delete();
	}

	private void copyDirectory(File sourceLocation, File targetLocation) throws IOException
	{
		if (sourceLocation.isDirectory())
		{
			if (!targetLocation.exists())
			{
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();

			for (String aChildren : children)
			{
				copyDirectory(new File(sourceLocation, aChildren), new File(targetLocation, aChildren));
			}
		}
		else
		{
			int buffer = 8192;

			InputStream in = new BufferedInputStream(new FileInputStream(sourceLocation), buffer);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(targetLocation), buffer);

			byte[] buf = new byte[buffer];
			int len;

			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}

			in.close();
			out.flush();
			out.close();
		}
	}
}
