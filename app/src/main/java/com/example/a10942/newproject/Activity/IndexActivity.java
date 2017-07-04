package com.example.a10942.newproject.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.example.a10942.newproject.R;
import com.example.a10942.newproject.Utils.SPUtils;
import com.example.a10942.newproject.Utils.SensorEventHelper;
import com.example.a10942.newproject.Utils.Utils;
import com.google.zxing.client.android.ScannerActivity;

import de.hdodenhof.circleimageview.CircleImageView;
import me.weyye.hipermission.HiPermission;
import me.weyye.hipermission.PermissionCallback;

public class IndexActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        LocationSource, AMapLocationListener, AMap.OnMarkerClickListener {
    private MapView mapView;//地图
    private AMap aMap;
    private CircleImageView mian_zxing;//借伞
    private Context mContext;
    private SPUtils sputils;//保存的登陆信息
    boolean str;//登陆状态
    private String TAG = "IndexActivity";
    private SensorEventHelper mSensorHelper;
    private MarkerOptions options;
    private Marker mLocMarker;
    private LatLng latlng = new LatLng(26.566398, 106.681249);
    private LatLng latlng1 = new LatLng(26.570502106, 106.685129);
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private boolean mFirstFix = false;
    private AMapLocationClientOption mLocationOption;
    private Circle mCircle;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    public static final String LOCATION_MARKER_FLAG = "你当前位置";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        mContext = IndexActivity.this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //地图初始化
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        init();

        findView();
    }

    private void findView() {
        mian_zxing = (CircleImageView) findViewById(R.id.mian_zxing);
        mian_zxing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScannerActivity.startScannerActivity(mContext, 233);
            }
        });
    }

    //界面初始化
    private void init() {
        //权限申请
        HiPermission.create(mContext)
                .checkMutiPermission(new PermissionCallback() {
                    @Override
                    public void onClose() {
                        Log.i(TAG, "onClose");
                        showToast("用户关闭权限申请");
                    }

                    @Override
                    public void onFinish() {
                        showToast("所有权限申请完成");
                    }

                    @Override
                    public void onDeny(String permisson, int position) {
                        Log.i(TAG, "onDeny");
                    }

                    @Override
                    public void onGuarantee(String permisson, int position) {
                        Log.i(TAG, "onGuarantee");
                    }
                });
        str = sputils.contains(mContext, "userName");
        if (str != true) {
            Toast.makeText(mContext, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(IndexActivity.this, LoginActivity.class));
            this.finish();
        }
        if (aMap == null) {
            aMap = mapView.getMap();
            setUpMap();
        }
        mSensorHelper = new SensorEventHelper(this);
        if (mSensorHelper != null) {
            mSensorHelper.registerSensorListener();
        }
    }

    /**
     * 设置一些amap的属性
     */
    private void setUpMap() {
        aMap.setOnMarkerClickListener(this);// 设置点击marker事件监听器
        aMap.setLocationSource(this);// 设置定位监听
        aMap.getUiSettings().setMyLocationButtonEnabled(true);// 设置默认定位按钮是否显示
        aMap.setMyLocationEnabled(true);// 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        // 设置定位的类型为定位模式 ，可以由定位、跟随或地图根据面向方向旋转几种
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);

    }

    //抽屉控件
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_exit) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    //定位成功的回掉函数
    @Override
    public void activate(OnLocationChangedListener listener) {
        mListener = listener;
        if (mlocationClient == null) {
            mlocationClient = new AMapLocationClient(this);
            mLocationOption = new AMapLocationClientOption();
            //设置定位监听
            mlocationClient.setLocationListener(this);
            //设置为高精度定位模式
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //设置定位参数
            mlocationClient.setLocationOption(mLocationOption);
            // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
            // 注意设置合适的定位时间的间隔（最小间隔支持为2000ms），并且在合适时间调用stopLocation()方法来取消定位请求
            // 在定位结束后，在合适的生命周期调用onDestroy()方法
            // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
            mlocationClient.startLocation();
            addMarkersToMap(latlng);
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mlocationClient != null) {
            mlocationClient.stopLocation();
            mlocationClient.onDestroy();
        }
        mlocationClient = null;
    }

    void showToast(String str) {
        Toast.makeText(mContext, str, Toast.LENGTH_SHORT).show();
    }


    //marker点击事件
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (aMap != null) {
            Utils utils = new Utils();
            jumpPoint(marker);
        }
        Toast.makeText(IndexActivity.this, "您点击了Marker" + marker.getId(), Toast.LENGTH_LONG).show();
//        marker.showInfoWindow();
        return false;
    }


    /**
     * 定位成功后回调函数
     */
    @Override
    public void onLocationChanged(AMapLocation amapLocation) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation != null
                    && amapLocation.getErrorCode() == 0) {
                LatLng location = new LatLng(amapLocation.getLatitude(), amapLocation.getLongitude());
                if (!mFirstFix) {
                    mFirstFix = true;
                    addCircle(location, amapLocation.getAccuracy());//添加定位精度圆
                    addMarker(location);//添加定位图标
                    mSensorHelper.setCurrentMarker(mLocMarker);//定位图标旋转
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 18));
                } else {
                    mCircle.setCenter(location);
                    mCircle.setRadius(amapLocation.getAccuracy());
                    mLocMarker.setPosition(location);
                    //   aMap.moveCamera(CameraUpdateFactory.changeLatLng(location));//定位成功后居中
                }
            } else {
                String errText = "定位失败," + amapLocation.getErrorCode() + ": " + amapLocation.getErrorInfo();
                Log.e("AmapErr", errText);

            }
        }
    }


    private void addCircle(LatLng latlng, double radius) {
        CircleOptions options = new CircleOptions();
        options.strokeWidth(1f);
        options.fillColor(FILL_COLOR);
        options.strokeColor(STROKE_COLOR);
        options.center(latlng);
        options.radius(radius);
        mCircle = aMap.addCircle(options);
    }

    /**
     * 定位的marker
     *
     * @param latlng
     */
    private void addMarker(LatLng latlng) {
//        options = new MarkerOptions();
//        options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(this.getResources(),
//                R.mipmap.navi_map_gps_locked)));
//        options.anchor(0.4f, 0.4f);
//        options.position(latlng);
//        mLocMarker = aMap.addMarker(options);
//        mLocMarker.setTitle(LOCATION_MARKER_FLAG);

        // 设置当前地图显示为当前位置
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 19));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.anchor(0.2f, 0.2f);
        markerOptions.title(LOCATION_MARKER_FLAG);
        markerOptions.visible(true);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap
                (BitmapFactory.decodeResource(getResources(), R.mipmap.navi_map_gps_locked));
        markerOptions.icon(bitmapDescriptor);
        aMap.addMarker(markerOptions);
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(LatLng latLng) {
        // 设置当前地图显示为当前位置
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.anchor(0.2f, 0.2f);
        markerOptions.title("伞的位置");
        markerOptions.visible(true);
        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap
                (BitmapFactory.decodeResource(getResources(), R.mipmap.action_location));
        markerOptions.icon(bitmapDescriptor);
        aMap.addMarker(markerOptions);
    }

    /**
     * 回调函数，并处理二维码返回的数据，
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 233 && resultCode == RESULT_OK) {
            String result = data.getStringExtra("result");
            Intent intent = new Intent(IndexActivity.this, ClockActivity.class);
            intent.putExtra("Result", result);
            startActivity(intent);
            // 关闭当前页面
            this.finish();
        }

    }

    /**
     * marker点击时跳动一下
     */
    public void jumpPoint(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = aMap.getProjection();
        final LatLng markerLatlng = marker.getPosition();
        Point markerPoint = proj.toScreenLocation(markerLatlng);
        markerPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(markerPoint);
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * markerLatlng.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * markerLatlng.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

}
