package com.example.weather02.gson;

import com.google.gson.annotations.SerializedName;

public class Weather {

        @SerializedName("province")
        public String provinceName;

        @SerializedName("city")
        public String cityName;

        @SerializedName("adcode")
        public String adcodeName;

        @SerializedName("weather")
        public String weatherName;

        @SerializedName("temperature")
        public String temperatureName;

        @SerializedName("winddirection")
        public String windDirection;

        @SerializedName("windpower")
        public String windPower;

        @SerializedName("humidity")
        public String humidityName;

        @SerializedName("reporttime")
        public String reportTimeName;

}