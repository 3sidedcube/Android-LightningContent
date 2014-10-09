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

	@Getter private boolean useExternalCache;
	@Getter private String cachePath;

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
			throw new IllegalAccessError("You must build the Ui settings object first using UiSettings$Builder");
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

		private Context context;

		/**
		 * Default constructor
		 */
		public Builder(Context context)
		{
			this.construct = new ContentSettings();
			this.context = context.getApplicationContext();

			construct.useExternalCache = true;
		}

		/**
		 * If not using external cache, set this to false and set the path to use on the file system
		 * @param useExternal {@code false} if using different cache location
		 */
		public Builder setUseExternalCache(boolean useExternal)
		{
			construct.useExternalCache = useExternal;
			return this;
		}

		/**
		 * Set the path to use as the cache dir instead of external cache of the device
		 */
		public Builder setPreferredCachePath(String path)
		{
			construct.cachePath = path;
			defaultResolver(new CacheResolver(context));
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
