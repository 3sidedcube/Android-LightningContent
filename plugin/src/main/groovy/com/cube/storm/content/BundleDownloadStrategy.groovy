package com.cube.storm.content

enum BundleDownloadStrategy {
	/**
	 * Always download a fresh bundle whenever the variant is assembled
	 */
	ALWAYS,

	/**
	 * Never download a bundle - this variant is not bundled with Storm content
	 */
	NEVER,

	/**
	 * Only download a bundle for this variant once and then use the copy in the build cache
	 */
	IF_MISSING
}
