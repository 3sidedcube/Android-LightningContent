package com.cube.storm.content.lib.resolver;

import android.net.Uri;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.cube.storm.ContentSettings;
import com.cube.storm.util.lib.resolver.Resolver;

import java.io.File;
import java.io.InputStream;

/**
 * Resolves a `cache://file/path` Uri into its actual path, either a `assets://file/path` or
 * `file://file/path`.
 * <p/>
 * Used when resolving a uri from Storm content (default scheme is {@code cache://}. It first checks the local cache directory
 * found in {@link com.cube.storm.ContentSettings#storagePath} for the file, and if it does not exist, will fallback to the
 * assets bundle path.
 * <p/>
 * Access this class via {@link com.cube.storm.ContentSettings#getUriResolvers()}. Do not instantiate this class directly.
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public class CacheResolver extends Resolver
{
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

	@Override public InputStream resolveFile(@NonNull Uri uri)
	{
		if ("cache".equalsIgnoreCase(uri.getScheme()))
		{
			Uri newUri = resolveUri(uri);

			if (newUri != null && !"cache".equalsIgnoreCase(newUri.getScheme()))
			{
				return resolveFile(newUri);
			}
		}
		else
		{
			Resolver resolver = ContentSettings.getInstance().getUriResolvers().get(uri.getScheme());

			if (resolver != null)
			{
				return resolver.resolveFile(uri);
			}
		}

		return null;
	}
}
