package com.cube.storm.content

import de.undercouch.gradle.tasks.download.Download
import groovyx.net.http.NativeHandlers
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy

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
		def authTask = project.task("stormAuthenticate") {
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

		/**
		 * Create download and unpack tasks for each unique app variant
		 */
		project.android.applicationVariants.all { variant ->

			def mergedStormConfig = new StormExtension()

			/**
			 * First of all merge all the Storm configuations for this bundle together. These come from (in order of priority):
			 * 1) the build type for the variant (e.g. debug / release)
			 * 2) each flavor comprising the variant
			 * 3) the Android default config
			 */
			mergedStormConfig = mergedStormConfig.merge(variant.buildType.storm)
			variant.productFlavors.each { flavor ->
				mergedStormConfig = mergedStormConfig.merge(flavor.storm)
			}
			mergedStormConfig = mergedStormConfig.merge(project.android.defaultConfig.storm)
			mergedStormConfig = mergedStormConfig.merge(new StormExtension(bundleDownloadStrategy: variant.buildType.name == "release" ? BundleDownloadStrategy.ALWAYS : BundleDownloadStrategy.IF_MISSING))

			if (mergedStormConfig.isValid()) {
				println "Storm configuration for \"${variant.name}\": ${mergedStormConfig}"

				String sanitisedUrl = mergedStormConfig.url.replaceAll("[\\\\/:*?\"<>|]", "_")
				String archiveDownloadLocation = "${project.buildDir}/storm/${sanitisedUrl}/bundle.tar.gz"
				String bundleUnpackDir = "${project.projectDir}/src/${variant.name}/assets"
				String downloadTaskName = "stormDownload${sanitisedUrl.capitalize()}"

				def downloadTask = project.tasks.findByName(downloadTaskName) ?: project.task(downloadTaskName, type: Download, dependsOn: mergedStormConfig.requiresAuth() ? authTask : null) {
					/**
					 * Only download if the flag to enable downloading on every assembly is explicitly requested
					 */
					onlyIf {
						if (project.gradle.startParameter.isOffline()) {
							println "Skipping bundle download in offline mode"
							return false
						}

						// Always download if explicitly requested
						if (project.gradle.startParameter.taskNames.any { name -> name.startsWith("storm") }) {
							println "Explicitly requested download"
							return true
						}

						switch (mergedStormConfig.bundleDownloadStrategy) {
							case BundleDownloadStrategy.ALWAYS:
								println "Downloading bundle as strategy is set to ALWAYS"
								return true
							case BundleDownloadStrategy.IF_MISSING:
								if (project.file(archiveDownloadLocation).exists()) {
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
					doFirst {
						println "Downloading from ${src}..."
						if (mergedStormConfig.requiresAuth()) {
							println "Setting auth token for user ${mergedStormConfig.authUsername}: ${authToken}"
							header "Authorization", authToken
						}
					}
					src mergedStormConfig.url
					dest archiveDownloadLocation
					requestInterceptor new RedirectInterceptor()
					overwrite true
				}
				def unpackTask = project.task("stormUnpack${variant.name.capitalize()}Bundle", type: Copy, dependsOn: downloadTask) {
					onlyIf {
						// Always unpack if explicitly requested
						if (project.gradle.startParameter.taskNames.any { name -> name.startsWith("storm") }) {
							println "Explicitly requested unpack"
							return true
						}

						switch (mergedStormConfig.bundleDownloadStrategy) {
							case BundleDownloadStrategy.ALWAYS:
								println "Unpacking bundle as strategy is set to ALWAYS"
								return true
							case BundleDownloadStrategy.IF_MISSING:
								if (project.file("${bundleUnpackDir}/manifest.json").exists()) {
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
					from project.tarTree(project.resources.gzip(downloadTask.dest))
					into bundleUnpackDir
				}

				project.tasks."generate${variant.name.capitalize()}Assets".dependsOn unpackTask

				variant.buildConfigField "String", "STORM_API_BASE", "\"${mergedStormConfig.apiBase}\""
				variant.buildConfigField "String", "STORM_API_VERSION", "\"${mergedStormConfig.apiVersion}\""
				variant.buildConfigField "String", "STORM_API_URL", "\"${mergedStormConfig.apiBase}/${mergedStormConfig.apiVersion}\""
				variant.buildConfigField "String", "STORM_APP_ID", "\"${mergedStormConfig.appId}\""
				variant.buildConfigField "String", "STORM_ORG_ID", "\"${mergedStormConfig.orgId}\""
				variant.buildConfigField "String", "STORM_ORG_NAME", "\"${mergedStormConfig.orgName}\""
				variant.buildConfigField "String", "STORM_APP_NAME", "\"${mergedStormConfig.orgName}-${mergedStormConfig.orgId}-${mergedStormConfig.appId}\""
			} else {
				println "ERROR: Incomplete Storm configuration for \"${variant.name}\": ${mergedStormConfig}"
			}
		}
	}

}
