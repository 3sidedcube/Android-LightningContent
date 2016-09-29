#Storm Library - Module Content

Storm is a collection of libraries that helps make mobile and desktop applications easy to create using a high quality WYSIACATWYG editor.

This module's purpose is to load pre-packed bundles from disk and deal with content/delta updates download.

#Usage

##Gradle

Simply include the following for your gradle dependencies `com.3sidedcube.storm:content:0.5`.

**Note** The versioning of the library will always be as follows:

`Major version.Minor version.Bug fix`

It is safe to use `+` in part of of the `Bug fix` version, but do not trust it 100%. Always use a *specific* version to prevent regression errors.

##Code

In your application singleton, add the following code

```java
ContentSettings contentSettings = new ContentSettings.Builder(this)
	.appId("3SC_STORM-1-1")
	.contentBaseUrl("http://storm.cubeapis.com/")
	.contentVersion("latest")
	.updateListener(new UpdateListener()
	{
		@Override public void onUpdateDownloaded()
		{
			Debug.out("Update downloaded!!!!");
		}
	})
	.build();
```

You can then check for updates by calling

```java
// Get the current bundle/delta's timestamp
Manifest manifest = ContentSettings.getInstance().getBundleBuilder().buildManifest(Uri.parse("cache://manifest.json"));
long lastUpdate = 0;

if (manifest != null)
{
	lastUpdate = manifest.getTimestamp();
}

// Get updates since timestamp
ContentSettings.getInstance().getUpdateManager().checkForUpdates(lastUpdate);
```

#Documentation

See the [Javadoc](http://3sidedcube.github.io/Android-LightningContent/) for full in-depth code-level documentation

#Contributors

[Callum Taylor (9A8BAD)](http://keybase.io/scruffyfox), [Tim Mathews (5C4869)](https://keybase.io/timxyz), [Matt Allen (DB74F5)](https://keybase.io/mallen), [Alan Le Fournis (067EA0)](https://keybase.io/alan3sc)

#License

See LICENSE.md
