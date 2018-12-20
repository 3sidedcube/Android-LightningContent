package com.cube.storm.content

class StormExtension {
    String apiBase = ""
    String apiVersion = ""
    String appId = ""
    String orgId = ""
    String orgName = ""
    String bundleEnvironment = ""
    String authUsername = ""
    String authPassword = ""
    Date bundleTimestamp = null

    /**
     * Whether or not the Storm bundle is included in the assembled app's assets directory
     */
    BundleDownloadStrategy bundleDownloadStrategy = null

    public String getUrl()
    {
        String environment = bundleEnvironment.isEmpty() ? "live" : bundleEnvironment
        String url = "${apiBase}/${apiVersion}/apps/${appId}/bundle?environment=${environment}"

        if (bundleTimestamp != null) {
            url += "&timestamp=${bundleTimestamp.time}"
        }

        return url
    }

    public boolean isValid()
    {
        return !apiBase.isEmpty() && !apiVersion.isEmpty() && !appId.isEmpty() && bundleDownloadStrategy != null
    }

    public StormExtension merge(StormExtension other)
    {
        return new StormExtension(
                apiBase: this.apiBase.isEmpty() ? other.apiBase : this.apiBase,
                apiVersion: this.apiVersion.isEmpty() ? other.apiVersion : this.apiVersion,
                appId: this.appId.isEmpty() ? other.appId: this.appId,
                orgId: this.orgId.isEmpty() ? other.orgId: this.orgId,
                orgName: this.orgName.isEmpty() ? other.orgName: this.orgName,
                bundleEnvironment: this.bundleEnvironment.isEmpty() ? other.bundleEnvironment : this.bundleEnvironment,
                bundleTimestamp: this.bundleTimestamp == null ? other.bundleTimestamp : this.bundleTimestamp,
                bundleDownloadStrategy: this.bundleDownloadStrategy == null ? other.bundleDownloadStrategy : this.bundleDownloadStrategy,
                authUsername: this.authUsername.isEmpty() ? other.authUsername : this.authUsername,
                authPassword: this.authPassword.isEmpty() ? other.authPassword : this.authPassword
        )
    }

    public boolean requiresAuth() {
        return bundleEnvironment.equalsIgnoreCase("test")
    }

    public String toString()
    {
        return "Storm(apiBase=${apiBase}, apiVersion=${apiVersion}, appId=${appId}, orgId=${orgId}, orgName=${orgName}, bundleEnv=${bundleEnvironment}, bundleTimestamp=${bundleTimestamp}, bundleDownloadStrategy=${bundleDownloadStrategy}, authUsername=${authUsername}, authPassword=${authPassword}, url=${url})"
    }
}
