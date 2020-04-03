package com.cube.storm.content.lib.manager;

import io.reactivex.rxjava3.core.Completable;

public class BackgroundWorkerUpdateManager implements UpdateManager
{
	@Override
	public Completable checkForBundle()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Completable checkForUpdates(long lastUpdate)
	{
		throw new UnsupportedOperationException();
	}
}
