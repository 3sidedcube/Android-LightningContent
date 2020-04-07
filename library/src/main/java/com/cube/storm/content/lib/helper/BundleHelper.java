package com.cube.storm.content.lib.helper;

import android.net.Uri;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.model.Manifest;

public class BundleHelper
{
	/**
	 * Deletes all files in {@link ContentSettings#getStoragePath()}
	 */
	public static void clearCache()
	{
		String path = ContentSettings.getInstance().getStoragePath();
		FileHelper.deleteRecursive(new File(path, "pages/"));
		FileHelper.deleteRecursive(new File(path, "data/"));
		FileHelper.deleteRecursive(new File(path, "content/"));
		FileHelper.deleteRecursive(new File(path, "languages/"));
		FileHelper.deleteRecursive(new File(path, "app.json"));
		FileHelper.deleteRecursive(new File(path, "manifest.json"));
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
