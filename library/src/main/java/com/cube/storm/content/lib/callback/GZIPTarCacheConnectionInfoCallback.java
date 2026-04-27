package com.cube.storm.content.lib.callback;

import androidx.annotation.NonNull;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;

import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import lombok.Getter;
import okhttp3.Call;
import okhttp3.Response;

/**
 * {@link FileCacheCallback} implementation for a GZIPped TAR file download that unzips the files.
 */
public abstract class GZIPTarCacheConnectionInfoCallback extends FileCacheCallback
{
	@Getter private final String filePath;
	@Getter private final Set<String> extractedFiles = new HashSet<>();

	public GZIPTarCacheConnectionInfoCallback(String filePath)
	{
		super(filePath + "/bundle.tar");
		this.filePath = filePath;
	}

	@Override
	public void onSuccess(@NonNull Call call, @NonNull Response response, @NonNull ConnectionInfo connectionInfo) throws IOException
	{
		super.onSuccess(call, response, connectionInfo);

		try
		{
			int buffer = 8192;
			long totalRead = 0;

			InputStream stream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(getMFile()), buffer), buffer);
			TarInputStream tis = new TarInputStream(stream);
			TarEntry file;

			extractedFiles.clear();

			while ((file = tis.getNextEntry()) != null)
			{
				if (file.getName().equals("./")) continue;

				String extractedFilePath = filePath + "/" + file.getName();
				File extractFile = new File(extractedFilePath);

				if (file.isDirectory())
				{
					extractFile.mkdirs();

					continue;
				}

				// create folders if they do not exist for file
				if (!new File(extractFile.getParent()).exists())
				{
					new File(extractFile.getParent()).mkdirs();
				}

				FileOutputStream fos = new FileOutputStream(extractedFilePath);
				BufferedOutputStream dest = new BufferedOutputStream(fos, buffer);

				int count = 0;
				byte data[] = new byte[buffer];

				while ((count = tis.read(data)) != -1)
				{
					dest.write(data, 0, count);
					totalRead += count;
				}

				dest.flush();
				dest.close();
				extractedFiles.add(extractedFilePath);
			}

			connectionInfo.responseLength = totalRead;
			tis.close();
		}
		catch (IOException e)
		{
			// e.printStackTrace();
			onFailure(call, e);
		}
	}
}
