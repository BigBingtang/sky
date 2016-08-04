package jc.sky.modules.screen;

import android.support.v4.app.FragmentActivity;
import android.util.Log;

import jc.sky.SKYHelper;

/**
 * @创建人 sky
 * @创建时间 16/2/27
 * @类描述
 */
public class SKYScreenHolder {

	private FragmentActivity activity;

	private boolean		isLanding	= false;

	private boolean		isRunning	= true;

	private String		activityName;

	public SKYScreenHolder(FragmentActivity activity, boolean isLanding) {
		this.activity = activity;
		this.activityName = activity.getClass().getSimpleName();
		this.isLanding = isLanding;
		log(" 创建.");
	}

	public void pause() {
		this.isRunning = false;
		log(" 暂停.");
	}

	public void resume() {
		this.isRunning = true;
		log(" 运行.");
	}

	public void result(){
		this.isRunning = true;
	}

	public FragmentActivity getActivity() {
		return activity;
	}

	public boolean isLanding() {
		return isLanding;
	}

	public void setLanding(boolean isLanding) {
		this.isLanding = isLanding;
		log(isLanding ? " 定位!" : " 没有定位!");
	}

	public boolean isRunning() {
		return isRunning;
	}

	public void finish() {
		activity.finish();
		log(" 关闭.");
	}

	public void removed() {
		log(" 关闭.");
	}

	private void log(String message) {
		if (SKYHelper.getInstance().isLogOpen()) {
			Log.i("SKYActivityManager", activityName + message);
		}
	}

}
