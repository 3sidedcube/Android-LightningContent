package com.cube.storm.content.lib.worker;

import androidx.lifecycle.LiveData;
import androidx.work.WorkInfo;
import com.cube.storm.content.model.UpdateContentProgress;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

import java.util.List;

class BackgroundWorkerObserver implements androidx.lifecycle.Observer<List<WorkInfo>>
{
	private LiveData<List<WorkInfo>> workLiveData;

	@Getter
	private Subject<UpdateContentProgress> subject = BehaviorSubject.create();

	public BackgroundWorkerObserver(LiveData<List<WorkInfo>> workLiveData)
	{
		this.workLiveData = workLiveData;
	}

	@Override
	public void onChanged(List<WorkInfo> workInfoList)
	{
		WorkInfo workInfo = workInfoList.get(0);

		switch (workInfo.getState())
		{
			case RUNNING:
			{
				subject.onNext(UpdateContentProgress.fromWorkerData(workInfo.getOutputData()));
				break;
			}
			case FAILED:
			case CANCELLED:
			{
				subject.onError(new IllegalStateException());
				workLiveData.removeObserver(this);
				break;
			}
			case SUCCEEDED:
			{
				subject.onComplete();
				workLiveData.removeObserver(this);
				break;
			}
			case BLOCKED:
			case ENQUEUED:
			{
				break;
			}
		}
	}
}
