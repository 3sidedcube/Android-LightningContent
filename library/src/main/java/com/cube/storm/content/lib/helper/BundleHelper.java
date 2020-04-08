package com.cube.storm.content.lib.helper;

import android.net.Uri;
import android.util.Log;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Constants;
import com.cube.storm.content.model.Manifest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import timber.log.Timber;

import java.io.File;

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
}
