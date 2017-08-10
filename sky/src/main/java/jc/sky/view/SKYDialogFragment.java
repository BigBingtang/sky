package jc.sky.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import jc.sky.SKYHelper;
import jc.sky.common.utils.SKYAppUtil;
import jc.sky.common.utils.SKYCheckUtils;
import jc.sky.common.utils.SKYKeyboardUtils;
import jc.sky.core.SKYIBiz;
import jc.sky.core.SKYIView;
import jc.sky.display.SKYIDisplay;
import jc.sky.modules.structure.SKYStructureModel;
import jc.sky.view.adapter.recycleview.SKYRVAdapter;

/**
 * @author sky
 * @version 版本
 */
public abstract class SKYDialogFragment<B extends SKYIBiz> extends DialogFragment implements SKYIDialogFragment, DialogInterface.OnKeyListener, SKYIView {

	private boolean				targetActivity;

	/** 请求编码 **/
	protected int				mRequestCode		= 2013 << 5;

	/** 请求默认值 **/
	public final static String	ARG_REQUEST_CODE	= "SKY_request_code";

	/** View层编辑器 **/
	private SKYBuilder			SKYBuilder;

	SKYStructureModel			SKYStructureModel;

	private Unbinder			unbinder;

	/**
	 * 定制
	 *
	 * @param initialSKYBuilder
	 *            参数
	 * @return 返回值
	 **/
	protected abstract SKYBuilder build(SKYBuilder initialSKYBuilder);

	/**
	 * 编译
	 *
	 * @param view
	 *            参数
	 */
	protected void buildAfter(View view) {}

	/**
	 * 初始化dagger
	 */
	protected void initDagger() {

	}

	/**
	 * 数据
	 *
	 * @param savedInstanceState
	 *            参数
	 */
	protected void createData(Bundle savedInstanceState) {

	}

	/**
	 * 初始化数据
	 *
	 * @param savedInstanceState
	 *            数据
	 */
	protected abstract void initData(Bundle savedInstanceState);

	/**
	 * 自定义样式
	 *
	 * @return 返回值
	 */
	protected abstract int getSKYStyle();

	/**
	 * 是否可取消
	 *
	 * @return 返回值
	 */
	protected boolean isCancel() {
		return false;
	}

	protected void setDialogCancel(boolean flg) {
		getDialog().setCanceledOnTouchOutside(flg);
	}

	protected boolean isFull() {
		return false;
	}

	protected boolean isFullWidth() {
		return false;
	}

	protected boolean isFullheight() {
		return false;
	}

	/**
	 * 是否设置目标活动
	 *
	 * @return 返回值 返回值
	 */
	public boolean isTargetActivity() {
		return targetActivity;
	}

	/**
	 * 创建Dialog
	 *
	 * @param savedInstanceState
	 *            参数
	 * @return 返回值 返回值
	 */
	@Override public Dialog onCreateDialog(Bundle savedInstanceState) {
		// 创建对话框
		Dialog dialog = new Dialog(getActivity(), getSKYStyle());
		return dialog;
	}

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/** 打开开关触发菜单项 **/
		setHasOptionsMenu(true);
		// 获取指定碎片
		final Fragment targetFragment = getTargetFragment();
		// 如果有指定碎片 从指定碎片里获取请求码，反之既然
		if (targetFragment != null) {
			mRequestCode = getTargetRequestCode();
		}
	}

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		/** 初始化结构 **/
		initCore();
		SKYHelper.structureHelper().attach(SKYStructureModel);
		/** 初始化视图 **/
		SKYBuilder = new SKYBuilder(this, inflater);
		View view = build(SKYBuilder).create();
		/** 初始化所有组建 **/
		unbinder = ButterKnife.bind(this, view);
		// 获取参数-设置是否可取消
		setDialogCancel(isCancel());
		getDialog().setOnKeyListener(this);
		/** build 之后 **/
		buildAfter(view);
		return view;
	}

	/**
	 * 核心
	 */
	protected void initCore() {
		SKYStructureModel = new SKYStructureModel(this, getArguments());
	}

	public Object model() {
		return SKYStructureModel.getSKYProxy().impl;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (isFull()) {
			Window window = getDialog().getWindow();
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		} else if (isFullWidth()) {
			Window window = getDialog().getWindow();
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		} else if (isFullheight()) {
			Window window = getDialog().getWindow();
			window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		}
		/** 初始化业务数据 **/
		SKYStructureModel.initBizBundle();

		/** 初始化dagger **/
		initDagger();
		createData(savedInstanceState);
		initData(getArguments());
	}

	@Override public void onResume() {
		super.onResume();
		SKYHelper.structureHelper().printBackStackEntry(getFragmentManager());
	}

	@Override public void onPause() {
		super.onPause();
		// 恢复初始化
		recyclerRefreshing(false);
	}

	@Override public void onDestroyView() {
		super.onDestroyView();

	}

	@Override public void onDestroy() {
		super.onDestroy();
		detach();
		/** 移除builder **/
		if (SKYBuilder != null) {
			SKYBuilder.detach();
			SKYBuilder = null;
		}
		if (SKYStructureModel != null) {
			SKYHelper.structureHelper().detach(SKYStructureModel);
		}
		/** 清空注解view **/
		unbinder.unbind();
		/** 关闭键盘 **/
		SKYKeyboardUtils.hideSoftInput(getActivity());
		// 销毁
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
	}

	/**
	 * 清空
	 */
	protected void detach() {}

	/**
	 * 设置输入法
	 * 
	 * @param mode
	 *            参数
	 */
	public void setSoftInputMode(int mode) {
		getActivity().getWindow().setSoftInputMode(mode);
	}

	public <D extends SKYIDisplay> D display(Class<D> eClass) {

		return SKYHelper.display(eClass);
	}

	public B biz() {
		if (SKYStructureModel == null || SKYStructureModel.getSKYProxy() == null || SKYStructureModel.getSKYProxy().proxy == null) {
			Class service = SKYAppUtil.getSuperClassGenricType(getClass(), 0);
			return (B) SKYHelper.structureHelper().createNullService(service);
		}
		return (B) SKYStructureModel.getSKYProxy().proxy;
	}

	public <C extends SKYIBiz> C biz(Class<C> service) {
		if (SKYStructureModel != null && service.equals(SKYStructureModel.getService())) {
			if (SKYStructureModel == null || SKYStructureModel.getSKYProxy() == null || SKYStructureModel.getSKYProxy().proxy == null) {
				return SKYHelper.structureHelper().createNullService(service);
			}
			return (C) SKYStructureModel.getSKYProxy().proxy;
		}
		return SKYHelper.biz(service);
	}

	/**
	 * 创建menu
	 *
	 * @param menu
	 *            参数
	 */
	@Override public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if (SKYBuilder.getToolbarMenuId() > 0) {
			menu.clear();
			this.getActivity().getMenuInflater().inflate(SKYBuilder.getToolbarMenuId(), menu);
		}
	}

	@Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		SKYHelper.methodsProxy().activityInterceptor().onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	/**
	 * 获取fragment
	 * 
	 * @param <T>
	 *            参数
	 * @param clazz
	 *            参数
	 * @return 返回值
	 */
	public <T> T findFragment(Class<T> clazz) {
		SKYCheckUtils.checkNotNull(clazz, "class不能为空");
		return (T) getFragmentManager().findFragmentByTag(clazz.getName());
	}

	/********************** Actionbar业务代码 *********************/

	@Override public void showContent() {
		if (SKYBuilder != null) {
			SKYBuilder.layoutContent();
		}
	}

	@Override public void showLoading() {
		if (SKYBuilder != null) {
			SKYBuilder.layoutLoading();
		}
	}

	@Override public void showBizError() {
		if (SKYBuilder != null) {
			SKYBuilder.layoutBizError();
		}
	}

	@Override public void showEmpty() {
		if (SKYBuilder != null) {
			SKYBuilder.layoutEmpty();
		}
	}

	@Override public void showHttpError() {
		if (SKYBuilder != null) {
			SKYBuilder.layoutHttpError();
			recyclerRefreshing(false);
		}
	}

	@Override public int showState() {
		if (SKYBuilder != null) {
			return SKYBuilder.getLayoutState();
		} else {
			return SKYIView.STATE_CONTENT;
		}
	}

	@Override public <O extends SKYRVAdapter> O adapter() {
		return SKYBuilder == null ? null : (O) SKYBuilder.getSKYRVAdapterItem();
	}

	/**********************
	 * Actionbar业务代码
	 * 
	 * @return 返回值
	 *********************/
	public Toolbar toolbar() {
		return SKYBuilder == null ? null : SKYBuilder.getToolbar();
	}

	/**********************
	 * RecyclerView业务代码
	 * 
	 * @return 返回值
	 *********************/

	public RecyclerView.LayoutManager layoutManager() {
		return SKYBuilder == null ? null : SKYBuilder.getLayoutManager();
	}

	public RecyclerView recyclerView() {
		return SKYBuilder == null ? null : SKYBuilder.getRecyclerView();
	}

	public void recyclerRefreshing(boolean bool) {
		if (SKYBuilder != null) {
			SKYBuilder.recyclerRefreshing(bool);
		}
	}

	public SwipeRefreshLayout swipRefesh() {
		if (SKYBuilder == null) {
			return null;
		}
		return SKYBuilder.getSwipeContainer();
	}

	/**********************
	 * View业务代码
	 * 
	 * @return 返回值
	 *********************/

	public SKYView SKYView() {
		return SKYBuilder == null ? null : SKYBuilder.getSKYView();
	}

	/**
	 * 可见
	 */
	protected void onVisible() {}

	/**
	 * 不可见
	 */
	protected void onInvisible() {}

	/**
	 * 返回键
	 * 
	 * @return 返回值
	 */
	public boolean onKeyBack() {
		dismissAllowingStateLoss();
		return true;
	}

	/********************** Dialog业务代码 *********************/
	/**
	 * @param listenerInterface
	 *            参数
	 * @return 返回值
	 * @param <T>
	 *            参数 获取某种类型的所有侦听器
	 */
	protected <T> List<T> getDialogListeners(Class<T> listenerInterface) {
		final Fragment targetFragment = getTargetFragment();
		List<T> listeners = new ArrayList<>(2);
		if (targetFragment != null && listenerInterface.isAssignableFrom(targetFragment.getClass())) {
			listeners.add((T) targetFragment);
		}
		if (getActivity() != null && listenerInterface.isAssignableFrom(getActivity().getClass())) {
			listeners.add((T) getActivity());
		}
		return Collections.unmodifiableList(listeners);
	}

	/**
	 * 取消
	 *
	 * @param dialog
	 *            参数
	 */
	@Override public void onCancel(DialogInterface dialog) {
		super.onCancel(dialog);
		for (IDialogCancelListener listener : getCancelListeners()) {
			listener.onCancelled(mRequestCode);
		}
	}

	/**
	 * 获取取消的所有事件
	 *
	 * @return 返回值
	 */
	protected List<IDialogCancelListener> getCancelListeners() {
		return getDialogListeners(IDialogCancelListener.class);
	}

	/**
	 * 显示碎片
	 *
	 * @return 返回值
	 */
	@Override public DialogFragment show(FragmentManager fragmentManager) {
		show(fragmentManager, this.getClass().getName());
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param mRequestCode
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment show(FragmentManager fragmentManager, int mRequestCode) {
		this.mRequestCode = mRequestCode;
		show(fragmentManager, this.getClass().getName());
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param mTargetFragment
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment show(FragmentManager fragmentManager, Fragment mTargetFragment) {
		this.setTargetFragment(mTargetFragment, mRequestCode);
		show(fragmentManager, this.getClass().getName());
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param mTargetFragment
	 *            参数
	 * @param mRequestCode
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment show(FragmentManager fragmentManager, Fragment mTargetFragment, int mRequestCode) {
		this.setTargetFragment(mTargetFragment, mRequestCode);
		show(fragmentManager, this.getClass().getName());
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param activity
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment show(FragmentManager fragmentManager, Activity activity) {
		this.targetActivity = true;
		show(fragmentManager, this.getClass().getName());
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param activity
	 *            参数
	 * @param mRequestCode
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment show(FragmentManager fragmentManager, Activity activity, int mRequestCode) {
		this.targetActivity = true;
		this.mRequestCode = mRequestCode;
		show(fragmentManager, this.getClass().getName());
		return this;
	}

	/**
	 * @param item
	 *            参数
	 * @return 返回值
	 */
	@Override public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			getActivity().onBackPressed();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 显示碎片-不保存activity状态
	 *
	 * @return 返回值 返回值
	 */
	@Override public DialogFragment showAllowingStateLoss(FragmentManager fragmentManager) {
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(this, this.getClass().getName());
		ft.commitAllowingStateLoss();
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param mRequestCode
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment showAllowingStateLoss(FragmentManager fragmentManager, int mRequestCode) {
		this.mRequestCode = mRequestCode;
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(this, this.getClass().getName());
		ft.commitAllowingStateLoss();
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param mTargetFragment
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment showAllowingStateLoss(FragmentManager fragmentManager, Fragment mTargetFragment) {
		if (mTargetFragment != null) {
			this.setTargetFragment(mTargetFragment, mRequestCode);
		}
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(this, this.getClass().getName());
		ft.commitAllowingStateLoss();
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param mTargetFragment
	 *            参数
	 * @param mRequestCode
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment showAllowingStateLoss(FragmentManager fragmentManager, Fragment mTargetFragment, int mRequestCode) {
		if (mTargetFragment != null) {
			this.setTargetFragment(mTargetFragment, mRequestCode);
		}
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(this, this.getClass().getName());
		ft.commitAllowingStateLoss();
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param activity
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment showAllowingStateLoss(FragmentManager fragmentManager, Activity activity) {
		this.targetActivity = true;
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(this, this.getClass().getName());
		ft.commitAllowingStateLoss();
		return this;
	}

	/**
	 * @param fragmentManager
	 *            参数
	 * @param activity
	 *            参数
	 * @param mRequestCode
	 *            参数
	 * @return 返回值
	 */
	@Override public DialogFragment showAllowingStateLoss(FragmentManager fragmentManager, Activity activity, int mRequestCode) {
		this.targetActivity = true;
		this.mRequestCode = mRequestCode;
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(this, this.getClass().getName());
		ft.commitAllowingStateLoss();
		return this;
	}

	/**
	 * @param dialog
	 *            参数
	 * @param keyCode
	 *            参数
	 * @param event
	 *            参数
	 * @return 返回值
	 */
	@Override public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return onKeyBack();
		} else {
			return false;
		}
	}

	/**
	 * 获取内容视图
	 *
	 * @return 视图
	 */
	protected View contentView() {
		return SKYBuilder.getContentRootView();
	}
}