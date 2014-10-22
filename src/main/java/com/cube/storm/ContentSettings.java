package com.cube.storm;

import android.content.Context;

import com.cube.storm.content.lib.Environment;
import com.cube.storm.content.lib.listener.UpdateListener;
import com.cube.storm.content.lib.resolver.CacheResolver;
import com.cube.storm.util.lib.manager.FileManager;
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
	 * Default {@link com.cube.storm.util.lib.manager.FileManager} to use throughout the module
	 */
	@Getter private FileManager fileManager;

	/**
	 * The path to the storage folder on disk
	 */
	@Getter private String storagePath;

	/**
	 * Storm app ID in the format {@code SYSTEM_SOCIETYID_APPID}
	 */
	@Getter private String appId;

	/**
	 * Base content URL to download from.
	 *
	 * Example url {@code https://demo.stormcorp.co/}. Link must end in a slash
	 */
	@Getter private String contentBaseUrl;

	/**
	 * Content URL version of the API
	 *
	 * Example version {@code v1.0}. Version must not end in a slash
	 */
	@Getter private String contentVersion;

	/**
	 * Content environment
	 *
	 * Defaults to {@link com.cube.storm.content.lib.Environment#LIVE}
	 */
	@Getter private Environment contentEnvironment;

	/**
	 * Listener instance for updates downloaded
	 */
	@Getter private UpdateListener updateListener;

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
			fileManager(FileManager.getInstance());
		}

		/**
		 * Sets the new listener for updates
		 *
		 * @param listener The new listener
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder updateListener(UpdateListener listener)
		{
			construct.updateListener = listener;
			return this;
		}

		/**
		 * Set the app id to use when dealing with Storm CMS
		 *
		 * @param id The ID of the app in the format {@code SYSTEM_SOCIETYID_APPID}
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder appId(String id)
		{
			construct.appId = id;
			return this;
		}

		/**
		 * Set the app id to use when dealing with Storm CMS
		 *
		 * @param system The system name
		 * @param society The society ID
		 * @param app The app number
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder appId(String system, int society, int app)
		{
			construct.appId = system + "_" + society + "_" + app;
			return this;
		}

		/**
		 * Set the content URL to download bundles from
		 *
		 * @param baseUrl The base URL to download content from. Example url {@code https://demo.stormcorp.co/}. Link must end in a slash
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder contentBaseUrl(String baseUrl)
		{
			construct.contentBaseUrl = baseUrl;
			return this;
		}

		/**
		 * Set the content URL to download bundles from
		 *
		 * @param version The version of the endpoint to download content from. Example version {@code v1.0}. Version must not end in a slash
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder contentVersion(String version)
		{
			construct.contentVersion = version;
			return this;
		}

		/**
		 * Set the content URL to download bundles from
		 *
		 * @param environment The environment of the endpoint.
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder contentEnvironment(Environment environment)
		{
			construct.contentEnvironment = environment;
			return this;
		}

		/**
		 * Set the content URL to download bundles from
		 *
		 * @param baseUrl The base URL to download content from. Example url {@code https://demo.stormcorp.co/}. Link must end in a slash
		 * @param version The version of the endpoint to download content from. Example version {@code v1.0}. Version must not end in a slash
		 * @param environment The environment of the endpoint.
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder contentUrl(String baseUrl, String version, Environment environment)
		{
			construct.contentBaseUrl = baseUrl;
			construct.contentVersion = version;
			construct.contentEnvironment = environment;

			return this;
		}

		/**
		 * Set the path to use as the storage dir
		 *
		 * @param path The file dir path to the storage folder
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder storagePath(String path)
		{
			construct.storagePath = path;
			return this;
		}

		/**
		 * Set the default file manager
		 *
		 * @param manager The new file manager
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder fileManager(FileManager manager)
		{
			construct.fileManager = manager;
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
