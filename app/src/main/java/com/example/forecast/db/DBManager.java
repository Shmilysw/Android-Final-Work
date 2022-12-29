package com.example.forecast.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    public static SQLiteDatabase database;
    // 初始化数据库
    public static void initBD(Context context) {
        DBHelper dbHelper = new DBHelper(context);
        // 全局变量DBManager
        database = dbHelper.getWritableDatabase();
    }
    // 查找数据库城市列表
    @SuppressLint("Range") // 抑制错误，当数据库没有值，cursor.getColumnIndex 返回下标为 -1
    public static List<String> queryAllCityName() {
        // 游标
        Cursor cursor = database.query("info", null, null, null, null, null, null);
        List<String> cityList = new ArrayList<>();
        while (cursor.moveToNext()) {
            String city = cursor.getString(cursor.getColumnIndex("city"));
            cityList.add(city);
        }
        return cityList;
    }
    // 根据城市名称，替换信息内容
    public static int updateInfoByCity(String city, String content) {
        ContentValues values = new ContentValues();
        values.put("content", content);
        return database.update("info", values, "city=?", new String[]{city});
    }
    // 新增一条城市记录
    public static long addCityInfo(String city, String content) {
        ContentValues values = new ContentValues();
        values.put("city", city);
        values.put("content", content);
        return database.insert("info", null, values);
    }
    // 根据城市名，查询数据库当中的内容
    @SuppressLint("Range")
    public static String queryInfoCity(String city) {
        Cursor cursor = database.query("info", null, "city=?", new String[]{city}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
             String content = cursor.getString(cursor.getColumnIndex("content"));
            return content;
        }
        return null;
    }
    // 存储城市天气要求最多储存5个城市信息，一旦超过5个城市就不能存储了，获取目前已经存储的数量
    public static int getCityCount() {
        Cursor cursor = database.query("info", null, null, null, null, null, null);
        int count = cursor.getCount();
        return count;
    }
    // 查询数据库当中的全部信息
    @SuppressLint("Range")
    public static List<DatabaseBean> queryAllInfo() {
        Cursor cursor = database.query("info", null, null, null, null, null, null);
        List<DatabaseBean> list = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            String city = cursor.getString(cursor.getColumnIndex("city"));
            String content = cursor.getString(cursor.getColumnIndex("content"));
            DatabaseBean bean = new DatabaseBean(id, city, content);
            list.add(bean);
        }
        return list;
    }
    // 根据城市名称，删除这个城市在数据库当中的数据
    public static int deleteInfoByCity(String city) {
        return database.delete("info","city=?",new String[]{city});
    }
    // 删除表当中所有的数据信息
    public static void deleteAllInfo() {
        String sql = "delete from info";
        database.execSQL(sql);
    }

}
