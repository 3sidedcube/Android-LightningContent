package com.cube.storm.content.lib.helper;

import android.text.TextUtils;
import com.cube.storm.content.lib.Constants;
import com.cube.storm.util.lib.debug.Debug;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Helper class for deleting files
 *
 * @author Callum Taylor
 */
public class FileHelper
{
	public static void copyDirectory(File sourceLocation, File targetLocation) throws IOException
	{
		if (sourceLocation.isDirectory())
		{
			if (!targetLocation.exists())
			{
				targetLocation.mkdir();
			}

			String[] children = sourceLocation.list();

			for (String aChildren : children)
			{
				copyDirectory(new File(sourceLocation, aChildren), new File(targetLocation, aChildren));
			}
		}
		else
		{
			int buffer = 8192;

			InputStream in = new BufferedInputStream(new FileInputStream(sourceLocation), buffer);
			OutputStream out = new BufferedOutputStream(new FileOutputStream(targetLocation), buffer);

			byte[] buf = new byte[buffer];
			int len;

			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}

			in.close();
			out.flush();
			out.close();
		}
	}

	public static void deleteRecursive(File fileOrDirectory)
	{
		if (fileOrDirectory.isDirectory())
		{
			File[] files = fileOrDirectory.listFiles();

			if (files != null)
			{
				for (File child : files)
				{
					deleteRecursive(child);
				}
			}
		}

		fileOrDirectory.delete();
	}

	/**
	 * Purges files in a folder that do not exist in the manifest
	 *
	 * @param folderPath The folder to check, can be either, {@link Constants#FOLDER_PAGES}, {@link Constants#FOLDER_LANGUAGES}, {@link Constants#FOLDER_CONTENT}, or {@link Constants#FOLDER_DATA}
	 * @param fileList The list of files to delete
	 */
	public static void removeFiles(String folderPath, String[] fileList)
	{
		if (fileList != null)
		{
			for (String s : fileList)
			{
				if (!TextUtils.isEmpty(s))
				{
					boolean deleted = new File(folderPath, s).delete();

					if (!deleted)
					{
						Debug.out("%s was not deleted successfully", s);
					}
				}
			}
		}
	}
}
