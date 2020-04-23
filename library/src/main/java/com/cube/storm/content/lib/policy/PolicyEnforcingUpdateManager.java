package com.cube.storm.content.lib.policy;

import androidx.annotation.NonNull;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.manager.UpdateManager;
import com.cube.storm.content.lib.worker.ContentUpdateWorker;
import com.cube.storm.content.model.UpdateContentRequest;
import io.reactivex.Observable;

/**
 * {@link UpdateManager} which ensures updates only occur on a wi-fi connection, otherwise delegating to another
 * UpdateManager.
 */
public class PolicyEnforcingUpdateManager implements UpdateManager
{
	private UpdateManager delegate;

	public PolicyEnforcingUpdateManager(UpdateManager delegate)
	{
		this.delegate = delegate;
	}

	@Override
	public void cancelPendingRequests()
	{
		delegate.cancelPendingRequests();
	}

	@Override
	public UpdateContentRequest checkForBundle()
	{
		if (!ContentSettings.getInstance().getPolicyManager().canUpdate())
		{
			return new UpdateContentRequest(
				Long.toString(System.currentTimeMillis()),
				ContentUpdateWorker.UpdateType.FULL_BUNDLE,
				null,
				Observable.error(new IllegalStateException("Not connected to wifi"))
			);
		}
		return delegate.checkForBundle();
	}

	@Override
	public UpdateContentRequest checkForUpdates(long lastUpdate)
	{
		if (!ContentSettings.getInstance().getPolicyManager().canUpdate())
		{
			return new UpdateContentRequest(
				Long.toString(System.currentTimeMillis()),
				ContentUpdateWorker.UpdateType.DELTA,
				lastUpdate,
				Observable.error(new IllegalStateException("Not connected to wifi"))
			);
		}
		return delegate.checkForUpdates(lastUpdate);
	}

	@Override
	public UpdateContentRequest downloadUpdates(@NonNull String endpoint)
	{
		if (!ContentSettings.getInstance().getPolicyManager().canUpdate())
		{
			return new UpdateContentRequest(
				Long.toString(System.currentTimeMillis()),
				ContentUpdateWorker.UpdateType.DIRECT_DOWNLOAD,
				null,
				Observable.error(new IllegalStateException("Not connected to wifi"))
			);
		}
		return delegate.downloadUpdates(endpoint);
	}

	@Override
	public void scheduleBackgroundUpdates()
	{
		delegate.scheduleBackgroundUpdates();
	}

	@Override
	public Observable<UpdateContentRequest> updates()
	{
		return delegate.updates();
	}
}
