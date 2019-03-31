package com.sunday.memory_map.listener;

import android.location.Location;
import android.widget.TextView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

/**
 * Created by hshande on 2019/3/30.
 */

public class CustomLocationListener extends BDAbstractLocationListener {

    private LocationHandler handler;

    public CustomLocationListener(LocationHandler handler) {
        this.handler = handler;
    }

    private double latitude;
    private double longitude;

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public void onReceiveLocation(BDLocation location) {

        StringBuilder llBuf = new StringBuilder();
        StringBuilder addressBuf = new StringBuilder();
        llBuf.append("定位方式：");
        int locType = location.getLocType();
        if (locType == BDLocation.TypeGpsLocation) {
            llBuf.append("GPS\n");
        } else if (locType == BDLocation.TypeNetWorkLocation) {
            llBuf.append("Network\n");
        } else {
            llBuf = new StringBuilder();
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        llBuf.append("纬度：").append(latitude).append("\t");
        llBuf.append("经度：").append(longitude).append("");

        String province = location.getProvince();
        String city = location.getCity();
        String district = location.getDistrict();
        String street = location.getStreet();
        String address = location.getAddrStr();
//        addressBuf.append(province == null ? "" : province);
//        addressBuf.append(city == null ? "" : city);
//        addressBuf.append(district == null ? "" : district);
//        addressBuf.append(street == null ? "" : street);
        addressBuf.append(address == null ? "" : address);

        if (handler != null) {

            handler.afterLocation(latitude, longitude, llBuf.toString());
            handler.afterLocation(latitude, longitude, llBuf.toString(), addressBuf.toString());
        }
    }

    public interface LocationHandler {

        void afterLocation(double latitude, double longitude, String llMsg);

        void afterLocation(double latitude, double longitude, String llMsg, String addressMsg);
    }
}
