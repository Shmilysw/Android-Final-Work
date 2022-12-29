package com.example.forecast.city_manager;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.forecast.MainActivity;
import com.example.forecast.R;
import com.example.forecast.base.BaseActivity;
import com.example.forecast.bean.WeatherBean;
import com.google.gson.Gson;

public class SearchCityActivity extends BaseActivity implements View.OnClickListener {

    EditText searchEt;
    ImageView submitIv;
    GridView searchGv;

    String[]hotCitys = {"北京","上海","广州","深圳","珠海","佛山","南京","苏州","厦门",
            "长沙","成都","福州","杭州","武汉","青岛","西安","太原","沈阳","重庆","天津","南宁","台州","兰州","天水","陇南"};
    private ArrayAdapter<String> adapter;

    // 腾讯天气 API 的拼接
    String url1 = "https://wis.qq.com/weather/common?source=pc&weather_type=observe|index|rise|alarm|air|tips|forecast_24h&province=";
    String url2 = "&city=";
    private String provice;
    private String city;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_city);
        searchEt = findViewById(R.id.search_et);
        searchGv = findViewById(R.id.search_gv);
        submitIv = findViewById(R.id.search_iv_submit);
        submitIv.setOnClickListener(this);

        // 设置适配器
        adapter = new ArrayAdapter<>(this, R.layout.item_hotcity, hotCitys);
        searchGv.setAdapter(adapter);

        setListener();

        // 修改背景铺满任务栏
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0 全透明实现
            //getWindow.setStatusBarColor(Color.TRANSPARENT)
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
//            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            // 实现状态栏图标和文字颜色为暗色
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //4.4 全透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            // 设置状态栏颜色
            getWindow().setStatusBarColor(getResources().getColor(R.color.white));
            // 实现状态栏图标和文字颜色为暗色
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

    }
    // 设置监听事件
    private void setListener() {
        searchGv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                city = hotCitys[position];
                // 获取省份
                provice = GetProvice(city);
                String url = url1 + provice + url2 + city;
                // 数据的加载
                loadData(url);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_iv_submit:
                city = searchEt.getText().toString();
//                System.out.println(city);
                if (!TextUtils.isEmpty(city)) {
                    // 判断是否能够找到这个城市
                    provice = GetProvice(city);
                    String url = url1 + provice + url2 + city;
                    loadData(url);
                } else {
                    Toast.makeText(this, "输入内容不能为空！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onSuccess(String result) {
        super.onSuccess(result);
        WeatherBean weatherBean = new Gson().fromJson(result, WeatherBean.class);
        if (weatherBean.getData().getIndex().getClothes() != null) {
            Intent intent = new Intent(this, MainActivity.class);
            // FLAG_ACTIVITY_CLEAR_TASK 清空原来的栈，FLAG_ACTIVITY_NEW_TASK开新栈
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            city = provice + " " + city;
            intent.putExtra("city", city);
            startActivity(intent);
        }else{
            Toast.makeText(this, "暂时未收入此城市天气信息...", Toast.LENGTH_SHORT).show();
        }
    }

    private String GetProvice(String city) {
        String[] Citys = {"北京","上海","广东省 广州","广东省 深圳","广东省 珠海","广东省 佛山",
                "江苏省 南京","江苏省 苏州","福建省 厦门","湖南省 长沙","四川省 成都","福建省 福州","浙江省 杭州",
                "湖北省 武汉","山东省 青岛","陕西省 西安","山西省 太原","辽宁省 沈阳","重庆","天津","广西省 南宁",
                "浙江省 台州","福建省 福州","福建省 泉州","甘肃省 陇南","甘肃省 兰州","甘肃省 天水"};
        for(int i = 0; i < Citys.length ; i ++ ) {
            if(Citys[i].contains(city)) {
                if(Citys[i].split(" ").length > 1) {
                    provice = Citys[i].split(" ")[0];
                } else {
                    provice = Citys[i].split(" ")[0];
                }
                System.out.println(provice + " " + city);
                break;
            }
        }
        return provice;
    }

}