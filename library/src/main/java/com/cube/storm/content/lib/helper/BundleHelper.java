package com.cube.storm.content.lib.helper;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Constants;
import com.cube.storm.content.model.Manifest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Utility methods relating to Storm content bundles and their contents
 */
public class BundleHelper
{
	/**
	 * Deletes all files in {@link ContentSettings#getStoragePath()}
	 */
	public static void clearCache()
	{
		Timber.tag("storm_diagnostics").i("Clearing cached content");
		String path = ContentSettings.getInstance().getStoragePath();
		FileHelper.deleteRecursive(new File(path, "pages/"));
		FileHelper.deleteRecursive(new File(path, "data/"));
		FileHelper.deleteRecursive(new File(path, "content/"));
		FileHelper.deleteRecursive(new File(path, "languages/"));
		FileHelper.deleteRecursive(new File(path, "app.json"));
		FileHelper.deleteRecursive(new File(path, "manifest.json"));
	}

	public static boolean hasContent()
	{
		return readContentTimestamp() != null;
	}

	/**
	 * Checks the integrity of each file currently stored in cache
	 * <p/>
	 * This method compares the file hash with the hash in the manifest. If the hashes do not match, the bundle is discarded.
	 *
	 * @return true if the bundle has the correct integrity, false if it was discarded
	 */
	public static boolean integrityCheck(String contentPath)
	{
		boolean correct = true;
		File manifestFile = new File(contentPath, Constants.FILE_MANIFEST);
		String contentManifest = ContentSettings.getInstance().getFileManager().readFileAsString(manifestFile);
		if(TextUtils.isEmpty(contentManifest))
		{
			return false;
		}
		
		JsonObject manifest =  JsonParser.parseString(contentManifest).getAsJsonObject();

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
			FileHelper.deleteRecursive(new File(contentPath));
		}

		return correct;
	}

	public static Long readContentTimestamp()
	{
		Uri manifestUri = Uri.parse("cache://manifest.json");
		Manifest manifest = ContentSettings.getInstance().getBundleBuilder().buildManifest(manifestUri);

		if (manifest == null)
		{
			return null;
		}

		return manifest.getTimestamp();
	}

	public static Long readInitialTimestamp()
	{
		Uri manifestUri = Uri.parse("assets://manifest.json");
		Manifest manifest = ContentSettings.getInstance().getBundleBuilder().buildManifest(manifestUri);

		if (manifest == null)
		{
			return null;
		}

		return manifest.getTimestamp();
	}
	
	public static void deleteUnexpectedFiles(File path)
	{
		File manifest = new File(path, Constants.FILE_MANIFEST);
		JsonObject manifestJson = ContentSettings.getInstance().getFileManager().readFileAsJson(manifest).getAsJsonObject();
		
		// Map of folder directories
		Map <String, String> folderDirectories = new HashMap <>();
		folderDirectories.put("pages", Constants.FOLDER_PAGES);
		folderDirectories.put("languages", Constants.FOLDER_LANGUAGES);
		folderDirectories.put("content", Constants.FOLDER_CONTENT);
		folderDirectories.put("data", Constants.FOLDER_DATA);
		
		// Create map of expected data
		Map <String, String[]> expectedContent = new HashMap <>();
		for (Map.Entry<String, String> entry : folderDirectories.entrySet())
		{
			expectedContent.put(entry.getKey(), new File(path + "/" + entry.getValue() + "/").list());
		}
		
		for (Map.Entry<String, String[]> entry : expectedContent.entrySet())
		{
			String key = entry.getKey();
			if(!manifestJson.has(key))
			{
				continue;
			}
			String[] filePaths = entry.getValue();
			if(filePaths == null)
			{
				continue;
			}
			JsonArray pagesList = manifestJson.get(key).getAsJsonArray();
			
			for (JsonElement e : pagesList)
			{
				String filename = e.getAsJsonObject().get("src").getAsString();
				
				int size = filePaths.length;
				for (int index = 0; index < size; index++)
				{
					if (filePaths[index] != null && filePaths[index].equals(filename))
					{
						filePaths[index] = null;
						break;
					}
				}
			}
			
			FileHelper.removeFiles(path + "/" + folderDirectories.get(key) + "/", filePaths);
		}
	}
}
