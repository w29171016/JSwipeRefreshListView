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
import com.janedler.controller.IFooterAutoController;
import com.janedler.interfaces.IPullUpStates;
import com.janedler.interfaces.MODE;
import com.janedler.log.JSwipeRefreshLog;
import com.janedler.util.MeasureUtil;

/**
 * 滑到底部自动进行刷新
 *
 * 在XML中进行如下使用
 * <com.janedler.view.JSwipeRefreshAutoListView
 *  android:id="@+id/id_list_refresh_layout"
 *  android:layout_width="match_parent"
 *  android:layout_height="match_parent">
 * </com.janedler.view.JSwipeRefreshAutoListView>
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
public class JSwipeRefreshAutoListView extends JSwipeRefreshBaseListView implements IFooterAutoController {

    private View mFootRootView; //footer view
    private TextView mFooterTipTV;
    private LinearLayout mFooterContentLayout;
    private ProgressBar mFooterbar;
    private TextView mFooterHint;
    private String  mHint = "查看更多";
    private boolean mIsAllowLoad = true;
    private boolean mLoading;// 是否在加载数据

    public JSwipeRefreshAutoListView(Context context) {
        super(context);
    }

    public JSwipeRefreshAutoListView(Context context, AttributeSet attrs) {
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
        mFootRootView = LayoutInflater.from(mContext).inflate(R.layout.ui_footer_auto_layout, null, false);
        mFooterTipTV = (TextView) mFootRootView.findViewById(R.id.footer_tip);
        mFooterContentLayout = (LinearLayout) mFootRootView.findViewById(R.id.footer_content_layout);
        mFooterbar = (ProgressBar) mFootRootView.findViewById(R.id.footer_bar);
        mFooterHint = (TextView) mFootRootView.findViewById(R.id.footer_hint);
        MeasureUtil.measureView(mFootRootView);//对FooterView进行测量
        mFootContentHeight = mFootRootView.getMeasuredHeight(); //得到FooterView测量后高度
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(GONE);
        mFooterbar.setVisibility(GONE);
        mFooterHint.setVisibility(GONE);
        mFootRootView.setOnClickListener(new FooterViewClickListener());
        mFootRootView.setClickable(false);
        layout.addView(mFootRootView);
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

    @Override
    public void footerStyleAutoBottom() {
        JSwipeRefreshLog.e("Auto -- Footer style bottom");
        mFooterStatus = IPullUpStates.SCROLL_BOTTOM;
        mLoading = false;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(VISIBLE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText("正在加载");
    }

    @Override
    public void footerStyleAutoLoading() {
        JSwipeRefreshLog.e("Auto -- Footer style loading");
        mFooterStatus = IPullUpStates.SCROLL_LOADING;
        mLoading = true;
        mFootRootView.setClickable(false);
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(VISIBLE);
        mFooterbar.setVisibility(VISIBLE);
        mFooterHint.setVisibility(VISIBLE);
        mFooterHint.setText("正在加载");
    }

    @Override
    public void footerStyleAutoComplete() {
        JSwipeRefreshLog.e("Auto -- Footer style complete >> "+mHint);
        mLoading = false;
        mFooterStatus =  IPullUpStates.PULL_UP_NOMAL;
        mFootRootView.setVisibility(VISIBLE);
        mFooterTipTV.setVisibility(VISIBLE);
        mFooterContentLayout.setVisibility(GONE);
        mFooterbar.setVisibility(VISIBLE);
        mFooterHint.setVisibility(GONE);
        mFooterTipTV.setText(mHint);
    }

    /**
     * 不允许上拉刷新
     */
    @Override
    public void footerStylePullUpOnlyDown() {
        JSwipeRefreshLog.e("Auto -- Footer style disallow pullup");
        mLoading = false;
        mFooterStatus = IPullUpStates.PULL_UP_ONLY_DOWN;
        mFootRootView.setVisibility(GONE);
        mFooterTipTV.setVisibility(GONE);
        mFooterContentLayout.setVisibility(GONE);
        mFooterbar.setVisibility(GONE);
        mFooterHint.setVisibility(GONE);
    }

    @Override
    public boolean isAllowAutoLoad() {
        return mIsAllowLoad;
    }


    /**
     * 下拉刷新完成
     */
    @Override
    public void pullDownComplete() {
        super.pullDownComplete();
        mIsAllowLoad = true;
    }

    /**
     * 上拉加载更多成功 并且还有上拉数据
     */
    @Override
    public void pullUpSuccess() {
        pullUpSuccess("");
    }

    /**
     * 上拉加载更多成功
     */
    public void pullUpSuccess(String hint) {
        JSwipeRefreshLog.e("pullup is success");
        mLoading = false; // 修改加载标记
        this.mHint = hint;
        /*
        通过设置SetMode来只允许下拉
         */
        if (mFooterStatus == IPullUpStates.PULL_UP_ONLY_DOWN){
            footerStylePullUpOnlyDown();
            return ;
        }

        if (TextUtils.isEmpty(hint)){
            /*
            如果hint为空 则表示若继续上拉有更多的数据
             */
            mIsAllowLoad = true;
            mFootRootView.setClickable(true);
            footerStyleAutoBottom();
        }else{
            /*
            如果hint不为空 则表示若继续上拉也没有更多数据了
             */
            mIsAllowLoad = false;
            mFootRootView.setClickable(false);
            footerStyleAutoComplete();
        }
    }

    /**
     * 上拉失败提示
     */
    @Override
    public void pullUpError(){
        pullUpError("加载失败，点击重试");
    }

    /**
     * 上拉失败提示
     */
    public void pullUpError(String hint) {
        JSwipeRefreshLog.e("pullup is error");
        mLoading = false; // 修改加载标记
        mFootRootView.setClickable(true);
        this.mHint = TextUtils.isEmpty(hint)?"加载失败，点击重试":hint;
        if (mFooterStatus == IPullUpStates.PULL_UP_ONLY_DOWN){
            footerStylePullUpOnlyDown();
            return ;
        }
        mIsAllowLoad = false;
        footerStyleAutoComplete();
    }

    private class FooterViewClickListener implements OnClickListener{

        @Override
        public void onClick(View v) {
            JSwipeRefreshLog.e("FooterViewClickListener is clicked");
            footerStyleAutoLoading();
            mPullUpListener.onLoad();
        }
    }

}













