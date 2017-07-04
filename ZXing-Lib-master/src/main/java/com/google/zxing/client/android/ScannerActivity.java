/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.zxing.client.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.google.zxing.ResultMetadataType;
import com.google.zxing.client.android.camera.CameraManager;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;


/**
 * This activity opens the camera and does the actual scanning on a background thread. It draws a
 * viewfinder to help the user place the barcode correctly, shows feedback as the image processing
 * is happening, and then overlays the results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class ScannerActivity extends Activity implements SurfaceHolder.Callback, IScanActivity, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = ScannerActivity.class.getSimpleName();

    private static final long DEFAULT_INTENT_RESULT_DURATION_MS = 1500L;
    private static final long BULK_MODE_SCAN_DELAY_MS = 1000L;

    private static final String[] ZXING_URLS = {"http://zxing.appspot.com/scan", "zxing://scan/"};

    private static final int HISTORY_REQUEST_CODE = 0x0000bacc;

    private static final Collection<ResultMetadataType> DISPLAYABLE_METADATA_TYPES =
            EnumSet.of(ResultMetadataType.ISSUE_NUMBER,
                    ResultMetadataType.SUGGESTED_PRICE,
                    ResultMetadataType.ERROR_CORRECTION_LEVEL,
                    ResultMetadataType.POSSIBLE_COUNTRY);

    private CameraManager cameraManager;
    private CaptureActivityHandler handler;
    private Result savedResultToShow;
    private ViewfinderView viewfinderView;
    private View scanTitleView;
    private Result lastResult;
    private boolean hasSurface;
    private boolean copyToClipboard;
    private IntentSource source;
    private String sourceUrl;
    //  private ScanFromWebPageManager scanFromWebPageManager;
    private Collection<BarcodeFormat> decodeFormats;
    private Map<DecodeHintType, ?> decodeHints;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    /**
     * 周围光线检测管理
     */
    private AmbientLightManager ambientLightManager;
    private ImageView imgview;

    private SurfaceView surfaceView4CameraPreview;
    private SeekBar seekBar2ScaleCamera;
    /**
     * 缩放摄像头焦距的布局控件
     */
    private View scaleCameraLayout;
    private EditText textView4;

    public static void startScannerActivity(Context context, int requestActivityCode) {
        startScannerActivity(context, -1, requestActivityCode);
    }

    /**
     * 启动二维码扫描界面，携带自定义取景框的尺寸大小信息
     *
     * @param context
     * @param viewfinderOutLineStyleResId 取景器轮廓的尺寸大小信息. eg.:如果当前只扫描条形码，一般为长方形取景框，则可自定义长大于宽,当然默认的取景框样式对于条形码同样能扫
     * @param requestActivityCode
     */
    public static void startScannerActivity(Context context, int viewfinderOutLineStyleResId, int requestActivityCode) {
        startScannerActivity(context, viewfinderOutLineStyleResId, -1, requestActivityCode);
    }

    /**
     * 启动二维码扫描界面
     *
     * @param context                     上下文
     * @param viewfinderOutLineStyleResId 扫描界面中取景框的轮廓(尺寸大小)样式资源ID，该样式为改变取景框大小的
     * @param wholeScanLayoutId           整个自定义的二维码扫描界面布局资源ID,如果调用此方法，则需要注意里面的相关控件需要和scanner_layout.xml中的控件的ID以及类型一致
     * @param requestActivityCode         启动请求码
     */
    public static void startScannerActivity(Context context, int viewfinderOutLineStyleResId, int wholeScanLayoutId, int requestActivityCode) {
        Intent startIntent = new Intent(context, ScannerActivity.class);
        startIntent.putExtra(KEY_VIEWFINDERSTYLE, viewfinderOutLineStyleResId);
        startIntent.putExtra(KEY_SCAN_LAYOUT, wholeScanLayoutId);
        if (context instanceof Activity) {
            ((Activity) context).startActivityForResult(startIntent, requestActivityCode);
        } else {
            context.startActivity(startIntent);
        }
    }

    /**
     * 各使用方想得到本二维码扫描界面的结果，需要在{@link Activity#onActivityResult(int, int, Intent)}中通过返回的Intent
     * 获取key:"result"的字符串结果
     */
    public static final String INTENT_KEY_SCAN_RESULT = "scan_result";
    private static final String KEY_VIEWFINDERSTYLE = "viewfinder_style";
    private static final String KEY_SCAN_LAYOUT = "scan_layout";
    private int viewfinderOutLineStyleResId;
    private Camera camera;
    Button button4;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent startIntent = getIntent();
        viewfinderOutLineStyleResId = startIntent.getIntExtra(KEY_VIEWFINDERSTYLE, -1);
        if (viewfinderOutLineStyleResId <= 0) {
            viewfinderOutLineStyleResId = R.style.viewfinder_outline_style;
        }
        int customScanLayoutResID = startIntent.getIntExtra(KEY_SCAN_LAYOUT, -1);
        if (customScanLayoutResID <= 0) {
            customScanLayoutResID = R.layout.scanner_layout;
        }
        setContentView(customScanLayoutResID);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        ambientLightManager = new AmbientLightManager(this);
        Log.i("info", TAG + "--> onCreate()");
        //闪光灯
//        imgview = (ImageView) findViewById(R.id.scanner_deng);
        textView4 = (EditText) findViewById(R.id.textView4);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i("info", TAG + "--> onStart()");
        surfaceView4CameraPreview = (SurfaceView) findViewById(R.id.scanner_preview_view);
        viewfinderView = (ViewfinderView) findViewById(R.id.scanner_viewfinder_view);
        seekBar2ScaleCamera = (SeekBar) findViewById(R.id.scanner_seekbar);
        scaleCameraLayout = findViewById(R.id.scanner_scale_layout);
        scanTitleView = findViewById(R.id.scanner_title);
        //确定
        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = textView4.getText().toString();
                Intent result = new Intent();
                result.putExtra("result", str);
                setResult(RESULT_OK, result);
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraManager = new CameraManager(getApplication());
        cameraManager.changeViewfinderRectStyle(viewfinderOutLineStyleResId);
        viewfinderView.configCameraManager(cameraManager);

        handler = null;
        lastResult = null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        resetStatusView();


        beepManager.updatePrefs();
        ambientLightManager.start(cameraManager);
        inactivityTimer.onResume();

        Intent intent = getIntent();

        copyToClipboard = prefs.getBoolean(PreferencesActivity.KEY_COPY_TO_CLIPBOARD, true)
                && (intent == null || intent.getBooleanExtra(Intents.Scan.SAVE_HISTORY, true));

        source = IntentSource.NONE;
        sourceUrl = null;
//    scanFromWebPageManager = null;
        decodeFormats = null;
        characterSet = null;

        SurfaceHolder surfaceHolder = surfaceView4CameraPreview.getHolder();
        if (hasSurface) {
            // The activity was paused but not stopped, so the surface still exists. Therefore
            // surfaceCreated() won't be called, so init the camera here.
            initCamera(surfaceHolder);
        } else {
            // Install the callback and wait for surfaceCreated() to init the camera.
            surfaceHolder.addCallback(this);
        }
        Log.i("info", TAG + "--> onResume()");
    }

    private int getCurrentOrientation() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_90:
                    return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
            }
        } else {
            switch (rotation) {
                case Surface.ROTATION_0:
                case Surface.ROTATION_270:
                    return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                default:
                    return ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
            }
        }
    }

    private static boolean isZXingURL(String dataString) {
        if (dataString == null) {
            return false;
        }
        for (String url : ZXING_URLS) {
            if (dataString.startsWith(url)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        ambientLightManager.stop();
        beepManager.close();
        cameraManager.closeDriver();
        //historyManager = null; // Keep for onActivityResult
        if (!hasSurface) {
            SurfaceHolder surfaceHolder = surfaceView4CameraPreview.getHolder();
            surfaceHolder.removeCallback(this);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (source == IntentSource.NATIVE_APP_INTENT) {
                    setResult(RESULT_CANCELED);
                    finish();
                    return true;
                }
                if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
                    restartPreviewAfterDelay(0L);
                    return true;
                }
                break;
            case KeyEvent.KEYCODE_FOCUS:
            case KeyEvent.KEYCODE_CAMERA:
                // Handle these events so they don't launch the Camera app
                return true;
            // Use volume up/down to turn on light
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                cameraManager.setTorch(false);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                cameraManager.setTorch(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.capture, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        int itemMenuId = item.getItemId();
        if (itemMenuId == R.id.menu_settings) {
            intent.setClassName(this, PreferencesActivity.class.getName());
            startActivity(intent);
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

    }

    private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e("info", TAG + "--> surfaceCreated() holder=" + holder);
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
        Log.e("info", TAG + "--> surfaceDestroyed() holder=" + holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("info", TAG + "--> surfaceChanged() holder = " + holder + " format= " + format + " width=" + width + " height=" + height);
    }

    /**
     * A valid barcode has been found, so give an indication of success and show the results.
     *
     * @param rawResult   The contents of the barcode.
     * @param scaleFactor amount by which thumbnail was scaled
     * @param barcode     A greyscale bitmap of the camera data which was decoded.
     */
    public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
        inactivityTimer.onActivity();
        lastResult = rawResult;
        beepManager.playBeepSoundAndVibrate();
        String str = rawResult.getText();
        Intent result = new Intent();
        result.putExtra("result", str);
        setResult(RESULT_OK, result);
        finish();


    }


    private void sendReplyMessage(int id, Object arg, long delayMS) {
        if (handler != null) {
            Message message = Message.obtain(handler, id, arg);
            if (delayMS > 0L) {
                handler.sendMessageDelayed(message, delayMS);
            } else {
                handler.sendMessage(message);
            }
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            // Creating the handler starts the preview, which can also throw a RuntimeException.
            if (handler == null) {
                handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
            }
            decodeOrStoreSavedBitmap(null, null);
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            // Barcode Scanner has seen crashes in the wild of this variety:
            // java.?lang.?RuntimeException: Fail to connect to camera service
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
        Log.i("info", TAG + "-->initCamera() " + cameraManager.isCurCameraSupportZoom() + " max zoom" + cameraManager.getCameraMaxZoom());
        if (!cameraManager.isCurCameraSupportZoom()) {
            scaleCameraLayout.setVisibility(View.INVISIBLE);
            viewfinderView.setSeekBar(null);
            seekBar2ScaleCamera.setOnSeekBarChangeListener(null);
        } else {
            viewfinderView.setSeekBar(seekBar2ScaleCamera);
            seekBar2ScaleCamera.setOnSeekBarChangeListener(this);
            seekBar2ScaleCamera.setProgress(cameraManager.getCameraCurZoom());
            seekBar2ScaleCamera.setMax(cameraManager.getCameraMaxZoom());
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(this));
        builder.setOnCancelListener(new FinishListener(this));
        builder.show();
    }

    public void restartPreviewAfterDelay(long delayMS) {
        if (handler != null) {
            handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
        }
        resetStatusView();
    }

    private void resetStatusView() {
        viewfinderView.setVisibility(View.VISIBLE);
        lastResult = null;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public Activity getActivity() {
        return this;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (cameraManager != null) {
            cameraManager.zoomCamera(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
