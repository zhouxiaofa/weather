package com.example.weather02.util;

import android.text.TextUtils;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.weather02.db.City;
import com.example.weather02.db.County;
import com.example.weather02.db.Province;
import com.example.weather02.gson.Weather;

public class Utility {
    /*public static boolean handleProvinceResponse1(String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allProvinces = new JSONArray(response);
                for (int i = 0; i < allProvinces.length(); i++) {
                    JSONObject provinceObject = allProvinces.getJSONObject(i);
                    Province province = new Province();
                    province.setProvinceName(provinceObject.getString("name"));
                    province.setProvinceCode(provinceObject.getString("adcode"));
                    province.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
*/
    public static boolean handleProvinceResponse(String response){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray countryAll = jsonObject.getJSONArray("districts");
                for (int i = 0; i < countryAll.length(); i++) {
                    JSONObject countryLeve0 = countryAll.getJSONObject(i);
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
                    }
                    return true;
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCityResponse(String response, String provinceCode){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray provinceAll = jsonObject.getJSONArray("districts");
                for (int i = 0; i < provinceAll.length(); i++) {
                    JSONObject province1 = provinceAll.getJSONObject(i);
                    //插入市
                    JSONArray cityAll = province1.getJSONArray("districts");
                    for (int j = 0; j < cityAll.length(); j++) {
                        JSONObject city2 = cityAll.getJSONObject(j);
                        String adcode2 = city2.getString("adcode");
                        String name2 = city2.getString("name");
                        City cityN = new City();
                        cityN.setCityCode(adcode2);
                        cityN.setCityName(name2);
                        cityN.setProvinceCode(provinceCode);
                        cityN.save();
                    }
                    return true;
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse(String response, String cityCode){
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray cityAll = jsonObject.getJSONArray("districts");
                for (int i = 0; i < cityAll.length(); i++) {
                    JSONObject city2 = cityAll.getJSONObject(i);
                    //插入市
                    JSONArray countyAll = city2.getJSONArray("districts");
                    for (int j = 0; j < countyAll.length(); j++) {
                        JSONObject county3 = countyAll.getJSONObject(j);
                        String adcode3 = county3.getString("adcode");
                        String name3 = county3.getString("name");
                        County countyN = new County();
                        countyN.setCountyCode(adcode3);
                        countyN.setCountyName(name3);
                        countyN.setCityCode(cityCode);
                        countyN.save();
                    }
                    return true;
                }
            }
            catch(JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }


    /*public static boolean handleCityResponse1(String response, String  provinceCode) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONArray allCities = new JSONArray(response);
                for (int i = 0; i < allCities.length(); i++) {
                    JSONObject cityObject = allCities.getJSONObject(i);
                    City city = new City();
                    city.setCityName(cityObject.getString("name"));
                    city.setCityCode(cityObject.getInt("adcode"));
                    city.setProvinceCode(provinceCode);
                    city.save();
                }
                return true;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean handleCountyResponse1(String response, String  cityCode){
        if(!TextUtils.isEmpty(response)){
            try{
                JSONArray allCounties = new JSONArray(response);
                for(int i=0; i<allCounties.length(); i++){
                    JSONObject countyObject = allCounties.getJSONObject(i);
                    County county = new County();
                    county.setCountyName(countyObject.getString("name"));
                    county.setCountyCode(countyObject.getString("adcode"));
                    county.save();
                }
                return true;
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
        return false;
    }
*/
    public static Weather handleWeatherResponse(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("lives");
                for(int i=0; i<jsonArray.length(); i++){
                    JSONObject x = jsonArray.getJSONObject(i);
                String weatherContent = x.toString();
                return new Gson().fromJson(weatherContent, Weather.class);
            } }catch (Exception e) {
                e.printStackTrace();
            }
        return null;
    }
}

