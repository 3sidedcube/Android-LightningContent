package com.cube.storm.content.model;

import androidx.work.Data;
import lombok.Value;

@Value
public class UpdateContentProgress
{
	private static final String WORKER_DATA_KEY_PHASE = "phase";
	private static final String WORKER_DATA_KEY_PROGRESS = "progress";
	private static final String WORKER_DATA_KEY_PROGRESS_MAX = "progress_max";

	public static UpdateContentProgress checkingForBundle()
	{
		return new UpdateContentProgress(Phase.CHECKING_FOR_BUNDLE, 0, 0);
	}

	public static UpdateContentProgress checkingForDelta()
	{
		return new UpdateContentProgress(Phase.CHECKING_FOR_DELTA, 0, 0);
	}

	public static UpdateContentProgress downloading(long bytesDownloaded, long bytesTotal)
	{
		return new UpdateContentProgress(Phase.DOWNLOADING, bytesDownloaded, bytesTotal);
	}

	public static UpdateContentProgress fromWorkerData(Data workerData)
	{
		return new UpdateContentProgress(
			Phase.values()[workerData.getInt(WORKER_DATA_KEY_PHASE, 0)],
			workerData.getLong(WORKER_DATA_KEY_PROGRESS, 0L),
			workerData.getLong(WORKER_DATA_KEY_PROGRESS_MAX, 0L));
	}

	public enum Phase
	{
		CHECKING_FOR_BUNDLE,
		CHECKING_FOR_DELTA,
		DOWNLOADING
	}

	Phase phase;
	long progress;
	long progressMax;

	public Data toWorkerData()
	{
		return new Data.Builder()
			       .putInt(WORKER_DATA_KEY_PHASE, phase.ordinal())
			       .putLong(WORKER_DATA_KEY_PROGRESS, progress)
			       .putLong(WORKER_DATA_KEY_PROGRESS_MAX, progress)
			       .build();
	}
}
