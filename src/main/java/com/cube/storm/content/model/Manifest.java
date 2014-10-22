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
	@Getter protected ArrayList<FileDescriptor> content;
	@Getter protected ArrayList<FileDescriptor> pages;
	@Getter protected ArrayList<FileDescriptor> languages;
	@Getter protected ArrayList<FileDescriptor> data;
	@Getter protected long timestamp;

	public static class FileDescriptor
	{
		@Getter protected String src;
		@Getter protected String hash;
	}
}
