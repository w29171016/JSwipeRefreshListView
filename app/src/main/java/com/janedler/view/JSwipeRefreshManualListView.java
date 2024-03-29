package com.janedler.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.janedler.R;
import com.janedler.controller.IFooterController;
import com.janedler.controller.IFooterManualController;
import com.janedler.interfaces.IPullUpStates;
import com.janedler.interfaces.MODE;
import com.janedler.log.JSwipeRefreshLog;
import com.janedler.util.MeasureUtil;

/**
 * 滑到底部进行上拉后进行刷新
 *
 * 在XML中进行如下使用
 * <com.janedler.view.JSwipeRefreshManualListView
 *  android:id="@+id/id_list_refresh_layout"
 *  android:layout_width="match_parent"
 *  android:layout_height="match_parent">
 * </com.janedler.view.JSwipeRefreshManualListView>
 *
 * 在Java代码中：
 * JSwipeRefreshManualListView mSwipeRefreshLayout = (JSwipeRefreshManualListView) findViewById(R.id.id_list_refresh_layout);
 * 通过getListView()可以获得默认的ListView
 * ListView mListView = mSwipeRefreshLayout.getListView();
 * -------支持动态配置上拉或者下拉--------
 * mSwipeRefreshLayout.setMode(MODE.BOTH); //支持上拉下拉刷新
 * mSwipeRefreshLayout.setMode(MODE.ONLY_DOWN); //只支持上拉刷新
 * 提供了3种监听器
 * -------下拉监听器-------
 * mSwipeRefreshLayout.setOnPullDownListener(new OnPullDownListener() {
 *   @Override
 *   public void onRefresh() {
 *      //在这里你可以动态配置HeaderView与FooterView
 *      mSwipeRefreshLayout.setPullDownComplete(); //通知下拉刷新完成
 *  }
 * });
 * -------上拉监听器-------
 * mSwipeRefreshLayout.setOnPullUpListener(new OnPullUpListener() {
 *   @Override
 *   public void onLoad() {
 *     mSwipeRefreshLayout.setPullUpComplete(true); //通知上拉刷新完成 并且还有更多的数据
 *     mSwipeRefreshLayout.setPullUpComplete(false);//通知上拉刷新完成 并且没有数据了
 *   }
 * });
 * ------自动下拉--------
 *  mSwipeRefreshLayout.autoRefreshing();
 *
 * Created by janedler on 16/4/2.
 */
public class JSwipeRefreshManualListView extends JSwipeRefreshBaseListView implements IFooterManualController {

    private View mFootRootView; //footer view
    private TextView mFooterTipTV;
    private LinearLayout mFooterContentLayout;
    private ProgressBar mFooterbar;
    private TextView mFooterHint;
    private boolean mLoading;// 是否在加载数据
    private String mHint;

    public JSwipeRefreshManualListView(Context context) {
        super(context);
    }

    public JSwipeRefreshManualListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected IFooterController getController() {
        return this;
    }

    /**
     * 设置FooterView
     */
    @Override
    public View setFooterView() {
        LinearLayout layout = new LinearLayout(mContext);
        layout.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);
        mFootRootView = LayoutInflater.from(mContext).inflate(R.layout.ui_footer_manual_layout, null, false);
        mFooterTipTV = (TextView) mFootRootView.findViewById(R.id.footer_tip);
        mFooterContentLayout = (LinearLayout) mFootRootView.findViewById(R.id.footer_content_layout);
        mFooterbar = (ProgressBar) mFootRootView.findViewById(R.id.footer_bar);
        mFooterHint = (TextView) mFootRootView.findViewById(R.id.footer_hint);
        MeasureUtil.measureView(mFootRootView);//对FooterView进行测量
        mFootContentHeight = mFootRootView.getMeasuredHeight(); //得到FooterView测量后高度
        layout.addView(mFootRootView);
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(GONE);
        mFooterbar.setVisibility(GONE);
        mFooterHint.setVisibility(GONE);
        if (mListView.getFooterViewsCount() == 0) mListView.addFooterView(layout);
        return mFootRootView;
    }


    /**
     * 设置是否运行上拉或者下拉
     *
     * @param mode 0允许上下拉 MODE.BOTH
     *             1只允许下拉  MODE.ONLY_DOWN
     */
    @Override
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
            footerStylePullUpOnlyDown();
        }
    }

    /**
     * 用于判断是否正在加载更多的数据
     * @return
     */
    @Override
    public boolean isPullUpLoading() {
        return mLoading;
    }

    /**
     * 初始状态
     */
    @Override
    public void footerStyleManualNomal() {
        JSwipeRefreshLog.e("Manual -- Footer style nomal");
        mLoading = false;
        mFooterStatus = IPullUpStates.PULL_UP_NOMAL;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(VISIBLE);
        mFooterContentLayout.setVisibility(GONE);
    }

    /**
     * 上拉的状态
     */
    @Override
    public void footerStyleManualPullUp() {
        JSwipeRefreshLog.e("Manual -- Footer style pull up");
        mFooterStatus = IPullUpStates.PULL_UP;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(VISIBLE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText("上拉进行加载");
    }


    /**
     * 上拉到一定位置开始释放的状态
     */
    @Override
    public void footerStyleManualRelease() {
        JSwipeRefreshLog.e("Manual -- Footer style release");
        mFooterStatus = IPullUpStates.PULL_UP_RELEASE;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(VISIBLE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText("松开进行加载");
    }

    /**
     * 开始加载更多的状态
     */
    @Override
    public void footerStyleManualLoading() {
        JSwipeRefreshLog.e("Manual -- Footer style loading");
        mFooterStatus = IPullUpStates.PULL_UP_LOADING;
        if (mPullUpListener != null) mPullUpListener.onLoad();
        mLoading = true;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(VISIBLE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText("正在加载");
    }


    /**
     * 加载完毕
     */
    @Override
    public void footerStyleManualOver() {
        JSwipeRefreshLog.e("Manual -- Footer style over");
        mLoading = false;
        mFooterStatus = IPullUpStates.PULL_UP_OVER;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(GONE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText(mHint);
    }



    /**
     * 不允许上拉刷新
     */
    @Override
    public void footerStylePullUpOnlyDown() {
        mLoading = false;
        mFooterStatus = IPullUpStates.PULL_UP_ONLY_DOWN;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(GONE);
        mFooterbar.setVisibility(GONE);
        mFooterHint.setVisibility(GONE);
    }


    /**
     * 上拉刷新完成了
     */
    @Override
    public void pullUpSuccess() {
        pullUpSuccess("");
    }



    public void pullUpSuccess(String hint) {
        mLoading = false; // 修改加载标记
        this.mHint = hint;

        if (mFooterStatus == IPullUpStates.PULL_UP_ONLY_DOWN){
            footerStylePullUpOnlyDown();
            return ;
        }


        if (TextUtils.isEmpty(hint)){
            setFooterPadding(0, 0, 0, -mFootContentHeight,true);
            footerStylePullUpHint();
        } else {
            setFooterPadding(0, 0, 0, 0,true);
            footerStyleManualOver();
        }
    }

    /**
     * 上拉刷新失败了
     */
    public void pullUpError(){
        pullUpSuccess("");
    }

    /**
     * 隐藏状态
     */
    private void footerStylePullUpHint(){
        mLoading = false;
        mFooterStatus = IPullUpStates.PULL_UP_NOMAL;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(VISIBLE);
        mFooterContentLayout.setVisibility(GONE);
    }




}













