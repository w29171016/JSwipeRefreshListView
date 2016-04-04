package com.janedler.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AbsListView;
import android.widget.ListView;

import com.janedler.R;
import com.janedler.exception.SwipeRefreshException;
import com.janedler.controller.IFooterController;
import com.janedler.interfaces.IPullUpStates;
import com.janedler.interfaces.MODE;
import com.janedler.listener.OnPullDownListener;
import com.janedler.listener.OnPullUpListener;
import com.janedler.log.JSwipeRefreshLog;

/**
 * SwipeRefreshLayout扩展积累 支持自定上拉加载更多的样式
 *
 * @author janedler
 */
public abstract class JSwipeRefreshBaseListView extends SwipeRefreshLayout {

    private int mTouchSlop;// 最小滑动距离
    protected Context mContext;  //context
    protected int mFootContentHeight; //footer height
    protected int mMode = MODE.ONLY_DOWN; //设置是否运行上拉或者下拉
    protected int mFooterStatus = IPullUpStates.PULL_UP_NOMAL; //FooterView 视图状态
    protected IFooterController mController;
    protected ListView mListView; //ListView View
    private View mFootRootView; //FooterView
    private int mDistanceY; //滑动的距离
    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    private int mTotalItemCount;
    private boolean isBottom = false;
    private int mStartY = 0;
    private int mPaddingBottom = 0;
    private boolean isRecord = false;
    // 加载数据的监听
    protected OnPullUpListener mPullUpListener;
    // 刷新监听
    protected OnPullDownListener mPullDownListener;
    //Listview滚动监听器
    protected JOnScrollListener mJOnScrollListener;
    // 刷新监听标记
    private boolean mRefreshListenerFlag = false;

    public JSwipeRefreshBaseListView(Context context) {
        this(context, null);
    }

    public JSwipeRefreshBaseListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mController = getController();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        inflateListView(R.layout.ui_swipe_default_listview);
        setHeaderDefaltStyle(); //设置默认的头部加载样式
        mFootRootView = mController.setFooterView();
        mRefreshListenerFlag = true;
        setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mController.isPullUpLoading()) {
                    mController.pullDownComplete();
                    return;
                }
                if (mPullDownListener != null) mPullDownListener.onRefresh();
            }
        });
    }

    /**
     * @return
     */
    private void inflateListView(int resListViewId) {
        mListView = (ListView) LayoutInflater.from(mContext).inflate(resListViewId, null, false);
        mListView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mListView.setOnScrollListener(new JOnScrollListener());
        this.addView(mListView);
    }


    /**
     * 设置默认的头部加载样式
     */
    private void setHeaderDefaltStyle() {
        // 设置刷新进度颜色变化
        setColorSchemeColors(Color.BLUE, Color.GREEN, Color.RED);
        // 设置刷新进度的背景
        setProgressBackgroundColorSchemeColor(Color.argb(105, 22, 55, 66));
        // 设置刷新进度的大小
        setSize(SwipeRefreshLayout.DEFAULT);
    }

    protected void setFooterPadding(int left, int top, int right, int bottom, boolean isRecordPadding) {
        if (isRecordPadding) mPaddingBottom = bottom;
        mFootRootView.setPadding(left, top, right, bottom);
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                doActionDown(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                doActionMove(ev);
                break;
            case MotionEvent.ACTION_UP:
                doActionUp(ev);
                break;
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void doActionDown(MotionEvent event) {
        isRecord = false;
        isBottom = false;
    }

    private void doActionMove(final MotionEvent event) {

        isBottom = isBottom ? true : mFirstVisibleItem + mVisibleItemCount == mTotalItemCount;
        if (!isBottom || mController.isPullUpLoading() || isRefreshing()) {
            return;
        }
        if (!isRecord) {
            mStartY = (int) event.getRawY();
            isRecord = true;
        }


        if (mController instanceof JSwipeRefreshManualListView) {

            mListView.post(new Runnable() {
                @Override
                public void run() {
                    mDistanceY = (int) (event.getRawY() - mStartY);
                    if (-mDistanceY < mTouchSlop) {
                        return;
                    }
                    if (mMode == MODE.ONLY_DOWN) {
                        setFooterPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop, false);
                        mController.footerStylePullUpOnlyDown();
                        return;
                    }
                    switch (mFooterStatus) {
                        case IPullUpStates.PULL_UP_NOMAL:
                            setFooterPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop, false);
                            ((JSwipeRefreshManualListView) mController).footerStyleManualPullUp();
                            break;

                        case IPullUpStates.PULL_UP:
                            setFooterPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop, false);
                            if (-mDistanceY + mPaddingBottom > mFootContentHeight) {
                                ((JSwipeRefreshManualListView) mController).footerStyleManualRelease();
                            }
                            break;

                        case IPullUpStates.PULL_UP_RELEASE:
                            setFooterPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop, false);
                            if (-mDistanceY + mPaddingBottom > mFootContentHeight) {
                                ((JSwipeRefreshManualListView) mController).footerStyleManualRelease();
                            } else if (-mDistanceY + mPaddingBottom > 0 && -mDistanceY + mPaddingBottom <= mFootContentHeight) {
                                ((JSwipeRefreshManualListView) mController).footerStyleManualPullUp();
                            } else {

                            }
                            break;
                        case IPullUpStates.PULL_UP_OVER:
                            setFooterPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop, false);
                            break;
                    }
                }

            });
        }
    }


    private void doActionUp(MotionEvent event) {
        isRecord = false;
        isBottom = false;
        mDistanceY = 0;
        mStartY = 0;
        if (mController instanceof JSwipeRefreshManualListView) {
            switch (mFooterStatus) {
                case IPullUpStates.PULL_UP_NOMAL:
                    //updateFooterView(IPullUpStates.PULL_UP_NOMAL);
                    break;

                case IPullUpStates.PULL_UP:
                    setFooterPadding(0, 0, 0, -mFootContentHeight, true);
                    ((JSwipeRefreshManualListView) mController).footerStyleManualNomal();
                    break;

                case IPullUpStates.PULL_UP_RELEASE:
                    setFooterPadding(0, 0, 0, 0, true);
                    ((JSwipeRefreshManualListView) mController).footerStyleManualLoading();
                    break;
                case IPullUpStates.PULL_UP_OVER:
                    setFooterPadding(0, 0, 0, 0, true);
                    break;
                case IPullUpStates.PULL_UP_ONLY_DOWN:
                    setFooterPadding(0, 0, 0, 0, true);
                    break;
            }
        }
    }

    public ListView getListView() {
        return mListView;
    }


    /**
     * 上拉刷新Listener
     */
    public void setOnPullUpListener(@NonNull OnPullUpListener loadListener) {
        mPullUpListener = loadListener;
    }

    /**
     * 下拉刷新Listener
     */
    public void setOnPullDownListener(@NonNull OnPullDownListener refreshListener) {
        mPullDownListener = refreshListener;
    }

    /**
     * ListView ScrollListener
     *
     * @param jOnScrollListener
     */
    public void setJOnScrollListener(JOnScrollListener jOnScrollListener) {
        this.mJOnScrollListener = jOnScrollListener;
    }

    @Override
    public void setOnRefreshListener(OnRefreshListener listener) {
        if (mRefreshListenerFlag) {
            super.setOnRefreshListener(listener);
            mRefreshListenerFlag = false;
        } else {
            throw new SwipeRefreshException("请使用setOnPullDownListener方法");
        }

    }


    /**
     * ListView 滚动监听器
     */
    private class JOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mJOnScrollListener != null)
                mJOnScrollListener.onScrollStateChanged(view, scrollState);
            if (mController instanceof JSwipeRefreshAutoListView && ((JSwipeRefreshAutoListView) mController).isAllowAutoLoad() && mMode != MODE.ONLY_DOWN && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && (mFirstVisibleItem + mVisibleItemCount == mTotalItemCount)) {
                JSwipeRefreshLog.e("Footer scroll bottom");
                ((JSwipeRefreshAutoListView) mController).footerStyleAutoLoading();
                if (mPullUpListener != null) mPullUpListener.onLoad();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (mJOnScrollListener != null)
                mJOnScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            mFirstVisibleItem = firstVisibleItem;
            mVisibleItemCount = visibleItemCount;
            mTotalItemCount = totalItemCount;

        }
    }

    /**
     * 自动下拉刷新
     */
    public void autoRefreshing() {

        if(isRefreshing() || mController.isPullUpLoading()){
            JSwipeRefreshLog.e("please wait, listview is loading");
            return;
        }

        mListView.post(new Runnable() {
            @Override
            public void run() {
                mListView.setSelection(0);
                setRefreshing(true);
                if (mPullDownListener != null) mPullDownListener.onRefresh();
            }
        });
    }


    /**
     * 下拉刷新完成
     */
    public void pullDownComplete() {
        JSwipeRefreshLog.e("pulldown is complete");
        mListView.post(new Runnable() {
            @Override
            public void run() {
                setRefreshing(false);
                mFooterStatus = IPullUpStates.PULL_UP_NOMAL;
                if (mMode == MODE.BOTH) {
                    if (mController instanceof JSwipeRefreshManualListView) {
                        ((JSwipeRefreshManualListView) mController).footerStyleManualNomal();
                    }
                    if (mController instanceof JSwipeRefreshAutoListView) {
                        ((JSwipeRefreshAutoListView) mController).footerStyleAutoBottom();
                    }

                }
                if (mMode == MODE.ONLY_DOWN) {
                    mController.footerStylePullUpOnlyDown();
                }
            }
        });
    }

    protected abstract IFooterController getController();
}
