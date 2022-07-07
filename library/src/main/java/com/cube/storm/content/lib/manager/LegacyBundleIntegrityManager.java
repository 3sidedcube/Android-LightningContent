package com.cube.storm.content.lib.manager;

import com.cube.storm.content.lib.helper.BundleHelper;

import java.io.File;

/**
 * {@link BundleIntegrityManager} implementation based on the pre-existing Storm logic from before the introduction of {@link BundleIntegrityManager}
 * {@link LegacyBundleIntegrityManager#integrityCheck(String)} leverages {@link BundleHelper#integrityCheck(String)} to ensure that every file listed in the manifest matches the hash of that file in the bundle
 * {@link LegacyBundleIntegrityManager#enforceIntegrityAfterDeployment(File)} leverages {@link BundleHelper#deleteUnexpectedFiles(File)} to delete any files in the cache that are not listed in the manifest
 *
 * @author JR Mitchell
 */
public class LegacyBundleIntegrityManager implements BundleIntegrityManager
{
	@Override public boolean integrityCheck(String contentPath)
	{
		return BundleHelper.integrityCheck(contentPath);
	}
	
	@Override public void enforceIntegrityAfterDeployment(File cachePath)
	{
		BundleHelper.deleteUnexpectedFiles(cachePath);
	}
}
