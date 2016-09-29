package com.cube.storm.content.lib;

import lombok.Getter;

/**
 * Environment enum used for telling the API what environment to fetch the bundle/delta from
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public enum Environment
{
	/**
	 * Test environment. This environment requires an authorization token to download bundles
	 */
	TEST("test"),

	/**
	 * Live environment
	 */
	LIVE("live");

	/**
	 * Environment label for api requests
	 */
	@Getter private String environmentLabel;

	private Environment(String str)
	{
		this.environmentLabel = str;
	}
}
