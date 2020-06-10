package com.cube.storm.content.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Environment;
import com.cube.storm.content.lib.worker.ContentUpdateWorker;
import io.reactivex.Observable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateContentRequest
{
	private static String generateId()
	{
		return Long.toString(System.currentTimeMillis());
	}

	public static UpdateContentRequest deltaUpdate(
		long contentTimestamp,
		@NonNull Observable<UpdateContentProgress> progress
	)
	{
		String id = generateId();
		return new UpdateContentRequest(id, ContentUpdateWorker.UpdateType.DELTA, null, contentTimestamp, progress);
	}

	public static UpdateContentRequest fullBundle(
		@Nullable Long buildTimestamp,
		@NonNull Observable<UpdateContentProgress> progress
	)
	{
		String id = generateId();
		return new UpdateContentRequest(id, ContentUpdateWorker.UpdateType.FULL_BUNDLE, buildTimestamp, null, progress);
	}

	public static UpdateContentRequest deltaUpdateFromLocalContent(@NonNull Observable<UpdateContentProgress> progress)
	{
		String id = generateId();
		return new UpdateContentRequest(id, ContentUpdateWorker.UpdateType.DELTA, null, null, progress);
	}

	public static UpdateContentRequest directDownload(@NonNull Observable<UpdateContentProgress> progress)
	{
		String id = generateId();
		return new UpdateContentRequest(id, ContentUpdateWorker.UpdateType.DIRECT_DOWNLOAD, null, null, progress);
	}

	String id;
	ContentUpdateWorker.UpdateType updateType;
	Long buildTimestamp;
	Long updateTimestamp;
	Observable<UpdateContentProgress> progress;
	Environment environment = ContentSettings.getInstance().getContentEnvironment();
}
