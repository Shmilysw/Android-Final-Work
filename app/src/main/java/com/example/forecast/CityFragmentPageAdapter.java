package com.example.forecast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import java.util.List;

public class CityFragmentPageAdapter extends FragmentStatePagerAdapter {

    List<Fragment> fragmentList;

    public CityFragmentPageAdapter(@NonNull FragmentManager fm, List<Fragment> fragmentList) {
        super(fm);
        this.fragmentList = fragmentList;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    @Override
    public int getCount() {
        return fragmentList.size();
    }

    int childCount = 0; // 表示ViewPager包含的页数
    // 当ViewPager的页数发生变化时，必须要重写两个函数

    @Override
    public void notifyDataSetChanged() {
        this.childCount = getCount();
        super.notifyDataSetChanged(); // 调用父类的方法
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        if (childCount > 0) {
            childCount -- ;
            return POSITION_NONE;
        }
        return super.getItemPosition(object);
    }
}
