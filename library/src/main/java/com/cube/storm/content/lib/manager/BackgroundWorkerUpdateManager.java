package com.cube.storm.content.lib.manager;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Operation;
import androidx.work.WorkManager;
import com.cube.storm.content.lib.task.ContentUpdateWorker;
import io.reactivex.rxjava3.core.Completable;

public class BackgroundWorkerUpdateManager implements UpdateManager
{
	private static final String CONTENT_CHECK_WORK_NAME = "storm_content_check";
	private static final Constraints CONTENT_CHECK_WORK_CONSTRAINTS = new Constraints.Builder().setRequiredNetworkType(
		NetworkType.UNMETERED).build();
	private static final OneTimeWorkRequest CONTENT_CHECK_WORK_REQUEST = new OneTimeWorkRequest.Builder(
		ContentUpdateWorker.class).setConstraints(CONTENT_CHECK_WORK_CONSTRAINTS).build();

	private WorkManager workManager;

	public BackgroundWorkerUpdateManager(@NonNull Context context)
	{
		workManager = WorkManager.getInstance(context);
	}

	@Override
	public void cancelPendingRequests()
	{
		workManager.cancelUniqueWork(CONTENT_CHECK_WORK_NAME);
	}

	@Override
	public Completable checkForBundle()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Completable checkForUpdates(long lastUpdate)
	{
		Operation workOp = workManager.enqueueUniqueWork(
			CONTENT_CHECK_WORK_NAME,
			ExistingWorkPolicy.KEEP,
			CONTENT_CHECK_WORK_REQUEST
		);
		return Completable.fromFuture(workOp.getResult());
	}
}
