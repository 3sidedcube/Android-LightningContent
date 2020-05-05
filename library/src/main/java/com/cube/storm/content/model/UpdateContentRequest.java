package com.cube.storm.content.model;

import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Environment;
import com.cube.storm.content.lib.worker.ContentUpdateWorker;
import io.reactivex.Observable;
import lombok.Value;

@Value
public class UpdateContentRequest
{
	String id;
	ContentUpdateWorker.UpdateType updateType;
	Long timestamp;
	Observable<UpdateContentProgress> progress;
	Environment environment = ContentSettings.getInstance().getContentEnvironment();
}
