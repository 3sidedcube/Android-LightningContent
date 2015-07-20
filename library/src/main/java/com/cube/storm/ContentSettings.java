package com.cube.storm;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.cube.storm.content.lib.Environment;
import com.cube.storm.content.lib.factory.FileFactory;
import com.cube.storm.content.lib.listener.UpdateListener;
import com.cube.storm.content.lib.manager.APIManager;
import com.cube.storm.content.lib.manager.UpdateManager;
import com.cube.storm.content.lib.parser.BundleBuilder;
import com.cube.storm.content.lib.resolver.CacheResolver;
import com.cube.storm.util.lib.manager.FileManager;
import com.cube.storm.util.lib.resolver.AssetsResolver;
import com.cube.storm.util.lib.resolver.FileResolver;
import com.cube.storm.util.lib.resolver.Resolver;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * This is the entry point class of the library. To enable the use of the library, you must instantiate
 * a new {@link com.cube.storm.ContentSettings.Builder} object in your {@link android.app.Application} singleton class.
 * <p/>
 * This class should not be directly instantiated.
 * <p/>
 * Example instantiation code to include. You should include this code in your {@link android.app.Application} instance, before instantiating
 * your other Storm Settings classes.
 * <p/>
 * Here's an example settings
 <pre>
ContentSettings contentSettings = new ContentSettings.Builder(this)
	.appId("STORM-1-1")
	.contentBaseUrl("http://storm.cubeapis.com/")
	.contentVersion("latest")
	.updateListener(new UpdateListener()
	{
		@Override public void onUpdateDownloaded()
		{
			// bundle update downloaded
		}
	})
	.build();
 </pre>
 *
 * The update listener gets called when a bundle has been downloaded and extracted to the cache directory supplied
 * by the {@link com.cube.storm.ContentSettings#storagePath} which defaults to {@link android.content.Context#getFilesDir().getAbsolutePath())}.
 * <p/>
 * If you ae using the {@code LighteningLanguage} module, you should include the line {@code languageSettings.reloadLanguage(getApplicationContext());} in order
 * for the language pack to be applied to the current context of the application.
 *
 * @author Callum Taylor
 * @project LighteningContent
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
	 * Default private constructor
	 */
	private ContentSettings(){}

	/**
	 * Default {@link com.cube.storm.util.lib.manager.FileManager} to use throughout the module
	 */
	@Getter @Setter private FileManager fileManager;

	/**
	 * Default {@link com.cube.storm.content.lib.manager.APIManager} to use throughout the module
	 */
	@Getter @Setter private APIManager apiManager;

	/**
	 * Default {@link com.cube.storm.content.lib.manager.UpdateManager} to use throughout the module
	 */
	@Getter @Setter private UpdateManager updateManager;

	/**
	 * The path to the storage folder on disk
	 */
	@Getter @Setter private String storagePath;

	/**
	 * Storm app ID in the format {@code SYSTEM-SOCIETYID-APPID}
	 */
	@Getter @Setter private String appId;

	/**
	 * Base content URL to download from.
	 * <p/>
	 * Example url {@code https://demo.stormcorp.co/}. Link must end in a slash
	 */
	@Getter @Setter private String contentBaseUrl;

	/**
	 * Content URL version of the API
	 * <p/>
	 * Example version {@code v1.0}. Version must not end in a slash
	 */
	@Getter @Setter private String contentVersion;

	/**
	 * Content environment
	 * <p/>
	 * Defaults to {@link com.cube.storm.content.lib.Environment#LIVE}
	 */
	@Getter @Setter private Environment contentEnvironment;

	/**
	 * Listener instance for updates downloaded
	 */
	@Getter @Setter private UpdateListener updateListener;

	/**
	 * The gson builder class used to build classes such as manifest
	 */
	@Getter @Setter private BundleBuilder bundleBuilder;

	/**
	 * Factory class responsible for loading a file from disk based on its Uri
	 */
	@Getter @Setter private FileFactory fileFactory;

	/**
	 * Uri resolver used to load a file based on it's protocol.
	 */
	@Getter @Setter private Map<String, Resolver> uriResolvers = new LinkedHashMap<String, Resolver>(2);

	/**
	 * Authorization token to use when downloading updates/bundles from the test environment
	 */
	@Getter @Setter private String authorizationToken;

	/**
	 * The builder class for {@link com.cube.storm.ContentSettings}. Use this to create a new {@link com.cube.storm.ContentSettings} instance
	 * with the customised properties specific for your project.
	 * <p/>
	 * The following methods are required to be set before you can download any content
	 * <ul>
	 *     <li>{@link #appId(String)} or {@link #appId(String, int, int)}</li>
	 *     <li>{@link #contentBaseUrl(String)}</li>
	 *     <li>{@link #contentVersion}</li>
	 * </ul>
	 * <p/>
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

			APIManager(new APIManager(){});
			UpdateManager(new UpdateManager(){});

			fileFactory(new FileFactory(){});
			bundleBuilder(new BundleBuilder(){});

			registerUriResolver("file", new FileResolver());
			registerUriResolver("assets", new AssetsResolver(this.context));
			registerUriResolver("cache", new CacheResolver());

			storagePath(this.context.getFilesDir().getAbsolutePath());
			fileManager(FileManager.getInstance());
			contentEnvironment(Environment.LIVE);
		}

		/**
		 * Sets the new listener for updates
		 *
		 * @param listener The new listener
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder updateListener(@Nullable UpdateListener listener)
		{
			construct.updateListener = listener;
			return this;
		}

		/**
		 * Set the app id to use when dealing with Storm CMS
		 *
		 * @param id The ID of the app in the format {@code SYSTEM-SOCIETYID-APPID}
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder appId(@NonNull String id)
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
		public Builder appId(@NonNull String system, int society, int app)
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
		public Builder contentBaseUrl(@NonNull String baseUrl)
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
		public Builder contentVersion(@NonNull String version)
		{
			construct.contentVersion = version;
			return this;
		}

		/**
		 * Set the content URL to download bundles from
		 * <p/>
		 * If {@link com.cube.storm.content.lib.Environment#TEST} is set, you <b>must</b> call {@link #authorizationToken(String)}
		 *
		 * @param environment The environment of the endpoint.
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder contentEnvironment(@NonNull Environment environment)
		{
			construct.contentEnvironment = environment;
			return this;
		}

		/**
		 * Set the authorization header to be used for test environment
		 *
		 * @param token The valid authorization token
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder authorizationToken(@NonNull String token)
		{
			construct.authorizationToken = token;
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
		public Builder contentUrl(@NonNull String baseUrl, @NonNull String version, @NonNull Environment environment)
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
		public Builder storagePath(@NonNull String path)
		{
			construct.storagePath = path;
			return this;
		}

		/**
		 * Sets the default {@link com.cube.storm.content.lib.parser.BundleBuilder} for the module
		 *
		 * @param bundleBuilder The new {@link com.cube.storm.content.lib.parser.BundleBuilder}
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder bundleBuilder(@NonNull BundleBuilder bundleBuilder)
		{
			construct.bundleBuilder = bundleBuilder;
			return this;
		}

		/**
		 * Set the default file manager
		 *
		 * @param manager The new file manager
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder fileManager(@NonNull FileManager manager)
		{
			construct.fileManager = manager;
			return this;
		}

		/**
		 * Set the default API manager
		 *
		 * @param manager The new API manager
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder APIManager(@NonNull APIManager manager)
		{
			construct.apiManager = manager;
			return this;
		}

		/**
		 * Set the default Update manager
		 *
		 * @param manager The new Update manager
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder UpdateManager(@NonNull UpdateManager manager)
		{
			construct.updateManager = manager;
			return this;
		}

		/**
		 * Registers a uri resolver
		 *
		 * @param protocol The string protocol to register
		 * @param resolver The resolver to use for the registered protocol
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder registerUriResolver(@NonNull String protocol, @NonNull Resolver resolver)
		{
			construct.uriResolvers.put(protocol, resolver);
			return this;
		}

		/**
		 * Registers a uri resolvers
		 *
		 * @param resolvers The map of resolvers to register
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder registerUriResolver(@NonNull Map<String, Resolver> resolvers)
		{
			construct.uriResolvers.putAll(resolvers);
			return this;
		}

		/**
		 * Sets the default {@link com.cube.storm.content.lib.factory.FileFactory} for the module
		 *
		 * @param fileFactory The new {@link com.cube.storm.content.lib.factory.FileFactory}
		 *
		 * @return The {@link com.cube.storm.ContentSettings.Builder} instance for chaining
		 */
		public Builder fileFactory(@NonNull FileFactory fileFactory)
		{
			construct.fileFactory = fileFactory;
			return this;
		}

		/**
		 * Builds the final settings object and sets its instance. Use {@link #getInstance()} to retrieve the settings
		 * instance.
		 * <p/>
		 * Throws {@link java.lang.IllegalAccessException} if {@link #appId}, {@link #contentBaseUrl}, or {@link #contentVersion} has
		 * not been set.
		 *
		 * @return The newly set {@link com.cube.storm.ContentSettings} instance
		 */
		public ContentSettings build()
		{
			if (TextUtils.isEmpty(construct.appId)
			|| TextUtils.isEmpty(construct.contentBaseUrl)
			|| TextUtils.isEmpty(construct.contentVersion))
			{
				throw new IllegalArgumentException("You must provide an app id, content url, and version.");
			}

			return (ContentSettings.instance = construct);
		}
	}
}
