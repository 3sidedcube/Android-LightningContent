package com.cube.storm.content.lib.helper;

import com.squareup.otto.Bus;

/**
 * Maintains a singleton instance for obtaining the bus. Ideally this would be
 * replaced with a more efficient means such as through injection directly into
 * interested classes.
 */
public final class BusHelper
{
	private static final Bus BUS = new Bus();

	private BusHelper()
	{
		// No instances.
	}

	public static Bus getInstance()
	{
		return BUS;
	}
}
