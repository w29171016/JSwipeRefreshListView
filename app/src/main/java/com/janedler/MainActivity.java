package com.janedler;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.janedler.adapter.MyAdapter;
import com.janedler.listener.OnPullDownListener;
import com.janedler.listener.OnPullUpListener;
import com.janedler.view.JSwipeRefreshListView;
import com.janedler.view.MODE;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private JSwipeRefreshListView mSwipeRefreshLayout;

    private ListView mListView;

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSwipeRefreshLayout = (JSwipeRefreshListView) findViewById(R.id.id_list_refresh_layout);

        mListView = (ListView) findViewById(R.id.id_list_view);

        mSwipeRefreshLayout.setListView(mListView, MODE.BOTH);

        mAdapter = new MyAdapter(this);

        mListView.setAdapter(mAdapter);

        mSwipeRefreshLayout.setOnPullDownListener(new OnPullDownListener() {
            @Override
            public void onRefresh() {

                mListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSwipeRefreshLayout.setMode(MODE.BOTH);

                        ArrayList<String> list = getRefeshData();
                        mAdapter.setList(list);
                        mSwipeRefreshLayout.setPullDownComplete();
                    }
                }, 3000);


            }
        });

        mSwipeRefreshLayout.setOnPullUpListener(new OnPullUpListener() {
            @Override
            public void onLoad() {
                mListView.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        int ran = new Random().nextInt(2);
                        Log.e("TAG",ran+"");
                        if (ran == 0){

                            ArrayList<String> list = getLoadData();
                            mAdapter.addList(list);
                            mSwipeRefreshLayout.setPullUpComplete(true);
                        }else{
                            mSwipeRefreshLayout.setMode(MODE.ONLY_DOWN);
                            mSwipeRefreshLayout.setPullUpComplete(false);
                        }


                    }
                }, 3000);
            }
        });

        mSwipeRefreshLayout.autoRefreshing();


    }


    private ArrayList<String> getRefeshData() {
        ArrayList<String> list = new ArrayList<>();
        mAdapter.cleanAdapter();
        for (int i = 0; i < 15; i++) {
            list.add(UUID.randomUUID()+"");
        }
        return list;
    }


    private ArrayList<String> getLoadData() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            list.add(UUID.randomUUID()+"");
        }
        return list;
    }

}
