package com.cube.storm.content.lib.factory;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cube.storm.ContentSettings;
import com.cube.storm.util.lib.resolver.Resolver;

/**
 * Factory class used to resolve a file based on it's Uri
 *
 * @author Callum Taylor
 * @project StormContent
 */
public abstract class FileFactory
{
	/**
	 * Loads a file from disk based on its Uri location
	 *
	 * @param fileUri The file Uri to resolve
	 *
	 * @return The file byte array, nor null
	 */
	@Nullable
	public byte[] loadFromUri(@NonNull Uri fileUri)
	{
		Resolver resolver = ContentSettings.getInstance().getUriResolvers().get(fileUri.getScheme());

		if (resolver != null)
		{
			return resolver.resolveFile(fileUri);
		}

		return null;
	}
}
