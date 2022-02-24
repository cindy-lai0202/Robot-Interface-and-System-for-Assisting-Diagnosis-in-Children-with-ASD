package com.example.chooseimage_zenbo;

import android.Manifest;
import android.app.ProgressDialog;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.asus.robotframework.API.RobotCallback;
import com.asus.robotframework.API.RobotCmdState;
import com.asus.robotframework.API.RobotErrorCode;
import com.asus.robotframework.API.RobotFace;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.robot.asus.robotactivity.RobotActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission_group.CAMERA;

public class MainActivity3 extends RobotActivity {
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private Handler timerUpdateHandler;
    private boolean timerRunning = false;
    private int currentTimer ;
    String name;
    FirebaseStorage storage;
    StorageReference storageReference;
    Uri fileUri;

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
    TextureView textureView;
    String inputname,number;
    ImageView iv1,iv2,iv3,iv4,iv5,iv6;
    ImageView[] imgs = {iv1,iv2,iv3,iv4,iv5,iv6};
    int[] card = {R.drawable.apple,R.drawable.banana,R.drawable.orange,R.drawable.pineapple,R.drawable.strawberry,R.drawable.watermelon};
    String[] str = {"鳳梨", "蘋果", "西瓜", "香蕉", "橘子", "草莓"};
    int[] view = {R.id.img1,R.id.img2,R.id.img3,R.id.img4,R.id.img5,R.id.img6};
    int bingo = 0;

    public MainActivity3(RobotCallback robotCallback, RobotCallback.Listen robotListenCallback) {
        super(robotCallback, robotListenCallback);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);
        Bundle bundle = this.getIntent().getExtras();
        number = bundle.getString("ID");
        inputname = bundle.getString("name");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        timerUpdateHandler = new Handler();
        currentTimer = 2;
        if (!timerRunning) {
            timerRunning = true;
            timerUpdateHandler.post(timerUpdateTask);
        }

        for(int i=0;i<6;i++) {
            imgs[i] = (ImageView) findViewById(view[i]);
        }

        robotAPI.robot.setExpression(RobotFace.HIDEFACE,inputname+"，可以幫我找出" + str[2]+","+str[3]+"還有"+str[4]+ "嗎?");
        for (int i = 0; i < 10000; i++) {
            int shuffle2 = (int) (Math.random() * 6);
            int c = card[shuffle2];
            card[shuffle2] = card[0];
            card[0] = c;
        }
        for (int i = 0; i < card.length; i++) {
            imgs[i].setImageResource(card[i]);
            if (card[i] == R.drawable.watermelon || card[i] == R.drawable.banana || card[i] == R.drawable.orange) {
                final int j = i;
                imgs[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bingo++;
                        imgs[j].setImageResource(R.drawable.white);
                        if(bingo == 3){
                            robotAPI.robot.setExpression(RobotFace.ACTIVE,"哇!"+inputname+"你太厲害了!");
                            robotAPI.robot.speak("完成水果遊戲了!");
                            stopRecordingVideo();
                            fileUri = Uri.fromFile(new File("/storage/emulated/0/Android/data/com.example.chooseimage_zenbo/files/"+name));
                            uploadVideo();
                        }
                    }
                });
            } else {
                imgs[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        robotAPI.robot.speak("答錯瞜，再仔細找找!");
                    }
                });
            }
                /*if (card[i] == R.drawable.apple) {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答對瞜，你好棒!");
                            try {
                                Thread.sleep(2000); //1000為1秒
                            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
                            startActivity(intent);
                            System.exit(0);
                        }
                    });
                } else {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答錯瞜，再仔細找找!");
                        }
                    });
                }
                if (card[i] == R.drawable.watermelon) {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答對瞜，你好棒!");
                            try {
                                Thread.sleep(2000); //1000為1秒
                            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
                            startActivity(intent);
                            System.exit(0);
                        }
                    });
                } else {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答錯瞜，再仔細找找!");
                        }
                    });
                }
                if (card[i] == R.drawable.banana) {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答對瞜，你好棒!");
                            try {
                                Thread.sleep(2000); //1000為1秒
                            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
                            startActivity(intent);
                            System.exit(0);
                        }
                    });
                } else {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答錯瞜，再仔細找找!");
                        }
                    });
                }
                if (card[i] == R.drawable.orange) {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答對瞜，你好棒!");
                            try {
                                Thread.sleep(2000); //1000為1秒
                            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
                            startActivity(intent);
                            System.exit(0);
                        }
                    });
                } else {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答錯瞜，再仔細找找!");
                        }
                    });
                }
                if (card[i] == R.drawable.strawberry) {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答對瞜，你好棒!");
                            try {
                                Thread.sleep(2000); //1000為1秒
                            } catch (InterruptedException e) {
// TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(MainActivity2.this,MainActivity3.class);
                            startActivity(intent);
                            System.exit(0);
                        }
                    });
                } else {
                    imgs[i].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            robotAPI.robot.speak("答錯瞜，再仔細找找!");
                        }
                    });
                }*/
        }
    }
    private Runnable timerUpdateTask = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (currentTimer >0) {
                currentTimer--;
                timerUpdateHandler.postDelayed(timerUpdateTask, 1000);
            }else{
                startRecordingVideo();
            }
        }
    };
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

        textureView = (TextureView)findViewById(R.id.camera2video_activity_textureview);


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
                        MainActivity3.this.cameraDevice = null;
                    }
                }

                @Override
                public void onError(CameraDevice camera, int error) {
                    Toast.makeText(MainActivity3.this, "Open Error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MainActivity3.this, "Failed", Toast.LENGTH_SHORT).show();
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
                    MainActivity3.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isRecordingVideo = true;

                            mediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(MainActivity3.this, "Failed", Toast.LENGTH_SHORT).show();
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
        name="chooseimage3_"+timeStamp+".mp4";
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

        /*Toast.makeText(this, "Video saved: " + nextVideoAbsolutePath, Toast.LENGTH_SHORT).show();*/
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
    private void uploadVideo()
    {
        if (fileUri != null) {

            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog
                    = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            // progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref
                    = storageReference
                    .child(
                            number+"/"
                                    + name);

            // adding listeners on upload
            // or failure of image
            ref.putFile(fileUri)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    progressDialog.dismiss();
                                    /*Toast
                                            .makeText(MainActivity3.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();*/
                                    robotAPI.robot.setExpression(RobotFace.HIDEFACE);
                                    System.exit(0);
                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {

                            // Error, Image not uploaded
                            progressDialog.dismiss();
                            Toast
                                    .makeText(MainActivity3.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
                    /*.addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot)
                                {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int)progress + "%");
                                }
                            });*/

        }
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
                        textureView = (TextureView)findViewById(R.id.camera2video_activity_textureview);
                        return;
                    }
                }
                checkPermission();
                return;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("--------onStop","onStop");
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.e("--------onStart","onStarte");
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("--------onRestart","onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("--------onDestroy","onDestroy");
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


    public MainActivity3() {
        super(robotCallback, robotListenCallback);
    }
}

