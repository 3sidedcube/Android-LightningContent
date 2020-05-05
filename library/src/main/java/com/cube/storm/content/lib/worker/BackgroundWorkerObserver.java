package com.cube.storm.content.lib.worker;

import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.WorkInfo;
import com.cube.storm.content.model.UpdateContentProgress;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

/**
 * Adapter for the {@link WorkInfo} {@link LiveData} provided by Android {@link androidx.work.WorkManager} so that it
 * emits events compatible with RxJava.
 */
class BackgroundWorkerObserver implements androidx.lifecycle.Observer<WorkInfo>
{
	private LiveData<WorkInfo> workLiveData;

	/**
	 * BehaviorSubject so that subscribers receive the most recent event upon subscription, and then all subsequent
	 * events
	 */
	@Getter
	private Subject<UpdateContentProgress> subject = BehaviorSubject.create();

	public BackgroundWorkerObserver(LiveData<WorkInfo> workLiveData)
	{
		this.workLiveData = workLiveData;
	}

	@Override
	public void onChanged(WorkInfo workInfo)
	{
		switch (workInfo.getState())
		{
			case ENQUEUED:
			case BLOCKED:
			{
				subject.onNext(UpdateContentProgress.waiting());
				break;
			}
			case RUNNING:
			{
				if (workInfo.getProgress() != Data.EMPTY)
				{
					subject.onNext(UpdateContentProgress.fromWorkerData(workInfo.getProgress()));
				}
				break;
			}
			case CANCELLED:
			{
				subject.onError(new IllegalStateException("Job cancelled"));
				workLiveData.removeObserver(this);
				break;
			}
			case FAILED:
			{
				String errorMessage = workInfo.getOutputData().getString("error");
				subject.onError(new IllegalStateException(errorMessage));
				workLiveData.removeObserver(this);
				break;
			}
			case SUCCEEDED:
			{
				subject.onComplete();
				workLiveData.removeObserver(this);
				break;
			}
		}
	}
}
