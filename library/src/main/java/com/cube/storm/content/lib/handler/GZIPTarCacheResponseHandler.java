package com.cube.storm.content.lib.handler;

import lombok.Getter;
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

			getConnectionInfo().responseLength = totalRead;
			tis.close();
		}
		catch (IOException e)
		{
			// Propagate checked exception as an unchecked exception
			throw new RuntimeException(e);
		}
	}

	@Override public void generateContent()
	{
	}
}
