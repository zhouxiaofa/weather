package com.example.weather02;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weather02.gson.Weather;
import com.example.weather02.util.Utility;

import org.litepal.LitePal;

public class MainActivity extends AppCompatActivity {
    private Button searchButton;//查找按钮
    private EditText chengShi;//通过城市查询天气
    private Button myConcern;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        chengShi = findViewById(R.id.chengshi_text);
        searchButton = findViewById(R.id.search_button);
        myConcern = findViewById(R.id.concern_text);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchCountyCode = String.valueOf(chengShi.getText());
                if(searchCountyCode.length() != 6){
                    Toast.makeText(MainActivity.this,"城市ID长度为6位!",Toast.LENGTH_LONG).show();
                }else{
                    Intent intent = new Intent(MainActivity.this,WeatherActivity.class);
                    intent.putExtra("adcode",searchCountyCode);
                    startActivity(intent);
                }
            }
        });
        myConcern.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,MyConcernList.class);
                startActivity(intent);
            }
        });
        SharedPreferences pres = getSharedPreferences(String.valueOf(this),MODE_PRIVATE);
        if (pres.getString("weather",null)!= null){
            Intent intent = new Intent(this,WeatherActivity.class);
            startActivity(intent);
            finish();
        }
    }
}