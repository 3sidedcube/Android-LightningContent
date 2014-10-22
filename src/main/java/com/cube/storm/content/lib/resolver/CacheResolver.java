package com.cube.storm.content.lib.resolver;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cube.storm.ContentSettings;
import com.cube.storm.util.lib.resolver.Resolver;

import java.io.File;

/**
 * Resolves a `cache://file/path` Uri into its actual path, either a `assets://file/path` or
 * `file://file/path`.
 *
 * @author Callum Taylor
 * @project StormContent
 */
public class CacheResolver extends Resolver
{
	private Context context;

	public CacheResolver(Context context)
	{
		this.context = context;
	}

	@Override public Uri resolveUri(@NonNull Uri uri)
	{
		if ("cache".equalsIgnoreCase(uri.getScheme()))
		{
			File f = new File(ContentSettings.getInstance().getStoragePath() + "/" + uri.getHost() + "/" + uri.getPath());

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

	@Override public byte[] resolveFile(@NonNull Uri uri)
	{
		if ("file".equalsIgnoreCase(uri.getScheme()))
		{
			File f = new File(ContentSettings.getInstance().getStoragePath() + "/" + uri.getHost() + "/" + uri.getPath());
			return ContentSettings.getInstance().getFileManager().readFile(f);
		}
		else if ("assets".equalsIgnoreCase(uri.getScheme()))
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

				return ContentSettings.getInstance().getFileManager().readFile(context.getAssets().open(path));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if ("cache".equalsIgnoreCase(uri.getScheme()))
		{
			Uri newUri = resolveUri(uri);

			if (newUri != null && !"cache".equalsIgnoreCase(uri.getScheme()))
			{
				return resolveFile(newUri);
			}
		}

		return null;
	}
}
