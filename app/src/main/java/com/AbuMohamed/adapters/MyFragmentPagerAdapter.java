package com.AbuMohamed.adapters;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.AbuMohamed.introSlider.FirstSlider;
import com.AbuMohamed.introSlider.SecondSlider;
import com.AbuMohamed.introSlider.ThirdSlider;


public class MyFragmentPagerAdapter extends FragmentPagerAdapter {

    private Context mContext;
    int tabCount;

    public MyFragmentPagerAdapter(FragmentManager fm, Context mContext, int tabCount) {
        super(fm);
        this.mContext = mContext;
        this.tabCount = tabCount;
    }

    @Override
    public Fragment getItem(int i) {
        Fragment mFragment = null;
        switch (i){
            case 0:
                mFragment = new FirstSlider();
                break;
            case 1:
                mFragment = new SecondSlider();
                break;
            default:
                return new ThirdSlider();
        }
        return mFragment;
    }

    @Override
    public int getCount() {
        return tabCount;
    }

//    @Override
//    public CharSequence getPageTitle(int position) {
//
//        switch (position) {
//            case 0:
//                return "INTERNATIONAL";
//            case 1:
//                return "LEAGUE";
//            default:
//                return null;
//        }
//    }
}
