package com.cube.storm.content

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Copy

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

		project.android.applicationVariants.all { variant ->

			def mergedStormConfig = new StormExtension()

			// Create a merged Storm configuration
			mergedStormConfig = mergedStormConfig.merge(variant.buildType.storm)
			variant.productFlavors.each { flavor ->
				mergedStormConfig = mergedStormConfig.merge(flavor.storm)
			}
			mergedStormConfig = mergedStormConfig.merge(project.android.defaultConfig.storm)
			mergedStormConfig = mergedStormConfig.merge(new StormExtension(isBundled: true, isBundledAutomatically: variant.buildType.name == "release"))

			if (mergedStormConfig.isValid()) {
				println "Storm configuration for \"${variant.name}\": ${mergedStormConfig}"

				def downloadTask = project.task("stormDownload${variant.name.capitalize()}Bundle", type: de.undercouch.gradle.tasks.download.Download) {
					/**
					 * Only download if the flag to enable downloading on every assembly is explicitly requested
					 */
					onlyIf {
						if (project.gradle.startParameter.isOffline()) {
							println "Skipping bundle download in offline mode"
							return false
						}

						// Always download if explicitly requested
						if (project.gradle.startParameter.taskNames.any { name -> name.startsWith("storm")}) {
							println "Explicitly requested download"
							return true
						} else if (!project.file("${project.projectDir}/src/${variant.name}/assets/manifest.json").exists()) {
							println "Download bundle, as none exists at ${project.buildDir}/storm/${variant.name}/bundle.tar.gz"
							return true
						} else if (!mergedStormConfig.isBundledAutomatically) {
							println "Skipping automatic bundle download as isBundledAutomatically = false"
							return false
						}

						return true
					}
					src mergedStormConfig.createBundleUrl("live")
					dest "${project.buildDir}/storm/${variant.name}/bundle.tar.gz"
					overwrite true
				}
				def unpackTask = project.task("stormUnpack${variant.name.capitalize()}Bundle", type: Copy, dependsOn: downloadTask) {
					onlyIf {
						// Always unpack if explicitly requested
						if (project.gradle.startParameter.taskNames.any { name -> name.startsWith("storm")}) {
							println "Explicitly requested unpack"
							return true
						} else if (!project.file("${project.projectDir}/src/${variant.name}/assets/manifest.json").exists()) {
							println "Unpack bundle, as none exists at ${project.projectDir}/src/${variant.name}/assets/manifest.json"
							return true
						} else if (!mergedStormConfig.isBundledAutomatically) {
							println "Skipping bundle unpack as isBundledAutomatically = false"
							return false
						}

						return true
					}
					from project.tarTree(project.resources.gzip(downloadTask.dest))
					into "${project.projectDir}/src/${variant.name}/assets/"
				}

				if (mergedStormConfig.isBundled)
				{
					project.tasks."generate${variant.name.capitalize()}Assets".dependsOn unpackTask
				}

				variant.buildConfigField "String", "STORM_API_BASE", "\"${mergedStormConfig.apiBase}\""
				variant.buildConfigField "String", "STORM_API_VERSION", "\"${mergedStormConfig.apiVersion}\""
				variant.buildConfigField "String", "STORM_API_URL", "\"${mergedStormConfig.apiBase}/${mergedStormConfig.apiVersion}/\""
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
