package com.cube.storm.content.lib.manager;

import com.cube.storm.content.lib.helper.BundleHelper;

public class DefaultMigrationManager implements MigrationManager
{
	@Override
	public boolean migrate()
	{
		Long initialTimestamp = BundleHelper.readInitialTimestamp();
		Long latestTimestamp = BundleHelper.readContentTimestamp();

		if (initialTimestamp != null && latestTimestamp != null && initialTimestamp > latestTimestamp)
		{
			BundleHelper.clearCache();
			return true;
		}

		return false;
	}
}
