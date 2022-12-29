package com.example.forecast;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.forecast.base.BaseFragment;
import com.example.forecast.bean.WeatherBean;
import com.example.forecast.db.DBManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class CityWeatherFragment extends BaseFragment implements View.OnClickListener {
    TextView tempTv, cityTv, conditionTv, windTv, tempRangeTv, dateTv, clothIndexTv, carIndexTv, coldIndexTv, sportIndexTv, raysIndexTv, tipTv, umbrellaTv;
    ImageView dayIv;
    LinearLayout futureLayout;
    ScrollView outLayout;
    String provice;
    String city;
    private WeatherBean.DataBean.IndexBean index;

    private SharedPreferences pref;
    private int bgNum;

    // 换壁纸的函数
    public void exchangeBg() {
        pref = getActivity().getSharedPreferences("bg_pref", MODE_PRIVATE);
        bgNum = pref.getInt("bg", 3);
        switch (bgNum) {
            case 0:
                outLayout.setBackgroundResource(R.mipmap.bg);
                break;
            case 1:
                outLayout.setBackgroundResource(R.mipmap.bg2);
                break;
            case 2:
                outLayout.setBackgroundResource(R.mipmap.bg3);
                break;
            case 3:
                outLayout.setBackgroundResource(R.mipmap.bg_);
                break;
        }
    }

    // 腾讯天气 API 的拼接
    String url1 = "https://wis.qq.com/weather/common?source=pc&weather_type=observe|index|rise|alarm|air|tips|forecast_24h&province=";
    String url2 = "&city=";

    // 图片链接
//    private static final String SUNNY = "https://tse1-mm.cn.bing.net/th/id/OIP-C.m9Hoc3x7HetqmJ6gOXdbzgHaHo?pid=ImgDet&rs=1";
    private static final String SUNNY = "https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/currentweather/day/00.png";
//    private static final String RAIN = "https://tse3-mm.cn.bing.net/th/id/OIP-C.tORoCN0CM-s9K6YG7JG_0gHaHa?pid=ImgDet&rs=1";
    private static final String RAIN = "https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/currentweather/day/09.png";
//    private static final String DUOYUN = "https://img.51miz.com/Element/00/77/45/36/bf3b5b3d_E774536_bc69b86b.png";
//    private static final String FOG = "https://img.tukuppt.com/png_preview/00/20/21/JswzD787Df.jpg!/fw/780";
    private static final String DUOYUN = "https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/currentweather/day/01.png";
//    private static final String FOG = "https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/currentweather/day/01.png";
    private static final String FOG = "https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/currentweather/day/32.png";
//    private static final String SNOW = "https://tse1-mm.cn.bing.net/th/id/OIP-C.H7Noy_D5rfxWyJsfis5cGgHaHa?w=177&h=180&c=7&r=0&o=5&dpr=1.3&pid=1.7";
    private static final String SNOW = "https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/currentweather/day/16.png";

    private static final String HAZE = "https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/currentweather/day/53.png";

    // （晚上），有点麻烦，根据时间来，有空再改。
    private static final String NIGHT = "https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/currentweather/night/00.png";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_city_weather, container, false);
        initView(view);
        // 换壁纸，换的是那个fragment_city_weather的背景，底部有点短~~~
//        exchangeBg();
        // 可以通过activity传值获取到当前fragment加载的是那个地方的天气情况
        Bundle bundle = getArguments();
        String provice_city = bundle.getString("city");

        // 获取省份
        if(provice_city.split(" ").length > 1) {
            provice = provice_city.split(" ")[0];
            city = provice_city.split(" ")[1];
        } else {
            city = provice_city.split(" ")[0];
            provice = provice_city.split(" ")[0];
        }

//        String url = url1 + "浙江省" + url2 + city;
        String url = url1 + provice + url2 + city;
//        System.out.println(url);
        // 调用父类获取数据的方法
        loadData(url);
        return view;
    }


    @Override
    public void onSuccess(String result) {
//        super.onSuccess(result);
        // 解析并展示数据
        // System.out.println("--------------------------------------");
        parseShowData(result);
        // 更新数据
        int i = DBManager.updateInfoByCity(city, result);
        if (i <= 0) {
            // 更新数据库失败，说明没有这条城市信息，增加这个城市记录
            DBManager.addCityInfo(city, result);
        }

//        try {
//            parseShowData(result);
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onError(Throwable ex, boolean isOnCallback) {
//        super.onError(ex, isOnCallback);
        // 从数据库里面查找上一次信息显示在Fragment当中
        String s = DBManager.queryInfoCity(city);
        if (!TextUtils.isEmpty(s)) {
            parseShowData(s);
        }
    }
    
    // 腾讯天气传的json数据，你别说，还挺多的哈哈~
    /*
    JSON
    data
    air
    alarm
    forecast_24h
    index
    observe
    rise
    tips
    message : "OK"
    status : 200
    */
    /*
    forecast_24h
            0
            day_weather : "多云"
            day_weather_code : "01"
            day_weather_short : "多云"
            day_wind_direction : "东北风"
            day_wind_direction_code : "1"
            day_wind_power : "4"
            day_wind_power_code : "1"
            max_degree : "14"
            min_degree : "10"
            night_weather : "小雨"
            night_weather_code : "07"
            night_weather_short : "小雨"
            night_wind_direction : "北风"
            night_wind_direction_code : "8"
            night_wind_power : "4"
            night_wind_power_code : "1"
            time : "2022-12-07"
            1
            2
            3
            4
            5
            6
            7

    index
            airconditioner
            allergy
            carwash
            chill
            clothes
            cold
            comfort
            diffusion
            dry
            drying
            fish
            heatstroke
            makeup
            mood
            morning
            sports
            sunglasses
            sunscreen
            time : "20221208"
            tourism
            traffic
            ultraviolet
            umbrella

    observe
            degree : "14"
            humidity : "75"
            precipitation : "0"
            pressure : "1020"
            update_time : "202212082155"
            weather : "多云"
            weather_code : "01"
            weather_short : "多云"
            wind_direction : "7"
            wind_power : "2"

     */

    private void parseShowData(String result) {
        // 使用gson解析数据
        WeatherBean weatherBean = new Gson().fromJson(result, WeatherBean.class);
        WeatherBean.DataBean resultsBean = weatherBean.getData();

        index = resultsBean.getIndex();
        // 设置TextView
        cityTv.setText(city);
        tipTv.setText(resultsBean.getTips().getObserve().get_$0() + "\n" + resultsBean.getTips().getObserve().get_$1());
//        System.out.println(resultsBean.getTips().getObserve().get_$0());
//        System.out.println(resultsBean.getTips().getObserve().get_$1());
//        tipTv.setText(resultsBean.getTips().getObserve().get_$1());
//        System.out.println(resultsBean.getTips().getObserve().get_$0());
        // 获取今日天气情况
        WeatherBean.DataBean.ObserveBean todayDataBean = resultsBean.getObserve();
        /*todayDataBean的内容
        degree : "14"
        humidity : "74"
        precipitation : "0"
        pressure : "1020"
        update_time : "202212081950"
        weather : "多云"
        weather_code : "01"
        weather_short : "多云"
        wind_direction : "7"
        wind_power : "2"*/
        WeatherBean.DataBean.Forecast24hBean._$0Bean bean_0 = weatherBean.getData().getForecast_24h().get_$0();
        /*bean_0 获取当天的天气内容如下
        day_weather : "多云"
        day_weather_code : "01"
        day_weather_short : "多云"
        day_wind_direction : "东北风"
        day_wind_direction_code : "1"
        day_wind_power : "4"
        day_wind_power_code : "1"
        max_degree : "14"
        min_degree : "10"
        night_weather : "小雨"
        night_weather_code : "07"
        night_weather_short : "小雨"
        night_wind_direction : "北风"
        night_wind_direction_code : "8"
        night_wind_power : "4"
        night_wind_power_code : "1"
        time : "2022-12-07"*/
        String time = null;
        try {
            time = changeTime(todayDataBean.getUpdate_time());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println(time);
        // 发布时间
        dateTv.setText("发布时间 " + time);
        //
        windTv.setText("风向 " + bean_0.getDay_wind_direction());
        tempRangeTv.setText("温度 "+ bean_0.getMin_degree() + "~" + bean_0.getMax_degree() + "°C");
        conditionTv.setText(todayDataBean.getWeather());
        // 获取天气情况来确定图片
        String weather_condition = todayDataBean.getWeather();
        System.out.println(weather_condition);
        // 获取实时天气温度情况，需要处理字符串
        tempTv.setText(todayDataBean.getDegree() + "°C");
        // 设置显示的天气情况图片


//        getImage(SUNNY);
//        getImage(FOG);

        for (int i = 0; i < weather_condition.length() ; i ++ ) {
            if (weather_condition.charAt(i) == '晴') {
                dayIv.setImageResource(R.drawable.sunny);
//                getImage(SUNNY);
                break;
            }
            else if (weather_condition.charAt(i) == '雨') {
                dayIv.setImageResource(R.drawable.rain);
//                getImage(RAIN);
                break;
            }
            else if (i < weather_condition.length() - 1 && weather_condition.charAt(i) == '多' && weather_condition.charAt(i + 1) == '云') {
                dayIv.setImageResource(R.drawable.duoyun);
//                getImage(DUOYUN);
                break;
            }
            else if (weather_condition.charAt(i) == '雪') {
                dayIv.setImageResource(R.drawable.snow);
//                getImage(SNOW);
                break;
            }
            else if (weather_condition.charAt(i) == '雾') {
                dayIv.setImageResource(R.drawable.fog);
//                getImage(FOG);
                break;
            }
            else if (weather_condition.charAt(i) == '霾') {
                dayIv.setImageResource(R.drawable.haze);
//                getImage(HAZE);
                break;
            }
        }
        // 这什么勾八方法，不会用~
        // Picasso.with(getActivity()).load("https://mat1.gtimg.com/pingjs/ext2020/weather/pc/icon/weather/day/01.png").into(dayIv);
        // 获取未来三天的天气情况，加载到layout当中
        WeatherBean.DataBean.Forecast24hBean futureList = resultsBean.getForecast_24h();
        View itemView = LayoutInflater.from(getActivity()).inflate(R.layout.item_main_center, null);
        itemView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        futureLayout.addView(itemView);
        TextView idateTv = itemView.findViewById(R.id.item_center_tv_date);
        TextView iconTv = itemView.findViewById(R.id.item_center_tv_con);
        TextView itemprangeTv = itemView.findViewById(R.id.item_center_tv_temp);
        TextView wind = itemView.findViewById(R.id.item_center_tv_winddirection);
        // 获取对应的位置的天气情况
        idateTv.setText(futureList.get_$2().getTime()+"   明天");
        iconTv.setText(futureList.get_$2().getDay_weather());
        wind.setText(futureList.get_$2().getDay_wind_direction());
        itemprangeTv.setText(futureList.get_$2().getMin_degree()+"~"+futureList.get_$2().getMax_degree()+"°C");

        View itemView3 = LayoutInflater.from(getActivity()).inflate(R.layout.item_main_center, null);
        itemView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        futureLayout.addView(itemView3);
        TextView idateTv3 = itemView3.findViewById(R.id.item_center_tv_date);
        TextView iconTv3 = itemView3.findViewById(R.id.item_center_tv_con);
        TextView itemprangeTv3 = itemView3.findViewById(R.id.item_center_tv_temp);
        TextView wind3 = itemView3.findViewById(R.id.item_center_tv_winddirection);
        // 获取对应的位置的天气情况
        idateTv3.setText(futureList.get_$3().getTime()+"   后天");
        iconTv3.setText(futureList.get_$3().getDay_weather());
        wind3.setText(futureList.get_$3().getDay_wind_direction());
        itemprangeTv3.setText(futureList.get_$3().getMin_degree()+"~"+futureList.get_$3().getMax_degree()+"°C");

        View itemView2 = LayoutInflater.from(getActivity()).inflate(R.layout.item_main_center, null);
        itemView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        futureLayout.addView(itemView2);
        TextView idateTv2 = itemView2.findViewById(R.id.item_center_tv_date);
        TextView iconTv2 = itemView2.findViewById(R.id.item_center_tv_con);
        TextView itemprangeTv2 = itemView2.findViewById(R.id.item_center_tv_temp);
        TextView wind2 = itemView2.findViewById(R.id.item_center_tv_winddirection);
        // 获取对应的位置的天气情况
        idateTv2.setText(futureList.get_$4().getTime()+" 大后天");
        iconTv2.setText(futureList.get_$4().getDay_weather());
        wind2.setText(futureList.get_$4().getDay_wind_direction());
        itemprangeTv2.setText(futureList.get_$4().getMin_degree()+"~"+futureList.get_$4().getMax_degree()+"°C");

    }

    private void getImage(String weatherUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(weatherUrl).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                byte[] bytes = response.body().bytes();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                runOnUiThread(new Runnable() { // 主线程里进行
                    @Override
                    public void run() {
                        dayIv.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    private Thread mUiThread;
    final Handler mHandler = new Handler();

    public final void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != mUiThread) {
            mHandler.post(action);
        } else {
            action.run();
        }
    }

    //    时间格式化
    private String changeTime(String update_time) throws ParseException {
        SimpleDateFormat sf1 = new SimpleDateFormat("yyyyMMddHHmm");
        SimpleDateFormat sf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String sfstr = "";
        sfstr = sf2.format(sf1.parse(update_time));
        return sfstr;
    }

    private void initView(View view) {
        // 用于初始化控件操作
        tipTv  = view.findViewById(R.id.frag_tv_tips);
        tempTv = view.findViewById(R.id.frag_tv_currenttemp);
        cityTv = view.findViewById(R.id.frag_tv_city);
        conditionTv = view.findViewById(R.id.frag_tv_condition);
        windTv = view.findViewById(R.id.frag_tv_wind);
        tempRangeTv = view.findViewById(R.id.frag_tv_temprange);
        dateTv = view.findViewById(R.id.frag_tv_date);
        clothIndexTv = view.findViewById(R.id.frag_index_tv_dress);
        carIndexTv = view.findViewById(R.id.frag_index_tv_washcar);
        coldIndexTv = view.findViewById(R.id.frag_index_tv_cold);
        sportIndexTv = view.findViewById(R.id.frag_index_tv_sport);
        raysIndexTv = view.findViewById(R.id.frag_index_tv_rays);
        dayIv = view.findViewById(R.id.frag_iv_today);
        futureLayout = view.findViewById(R.id.frag_center_layout);
        outLayout = view.findViewById(R.id.out_layout); // *
        umbrellaTv = view.findViewById(R.id.frag_index_tv_umbrella);
        // 设置监听
        clothIndexTv.setOnClickListener(this);
        carIndexTv.setOnClickListener(this);
        coldIndexTv.setOnClickListener(this);
        sportIndexTv.setOnClickListener(this);
        raysIndexTv.setOnClickListener(this);
        umbrellaTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        switch (v.getId()) {
            case R.id.frag_index_tv_dress:
                builder.setTitle("穿衣指数");
                WeatherBean.DataBean.IndexBean.ClothesBean clothes = index.getClothes();
                String msg = clothes.getInfo() + '\n' + clothes.getDetail();
//                System.out.println(msg);
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_index_tv_washcar:
                builder.setTitle("洗车指数");
                WeatherBean.DataBean.IndexBean.CarwashBean carwash = index.getCarwash();
                msg = carwash.getInfo() + '\n' + carwash.getDetail();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_index_tv_cold:
                builder.setTitle("感冒指数");
                WeatherBean.DataBean.IndexBean.ColdBean cold = index.getCold();
                msg = cold.getInfo() + '\n' + cold.getDetail();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_index_tv_sport:
                builder.setTitle("运动指数");
                WeatherBean.DataBean.IndexBean.SportsBean sports = index.getSports();
                msg = sports.getInfo() + '\n' + sports.getDetail();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_index_tv_rays:
                builder.setTitle("紫外线指数");
                WeatherBean.DataBean.IndexBean.UltravioletBean ultraviolet = index.getUltraviolet();
                msg = ultraviolet.getInfo() + '\n' + ultraviolet.getDetail();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
            case R.id.frag_index_tv_umbrella:
                builder.setTitle("雨伞指数");
                WeatherBean.DataBean.IndexBean.UmbrellaBean umbrella = index.getUmbrella();
                msg = umbrella.getInfo() + '\n' + umbrella.getDetail();
                builder.setMessage(msg);
                builder.setPositiveButton("确定", null);
                break;
        }
        builder.create().show();
    }
}