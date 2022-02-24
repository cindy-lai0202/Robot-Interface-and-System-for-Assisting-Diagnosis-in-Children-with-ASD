package com.example.bubble2;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends RobotActivity {

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private Handler timerUpdateHandler;
    private boolean timerRunning = false;
    private int currentTimer ;
    String inputname,name;

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private CameraManager cameraManager;
    private String cameraBF;
    private ImageReader imageReader;
    private CameraCaptureSession cameraPrewSession;
    private CameraDevice cameraDevice;

    private Size prewSize;
    private Size videoSize;

    private MediaRecorder mediaRecorder;

    private HandlerThread backgroundThread;
    private Handler cameraHandler;

    private int sensorOrientation;

    private CaptureRequest.Builder previewRequestBuilder;
    private Surface recorderSurface;

    private Boolean isRecordingVideo = false;

    public MainActivity(RobotCallback robotCallback, RobotCallback.Listen robotListenCallback) {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        Bundle bundle = this.getIntent().getExtras();
        inputname = bundle.getString("name");
        robotAPI.robot.setExpression(RobotFace.HAPPY,"哇!，是泡泡欸，"+inputname+"可以幫我抓住它們嗎?");

        timerUpdateHandler = new Handler();

        currentTimer = 10;
        if (!timerRunning) {
            timerRunning = true;
            timerUpdateHandler.post(timerUpdateTask);
        }
        if(checkPermission()) {
            initView();
        }
    }


    private TextureView textureView;


    private Runnable timerUpdateTask = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (currentTimer >0) {
                currentTimer--;
                timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
            } /*else if(currentTimer <= 10 && currentTimer>0) {
                if(timerRunning==true) {
                    startRecordingVideo();

                }
                timerRunning = false;
                currentTimer--;
                timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
            }*/else{
                //stopRecordingVideo();
                //Intent intent1 = new Intent(MainActivity.this,upload.class);
                //intent1.putExtra("path", name);
                //startActivity(intent1);
                robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                System.exit(0);
            }
        }
    };
    public void initView(){
        textureView = (TextureView)findViewById(R.id.camera2video_activity_textureview);
    }

    public void startThread(){
        backgroundThread = new HandlerThread("Camera2");
        backgroundThread.start();
        cameraHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            cameraHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        startThread();

        if (textureView.isAvailable()) {
            initCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    initCamera(width,height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                    configureTransform(width,height);
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            });
        }

    }

    @Override
    public void onPause() {
        closeCamera();
        stopThread();
        super.onPause();
        robotAPI.robot.stopSpeak();
    }

    private void initCamera(int width, int height){
        setupCamera(width,height);
        openCamera();
        configureTransform(width,height);
    }

    private void closeCamera() {

        closePreviewSession();

        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (imageReader != null) {
            imageReader.close();
            imageReader = null;
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == textureView || null == prewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, prewSize.getHeight(), prewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / prewSize.getHeight(),
                    (float) viewWidth / prewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private void setupCamera(int width, int height) {
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId: cameraManager.getCameraIdList()) {

                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK)
                    continue;

                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);

                videoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

                prewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height, videoSize);

                mediaRecorder = new MediaRecorder();

                cameraBF = cameraId;
                break;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {
        List<Size> bigEnough = new ArrayList<Size>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        } else {
            return choices[0];
        }
    }

    public void openCamera(){
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            cameraManager.openCamera(cameraBF, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();

                    if (textureView != null ) {
                        configureTransform(textureView.getWidth(), textureView.getHeight());
                    }
                }

                @Override
                public void onDisconnected(CameraDevice camera) {
                    if (cameraDevice != null) {
                        cameraDevice.close();
                        MainActivity.this.cameraDevice = null;
                    }
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    Toast.makeText(MainActivity.this, "Open Error", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }




    private void startPreview() {
        if (null == cameraDevice || !textureView.isAvailable() || null == prewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(prewSize.getWidth(), prewSize.getHeight());
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            previewRequestBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Arrays.asList(previewSurface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    cameraPrewSession = cameraCaptureSession;
                    try {
                        previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                        cameraPrewSession.setRepeatingRequest(previewRequestBuilder.build(), null, cameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closePreviewSession() {
        if (cameraPrewSession != null) {
            cameraPrewSession.close();
            cameraPrewSession = null;
        }
    }


    private void startRecordingVideo() {
        if (cameraDevice == null || !textureView.isAvailable() || prewSize == null ) {
            return;
        }

        try {
            closePreviewSession();

            setUpMediaRecorder();

            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(prewSize.getWidth(), prewSize.getHeight());
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            previewRequestBuilder.addTarget(previewSurface);

            recorderSurface = mediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            previewRequestBuilder.addTarget(recorderSurface);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    cameraPrewSession = cameraCaptureSession;
                    try {
                        previewRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                        cameraPrewSession.setRepeatingRequest(previewRequestBuilder.build(), null, cameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isRecordingVideo = true;

                            mediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String nextVideoAbsolutePath;

    private void setUpMediaRecorder() throws IOException {

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (nextVideoAbsolutePath == null || nextVideoAbsolutePath.isEmpty()) {
            nextVideoAbsolutePath = getVideoFilePath(this);

        }
        mediaRecorder.setOutputFile(nextVideoAbsolutePath);
        Log.e("file","setoutputfile");
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (sensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mediaRecorder.prepare();
    }

    private String getVideoFilePath(Context context) {
        String timeStamp = new SimpleDateFormat("yyyy_MMdd_HHmmss").format(new Date());
        name="bubble"+timeStamp+".mp4";
        String tmp = context.getExternalFilesDir(null).getAbsolutePath() + "/"
                +name;
        return tmp;
    }

    private void stopRecordingVideo() {

        isRecordingVideo = false;

        try {
            cameraPrewSession.stopRepeating();
            cameraPrewSession.abortCaptures();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mediaRecorder.stop();
                mediaRecorder.reset();
            }
        };
        timer.schedule(timerTask,30);

        Toast.makeText(this, "Video saved: " + nextVideoAbsolutePath, Toast.LENGTH_SHORT).show();
        nextVideoAbsolutePath = null;

        startPreview();
    }


    public Boolean checkPermission(){
        if(Build.VERSION.SDK_INT >= 23){
            String permissions[] = {CAMERA, RECORD_AUDIO};
            List<String> pm_list = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                int pm = ActivityCompat.checkSelfPermission(this, permissions[i]);
                if (pm != PackageManager.PERMISSION_GRANTED) {
                    pm_list.add(permissions[i]);
                }
            }
            if (pm_list.size() > 0) {
                ActivityCompat.requestPermissions(this, pm_list.toArray(new String[pm_list.size()]), 1);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case 1:
                if (grantResults.length > 0){
                    int i=0;
                    for(i =0;i<permissions.length;i++) {
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            break;
                        }
                    }
                    if(i==permissions.length){
                        initView();
                        return;
                    }
                }
                checkPermission();
                return;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    public static RobotCallback robotCallback = new RobotCallback() {
        @Override
        public void onResult(int cmd, int serial, RobotErrorCode err_code, Bundle result) {
            super.onResult(cmd, serial, err_code, result);
        }

        @Override
        public void onStateChange(int cmd, int serial, RobotErrorCode err_code, RobotCmdState state) {
            super.onStateChange(cmd, serial, err_code, state);
        }

        @Override
        public void initComplete() {
            super.initComplete();

        }
    };
    public static RobotCallback.Listen robotListenCallback = new RobotCallback.Listen() {
        @Override
        public void onFinishRegister() {

        }

        @Override
        public void onVoiceDetect(JSONObject jsonObject) {

        }

        @Override
        public void onSpeakComplete(String s, String s1) {

        }
        @Override
        public void onEventUserUtterance(JSONObject jsonObject) {
        }

        @Override
        public void onResult(JSONObject jsonObject) {

        }

        @Override
        public void onRetry(JSONObject jsonObject) {

        }
    };

    public MainActivity() {
        super(robotCallback, robotListenCallback);
    }

}
