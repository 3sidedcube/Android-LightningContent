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
 * <p/>
 * Access this class via {@link com.cube.storm.ContentSettings#getBundleBuilder()}. Do not instantiate this class directly
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
