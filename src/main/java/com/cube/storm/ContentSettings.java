package com.cube.storm;

import android.content.Context;

import com.cube.storm.content.lib.resolver.CacheResolver;
import com.cube.storm.util.lib.resolver.Resolver;

import lombok.Getter;

/**
 * This is the entry point class of the library. To enable the use of the library, you must instantiate
 * a new {@link com.cube.storm.ContentSettings.Builder} object in your {@link android.app.Application} singleton class.
 *
 * This class should not be directly instantiated.
 *
 * @author Callum Taylor
 * @project StormContent
 */
public class ContentSettings
{
	/**
	 * The singleton instance of the settings
	 */
	private static ContentSettings instance;

	/**
	 * The path to the storage folder on disk
	 */
	@Getter private String storagePath;

	/**
	 * Gets the instance of the {@link com.cube.storm.ContentSettings} class
	 * Throws a {@link IllegalAccessError} if the singleton has not been instantiated properly
	 *
	 * @return The instance
	 */
	public static ContentSettings getInstance()
	{
		if (instance == null)
		{
			throw new IllegalAccessError("You must build the Content settings object first using ContentSettings$Builder");
		}

		return instance;
	}

	/**
	 * The default resolver for the bundled content
	 */
	@Getter private Resolver defaultResolver;

	/**
	 * Default private constructor
	 */
	private ContentSettings(){}

	/**
	 * The builder class for {@link com.cube.storm.ContentSettings}. Use this to create a new {@link com.cube.storm.ContentSettings} instance
	 * with the customised properties specific for your project.
	 *
	 * Call {@link #build()} to build the settings object.
	 */
	public static class Builder
	{
		/**
		 * The temporary instance of the {@link com.cube.storm.ContentSettings} object.
		 */
		private ContentSettings construct;

		/**
		 * The application context
		 */
		private Context context;

		/**
		 * Default constructor
		 */
		public Builder(Context context)
		{
			this.construct = new ContentSettings();
			this.context = context.getApplicationContext();

			defaultResolver(new CacheResolver(context));
			storagePath(this.context.getFilesDir().getAbsolutePath());
		}

		/**
		 * Set the path to use as the storage dir
		 */
		public Builder storagePath(String path)
		{
			construct.storagePath = path;
			return this;
		}

		/**
		 * Sets the default resolver for the bundled content
		 *
		 * @param defaultResolver The new default resolver
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder defaultResolver(Resolver defaultResolver)
		{
			construct.defaultResolver = defaultResolver;
			return this;
		}

		/**
		 * Builds the final settings object and sets its instance. Use {@link #getInstance()} to retrieve the settings
		 * instance.
		 *
		 * @return The newly set {@link com.cube.storm.ContentSettings} instance
		 */
		public ContentSettings build()
		{
			return (ContentSettings.instance = construct);
		}
	}
}
