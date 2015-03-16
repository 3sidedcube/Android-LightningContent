package com.cube.storm.content.model;

import java.util.ArrayList;

import lombok.Getter;

/**
 * Basic model structure for manifest json file
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public class Manifest
{
	/**
	 * List of files in the content folder. This will mainly consist of images, videos, and audio files
	 */
	@Getter protected ArrayList<FileDescriptor> content;

	/**
	 * List of files in the pages folder. This will contain all the content pages from the CMS
	 */
	@Getter protected ArrayList<FileDescriptor> pages;

	/**
	 * List of files in the languages folder. This will contain all the language packs.
	 */
	@Getter protected ArrayList<FileDescriptor> languages;

	/**
	 * List of files in the data folder. This will contain all the misc data files such as {@code identifiers.json}
	 */
	@Getter protected ArrayList<FileDescriptor> data;

	/**
	 * Timestamp of the bundle
	 */
	@Getter protected long timestamp;

	/**
	 * File descriptor class. Has the path of the file and its hash. Used to check integrity of the bundle
	 */
	public static class FileDescriptor
	{
		@Getter protected String src;
		@Getter protected String hash;
	}
}
