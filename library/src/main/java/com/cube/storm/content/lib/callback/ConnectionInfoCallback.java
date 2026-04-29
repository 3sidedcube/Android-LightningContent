package com.cube.storm.content.lib.callback;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.cube.storm.content.lib.model.ConnectionInfo;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * {@link Callback} implementation replicating the behaviour from AsyncHttp using OkHttp directly.
 */
public abstract class ConnectionInfoCallback implements Callback {

	@NonNull
	private ConnectionInfo getConnectionInfoFromCall(@NonNull Call call, @Nullable Response response)
	{
		ConnectionInfo connectionInfo = new ConnectionInfo();
		connectionInfo.connectionUrl = call.request().url().toString();
		connectionInfo.requestMethod = call.request().method();
		connectionInfo.requestHeaders = call.request().headers();
		if (response != null)
		{
			connectionInfo.responseHeaders = response.headers();
			connectionInfo.responseCode = response.code();
			connectionInfo.responseTime = response.receivedResponseAtMillis() - response.sentRequestAtMillis();
			connectionInfo.responseLength = response.body().contentLength();
		}
		return connectionInfo;
	}

	@Override
	public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
		ConnectionInfo connectionInfo = getConnectionInfoFromCall(call, response);
		// Replicates behaviour from AsyncHttp
		if (connectionInfo.responseCode < 400 && connectionInfo.responseCode > 100)
		{
			onSuccess(call, response, connectionInfo);
		}
		else
		{
			onFailure(call, null, connectionInfo);
		}
		onFinish(connectionInfo);
	}

	@Override
	public void onFailure(@NonNull Call call, @NonNull IOException e) {
		ConnectionInfo connectionInfo = getConnectionInfoFromCall(call, null);
		onFailure(call, e, connectionInfo);
		onFinish(connectionInfo);
	}

	public abstract void onSuccess(@NonNull Call call, @NonNull Response response, @NonNull ConnectionInfo connectionInfo) throws IOException;

	public abstract void onFailure(@NonNull Call call, @Nullable IOException e, @NonNull ConnectionInfo connectionInfo);

	public void onFinish(@NonNull ConnectionInfo connectionInfo)
	{
		// Empty
	}
}
