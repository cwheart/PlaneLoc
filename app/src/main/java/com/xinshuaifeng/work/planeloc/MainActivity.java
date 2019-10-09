package com.xinshuaifeng.work.planeloc;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationListener;
import com.xinshuaifeng.work.planeloc.ui.login.LoginActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {
    private AMapLocationClient mLocationClient;
    private SharedPreferences sp;
    private PosterThread posterThread;
    private SensorManager sensorManager;
    private TextView planeNoView;
    private TextView tailNoView;
    private TextView presureView;
    private TextView locationView;
    private TextView speedView;
    private TextView heightView;
    private TextView presureHeightView;
    private TextView statusView;
    private float presure;
    private double presureHeight;
    private LocationData locData = new LocationData();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initUi();
        initPlaneNo();
        initSensor();
        initThreadPoster();
        initLocation();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingActivity = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(settingActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    protected void onRestart() {
        super.onRestart();
        initPlaneNo();
    }

    private void initUi() {
        this.planeNoView = findViewById(R.id.planeNoView);
        this.tailNoView = findViewById(R.id.tailNoView);
        this.presureView = findViewById(R.id.presureTextView);
        this.locationView = findViewById(R.id.locationView);
        this.speedView = findViewById(R.id.speedView);
        this.heightView = findViewById(R.id.heightView);
        this.presureHeightView = findViewById(R.id.presureHeightView);
        this.statusView = findViewById(R.id.statusView);
    }

    private void initPlaneNo() {
        if(sp == null) {
            sp = getSharedPreferences("plane", Context.MODE_PRIVATE);
        }
        String planeNoText = "呼号: " + sp.getString("planeNo", "未设置");
        String tailNoText = "尾号: " + sp.getString("tailNo", "未设置");
        this.planeNoView.setText(planeNoText);
        this.tailNoView.setText(tailNoText);

        this.locData.setPlaneNo(sp.getString("planeNo", "未设置"));
        this.locData.setTailNo(sp.getString("tailNo", "未设置"));
    }

    private void initSensor() {
        if(sensorManager == null) {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void initThreadPoster() {
        if (null == posterThread) {
            posterThread = new PosterThread();
            posterThread.setLoc(this.locData);
            posterThread.setHost(sp.getString("host", "192.168.1.1"));
            new Thread(posterThread).start();
        }
    }

    private void initLocation() {
        mLocationClient = new AMapLocationClient(getApplicationContext());
        mLocationClient.setLocationListener(mLocationListener);
        mLocationClient.startLocation();
    }

    // 定位变化回调
    AMapLocationListener mLocationListener = new AMapLocationListener() {

        @Override
        public void onLocationChanged(AMapLocation loc) {
            int status = loc.getGpsAccuracyStatus();
            if(status == AMapLocation.GPS_ACCURACY_GOOD) {
                statusView.setText("GPS: 信号强");
            } else {
                statusView.setText("GPS: 信号弱");
            }
            locData.setLatitude(loc.getLatitude());
            locData.setLongitude(loc.getLongitude());
            locData.setSpeed(loc.getSpeed());
            locData.setAltitude(loc.getAltitude());
            if(sp.getString("host", "192.168.1.1") != sp.getString("hostWas", "192.168.1.1")) {
                posterThread.setHost(sp.getString("host", "192.168.1.1"));
            }

            locationView.setText("定位: " + loc.getLongitude() + ", " + loc.getLatitude());
            heightView.setText("定位高度: " + loc.getAltitude() + "米");
            speedView.setText("速度: " + loc.getSpeed() + "米每秒");
        }
    };

    // 气压变化回调
    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent arg0) {
            locData.setPressure(arg0.values[0]);
            presureView.setText("气压: " + locData.getPressureValue() + "hPa");
            presureHeightView.setText("气压高度: " + locData.getPressureAltitudeValue() + "米");
        }

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {

        }
    };
}


