package com.janedler.view;

/**
 * Created by dell on 2016/3/31.
 */
public interface IPullUpStates {

    public int PULL_UP_NOMAL = 0;  //初始状态

    public int PULL_UP = 1;  //上拉的状态

    public int PULL_UP_RELEASE = 2;  //上拉到一定位置开始释放的状态

    public int PULL_UP_LOADING = 3;  //开始加载更多的状态

    public int PULL_UP_OVER = 4;  //加载完毕

    public int PULL_UP_ONLY_DOWN = 5;  //不允许上拉刷新

}
