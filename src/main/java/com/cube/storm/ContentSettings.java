package com.cube.storm;

import android.content.Context;

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
		private boolean useExternalCache = true;
		private String cachePath;

		/**
		 * Default constructor
		 */
		public Builder(Context context)
		{
			this.construct = new ContentSettings();
			this.context = context.getApplicationContext();
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

		/**
		 * If not using external cache, set this to false and set the path to use on the file system
		 * @param useExternalCache {@code false} if using different cache location
		 */
		public ContentSettings setUseExternalCache(boolean useExternalCache)
		{
			this.useExternalCache = useExternalCache;
			return instance;
		}

		/**
		 * Set the path to use as the cache dir instead of external cache of the device
		 */
		public ContentSettings setPreferredCachePath(String cachePath)
		{
			this.cachePath = cachePath;
			return instance;
		}
	}
}
