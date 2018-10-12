package com.cube.storm.content

class StormExtension {
    String apiBase = ""
    String apiVersion = ""
    String appId = ""
    String orgId = ""
    String orgName = ""

    /**
     * Whether or not the Storm bundle is included in the assembled app's assets directory
     */
    Boolean isBundled = null

    /**
     * Whether or not the Storm bundle is automatically downloaded every time the app is assembled.
     *
     * This property only takes effect if isBundled = true
     *
     * By default this is only true for release builds
     */
    Boolean isBundledAutomatically = null

    public String createBundleUrl(String environment)
    {
        return "${apiBase}/${apiVersion}/apps/${appId}/bundle?environment=${environment}"
    }

    public boolean isValid()
    {
        return !apiBase.isEmpty() && !apiVersion.isEmpty() && !appId.isEmpty() && isBundled != null && isBundledAutomatically != null
    }

    public StormExtension merge(StormExtension other)
    {
        return new StormExtension(
                apiBase: this.apiBase.isEmpty() ? other.apiBase : this.apiBase,
                apiVersion: this.apiVersion.isEmpty() ? other.apiVersion : this.apiVersion,
                appId: this.appId.isEmpty() ? other.appId: this.appId,
                orgId: this.orgId.isEmpty() ? other.orgId: this.orgId,
                orgName: this.orgName.isEmpty() ? other.orgName: this.orgName,
                isBundled: this.isBundled == null ? other.isBundled : this.isBundled,
                isBundledAutomatically: this.isBundledAutomatically == null ? other.isBundledAutomatically : this.isBundledAutomatically
        )
    }

    public String toString()
    {
        return "Storm(apiBase=${apiBase}, apiVersion=${apiVersion}, appId=${appId}, orgId=${orgId}, orgName=${orgName}, isBundled=${isBundled}, isBundledAutomatically=${isBundledAutomatically})"
    }
}
