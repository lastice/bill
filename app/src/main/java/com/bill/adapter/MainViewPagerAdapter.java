package com.bill.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.bill.MainFragment;
import com.bill.util.GlobalUtil;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MainViewPagerAdapter extends FragmentStatePagerAdapter {

    private List<MainFragment> fragments = new ArrayList<MainFragment>();
    private List<String> dates = new ArrayList<String>();

    private String TAG = "MainViewPagerAdapter";

    public MainViewPagerAdapter(FragmentManager fm) {
        super(fm);
        initFragments();
    }

    private void initFragments() {//初始化Fragments

        dates = GlobalUtil.getInstance().databaseHelper.getAvailableDate();
        for (String date : dates) {
            MainFragment fragment = MainFragment.newInstance(date);
            fragments.add(fragment);
        }

    }

    //登陆之后重新fragments
    public void reInitFragments() {
        fragments.clear();
        dates = GlobalUtil.getInstance().databaseHelper.getAvailableDate();
        Log.d(TAG, "reInit" + dates);
        for (String date : dates) {
            MainFragment fragment = MainFragment.newInstance(date);
//            fragment.getDate();
            fragments.add(fragment);
        }
    }

    //添加删除记录时调用
    public void reload() {
        for (MainFragment fragment : fragments) {
            fragment.reload();
        }

    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e(TAG, "instantiateItem: 当前位置position=" + position);
        return super.instantiateItem(container, position);
    }


    public int getLastIndex() {
        return fragments.size() - 1;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public LinkedList<Double> getTotalCost(int index) {
        return fragments.get(index).getTotalCost();
    }
}
