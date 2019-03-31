package com.sunday.memory_map;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.sunday.memory_map.bean.Record;
import com.sunday.memory_map.listener.CustomLocationListener;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static com.baidu.location.d.a.i;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;

    private boolean focusMe = true;

    private BaiduMap baiduMap;

    @BindView(R.id.position_tv)
    TextView positionTV;
    @BindView(R.id.address_tv)
    TextView addressTV;
    @BindView(R.id.position_rl)
    RelativeLayout positionRL;
    @BindView(R.id.favorite_iv)
    ImageView favoriteIV;
    @BindView(R.id.focus_iv)
    ImageView focusIV;
    @BindView(R.id.map)
    MapView mapV;

    private final static int LOCATION_TIME = 2000;

    // 声明一个集合，在后面的代码中用来存储用户拒绝授权的权
    List<String> mPermissionList = new ArrayList<>();

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SDKInitializer.initialize(getApplicationContext());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bringToFront();

        mPermissionList.clear();
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(MainActivity.this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);
            }
        }
        if (mPermissionList.isEmpty()) {//未授予的权限为空，表示都授予了
            realStart();
        } else {//请求权限方法
            String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);//将List转为数组
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        }
        initData();
    }

    private void initData() {

        List<Record> records = DataSupport.order("id asc").find(Record.class);
        if (records == null || records.isEmpty()) {

            return;
        }

        for (Record record : records) {
            addOverlay(record);
        }
    }

    int[] imgIds = new int[]{R.mipmap.icon_marka, R.mipmap.icon_markb, R.mipmap.icon_markc};

    public void addOverlay(Record record) {

        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.text_up_img_down, null);
        TextView tv_temp = view.findViewById(R.id.baidumap_custom_text);//获取自定义布局中的textview
        ImageView img_temp = view.findViewById(R.id.baidumap_custom_img);//获取自定义布局中的imageview
        tv_temp.setText(record.getRemark());//设置要显示的文本
        img_temp.setImageResource(imgIds[(int) (Math.random() * imgIds.length)]);
        BitmapDescriptor custom = BitmapDescriptorFactory.fromView(view);
        LatLng latLng2 = new LatLng(record.getLatitude(), record.getLongitude());//初始画时
        OverlayOptions ooD = new MarkerOptions().position(latLng2).icon(custom)
                .zIndex(0).period(10);
        baiduMap.addOverlay(ooD);
    }

    private void bringToFront() {
        positionRL.bringToFront();
        addressTV.bringToFront();
        focusIV.bringToFront();
    }

    private void realStart() {
        baiduMap = mapV.getMap();
        baiduMap.setMyLocationEnabled(true);
        if (mLocationClient == null) {

            mLocationClient = new LocationClient(getApplicationContext());
            mLocationClient.registerLocationListener(new CustomLocationListener(new CustomLocationListener.LocationHandler() {
                @Override
                public void afterLocation(double latitude, double longitude, String llMsg) {

                }

                @Override
                public void afterLocation(double latitude, double longitude, String llMsg, String addressMsg) {
//                    latitude = 39.763175;
//                    longitude = 116.300244;
                    positionTV.setText(llMsg);

                    positionTV.setTag(new Double[]{latitude, longitude});

                    addressTV.setText(addressMsg);
                    if (focusMe) {
                        LatLng ll = new LatLng(latitude, longitude);
                        MapStatusUpdate update = MapStatusUpdateFactory.newLatLng(ll);
                        baiduMap.animateMapStatus(update);
                        update = MapStatusUpdateFactory.zoomTo(16f);
                        baiduMap.animateMapStatus(update);
                        focusMe = false;
                    }
                    MyLocationData.Builder locaBuilder = new MyLocationData.Builder();
                    locaBuilder.latitude(latitude);
                    locaBuilder.longitude(longitude);
                    MyLocationData locationData = locaBuilder.build();
                    baiduMap.setMyLocationData(locationData);
                }
            }));
            LocationClientOption option = new LocationClientOption();
            option.setScanSpan(LOCATION_TIME);
            option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
            option.setCoorType("bd09ll");
            option.setIsNeedAddress(true);
            option.setIsNeedLocationDescribe(true);
            option.setIsNeedLocationDescribe(true);
            option.setOpenGps(true);
            mLocationClient.setLocOption(option);
        }
        mLocationClient.start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapV.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapV.onPause();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {

                            showToast("必须同意所有权限才能使用");
                            finish();
                            return;
                        }
                    }
                    realStart();
                } else {

                    showToast("位置错误");
                    finish();
                }
                break;
        }
    }

    private void showToast(String string) {
        Toast.makeText(MainActivity.this, string, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {

            mLocationClient.stop();
        }
        mapV.onDestroy();
        baiduMap.setMyLocationEnabled(false);
    }

    @OnClick(R.id.position_tv)
    public void positionClick(View view) {

        int visibility = addressTV.getVisibility();
        if (visibility == View.VISIBLE) {
            addressTV.setVisibility(View.GONE);
        } else {
            addressTV.setVisibility(View.VISIBLE);
        }
    }

    @OnClick(R.id.favorite_iv)
    public void favoriteClick(View view) {

        showInputDialog();
    }

    @OnLongClick(R.id.favorite_iv)
    public boolean favoriteLongClick(View view) {


        return false;
    }

    @OnClick(R.id.focus_iv)
    public void focusClick(View view) {

        focusMe = true;
    }

    private void showInputDialog() {

        final EditText inputServer = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入备注").setIcon(android.R.drawable.ic_dialog_info).setView(inputServer)
                .setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                try {
                    Thread.sleep(LOCATION_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Record record = new Record();
                Object ll = positionTV.getTag();

                if (ll == null) {

                    showToast("定位失败");
                    dialog.dismiss();
                    return;
                }
                Double[] llAry = (Double[]) ll;
                record.setLatitude(Double.parseDouble(llAry[0].toString()));
                record.setLongitude(Double.parseDouble(llAry[1].toString()));
                record.setRemark(inputServer.getText().toString());
                record.save();
                addOverlay(record);
            }
        });
        builder.show();
    }


}
