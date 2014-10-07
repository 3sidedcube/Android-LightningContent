package com.cube.storm.content.lib.resolver;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.cube.storm.content.lib.manager.CacheManager;

import java.io.File;

/**
 * // TODO: Add class description
 *
 * @author Callum Taylor
 * @project StormContent
 */
public class CacheUriResolver
{
	/**
	 * Resolves a `cache://file/path` Uri into its actual path, either a `assets://file/path` or
	 * `file://file/path`.
	 *
	 * @param context The context to use
	 * @param uri The cache uri
	 *
	 * @return The found Uri, or null
	 */
	public Uri resolveUri(Context context, Uri uri)
	{
		if ("cache".equalsIgnoreCase(uri.getScheme()))
		{
			File f = new File(CacheManager.getCachePath() + "/" + uri.getHost() + "/" + uri.getPath());

			if (f.exists())
			{
				return Uri.fromFile(f);
			}
			else
			{
				try
				{
					String path = "";

					if (!TextUtils.isEmpty(uri.getHost()))
					{
						path += uri.getHost();
					}

					if (!TextUtils.isEmpty(uri.getPath()))
					{
						path += uri.getPath();
					}

					return Uri.parse("assets://" + path);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		return null;
	}
}
