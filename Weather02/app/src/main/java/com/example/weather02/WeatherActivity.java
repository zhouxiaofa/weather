package com.example.weather02;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import com.bumptech.glide.Glide;
import com.example.weather02.gson.Weather;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import com.example.weather02.util.HttpUtil;
import com.example.weather02.util.Utility;

import static com.example.weather02.MyDBhelper.DB_NAME;
import static com.example.weather02.MyDBhelper.TABLE_NAME;

public class WeatherActivity extends AppCompatActivity {

    public DrawerLayout drawerLayout;
    private Button navButton;
    private Button concern;
    private Button concealConcern;
    private Button goBack;
    private Button refresh;
    public SwipeRefreshLayout swipeRefresh;
    private ScrollView weatherLayout;
    private ImageView bingPicImg;
    private TextView provinceText;//省区
    private TextView cityText;//市区
    private TextView weatherText;//天气
    private TextView temperatureText;//温度
    private TextView humidityText;//湿度
    private TextView reportTimeText;//时间

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21){
            View decorView = getWindow().getDecorView();//获取DecorView实例控件
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);//活动布局显示在状态栏上
            getWindow().setStatusBarColor(Color.TRANSPARENT);//状态栏透明
        }
        setContentView(R.layout.activity_weather);
        weatherLayout = findViewById(R.id.weather_layout);
        bingPicImg = findViewById(R.id.bing_pic_img);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        provinceText = findViewById(R.id.province_text);
        cityText = findViewById(R.id.city_text);
        weatherText = findViewById(R.id.weather_text);
        temperatureText = findViewById(R.id.temperature_text);
        humidityText = findViewById(R.id.humidity_text);
        reportTimeText = findViewById(R.id.reporttime_text);
        drawerLayout = findViewById(R.id.drawer_layout);
        navButton = findViewById(R.id.nav_button);
        concern = findViewById(R.id.concern);
        concealConcern = findViewById(R.id.concealConcern);
        goBack = findViewById(R.id.goBack);
        refresh = findViewById(R.id.refresh);
        SharedPreferences prefs = getSharedPreferences(String.valueOf(this),MODE_PRIVATE);
        String adcodeString = prefs.getString("weather",null);
        final String countyCode;
        final String countyName;
        if (adcodeString != null) {
            Weather weather = Utility.handleWeatherResponse(adcodeString);
            countyCode = weather.adcodeName;
            countyName = weather.cityName;
            showWeatherInfo(weather);
        } else {
            countyCode = getIntent().getStringExtra("adcode");
            countyName = getIntent().getStringExtra("city");
            weatherLayout.setVisibility(View.INVISIBLE);
            requestWeather(countyCode);
        }
        final String x = cityText.getText().toString();
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){//下拉进度条监听器
            @Override
            public void onRefresh() {
                requestWeather(countyCode);//回调方法
            }
        });
        navButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        concern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDBhelper dbHelper = new MyDBhelper(WeatherActivity.this, DB_NAME, null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put("city_code", countyCode);
                values.put("city_name", countyName);
                db.insert(TABLE_NAME, null, values);
                Toast.makeText(WeatherActivity.this, "关注成功！", Toast.LENGTH_LONG).show();
            }
        });
        concealConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDBhelper dbHelper = new MyDBhelper(WeatherActivity.this, DB_NAME, null, 1);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                db.delete(TABLE_NAME,"city_code=?",new String[]{String.valueOf(countyCode)});
                Toast.makeText(WeatherActivity.this, "取消关注成功！", Toast.LENGTH_LONG).show();
            }
        });
        goBack.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WeatherActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestWeather(countyCode);
            }
        });
        String bingPic = prefs.getString("bing_pic",null);
        if (bingPic != null){
            Glide.with(this).load(bingPic).into(bingPicImg);
        }else {
            loadBingPic();
        }

    }

    public void requestWeather(final String adCode) {
        String weatherUrl = "https://restapi.amap.com/v3/weather/weatherInfo?city=" + adCode + "&key=c1894e9fcaf35e9fceabe9afaf40d45f";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null /*&& "1".equals(weather.status) && "1".equals(weather.count) && "Ok".equals(weather.info) && "10000".equals(weather.infocode)*/) {
                            SharedPreferences.Editor editor = getSharedPreferences(String.valueOf(this),MODE_PRIVATE).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败,城市ID不存在，请重新输入！", Toast.LENGTH_SHORT).show();
                        }
                        swipeRefresh.setRefreshing(false);
                    }
                });
                loadBingPic();
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

   private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bingPicImg);
                    }
                });
            }
        });
    }

     private void showWeatherInfo(Weather weather) {
        String provinceName = weather.provinceName;
        String cityName = weather.cityName;
        String weatherName = weather.weatherName;
        String temperatureName = weather.temperatureName;
        String humidityName = weather.humidityName;
        String reportTime = weather.reportTimeName;
        provinceText.setText(provinceName);
        cityText.setText(cityName);
        weatherText.setText("天气:" + weatherName);
        temperatureText.setText("温度:" + temperatureName + "℃");
        humidityText.setText("湿度:" + humidityName + "%");
        reportTimeText.setText(reportTime);
        weatherLayout.setVisibility(View.VISIBLE);
    }
}