package com.cube.storm.content.lib.worker;

import androidx.lifecycle.LiveData;
import androidx.work.Data;
import androidx.work.WorkInfo;
import com.cube.storm.content.model.UpdateContentProgress;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.Subject;
import lombok.Getter;

class BackgroundWorkerObserver implements androidx.lifecycle.Observer<WorkInfo>
{
	private LiveData<WorkInfo> workLiveData;

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
			case FAILED:
			case CANCELLED:
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
