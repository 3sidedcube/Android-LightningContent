package com.cube.storm.content.lib.callback;

import androidx.annotation.NonNull;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import net.callumtaylor.asynchttp.obj.ConnectionInfo;

import java.io.IOException;

import lombok.AccessLevel;
import lombok.Getter;
import okhttp3.Call;
import okhttp3.Response;

/**
 * {@link ConnectionInfoCallback} implementation which parses the response as JSON.
 * Mimics the behaviour of JsonResponseHandler in AsyncHttp.
 */
public abstract class JsonCallback extends ConnectionInfoCallback
{
	@Getter(AccessLevel.PROTECTED) private JsonElement content;

	@Override
	public void onSuccess(@NonNull Call call, @NonNull Response response, @NonNull ConnectionInfo connectionInfo) throws IOException {
		try
		{
			content = JsonParser.parseString(response.body().string());
		}
		catch (Exception e) {}
	}
}
