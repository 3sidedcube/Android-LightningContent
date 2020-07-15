package com.cube.storm.content.lib.policy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.manager.UpdateManager;
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
	public UpdateContentRequest checkForBundle(@Nullable Long buildTimestamp)
	{
		if (!ContentSettings.getInstance().getPolicyManager().canUpdate())
		{
			return UpdateContentRequest.fullBundle(buildTimestamp, Observable.error(new IllegalStateException("Not connected to wifi")));
		}
		return delegate.checkForBundle(buildTimestamp);
	}

	@Override
	public UpdateContentRequest checkForUpdates(long lastUpdate)
	{
		if (!ContentSettings.getInstance().getPolicyManager().canUpdate())
		{
			return UpdateContentRequest.deltaUpdate(lastUpdate, Observable.error(new IllegalStateException("Not connected to wifi")));
		}
		return delegate.checkForUpdates(lastUpdate);
	}

	@Override
	public UpdateContentRequest downloadUpdates(@NonNull String endpoint)
	{
		if (!ContentSettings.getInstance().getPolicyManager().canUpdate())
		{
			return UpdateContentRequest.directDownload(Observable.error(new IllegalStateException("Not connected to wifi")));
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
