package com.zhihu.matisse;

import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * @Author zhangshuqi
 * @CreateTime 2018/5/30
 * @Describe
 */

public class BaseFragmentAdapter extends FragmentPagerAdapter {

    private List<String> titleList;
    private List<Fragment> fragmentList;

    public BaseFragmentAdapter(FragmentManager fm, List<Fragment> fragmentList) {
        this(fm, new ArrayList<String>(), fragmentList);
    }

    public BaseFragmentAdapter(FragmentManager fm, List<String> titleList, List<Fragment> fragmentList) {
        super(fm);
        this.titleList = titleList;
        this.fragmentList = fragmentList;
    }

    public BaseFragmentAdapter(FragmentManager fm, String[] titleList, List<Fragment> fragmentList) {
        super(fm);
        this.titleList = Arrays.asList(titleList);
        this.fragmentList = fragmentList;
    }

    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (titleList == null || titleList.size() == 0)
            return super.getPageTitle(position);
        return titleList.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        //如果注释这行，那么不管怎么切换，page都不会被销毁
        super.destroyItem(container, position, object);
    }
}
