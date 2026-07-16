package com.cube.storm.content

import org.gradle.api.DefaultTask
import org.gradle.api.file.ArchiveOperations
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

import javax.inject.Inject

/**
 * Untars a downloaded Storm bundle ({@code .tar.gz}) into a generated assets source directory.
 *
 * <p>The output directory is owned by the Android Gradle Plugin: it is wired in via
 * {@code variant.sources.assets.addGeneratedSourceDirectory(...)}, which both sets the location and
 * registers this task as a dependency of the variant's asset-merging step. That removes any need to
 * hook internal AGP task names (which change between AGP versions).</p>
 *
 * <p>The task uses injected {@link ArchiveOperations} / {@link FileSystemOperations} services rather
 * than the deprecated {@code Project} API at execution time, so it is compatible with Gradle 9.</p>
 */
abstract class StormUnpackTask extends DefaultTask {

	/**
	 * The downloaded bundle archive to unpack. Compression is inferred from the {@code .tar.gz}
	 * extension.
	 */
	@InputFile
	abstract RegularFileProperty getArchive()

	/**
	 * Destination assets directory. Set by AGP via {@code addGeneratedSourceDirectory}.
	 */
	@OutputDirectory
	abstract DirectoryProperty getOutputDir()

	@Inject
	abstract ArchiveOperations getArchiveOperations()

	@Inject
	abstract FileSystemOperations getFileSystemOperations()

	@TaskAction
	void unpack() {
		def destination = outputDir.get().asFile
		def tar = archiveOperations.tarTree(archive.get().asFile)

		// Start from a clean directory so removed bundle entries don't linger between builds.
		fileSystemOperations.delete {
			it.delete(destination)
		}
		fileSystemOperations.copy {
			it.from(tar)
			it.into(destination)
		}
	}
}
