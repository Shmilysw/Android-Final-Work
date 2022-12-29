package com.example.forecast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.forecast.city_manager.CityManagerActivity;
import com.example.forecast.db.DBManager;
import com.example.forecast.myrobot.MyRobotActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageView addCityIv, moreIv;
    LinearLayout pointLayout;
    ViewPager mainVp;

    // ViewPager的数据
    List<Fragment> fragmentList;
    // 表示选中城市的集合
    List<String> cityList;
    // 表示ViewPager的页面指数器显示集合
    List<ImageView> imageList;
    private CityFragmentPageAdapter adapter;
    private SharedPreferences pref;
    private int bgNum;
    private View outLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addCityIv = findViewById(R.id.main_iv_add);
        moreIv = findViewById(R.id.main_iv_more);
        pointLayout = findViewById(R.id.main_layout_point);
        mainVp = findViewById(R.id.main_vp);
        outLayout = findViewById(R.id.main_out_layout);
        exchangeBg(); // 更换壁纸函数
        // 添加点击事件
        addCityIv.setOnClickListener(this);
        moreIv.setOnClickListener(this);

        fragmentList = new ArrayList<>();
        cityList = DBManager.queryAllCityName(); // 获取数据库包含的城市信息列表
        imageList = new ArrayList<>();

        if (cityList.size() == 0) {
            cityList.add("台州");
//            cityList.add("杭州");
//            cityList.add("宁波");
        }
        // 因为可能搜索界面点击跳转此界面，会传值，所以此处获取一下
        try {
            Intent intent = getIntent();
            String city = intent.getStringExtra("city");
            if (!cityList.contains(city) && !TextUtils.isEmpty(city)) {
                cityList.add(city);
            }
        } catch (Exception e) {
            Log.i("animee","程序出现问题了！！");
        }

        /* 因为可能搜索界面点击跳转此界面，会传值，所以此处获取一下*/
//        try {
//            Intent intent = getIntent();
//            String city = intent.getStringExtra("city");
//            if (!cityList.contains(city)&&!TextUtils.isEmpty(city)) {
//                cityList.add(city);
//            }
//        }catch (Exception e){
//            Log.i("animee","程序出现问题了！！");
//        }

        // 初始化ViewPager页面的方法
        initPager();

        adapter = new CityFragmentPageAdapter(getSupportFragmentManager(), fragmentList);
        mainVp.setAdapter(adapter);
        // 创建小圆点指示器
        initPoint();
        // 设置最后一个城市信息
        mainVp.setCurrentItem(fragmentList.size() - 1);
        // 设置ViewPager页面监听
        setPagerListener();


        // 修改背景铺满任务栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0 全透明实现
            //getWindow.setStatusBarColor(Color.TRANSPARENT)
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4 全透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

    }

    // 换壁纸的函数
    public void exchangeBg() {
        pref = getSharedPreferences("bg_pref", MODE_PRIVATE);
        bgNum = pref.getInt("bg", 4);
        switch (bgNum) {
            case 0:
                outLayout.setBackgroundResource(R.mipmap.bg);
                break;
            case 1:
                outLayout.setBackgroundResource(R.mipmap.bg____);
                break;
            case 2:
                outLayout.setBackgroundResource(R.mipmap.bg___);
                break;
            case 3:
                outLayout.setBackgroundResource(R.mipmap.bg_);
                break;
            case 4:
                outLayout.setBackgroundResource(R.mipmap.bg__);
                break;
        }
    }


    private void setPagerListener() {
        // 设置监听事件
        mainVp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            // 表示页面所选择的位置
            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < imageList.size() ; i ++ ) {
                    imageList.get(i).setImageResource(R.mipmap.a1);
                }
                imageList.get(position).setImageResource(R.mipmap.a2);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initPoint() {
        // 创建小圆点 ViewPager页面指示器的函数
        for (int i = 0; i < fragmentList.size() ; i ++ ) {
            ImageView pIv = new ImageView(this);
            pIv.setImageResource(R.mipmap.a1);
            pIv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) pIv.getLayoutParams();
            lp.setMargins(0, 0, 20, 0);
            imageList.add(pIv);
            pointLayout.addView(pIv);
        }
        imageList.get(imageList.size() - 1).setImageResource(R.mipmap.a2);

    }

    private void initPager() {
        // 创建Fragment对象，添加到ViewPager数据源当中
        for (int i = 0; i < cityList.size(); i ++ ) {
            CityWeatherFragment cwFrag = new CityWeatherFragment();
            Bundle bundle = new Bundle();
            bundle.putString("city",cityList.get(i));
            cwFrag.setArguments(bundle);
            fragmentList.add(cwFrag);
        }
    }

    // 点击按钮页面跳转
    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.main_iv_add:
                intent.setClass(this, CityManagerActivity.class);
                break;
            case R.id.main_iv_more:
                intent.setClass(this, MoreActivity.class);
                break;
        }
        startActivity(intent);
    }

    // 当页面重新加载时会调用的函数，这个函数在页面获取焦点之前进行调用，此处完成ViewPager页面的更新

    @Override
    protected void onRestart() {
        super.onRestart();
        // 获取数据库当中还剩下的城市集合
        List<String> list = DBManager.queryAllCityName();
        if (list.size() == 0) {
            list.add("台州");
        }
        cityList.clear(); // 重新加载之前，清空原本数据源
        cityList.addAll(list);
        // 剩余城市也要创建对应的fragmengt页面
        fragmentList.clear();
        initPager();
        adapter.notifyDataSetChanged();
        // 页面数量发生改变，指示器的数量也会发生变化，重新设置添加指示器
        imageList.clear();
        pointLayout.removeAllViews(); // 将布局当中所以元素移除
        initPoint(); // 创建新的小点点指示器
        mainVp.setCurrentItem(fragmentList.size() - 1);
    }

    private long exitTime; // 记录第一次点击时的时间

    // 设置
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出天气预报APP",
                        Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                MainActivity.this.finish();
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}