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
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.janedler.R;
import com.janedler.exception.ErrorMethodException;
import com.janedler.listener.OnPullDownListener;
import com.janedler.listener.OnPullUpListener;

/**
 * SwipeRefreshLayout 支持上拉与下拉刷新
 *
 * @author janedler
 */
public class JSwipeRefreshListView extends SwipeRefreshLayout {

    private int mTouchSlop;// 最小滑动距离
    private Context mContext;  //context
    private View mFootRootView; //footer view
    private TextView mFooterTipTV;
    private LinearLayout mFooterContentLayout;
    private ProgressBar mFooterbar;
    private TextView mFooterHint;
    private ListView mListView;// 操作的子view数据展示

    private int mFootContentHeight;


    private int mDistanceY; //滑动的距离
    private int mFirstVisibleItem;
    private int mVisibleItemCount;
    private int mTotalItemCount;
    private boolean isBottom = false;
    private int mStartY = 0;
    private int mPaddingBottom = 0;

    private boolean isRecord = false;
    private int mFooterStatus = IPullUpStates.PULL_UP_NOMAL;

    // 是否在加载数据
    private boolean mLoading;

    // 加载数据的监听
    private OnPullUpListener mPullUpListener;
    // 刷新监听
    private OnPullDownListener mPullDownListener;
    //Listview滚动监听器
    private JOnScrollListener mJOnScrollListener;

    // 刷新监听标记
    private boolean mRefreshListenerFlag = false;

    private int mMode = MODE.ONLY_DOWN;


    public JSwipeRefreshListView(Context context) {
        this(context, null);
    }

    public JSwipeRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setHeaderDefaltStyle(); //设置默认的头部加载样式
        mRefreshListenerFlag = true;
        setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isLoading()) {
                    setPullDownComplete();
                    return;
                }
                if (mPullDownListener != null) mPullDownListener.onRefresh();
            }
        });
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

    //计算headView的width以及height
    private void measureView(View child) {
        LayoutParams params = child.getLayoutParams();
        if (params == null)
            params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0, params.width);
        int lpHeight = params.height;
        int childHeightSpec;
        if (lpHeight > 0) {
            childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight, MeasureSpec.EXACTLY);
        } else {
            childHeightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthSpec, childHeightSpec);
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
        if (!isBottom || isLoading() || isRefreshing()) {
            return;
        }
        if (!isRecord) {
            mStartY = (int) event.getRawY();
            isRecord = true;
        }


        mListView.post(new Runnable() {
            @Override
            public void run() {
                mDistanceY = (int) (event.getRawY() - mStartY);
                if (-mDistanceY < mTouchSlop) {
                    return;
                }
                if (mMode == MODE.ONLY_DOWN) {
                    footerStyleOnlyDown();
                    mFootRootView.setPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop);
                    updateFooterView(IPullUpStates.PULL_UP_ONLY_DOWN);
                    return ;
                }
                switch (mFooterStatus) {

                    case IPullUpStates.PULL_UP_NOMAL:
                        mFootRootView.setPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop);
                        updateFooterView(IPullUpStates.PULL_UP);
                        break;

                    case IPullUpStates.PULL_UP:
                        mFootRootView.setPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop);
                        if (-mDistanceY + mPaddingBottom > mFootContentHeight) {
                            updateFooterView(IPullUpStates.PULL_UP_RELEASE);
                        }
                        break;

                    case IPullUpStates.PULL_UP_RELEASE:
                        mFootRootView.setPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop);
                        if (-mDistanceY + mPaddingBottom > mFootContentHeight) {
                            updateFooterView(IPullUpStates.PULL_UP_RELEASE);
                        } else if (-mDistanceY + mPaddingBottom > 0 && -mDistanceY + mPaddingBottom <= mFootContentHeight) {
                            updateFooterView(IPullUpStates.PULL_UP);
                        } else {

                        }
                        break;
                    case IPullUpStates.PULL_UP_OVER:
                        mFootRootView.setPadding(0, 0, 0, mFootContentHeight - mDistanceY - mTouchSlop);
                        break;
                }
            }

        });
    }


    private void doActionUp(MotionEvent event) {
        isRecord = false;
        isBottom = false;
        mDistanceY = 0;
        mStartY = 0;
        switch (mFooterStatus) {
            case IPullUpStates.PULL_UP_NOMAL:
                //updateFooterView(IPullUpStates.PULL_UP_NOMAL);
                break;

            case IPullUpStates.PULL_UP:
                mPaddingBottom = -mFootContentHeight;
                mFootRootView.setPadding(0, 0, 0, mPaddingBottom);
                updateFooterView(IPullUpStates.PULL_UP_NOMAL);
                break;

            case IPullUpStates.PULL_UP_RELEASE:
                mPaddingBottom = 0;
                mFootRootView.setPadding(0, 0, 0, mPaddingBottom);
                updateFooterView(IPullUpStates.PULL_UP_LOADING);
                break;
            case IPullUpStates.PULL_UP_OVER:
                mPaddingBottom = 0;
                mFootRootView.setPadding(0, 0, 0, mPaddingBottom);
                break;
            case IPullUpStates.PULL_UP_ONLY_DOWN:
                mPaddingBottom = 0;
                mFootRootView.setPadding(0, 0, 0, mPaddingBottom);
                break;
        }
    }

    private void updateFooterView(int status) {

        switch (status) {
            case IPullUpStates.PULL_UP_NOMAL:
                mLoading = false;
                break;

            case IPullUpStates.PULL_UP:
                footerStylePull("上拉进行加载");
                break;

            case IPullUpStates.PULL_UP_RELEASE:
                footerStylePull("松开进行加载");
                break;

            case IPullUpStates.PULL_UP_LOADING:
                if (mPullUpListener != null) mPullUpListener.onLoad();
                mLoading = true;
                footerStyleLoading();
                break;

            case IPullUpStates.PULL_UP_OVER:
                mLoading = false;
                footerStyleComplete("没有更多数据");
                break;

            case IPullUpStates.PULL_UP_ONLY_DOWN:

                break;

        }
        mFooterStatus = status;


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
            throw new ErrorMethodException("请使用setOnRefreshDealListener方法");
        }
    }


    /**
     * 设置操作ListView
     *
     * @param listView ListView
     * @param mode     设置是否运行上拉或者下拉
     *                 0允许上下拉 MODE.BOTH
     *                 1只允许下拉  MODE.ONLY_DOWN
     */
    public void setListView(@NonNull ListView listView, int mode) {
        mListView = listView;
        // 初始化加载布局
        View footerView = LayoutInflater.from(mContext).inflate(R.layout.ui_footer_layout, mListView, false);
        mFootRootView = footerView.findViewById(R.id.footer_layout);
        mFooterTipTV = (TextView) footerView.findViewById(R.id.footer_tip);
        mFooterContentLayout = (LinearLayout) footerView.findViewById(R.id.footer_content_layout);
        mFooterbar = (ProgressBar) footerView.findViewById(R.id.footer_bar);
        mFooterHint = (TextView) footerView.findViewById(R.id.footer_hint);
        measureView(mFootRootView);
        mFootContentHeight = mFootRootView.getMeasuredHeight();
        setMode(mode);
        mFootRootView.setVisibility(GONE);
        if (mListView.getFooterViewsCount() == 0) mListView.addFooterView(footerView);
        mListView.setOnScrollListener(new JOnScrollListener());
    }

    /**
     * 设置是否运行上拉或者下拉
     *
     * @param mode 0允许上下拉 MODE.BOTH
     *             1只允许下拉  MODE.ONLY_DOWN
     */
    public void setMode(int mode) {
        mMode = mode;
        if (mode == MODE.BOTH) {
            mFootRootView.setVisibility(VISIBLE);
        } else if (mode == MODE.ONLY_DOWN) {
            mFootRootView.setVisibility(VISIBLE);
            mFooterTipTV.setVisibility(GONE);
            mFooterContentLayout.setVisibility(GONE);
            mFooterbar.setVisibility(GONE);
            mFooterHint.setVisibility(GONE);
            updateFooterView(IPullUpStates.PULL_UP_ONLY_DOWN);

        }
    }

    private void footerStyleTip() {
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(VISIBLE);
        mFooterContentLayout.setVisibility(GONE);
    }

    private void footerStylePull(String hint) {
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(VISIBLE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText(hint);
    }

    private void footerStyleLoading() {
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(VISIBLE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText("正在加载");
    }

    private void footerStyleComplete(String hint) {
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(GONE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText(hint);
    }

    private void footerStyleOnlyDown(){
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(GONE);
        mFooterbar.setVisibility(GONE);
        mFooterHint.setVisibility(GONE);
    }


    private class JOnScrollListener implements AbsListView.OnScrollListener {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (mJOnScrollListener != null)
                mJOnScrollListener.onScrollStateChanged(view, scrollState);
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
     * 是否刷新或者加载数据
     *
     * @return boolean true加载数据 false没有加载数据
     */
    public boolean isLoading() {
        return mLoading;
    }

    /**
     * 自动下拉刷新
     */
    public void autoRefreshing() {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                setRefreshing(true);
                if (mPullDownListener != null) mPullDownListener.onRefresh();
            }
        });
    }

    /**
     * 下拉刷新完成
     */
    public void setPullDownComplete() {
        mListView.post(new Runnable() {
            @Override
            public void run() {
                setRefreshing(false);
                mFooterStatus = IPullUpStates.PULL_UP_NOMAL;
                if (mFirstVisibleItem + mVisibleItemCount <= mTotalItemCount && mMode == MODE.BOTH) {
                    footerStyleTip();
                }
            }
        });

    }


    /**
     * 上拉完成
     */
    public void setPullUpComplete() {
        setPullUpComplete(true);
    }


    public void setPullUpComplete(boolean isHavaMore) {
        mLoading = false; // 修改加载标记

        if (mFooterStatus == IPullUpStates.PULL_UP_ONLY_DOWN){
            mPaddingBottom = -mFootContentHeight;
            mFootRootView.setPadding(0, 0, 0, mPaddingBottom);
            return ;
        }


        if (isHavaMore) {
            mPaddingBottom = -mFootContentHeight;
            mFootRootView.setPadding(0, 0, 0, mPaddingBottom);
            updateFooterView(IPullUpStates.PULL_UP_NOMAL);

        } else {
            mPaddingBottom = 0;
            mFootRootView.setPadding(0, 0, 0, mPaddingBottom);
            updateFooterView(IPullUpStates.PULL_UP_OVER);
        }

    }

}
