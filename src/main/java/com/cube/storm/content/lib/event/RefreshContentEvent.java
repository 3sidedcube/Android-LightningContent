package com.cube.storm.content.lib.event;

/**
 * Event that gets posted at the end of a content update. Use {@link com.cube.storm.content.lib.helper.BusHelper#getInstance()} to register/unregister
 * a class to receive this event.
 * <p/>
 * For more information, see the documentation on Otto by Square
 */
public class RefreshContentEvent
{
	public RefreshContentEvent()
	{

	}
}
