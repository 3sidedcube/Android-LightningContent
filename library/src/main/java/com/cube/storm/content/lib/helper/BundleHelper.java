package com.cube.storm.content.lib.helper;

import android.net.Uri;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.model.Manifest;

public class BundleHelper
{
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
}
