package com.cube.storm.content

import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import com.android.build.api.variant.BuildConfigField
import de.undercouch.gradle.tasks.download.Download
import groovyx.net.http.NativeHandlers
import org.gradle.api.Plugin
import org.gradle.api.Project

import static groovyx.net.http.ContentTypes.JSON
import static groovyx.net.http.HttpBuilder.configure

class StormPlugin implements Plugin<Project> {

	void apply(Project project) {

		// Create a Storm extension on:
		// i) the default Android configuration
		// ii) any defined build types (e.g. debug and release)
		// iii) any defined product flavors (e.g. staging and production)
		project.android.defaultConfig.extensions.create("storm", StormExtension)
		project.android.buildTypes.all { buildType ->
			buildType.extensions.create("storm", StormExtension)
		}
		project.android.productFlavors.whenObjectAdded { flavor ->
			flavor.extensions.create("storm", StormExtension)
		}

		/**
		 * Create an authentication task as a task dependency for any bundle download tasks requiring an auth token
		 *
		 * Two storm properties need to be supplied in the android defaultConfig for this to work:
		 * - authUsername - Username to auth with
		 * - authPassword - Password to auth with
		 */
		String authToken = null
		def authTask = project.tasks.register("stormAuthenticate") {
			doLast {
				String uri = "https://auth.cubeapis.com/v1.6/authentication"
				println "Authenticating ${project.android.defaultConfig.storm.authUsername} at ${uri}"
				configure {
					request.uri = uri
					request.contentType = JSON[0]
					response.parser(JSON[0]) { config, resp ->
						Object jsonMap = NativeHandlers.Parsers.json(config, resp)
						authToken = jsonMap["token"]
						println "Successfully retrieved auth token for user ${project.android.defaultConfig.storm.authUsername}: ${authToken}"
					}
				}.post() {
					request.body = new AuthRequest(project.android.defaultConfig.storm.authUsername, project.android.defaultConfig.storm.authPassword)
					response.failure { response ->
						println "ERROR: Could not authenticate with Storm"
					}
				}
			}
		}

		def android = project.android

		// Capture invocation state at configuration time so the task predicates below don't touch the
		// Project object at execution time (which the Gradle 9 configuration cache forbids).
		boolean offline = project.gradle.startParameter.isOffline()
		boolean explicitStormRequest = project.gradle.startParameter.taskNames.any { name -> name.startsWith("storm") }

		/**
		 * Create download and unpack tasks for each unique app variant.
		 *
		 * Migrated from the legacy {@code android.applicationVariants} iterator (removed in AGP 9) to the
		 * stable {@code AndroidComponents.onVariants} API, which exists since AGP 7.2 and so supports
		 * AGP 7.2 -> 9.x from a single code path.
		 */
		def androidComponents = project.extensions.getByType(ApplicationAndroidComponentsExtension)
		androidComponents.onVariants(androidComponents.selector().all()) { variant ->

			/**
			 * First of all merge all the Storm configuations for this bundle together. These come from (in order of priority):
			 * 1) the build type for the variant (e.g. debug / release)
			 * 2) each flavor comprising the variant
			 * 3) the Android default config
			 *
			 * The new variant API exposes the build type and flavors by name only, so the DSL objects that
			 * carry the merged {@code storm { }} extension are looked up back off the android extension.
			 */
			def mergedStormConfig = new StormExtension()

			def buildType = android.buildTypes.findByName(variant.buildType)
			if (buildType?.storm != null) {
				mergedStormConfig = mergedStormConfig.merge(buildType.storm)
			}
			variant.productFlavors.each { flavorPair ->
				def flavor = android.productFlavors.findByName(flavorPair.second)
				if (flavor?.storm != null) {
					mergedStormConfig = mergedStormConfig.merge(flavor.storm)
				}
			}
			mergedStormConfig = mergedStormConfig.merge(android.defaultConfig.storm)
			mergedStormConfig = mergedStormConfig.merge(new StormExtension(bundleDownloadStrategy: variant.buildType == "release" ? BundleDownloadStrategy.ALWAYS : BundleDownloadStrategy.IF_MISSING))

			if (!mergedStormConfig.isValid()) {
				println "ERROR: Incomplete Storm configuration for \"${variant.name}\": ${mergedStormConfig}"
				return
			}

			println "Storm configuration for \"${variant.name}\": ${mergedStormConfig}"

			// Inject the Storm BuildConfig fields for this variant (requires buildFeatures { buildConfig true }).
			variant.buildConfigFields.put("STORM_API_BASE", new BuildConfigField("String", "\"${mergedStormConfig.apiBase}\"".toString(), null))
			variant.buildConfigFields.put("STORM_API_VERSION", new BuildConfigField("String", "\"${mergedStormConfig.apiVersion}\"".toString(), null))
			variant.buildConfigFields.put("STORM_API_URL", new BuildConfigField("String", "\"${mergedStormConfig.apiBase}/${mergedStormConfig.apiVersion}\"".toString(), null))
			variant.buildConfigFields.put("STORM_APP_ID", new BuildConfigField("String", "\"${mergedStormConfig.appId}\"".toString(), null))
			variant.buildConfigFields.put("STORM_ORG_ID", new BuildConfigField("String", "\"${mergedStormConfig.orgId}\"".toString(), null))
			variant.buildConfigFields.put("STORM_ORG_NAME", new BuildConfigField("String", "\"${mergedStormConfig.orgName}\"".toString(), null))
			variant.buildConfigFields.put("STORM_APP_NAME", new BuildConfigField("String", "\"${mergedStormConfig.orgName}-${mergedStormConfig.orgId}-${mergedStormConfig.appId}\"".toString(), null))

			// NEVER means this variant is not bundled with Storm content: emit no download / unpack / assets.
			if (mergedStormConfig.bundleDownloadStrategy == BundleDownloadStrategy.NEVER) {
				println "Skipping bundle download/unpack for \"${variant.name}\" as strategy is set to NEVER"
				return
			}

			def strategy = mergedStormConfig.bundleDownloadStrategy
			boolean requiresAuth = mergedStormConfig.requiresAuth()
			String url = mergedStormConfig.url
			String authUsername = mergedStormConfig.authUsername
			String bypassKey = mergedStormConfig.bypassKey

			String sanitisedUrl = url.replaceAll("[\\\\/:*?\"<>|]", "_")
			def archiveFile = project.layout.buildDirectory.file("storm/${sanitisedUrl}/bundle.tar.gz")
			String downloadTaskName = "stormDownload${sanitisedUrl.capitalize()}"

			def downloadTask = project.tasks.names.contains(downloadTaskName)
					? project.tasks.named(downloadTaskName)
					: project.tasks.register(downloadTaskName, Download) {
				if (requiresAuth) {
					dependsOn authTask
				}
				/**
				 * Only download if the flag to enable downloading on every assembly is explicitly requested
				 */
				onlyIf {
					if (offline) {
						println "Skipping bundle download in offline mode"
						return false
					}

					// Always download if explicitly requested
					if (explicitStormRequest) {
						println "Explicitly requested download"
						return true
					}

					switch (strategy) {
						case BundleDownloadStrategy.ALWAYS:
							println "Downloading bundle as strategy is set to ALWAYS"
							return true
						case BundleDownloadStrategy.IF_MISSING:
							if (archiveFile.get().asFile.exists()) {
								println "Skipping bundle download as cached archive exists on disk and strategy is IF_MISSING"
								return false
							} else {
								println "Downloading initial bundle as no cached copy exists and strategy is IF_MISSING"
								return true
							}
						case BundleDownloadStrategy.NEVER:
							println "Skipping bundle download as strategy is set to NEVER"
							return false
					}

					return false
				}
				doFirst { downloadTaskRef ->
					println "Downloading from ${downloadTaskRef.src}..."
					if (requiresAuth) {
						println "Setting auth token for user ${authUsername}: ${authToken}"
						downloadTaskRef.header "Authorization", authToken
					}
					if (!bypassKey.isEmpty()) {
						downloadTaskRef.header "x-bypass-key", bypassKey
					}
				}
				src url
				dest archiveFile.get().asFile
				overwrite true
			}

			def unpackTask = project.tasks.register("stormUnpack${variant.name.capitalize()}Bundle", StormUnpackTask) {
				dependsOn downloadTask
				archive.set(archiveFile)
				// The task is referenced via the explicit closure parameter (not the implicit delegate) so
				// the predicate survives Gradle 9 configuration-cache serialization, which replaces a
				// closure's owner/delegate with a "broken" placeholder.
				onlyIf { unpackTaskRef ->
					// Nothing to unpack if the bundle was never downloaded (e.g. offline with no cached copy).
					if (!unpackTaskRef.archive.get().asFile.exists()) {
						println "Skipping bundle unpack as no downloaded archive exists"
						return false
					}

					// Always unpack if explicitly requested
					if (explicitStormRequest) {
						println "Explicitly requested unpack"
						return true
					}

					switch (strategy) {
						case BundleDownloadStrategy.ALWAYS:
							println "Unpacking bundle as strategy is set to ALWAYS"
							return true
						case BundleDownloadStrategy.IF_MISSING:
							if (new File(unpackTaskRef.outputDir.get().asFile, "manifest.json").exists()) {
								println "Skipping bundle unpack as copy already exists in assets dir and strategy is IF_MISSING"
								return false
							} else {
								println "Unpacking initial bundle as no copy exists in assets dir and strategy is IF_MISSING"
								return true
							}
						case BundleDownloadStrategy.NEVER:
							println "Skipping bundle unpack as strategy is set to NEVER"
							return false
					}

					return false
				}
			}

			// Hand the unpacked bundle to AGP as a generated assets source directory. AGP owns the output
			// location and wires the unpack task into the asset-merging graph automatically.
			variant.sources.assets?.addGeneratedSourceDirectory(unpackTask) { it.getOutputDir() }
		}
	}

}
