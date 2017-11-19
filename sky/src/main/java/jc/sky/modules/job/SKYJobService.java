package jc.sky.modules.job;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;

import jc.sky.core.SKYHelper;
import jc.sky.modules.log.L;

/**
 * @author sky
 * @version 1.0 on 2017-06-10 下午10:41
 * @see SKYJobService
 */
public class SKYJobService implements SKYIJobService {

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) @Override public void schedule(JobInfo.Builder builder) {
		JobScheduler jobScheduler = (JobScheduler) SKYHelper.getInstance().getSystemService(Context.JOB_SCHEDULER_SERVICE);
		if (jobScheduler == null) {
			if (SKYHelper.isLogOpen()) {
				L.tag("SKYJobService");
				L.i("无法获取JobScheduler实例~");
			}
			return;
		}
		jobScheduler.schedule(builder.build());
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) @Override public void cancel(int id) {
		JobScheduler jobScheduler = (JobScheduler) SKYHelper.getInstance().getSystemService(Context.JOB_SCHEDULER_SERVICE);
		if (jobScheduler == null) {
			if (SKYHelper.isLogOpen()) {
				L.tag("SKYJobService");
				L.i("无法获取JobScheduler实例~");
			}
			return;
		}
		jobScheduler.cancel(id);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) @Override public void cancelAll() {
		JobScheduler jobScheduler = (JobScheduler) SKYHelper.getInstance().getSystemService(Context.JOB_SCHEDULER_SERVICE);
		if (jobScheduler == null) {
			if (SKYHelper.isLogOpen()) {
				L.tag("SKYJobService");
				L.i("无法获取JobScheduler实例~");
			}
			return;
		}
		jobScheduler.cancelAll();
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP) @Override public JobInfo.Builder builder(int id, Class clazz) {
		JobInfo.Builder builder = new JobInfo.Builder(id, new ComponentName(SKYHelper.getInstance().getPackageName(), clazz.getName()));
		return builder;
	}
}
