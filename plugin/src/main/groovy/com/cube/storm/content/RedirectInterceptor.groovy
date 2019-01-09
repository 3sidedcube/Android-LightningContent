package com.cube.storm.content

import org.apache.http.HttpException
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.protocol.HttpContext

/**
 * Intercepts HTTP redirects and (i) logs the redirect for informational purposes, and (ii) removes any auth headers
 * from the redirected request for the specific case where the Storm API forwards us to an S3 bucket. In this case we
 * do not want to send the auth header onwards as it causes Amazon to reject the request.
 */
class RedirectInterceptor implements HttpRequestInterceptor {

	@Override
	void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
		if (context instanceof HttpContext) {
			HttpClientContext httpClientContext = (HttpClientContext)context
			if (httpClientContext.redirectLocations != null && !httpClientContext.redirectLocations.isEmpty()) {
				println "Remove auth headers from forwarded request: " + request.toString()
				request.removeHeaders("Authorization")
			}
		}
	}
}
