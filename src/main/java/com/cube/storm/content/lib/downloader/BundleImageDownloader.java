package com.cube.storm.content.lib.downloader;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.cube.storm.content.lib.manager.CacheManager;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * // TODO: Add class description
 *
 * @author Callum Taylor
 * @project StormContent
 */
public class BundleImageDownloader extends BaseImageDownloader
{
	public BundleImageDownloader(Context context)
	{
		super(context);
	}

	@Override public InputStream getStream(String imageUri, Object extra) throws IOException
	{
		Uri uri = Uri.parse(imageUri);

		if ("cache".equalsIgnoreCase(uri.getScheme()))
		{
			File f = new File(CacheManager.getCachePath() + "/" + uri.getHost() + "/" + uri.getPath());

			if (f.exists())
			{
				return new FileInputStream(f);
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

					return context.getAssets().open(path);
				}
				catch (Exception e)
				{
					e.printStackTrace();
					return null;
				}
			}
		}

		return super.getStream(imageUri, extra);
	}
}
