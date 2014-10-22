package com.cube.storm.content.lib.parser;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.model.Manifest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.io.UnsupportedEncodingException;

/**
 * Gson processor used to build models in the content such as manifest.json
 * <p/>
 * Access this class via {@link com.cube.storm.ContentSettings#getBundleBuilder()}. Do not instantiate this class directly
 *
 * @author Callum Taylor
 * @project StormContent
 */
public abstract class BundleBuilder
{
	private static Gson builder;

	/**
	 * Required to include view overrides
	 */
	public void rebuild()
	{
		builder = null;
		getGson();
	}

	/**
	 * Gets the gson object with the registered storm view type adapters. Use {@link #build(com.google.gson.JsonElement, Class)} or
	 * {@link #build(String, Class)} to build your page/view objects.
	 *
	 * @return The gson object
	 */
	private Gson getGson()
	{
		if (builder == null)
		{
			GsonBuilder gsonBuilder = new GsonBuilder();
			builder = gsonBuilder.create();
		}

		return builder;
	}

	/**
	 * Builds a Manifest object from a file Uri
	 *
	 * @param fileUri The file Uri to load from
	 *
	 * @return The manifest data or null
	 */
	@Nullable
	public Manifest buildManifest(@NonNull Uri fileUri)
	{
		byte[] pageData = ContentSettings.getInstance().getFileFactory().loadFromUri(fileUri);

		if (pageData != null)
		{
			return buildManifest(pageData);
		}

		return null;
	}

	/**
	 * Builds a manifest object from a byte array json string
	 *
	 * @param manifest The byte array json string manifest data
	 *
	 * @return The manifest data, or null
	 */
	@Nullable
	public Manifest buildManifest(@NonNull byte[] manifest)
	{
		try
		{
			return build(new String(manifest, "UTF-8"), Manifest.class);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Builds a manifest object from a json string
	 *
	 * @param manifest The json string manifest data
	 *
	 * @return The manifest data, or null
	 */
	@Nullable
	public Manifest buildManifest(@NonNull String manifest)
	{
		return build(manifest, Manifest.class);
	}

	/**
	 * Builds a manifest object from a json element
	 *
	 * @param manifest The json element manifest data
	 *
	 * @return The manifest data, or null
	 */
	@Nullable
	public Manifest buildManifest(@NonNull JsonElement manifest)
	{
		return build(manifest, Manifest.class);
	}

	/**
	 * Builds a class from a json string input
	 *
	 * @param input The json string input to build from
	 * @param outClass The out class type
	 * @param <T> The type of class returned
	 *
	 * @return The built object, or null
	 */
	@Nullable
	public <T> T build(String input, Class<T> outClass)
	{
		return outClass.cast(getGson().fromJson(input, outClass));
	}

	/**
	 * Builds a class from a json element input
	 *
	 * @param input The json element input to build from
	 * @param outClass The out class type
	 * @param <T> The type of class returned
	 *
	 * @return The built object, or null
	 */
	@Nullable
	public <T> T build(JsonElement input, Class<T> outClass)
	{
		return outClass.cast(getGson().fromJson(input, outClass));
	}
}
