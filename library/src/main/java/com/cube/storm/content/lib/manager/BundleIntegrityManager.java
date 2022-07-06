package com.cube.storm.content.lib.manager;

import java.io.File;

/**
 * Interface for ensuring that the integrity of any new bundle is verified before deployment and enforced after deployment
 *
 * @author JR Mitchell
 */
public interface BundleIntegrityManager
{
	/**
	 * Checks the integrity of each file currently stored in the given directory, deleting all files from the directory if its integrity could not be verified
	 *
	 * @param contentPath the path to the bundle directory
	 * @return true if the bundle has the correct integrity, false if it was deleted
	 */
	boolean integrityCheck(String contentPath);
	
	/**
	 * Makes any final changes to the cached directory after deploying the bundle to enforce the integrity of the content
	 *
	 * @param cachePath the path to the directory that the bundle has been deployed into
	 */
	void enforceIntegrityAfterDeployment(File cachePath);
}
