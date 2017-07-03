package com.example.a10942.newproject.base;

import com.amap.api.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 10942 on 2017/7/1 0001.
 */

public class Mybase {

    private LatLng latLng;
    private String string;

    public Mybase(LatLng latLng, String string) {
        this.latLng = latLng;
        this.string = string;
    }

    public Mybase() {
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }


    List<LatLng> pointList = new ArrayList<LatLng>();
//        pointList.add(new LatLng(26.5648462233, 106.6813015938));
//        pointList.add(new LatLng(26.5705845952, 106.6843485832));
//        pointList.add(new LatLng(26.5675715060, 106.6884684563));
//        pointList.add(new LatLng(26.5629653566, 106.6847133636));
//        pointList.add(new LatLng(26.5636946759, 106.6823101044));
//        pointList.add(new LatLng(26.5662328629, 106.6847831011));
//        pointList.add(new LatLng(26.5664103899, 106.6850996017));
//        pointList.add(new LatLng(26.5662088727, 106.6870629787));
//        pointList.add(new LatLng(26.5647598576, 106.6858828068));
//        List<String> listStirng = new ArrayList<>();
//        listStirng.add("第一个");
//        listStirng.add("第二个");
//        listStirng.add("第三个");
//        listStirng.add("第四个");
//        listStirng.add("第五个");
//        listStirng.add("第六个");
//        listStirng.add("第七个");
//        listStirng.add("第八个");
//        listStirng.add("第九个");
//        List<Mybase> list = new ArrayList<>();
//        for (int i = 0; i <= listStirng.size(); i++) {
//            LatLng latLng = pointList.get(i);
//            String str = listStirng.get(i);
//            addMarkersToMap(latLngs, str);
//        }
}
