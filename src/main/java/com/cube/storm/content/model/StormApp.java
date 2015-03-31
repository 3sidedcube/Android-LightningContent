package com.cube.storm.content.model;

import java.io.Serializable;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * Basic model for storm app.
 * <p/>
 * A basic data struct for the different apps in the data/identifiers.json file.
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public class StormApp implements Serializable
{
	@Getter @Setter private String appId;
	@Getter @Setter private String packageName;
	@Getter @Setter private Map<String, Object> name;
}
