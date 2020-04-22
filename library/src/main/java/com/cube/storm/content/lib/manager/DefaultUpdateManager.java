package com.cube.storm.content.lib.manager;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import com.cube.storm.ContentSettings;
import com.cube.storm.content.lib.Constants;
import com.cube.storm.content.lib.handler.GZIPTarCacheResponseHandler;
import com.cube.storm.content.lib.helper.BundleHelper;
import com.cube.storm.content.lib.helper.FileHelper;
import com.cube.storm.content.lib.worker.ContentUpdateWorker;
import com.cube.storm.content.model.UpdateContentProgress;
import com.cube.storm.content.model.UpdateContentRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;
import net.callumtaylor.asynchttp.AsyncHttpClient;
import net.callumtaylor.asynchttp.response.JsonResponseHandler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This is the manager class responsible for checking for and downloading updates from the server
 * <p/>
 * Access this class via {@link ContentSettings#getUpdateManager()}. Do not instantiate this class directly
 *
 * @author Callum Taylor
 * @project LightningContent
 */
public class DefaultUpdateManager implements UpdateManager
{
	private AsyncHttpClient apiClient;

	private Subject<UpdateContentRequest> updates = PublishSubject.create();

	/**
	 * Downloads the latest full bundle from the server
	 */
	@Override
	public UpdateContentRequest checkForBundle()
	{
		Subject<UpdateContentProgress> observer = BehaviorSubject.create();
		UpdateContentRequest updateContentRequest = new UpdateContentRequest(
			Long.toString(System.currentTimeMillis()),
			ContentUpdateWorker.UpdateType.FULL_BUNDLE,
			null,
			observer
		);
		updates.onNext(updateContentRequest);
		checkForBundle(observer);
		return updateContentRequest;
	}

	private void checkForBundle(Observer<UpdateContentProgress> observer)
	{
		observer.onNext(UpdateContentProgress.checking());
		apiClient = ContentSettings.getInstance().getApiManager().checkForBundle(new JsonResponseHandler()
		{
			@Override public void onSuccess()
			{
				apiClient = null;

				boolean toDownload = false;

				if (getConnectionInfo().responseCode < 300 && getConnectionInfo().responseCode >= 200)
				{
					try
					{
						if (getConnectionInfo().responseCode == 200)
						{
							JsonElement response = getContent();

							if (response != null && response.isJsonObject())
							{
								if (response.getAsJsonObject().has("file"))
								{
									String endpoint = response.getAsJsonObject().get("file").getAsString();
									downloadUpdates(endpoint, observer);
									toDownload = true;
								}
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (getConnectionInfo().responseCode == 303)
				{
					if (!TextUtils.isEmpty(getConnectionInfo().responseHeaders.get("Location")))
					{
						String location = getConnectionInfo().responseHeaders.get("Location");
						downloadUpdates(location, observer);
						toDownload = true;
					}
				}

				if (!toDownload)
				{
					observer.onComplete();
				}

				if (ContentSettings.getInstance().getUpdateListener() != null)
				{
					ContentSettings.getInstance().getUpdateListener().onUpdateCheckFinished(toDownload);
				}
			}

			@Override public void onFailure()
			{
				observer.onError(new IOException("Unexpected response when checking for bundle: " + getConnectionInfo().toString()));

				if (ContentSettings.getInstance().getUpdateListener() != null)
				{
					ContentSettings.getInstance().getUpdateListener().onUpdateCheckFailed(getConnectionInfo());
				}
			}
		});
	}

	/**
	 * Checks for updates on the server and downloads any new files in the form of a delta bundle
	 *
	 * @param lastUpdate The time of the last update. Usually found in the {@code manifest.json} file
	 */
	@Override
	public UpdateContentRequest checkForUpdates(final long lastUpdate)
	{
		Subject<UpdateContentProgress> observer = BehaviorSubject.create();
		UpdateContentRequest updateContentRequest = new UpdateContentRequest(
			Long.toString(System.currentTimeMillis()),
			ContentUpdateWorker.UpdateType.DELTA,
			lastUpdate,
			observer
		);
		updates.onNext(updateContentRequest);
		checkForUpdates(lastUpdate, observer);
		return updateContentRequest;
	}

	private void checkForUpdates(long lastUpdate, Observer<UpdateContentProgress> observer)
	{
		observer.onNext(UpdateContentProgress.checking());
		apiClient = ContentSettings.getInstance().getApiManager().checkForDelta(lastUpdate, new JsonResponseHandler()
		{
			@Override public void onSuccess()
			{
				apiClient = null;

				boolean toDownload = false;

				if (getConnectionInfo().responseCode < 300 && getConnectionInfo().responseCode >= 200)
				{
					try
					{
						if (getConnectionInfo().responseCode == 200)
						{
							JsonElement response = getContent();

							if (response != null && response.isJsonObject())
							{
								if (response.getAsJsonObject().has("file"))
								{
									String endpoint = response.getAsJsonObject().get("file").getAsString();
									downloadUpdates(endpoint, observer);
									toDownload = true;
								}
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
				else if (getConnectionInfo().responseCode == 303)
				{
					if (!TextUtils.isEmpty(getConnectionInfo().responseHeaders.get("Location")))
					{
						String location = getConnectionInfo().responseHeaders.get("Location");
						downloadUpdates(location, observer);
						toDownload = true;
					}
				}

				if (!toDownload)
				{
					observer.onComplete();
				}

				if (ContentSettings.getInstance().getUpdateListener() != null)
				{
					ContentSettings.getInstance().getUpdateListener().onUpdateCheckFinished(toDownload);
				}
			}

			@Override public void onFailure()
			{
				observer.onError(new IllegalStateException("Unexpected response when checking for delta update: " + getConnectionInfo().toString()));
				if (ContentSettings.getInstance().getUpdateListener() != null)
				{
					ContentSettings.getInstance().getUpdateListener().onUpdateCheckFailed(getConnectionInfo());
				}
			}
		});
	}

	/**
	 * Downloads a tar.gz file from the given endpoint
	 *
	 * @param endpoint The endpoint to the tar.gz bundle/delta file
	 */
	public UpdateContentRequest downloadUpdates(@NonNull String endpoint)
	{
		Subject<UpdateContentProgress> observer = BehaviorSubject.create();
		UpdateContentRequest updateContentRequest = new UpdateContentRequest(
			Long.toString(System.currentTimeMillis()),
			ContentUpdateWorker.UpdateType.DIRECT_DOWNLOAD,
			null,
			observer
		);
		updates.onNext(updateContentRequest);
		downloadUpdates(endpoint, observer);
		return updateContentRequest;
	}

	public void downloadUpdates(String endpoint, Observer<UpdateContentProgress> observer)
	{
		observer.onNext(UpdateContentProgress.downloading(0, 0));

		if (!TextUtils.isEmpty(ContentSettings.getInstance().getStoragePath()))
		{
			// download to temp file
			File deltaDirectory = new File(ContentSettings.getInstance().getStoragePath() + "/delta");
			FileHelper.deleteRecursive(deltaDirectory);
			deltaDirectory.mkdir();

			apiClient = new AsyncHttpClient(endpoint);
			apiClient.get(new GZIPTarCacheResponseHandler(ContentSettings.getInstance().getStoragePath() + "/delta")
			{
				@Override public void onByteChunkReceivedProcessed(long totalProcessed, long totalLength)
				{
					super.onByteChunkReceivedProcessed(totalProcessed, totalLength);

					observer.onNext(UpdateContentProgress.downloading(totalProcessed, totalLength));
					if (ContentSettings.getInstance().getDownloadListener() != null)
					{
						ContentSettings.getInstance().getDownloadListener().onDownloadProgress(totalProcessed, totalLength);
					}
				}

				@Override public void onSuccess()
				{
					try
					{
						super.onSuccess();
						observer.onNext(UpdateContentProgress.verifying());

						// delete the bundle
						new File(getFilePath() + "/bundle.tar").delete();

						File path = new File(ContentSettings.getInstance().getStoragePath());

						if (BundleHelper.integrityCheck(getFilePath()))
						{
							observer.onNext(UpdateContentProgress.deploying());
							// Move files from /delta to ../
							FileHelper.copyDirectory(new File(getFilePath()), new File(ContentSettings.getInstance().getStoragePath()));
							FileHelper.deleteRecursive(new File(getFilePath()));

							File manifest = new File(path, Constants.FILE_MANIFEST);
							JsonObject manifestJson = ContentSettings.getInstance().getFileManager().readFileAsJson(manifest).getAsJsonObject();

							// Create map of expected data
							Map<String, String[]> expectedContent = new HashMap<String, String[]>();
							expectedContent.put("pages", new File(path + "/" + Constants.FOLDER_PAGES + "/").list());
							expectedContent.put("languages", new File(path + "/" + Constants.FOLDER_LANGUAGES + "/").list());
							expectedContent.put("content", new File(path + "/" + Constants.FOLDER_CONTENT + "/").list());
							expectedContent.put("data", new File(path + "/" + Constants.FOLDER_DATA + "/").list());

							for (String key : expectedContent.keySet())
							{
								if (manifestJson.has(key))
								{
									JsonArray pagesList = manifestJson.get(key).getAsJsonArray();

									for (JsonElement e : pagesList)
									{
										String filename = e.getAsJsonObject().get("src").getAsString();

										int size = expectedContent.get(key) == null ? 0 : expectedContent.get(key).length;
										for (int index = 0; index < size; index++)
										{
											if (expectedContent.get(key)[index] != null && expectedContent.get(key)[index].equals(filename))
											{
												expectedContent.get(key)[index] = null;
												break;
											}
										}
									}

									FileHelper.removeFiles(path + "/" + key + "/", expectedContent.get(key));
								}
							}
						}
					}
					catch (Exception e)
					{
						observer.onError(e);
						if (ContentSettings.getInstance().getUpdateListener() != null)
						{
							ContentSettings.getInstance().getUpdateListener().onUpdateFailed(1, getConnectionInfo());
						}
					}
				}

				@Override public void onFailure()
				{
					observer.onError(new IllegalStateException("Failed to download bundle"));
					if (ContentSettings.getInstance().getUpdateListener() != null)
					{
						ContentSettings.getInstance().getUpdateListener().onUpdateFailed(1, getConnectionInfo());
					}
				}

				@Override public void onFinish()
				{
					apiClient = null;

					if (getConnectionInfo().responseCode >= 200 && getConnectionInfo().responseCode < 300)
					{
						observer.onComplete();
						if (ContentSettings.getInstance().getUpdateListener() != null)
						{
							ContentSettings.getInstance().getUpdateListener().onUpdateDownloaded();
						}
					}
					else
					{
						observer.onError(new IllegalStateException("Unexpected response when downloading bundle: " + getConnectionInfo().toString()));
						if (ContentSettings.getInstance().getUpdateListener() != null)
						{
							ContentSettings.getInstance().getUpdateListener().onUpdateFailed(0, getConnectionInfo());
						}
					}
				}
			});
		}
	}

	/**
	 * Cancels any pending api requests
	 */
	@Override
	public void cancelPendingRequests()
	{
		if (apiClient != null)
		{
			apiClient.cancel();
		}
	}

	@Override
	public void scheduleBackgroundUpdates()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Observable<UpdateContentRequest> updates()
	{
		return updates;
	}
}
