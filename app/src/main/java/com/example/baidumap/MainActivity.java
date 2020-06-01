package com.example.baidumap;

import android.content.Context;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

public class MainActivity extends AppCompatActivity {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;
    private Context context;
    //实现定位相关数据类型
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;//重写的监听类
    private boolean isFirstIn = true;
    private double mLatitude;//纬度
    private double mLongitude;//经度
    private String addStr;
    private float myCurrentX;

    private BitmapDescriptor myIconLocation1;
    private MyOrientationListener myOrientationListener;//方向感应器类对象
    private MyLocationConfiguration.LocationMode locationMode;

    private Button MyLocationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        this.context = this;
        initView();
        initLocation();
    }

    private void initLocation() {
        locationMode = MyLocationConfiguration.LocationMode.NORMAL;
        //客户端的定位服务
        mLocationClient = new LocationClient(this);
        myLocationListener = new MyLocationListener();
        //注册监听器
        mLocationClient.registerLocationListener(myLocationListener);
        //设置定位参数
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);

        myIconLocation1 = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
        MyLocationConfiguration configuration = new MyLocationConfiguration(locationMode,true,myIconLocation1);
        mBaiduMap.setMyLocationConfiguration(configuration);

        myOrientationListener = new MyOrientationListener(context);
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                myCurrentX = x;
            }
        });
    }

    private void initView() {
        mMapView = findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
        MyLocationButton=findViewById(R.id.my_location_button);
        MyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationByLL(mLatitude,mLongitude);
            }
        });
    }

    private void getLocationByLL(double la, double lg) {
        LatLng latLng = new LatLng(la,lg);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(msu);
        getLatAndLng(la,lg);
    }

    private void getLatAndLng(double la, double lg){
        String latAndlng = "经度："+ lg + "\n纬度：" + la+"\n地址："+addStr;
        Toast.makeText(context,latAndlng,Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onStart() {
        super.onStart();
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted()){
            mLocationClient.start();
        }
        myOrientationListener.start();
    }
    @Override
    protected void onStop() {
        super.onStop();
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        myOrientationListener.stop();
    }
    private class MyLocationListener implements BDLocationListener{
        @Override
        public void onReceiveLocation(BDLocation location) {
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            addStr=location.getAddrStr();
            MyLocationData data = new MyLocationData.Builder()//
                    .direction(myCurrentX)//
                    .accuracy(location.getRadius())//
                    .latitude(mLatitude)//
                    .longitude(mLongitude).build();
            mBaiduMap.setMyLocationData(data);
            if (isFirstIn){
                getLocationByLL(mLatitude,mLongitude);
                isFirstIn = false;
            }
        }
    }
}
