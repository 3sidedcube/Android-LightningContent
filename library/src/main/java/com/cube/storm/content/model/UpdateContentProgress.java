package com.cube.storm.content.model;

import androidx.annotation.NonNull;
import androidx.work.Data;
import lombok.Value;

@Value
public class UpdateContentProgress
{
	private static final String WORKER_DATA_KEY_PHASE = "phase";
	private static final String WORKER_DATA_KEY_PROGRESS = "progress";
	private static final String WORKER_DATA_KEY_PROGRESS_MAX = "progress_max";

	@NonNull
	public static UpdateContentProgress waiting()
	{
		return new UpdateContentProgress(Phase.WAITING, 0, 0);
	}

	@NonNull
	public static UpdateContentProgress checking()
	{
		return new UpdateContentProgress(Phase.CHECKING, 0, 0);
	}

	@NonNull
	public static UpdateContentProgress downloading(long bytesDownloaded, long bytesTotal)
	{
		return new UpdateContentProgress(Phase.DOWNLOADING, bytesDownloaded, bytesTotal);
	}

	@NonNull
	public static UpdateContentProgress fromWorkerData(Data workerData)
	{
		return new UpdateContentProgress(
			Phase.values()[workerData.getInt(WORKER_DATA_KEY_PHASE, Phase.UNKNOWN.ordinal())],
			workerData.getLong(WORKER_DATA_KEY_PROGRESS, 0L),
			workerData.getLong(WORKER_DATA_KEY_PROGRESS_MAX, 0L));
	}

	public enum Phase
	{
		UNKNOWN,
		WAITING,
		CHECKING,
		DOWNLOADING
	}

	Phase phase;
	long progress;
	long progressMax;

	@NonNull
	public Data toWorkerData()
	{
		return new Data.Builder()
			       .putInt(WORKER_DATA_KEY_PHASE, phase.ordinal())
			       .putLong(WORKER_DATA_KEY_PROGRESS, progress)
			       .putLong(WORKER_DATA_KEY_PROGRESS_MAX, progressMax)
			       .build();
	}
}
