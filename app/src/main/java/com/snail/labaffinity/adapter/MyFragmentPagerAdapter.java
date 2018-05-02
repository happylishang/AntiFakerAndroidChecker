package com.snail.labaffinity.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Author: lishang
 * Data: 17/1/5 下午2:10
 * Des:
 * version:
 */

public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    public MyFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return MyFragment.newInstance(String.valueOf(position));
    }

    @Override
    public int getCount() {
        return 10;
    }
}
