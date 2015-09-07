package com.cube.storm.content.lib.handler;

import net.callumtaylor.asynchttp.response.CacheResponseHandler;

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

/**
 * Caches the response directly to disk. Useful when downloading
 * large files. <b>note</b> This will delete any existing files
 * with the same file name
 */
public abstract class GZIPTarCacheResponseHandler extends CacheResponseHandler
{
	@Getter private String filePath;
	@Getter private Set<String> extractedFiles = new HashSet<>();

	public GZIPTarCacheResponseHandler(String filePath)
	{
		super(filePath + "/bundle.tar");

		this.filePath = filePath;
	}

	@Override public void onSuccess()
	{
		try
		{
			int buffer = 8192;
			long totalRead = 0;

			InputStream stream = new BufferedInputStream(new GZIPInputStream(new FileInputStream(getContent()), buffer), buffer);
			TarInputStream tis = new TarInputStream(stream);
			TarEntry file;

			extractedFiles.clear();

			while ((file = tis.getNextEntry()) != null)
			{
				if (file.getName().equals("./")) continue;

				String extractedFilePath = filePath + "/" + file.getName();

				if (file.isDirectory())
				{
					File f = new File(extractedFilePath);
					f.mkdir();

					continue;
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

			getConnectionInfo().responseLength = totalRead;
			tis.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	@Override public void generateContent()
	{
	}
}
