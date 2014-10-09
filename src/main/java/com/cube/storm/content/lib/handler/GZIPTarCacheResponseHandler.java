package com.cube.storm.content.lib.handler;

import com.cube.storm.content.lib.D;

import net.callumtaylor.asynchttp.AsyncHttpClient.ClientExecutorTask;
import net.callumtaylor.asynchttp.response.AsyncHttpResponseHandler;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.zip.GZIPInputStream;

/**
 * Caches the response directly to disk. Useful when downloading
 * large files. <b>note</b> This will delete any existing files
 * with the same file name
 */
public abstract class GZIPTarCacheResponseHandler extends AsyncHttpResponseHandler
{
	private final String mFilePath;

	public GZIPTarCacheResponseHandler(String filePath)
	{
		mFilePath = filePath;
	}

	@Override public void onBeginPublishedDownloadProgress(InputStream stream, ClientExecutorTask client, long totalLength) throws SocketTimeoutException, Exception
	{
		try
		{
			TarInputStream tis = new TarInputStream(new GZIPInputStream(stream, Math.max(8196, totalLength > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)totalLength)));
			TarEntry file;
			long totalRead = 0;

			while ((file = tis.getNextEntry()) != null)
			{
				if (file.getName().equals("./")) continue;

				if (file.isDirectory())
				{
					File f = new File(mFilePath + "/" + file.getName());
					D.out("created %s, %s", f.getAbsoluteFile(), f.mkdirs());
					continue;
				}

				D.out("writing %s", file.getName());
				FileOutputStream fos = new FileOutputStream(mFilePath + "/" + file.getName());
				BufferedOutputStream dest = new BufferedOutputStream(fos);

				int count = 0;
				byte data[] = new byte[8192];

				while ((count = tis.read(data)) != -1)
				{
					dest.write(data, 0, count);
					totalRead += count;
					output(totalRead, totalLength);
				}

				dest.flush();
				dest.close();
			}

			if (!client.isCancelled())
			{
				getConnectionInfo().responseLength = totalRead;
			}

			tis.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void output(long totalRead, long totalLength)
	{

	}

	@Override public void generateContent()
	{

	}

	@Override public void onFailure()
	{
		D.out(getConnectionInfo());
	}

	/**
	 * Processes the response from the stream.
	 * This is <b>not</b> ran on the UI thread
	 *
	 * @return The data represented as a file object
	 */
	@Override public String getContent()
	{
		return mFilePath;
	}
}
