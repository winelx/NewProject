package com.example.a10942.newproject.Activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.TextView;
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
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.FindCallback;
import com.example.a10942.newproject.R;
import com.example.a10942.newproject.Utils.ExitApplication;
import com.example.a10942.newproject.Utils.SPUtils;
import com.example.a10942.newproject.Utils.SensorEventHelper;
import com.example.a10942.newproject.Utils.Utils;
import com.google.zxing.client.android.ScannerActivity;

import java.util.ArrayList;
import java.util.List;

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
    private LocationSource.OnLocationChangedListener mListener;
    private AMapLocationClient mlocationClient;
    private boolean mFirstFix = false;
    private AMapLocationClientOption mLocationOption;
    private Circle mCircle;
    private static final int STROKE_COLOR = Color.argb(180, 3, 145, 255);
    private static final int FILL_COLOR = Color.argb(10, 0, 0, 180);
    public static final String LOCATION_MARKER_FLAG = "你当前位置";
    private TextView Login_registered, Login_Forgot_password;
    String address; //地址
    String LatLng_latitude;//纬度  "106.6858290000",
    String LatLng_longitude;//经度  26.5623960000"
    private List<AVObject> mList = new ArrayList<>();
    // "address": "贵州省贵阳市南明区花果园社区服务中心花果园大街花果园C区",
    AVQuery<AVObject> avQuery;
    List<Object> latlnged;
    //uiHandler在主线程中创建，所以自动绑定主线程
    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    latlnged = new ArrayList<Object>();
                    avQuery = new AVQuery<>("LatLng");
                    avQuery.orderByDescending("createdAt");
                    avQuery.include("address");
                    avQuery.include("LatLng_latitude");
                    avQuery.include("LatLng_longitude");
                    avQuery.findInBackground(new FindCallback<AVObject>() {
                        @Override
                        public void done(List<AVObject> list, AVException e) {
                            if (e == null) {
                                mList.addAll(list);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < mList.size(); i++) {
                                            address = mList.get(i).getString("address");
                                            LatLng_latitude = mList.get(i).getString("LatLng_latitude");
                                            LatLng_longitude = mList.get(i).getString("LatLng_longitude");
                                            double latitudes = Double.valueOf(LatLng_latitude);
                                            double longitudes = Double.valueOf(LatLng_longitude);
                                            addMarkersToMap(address, longitudes, latitudes);
                                        }
                                    }
                                }).start();


                            } else {
                                e.printStackTrace();
                            }
                        }
                    });
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
        //将该Activity添加到ExitApplication实例中，
        ExitApplication.getInstance().addActivity(this);
        mContext = IndexActivity.this;
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

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
        str = sputils.contains(mContext, "userName");
        if (str != true) {
            Toast.makeText(mContext, "请先登录", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(IndexActivity.this, LoginActivity.class));
            this.finish();
        } else {
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

    //抽屉界面的按钮点击事件
    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            //我的钱包
            startActivity(new Intent(IndexActivity.this, WalletActivity.class));
        } else if (id == R.id.nav_gallery) {
            //邀请朋友
           startActivity(new Intent(IndexActivity.this, FriendActivity.class));
        } else if (id == R.id.nav_slideshow) {
            //借伞记录
            startActivity(new Intent(IndexActivity.this, RecordActivity.class));
        } else if (id == R.id.nav_share) {
            //使用指南
            startActivity(new Intent(IndexActivity.this, SettingActivity.class));
        } else if (id == R.id.nav_send) {
            //设置
            startActivity(new Intent(IndexActivity.this, SettingActivity.class));
        } else if (id == R.id.nav_exit) {
            new AlertDialog.Builder(IndexActivity.this).setTitle("提示")//设置对话框标题

                    .setMessage("你确定退出当前账号吗？")//设置显示的内容

                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {//确定按钮的响应事件
                            AVUser.logOut();// 清除缓存用户对象
                            AVUser currentUser = AVUser.getCurrentUser();// 现在的 currentUser 是 null 了
                            sputils.clear(mContext);
                            startActivity(new Intent(IndexActivity.this, LoginActivity.class));
                            ExitApplication.getInstance().exit();
                        }
                    }).setNegativeButton("返回", new DialogInterface.OnClickListener() {//添加返回按钮
                @Override

                public void onClick(DialogInterface dialog, int which) {//响应事件
                    // TODO Auto-generated method stub
                    Log.i("alertdialog", " 请保存数据！");
                }

            }).show();//在按键响应事件中显示此对话框

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
                Message msg = new Message();
                msg.what = 1;
                uiHandler.sendMessage(msg);
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
        // 设置当前地图显示为当前位置
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 19));
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.anchor(0.5f, 0.5f);
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
    private void addMarkersToMap(String str, double latitude, double longitude) {
//        // 设置当前地图显示为当前位置
        LatLng lanlng = new LatLng(latitude, longitude);

        final Marker marker = aMap.addMarker(new MarkerOptions().position(lanlng).title(str));
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

    // 物理返回键，双击退出
    private long exitTime = 0;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 3000) {
                Toast.makeText(IndexActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                ExitApplication.getInstance().exit();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
