package com.example.weather02.db;

import org.litepal.crud.LitePalSupport;

public class Province extends LitePalSupport {

    private String provinceName;
    private String  provinceCode;

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public String  getProvinceCode() { return provinceCode; }

    public void setProvinceCode(String  provinceCode) {
        this.provinceCode = provinceCode;
    }

}