package com.example.weather02;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.weather02.db.City;
import com.example.weather02.db.County;
import com.example.weather02.db.Province;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import com.example.weather02.util.HttpUtil;
import com.example.weather02.util.Utility;

public class ChooseAreaFragment extends Fragment {
    private static final int LEVEL_PROVINCE = 0;
    private static final int LEVEL_CITY = 1;
    private static final int LEVEL_COUNTY = 2;
    private List<String> dataList = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private TextView titleText;
    private Button backButton;
    private ListView listView;

    /**
     * 省列表
     */
    private List<Province> provinceList;
    /**
     * 城市列表
     */
    private List<City> cityList;
    /**
     * 城镇列表
     */
    private List<County> countyList;
    /**
     * 当前等级
     */
    private int currentLevel;

    /**
     * 选中的省份
     */
    private Province selectedProvince;

    /**
     * 选中的城市
     */
    private City selectedCity;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area,container,false);
        titleText = view.findViewById(R.id.title_text);
        backButton = view.findViewById(R.id.back_button);
        listView = view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,dataList);
        listView.setAdapter(adapter);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    selectedProvince = provinceList.get(position);
                    queryCities();
                } else if (currentLevel == LEVEL_CITY) {
                    selectedCity = cityList.get(position);
                    queryCounties();
                } else if (currentLevel == LEVEL_COUNTY) {
                    String countyCode = countyList.get(position).getCountyCode();
                    String countyName = countyList.get(position).getCountyName();
                    if (getActivity() instanceof MainActivity) {
                        Intent intent = new Intent(getActivity(), WeatherActivity.class);
                        intent.putExtra( "adcode",countyCode);
                        intent.putExtra("city",countyName);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (getActivity() instanceof WeatherActivity) {//在WeatherActivity活动中
                        WeatherActivity activity = (WeatherActivity) getActivity();
                        activity.drawerLayout.closeDrawers();//关闭滑动菜单
                        activity.swipeRefresh.setRefreshing(true);//下拉刷新进度条
                        activity.requestWeather(countyCode);
                    }
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLevel == LEVEL_COUNTY){
                    queryCities();
                }else if (currentLevel == LEVEL_CITY){
                    queryProvinces();
                }
            }
        });

        queryProvinces();
    }

    /**
     * 查询所有的省，优先从数据库查询，如果没有查到再去服务器上查询
     */
    private void queryProvinces(){
        titleText.setText("中国");
        backButton.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size()>0){
            dataList.clear();
            for (Province province : provinceList){
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        }else{
            String address = "https://restapi.amap.com/v3/config/district?keywords=中国&subdistrict=1&key=c1894e9fcaf35e9fceabe9afaf40d45f";
            queryFromServer(address,"province");
        }
    }
    /**
     * 查询选中省内所有的市，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCities() {
        titleText.setText(selectedProvince.getProvinceName());
        backButton.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceCode = ?",
                String.valueOf(selectedProvince.getProvinceCode())).find(City.class);
        if (cityList.size()>0){
            dataList.clear();
            for (City city : cityList){
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        }else {
            String provinceName = selectedProvince.getProvinceName();
            String address = "https://restapi.amap.com/v3/config/district?keywords="+provinceName+"&subdistrict=1&key=c1894e9fcaf35e9fceabe9afaf40d45f";
            queryFromServer(address,"city");
        }
    }

    /**
     * 查询选中市内所有的县，优先从数据库查询，如果没有查询到再去服务器上查询。
     */
    private void queryCounties() {
        titleText.setText(selectedCity.getCityName());
        backButton.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityCode=?",
                String.valueOf(selectedCity.getCityCode())).find(County.class);
        if (countyList.size() >0){
            dataList.clear();
            for (County county:countyList){
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        }else{
            String cityName = selectedCity.getCityName();
            String address = "https://restapi.amap.com/v3/config/district?keywords="+cityName+"&subdistrict=1&key=c1894e9fcaf35e9fceabe9afaf40d45f";
            queryFromServer(address,"county");
        }
    }


    /**
     * 根据传入的地址和类型从服务器上查询省市县数据
     */
    private void queryFromServer(String address, final String type){
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                if("province".equals(type)){
                    result = Utility.handleProvinceResponse(responseText);
                }else if("city".equals(type)){
                    result = Utility.handleCityResponse(responseText,selectedProvince.getProvinceCode());
                }else if("county".equals(type)){
                    result = Utility.handleCountyResponse(responseText,selectedCity.getCityCode());
                }
                if(result){
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if("province".equals(type)){
                                queryProvinces();
                            }else if("county".equals(type)){
                                queryCities();
                            }else if("county".equals(type)){
                                queryCounties();
                            }
                        }
                    });
                }
            }
        });
    }
    /*private void queryProvinceFromServer(String address) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject =new JSONObject(responseString);
                    JSONArray countryAll = jsonObject.getJSONArray("districts");
                    for (int i = 0; i < countryAll.length(); i++) {
                        JSONObject countryLeve0 = countryAll.getJSONObject(i);
                        String country = countryLeve0.getString("level");
                        int level = 0;
                        if (country.equals("country")) {
                            level = 0;
                        }

                        //插入省
                        JSONArray provinceAll = countryLeve0.getJSONArray("districts");
                        for (int j = 0; j < provinceAll.length(); j++) {
                            JSONObject province1 = provinceAll.getJSONObject(j);
                            String adcode1 = province1.getString("adcode");
                            String name1 = province1.getString("name");
                            Province provinceN = new Province();
                            provinceN.setProvinceCode(adcode1);
                            provinceN.setProvinceName(name1);
                            provinceN.save();
                            String province = province1.getString("level");
                            if (province.equals("province")) {
                                queryProvinces();
                            }
                                }
                            }
                        }
                            catch(JSONException e){
                                e.printStackTrace();
                            }
                        }

        @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void queryCityFromServer(String address) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray provinceAll = jsonObject.getJSONArray("districts");
                        for (int j = 0; j < provinceAll.length(); j++) {
                            JSONObject province1 = provinceAll.getJSONObject(j);
                            String province = province1.getString("level");
                            String adcode1 = province1.getString("adcode");
                            int level2 = 0;
                            if (province.equals("province")) {
                                level2 = 2;
                            }
                            //插入市
                            JSONArray cityAll = province1.getJSONArray("districts");
                            for (int z = 0; z < cityAll.length(); z++) {
                                JSONObject city2 = cityAll.getJSONObject(z);
                                String adcode2 = city2.getString("adcode");
                                String name2 = city2.getString("name");
                                City cityN = new City();
                                cityN.setCityCode(adcode2);
                                cityN.setCityName(name2);
                                cityN.setProvinceCode(adcode1);
                                cityN.save();
                                String city = city2.getString("level");
                                if (city.equals("city")) {
                                    queryCities();
                                }

                                    }
                                }
                            }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    private void queryCountyFromServer(String address) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String responseString = response.body().string();
                    JSONObject jsonObject = new JSONObject(responseString);
                    JSONArray cityAll = jsonObject.getJSONArray("districts");
                            for (int z = 0; z < cityAll.length(); z++) {
                                JSONObject city2 = cityAll.getJSONObject(z);
                                String city = city2.getString("level");
                                String adcode2 = city2.getString("adcode");
                                int level3 = 0;
                                if (city.equals("city")) {
                                    level3 = 3;
                                }
                                //插入市
                                JSONArray countyAll = city2.getJSONArray("districts");
                                for (int w = 0; w < countyAll.length(); w++) {
                                    JSONObject county3 = countyAll.getJSONObject(w);
                                    String adcode3 = county3.getString("adcode");
                                    String name3 = county3.getString("name");
                                    County countyN = new County();
                                    countyN.setCountyCode(adcode3);
                                    countyN.setCountyName(name3);
                                    countyN.setCityCode(adcode2);
                                    countyN.save();
                                    String county = county3.getString("level");
                                    if (county.equals("street")) {
                                        queryCounties();
                                    }
                                }
                            }
                        }
                catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }
*/
    /*private void queryFromServer(String address) {
        HttpUtil.sendOkHttpRequest(address, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    JSONObject jsonObject = new JSONObject(String.valueOf(response));
                    JSONArray countryAll = jsonObject.getJSONArray("districts");
                    for (int i = 0; i < countryAll.length(); i++) {
                        JSONObject countryLeve0 = countryAll.getJSONObject(i);
                        String adcode0 = countryLeve0.getString("adcode");
                        String name0 = countryLeve0.getString("name");
                        String country = countryLeve0.getString("level");
                        int level = 0;
                        if (country.equals("country")) {
                            level = 0;
                        }

                        //插入国家
                        JSONArray provinceAll = countryLeve0.getJSONArray("districts");
                        for (int j = 0; j < provinceAll.length(); j++) {
                            JSONObject province1 = provinceAll.getJSONObject(j);
                            String adcode1 = province1.getString("adcode");
                            String name1 = province1.getString("name");
                            Province provinceN = new Province();
                            provinceN.setProvinceCode(adcode1);
                            provinceN.setProvinceName(name1);
                            provinceN.save();
                            String province = province1.getString("level");
                            if (province.equals("province")) {
                                queryProvinces();
                            }
                            //插入省
                            JSONArray cityAll = province1.getJSONArray("districts");
                            for (int z = 0; z < cityAll.length(); z++) {
                                JSONObject city2 = cityAll.getJSONObject(z);
                                String adcode2 = city2.getString("adcode");
                                String name2 = city2.getString("name");
                                City cityN = new City();
                                cityN.setCityCode(adcode2);
                                cityN.setCityName(name2);
                                cityN.save();
                                String city = city2.getString("level");
                                if (city.equals("city")) {
                                    queryCities();
                                }
                                //插入市
                                JSONArray county0 = city2.getJSONArray("districts");
                                for (int w = 0; w < county0.length(); w++) {
                                    JSONObject street3 = county0.getJSONObject(w);
                                    String adcode3 = street3.getString("adcode");
                                    String name3 = street3.getString("name");
                                    County countyN = new County();
                                    countyN.setCountyCode(adcode3);
                                    countyN.setCountyName(name3);
                                    countyN.save();
                                    String county = street3.getString("level");
                                    if (county.equals("street")) {
                                        queryCounties();
                                    }
                                }
                            }
                        }
                    }}
                catch(JSONException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"加载失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }*/
    /*private void getAdcode() {
        String address = edit_city.getText().toString();
        String url = "https://restapi.amap.com/v3/geocode/geo?key=你在高德控制台申请的Web服务的key&address=" + address;

        final Request request = new Request.Builder().url(url).get().build();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = httpClient.newCall(request).execute();
                    //请求成功
                    if (response.isSuccessful()) {
                        String result = response.body().string();

                        Log.i("result", result);

                        //转JsonObject
                        JsonObject object = new JsonParser().parse(result).getAsJsonObject();
                        //转JsonArray
                        JsonArray array = object.get("geocodes").getAsJsonArray();
                        JsonObject info = array.get(0).getAsJsonObject();

                        //获取adcode
                        String adcode = info.get("adcode").getAsString();
                        Log.i("测试获取adcode", adcode);

                        //请求天气查询
                        getWeather(adcode);

                        Message message = Message.obtain();
                        message.what = 2;
                        message.obj = adcode;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    Log.i("SearchMainActivity.java", "服务器异常:" + e.toString());

                    Message message = Message.obtain();
                    message.what = 0;
                    message.obj = "服务器异常";
                    e.printStackTrace();
                }
            }
        }).start();
    }*/
}