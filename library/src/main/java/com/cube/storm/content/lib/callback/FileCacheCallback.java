package com.cube.storm.content.lib.callback;

import androidx.annotation.NonNull;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import lombok.AccessLevel;
import lombok.Getter;
import okhttp3.Call;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;

/**
 * {@link ConnectionInfoCallback} implementation which replicates the AsyncHttp CacheResponseHandler.
 * Caches the response directly to disk. Useful when downloading
 * large files. <b>note</b> This will delete any existing files
 * with the same file name
 */
public abstract class FileCacheCallback extends ConnectionInfoCallback
{
	@Getter(AccessLevel.PROTECTED)
	private final File mFile;
	private BufferedOutputStream fos;

	public FileCacheCallback(String filePath)
	{
		mFile = new File(filePath);

		if (mFile.exists())
		{
			mFile.delete();
		}

		try
		{
			mFile.createNewFile();
			fos = new BufferedOutputStream(new FileOutputStream(mFile));
		}
		catch (Exception e){}
	}

	@Override
	public void onSuccess(@NonNull Call call, @NonNull Response response, @NonNull ConnectionInfo connectionInfo) throws IOException
	{
		long totalProcessed = 0;
		long totalLength;
		int chunkSize = 8192;

		BufferedSource source = response.body().source();
		Buffer buffer = new Buffer();
		while (!source.exhausted())
		{
			long written = source.read(buffer, chunkSize);
			buffer.writeTo(fos);
			buffer.clear();
			totalLength = (int) response.body().contentLength();
			totalProcessed += written;
			onByteChunkProcessed(totalProcessed, totalLength);
		}
		try {
			fos.flush();
			fos.close();
		}
		catch (Exception e) {}
	}

	public abstract void onByteChunkProcessed(long totalProcessed, long totalLength);
}
