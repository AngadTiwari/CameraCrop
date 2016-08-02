package com.angtwr31.cameracrop;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class CameraActivity extends AppCompatActivity {

    private static float PREVIEW_SIZE_FACTOR = 0.80f;
    private Camera mCamera;
    private CameraPreview mPreview;
    private static CameraActivity mInstance;
    private ImageView button_capture, button_switch;

    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private FrameLayout previewFrame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mInstance = this;
        mCamera = getCameraInstance(currentCameraId);// Create an instance of Camera

        if(mCamera==null){
            finish();
        }

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        previewFrame = (FrameLayout) findViewById(R.id.camera_preview);
        previewFrame.addView(mPreview);

        // Add a listener to the Capture button
        button_capture = (ImageView) findViewById(R.id.button_capture);
        button_switch = (ImageView) findViewById(R.id.button_switch);

        if(!isFrontCameraAvailable())
            button_switch.setVisibility(View.GONE);

        button_capture.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CommonUtility.disableDoubleClick(v);
                        // get an image from the camera
                        mCamera.takePicture(null, null, mPicture);
                    }
                }
        );
        button_switch.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        CommonUtility.disableDoubleClick(v);
                        // get an switch image to front camera
                        if(mCamera==null)
                            return;

                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                        mPreview = null;
                        previewFrame.removeAllViews();

                        //swap the id of the camera to be used
                        if(currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK){
                            currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                        }
                        else {
                            currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                        }
                        mCamera = getCameraInstance(currentCameraId);
                        // Create our Preview view and set it as the content of our activity.
                        mPreview = new CameraPreview(CameraActivity.this, mCamera);
                        previewFrame.addView(mPreview);
                    }
                }
        );
    }

    /** A safe way to get an instance of the Camera object. */
    public static Camera getCameraInstance(int cameraId){
        Camera c = null;
        try {
            c = Camera.open(cameraId); // attempt to get a Camera instance
            final Camera.Parameters params = c.getParameters();
            List<String> focusModes = params.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                // Autofocus mode is supported
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                c.setParameters(params);
            }
            final Camera.Size size = getOptimalSize(c);
            params.setPreviewSize(size.width, size.height);
            params.setPictureFormat(ImageFormat.JPEG);
            params.setJpegQuality(100);
            c.setParameters(params);
            setCameraDisplayOrientation(mInstance, cameraId, c);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {

        if(camera==null)
            return;

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();

        android.hardware.Camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private static Camera.Size getOptimalSize(Camera camera) {
        Camera.Size result = null;
        final Camera.Parameters parameters = camera.getParameters();
        Log.i(TAG, "window width: " + getWidth() + ", height: " + getHeight());
        for (final Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= getWidth() * PREVIEW_SIZE_FACTOR && size.height <= getHeight() * PREVIEW_SIZE_FACTOR) {
                if (result == null) {
                    result = size;
                } else {
                    final int resultArea = result.width * result.height;
                    final int newArea = size.width * size.height;

                    if (newArea > resultArea) {
                        result = size;
                    }
                }
            }
        }
        if (result == null) {
            result = parameters.getSupportedPreviewSizes().get(0);
        }
        Log.i(TAG, "Using PreviewSize: " + result.width + " x " + result.height);
        return result;
    }

    private BitmapFactory.Options getBitmapOption(Camera camera){
        BitmapFactory.Options opt;

        opt = new BitmapFactory.Options();
        opt.inTempStorage = new byte[16 * 1024];
        Camera.Parameters parameters = camera.getParameters();
        Camera.Size size = parameters.getPictureSize();

        int height11 = size.height;
        int width11 = size.width;
        float mb = (width11 * height11) / 1024000;

        if (mb > 4f)
            opt.inSampleSize = 4;
        else if (mb > 3f)
            opt.inSampleSize = 2;

        return opt;
    }

    private static float getWidth() {
        return mInstance.getWindow().getDecorView().getWidth();
    }

    private static float getHeight() {
        return mInstance.getWindow().getDecorView().getHeight();
    }

    private static String TAG = CameraActivity.class.getSimpleName();
    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = new File(Environment.getExternalStorageDirectory(), "pickImageResult.jpeg");
            if (pictureFile == null){
                Log.d(TAG, "Error creating media file, check storage permissions: ");
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ) {
                    // Notice that width and height are reversed
                    Bitmap bm = BitmapFactory.decodeByteArray(data , 0, data.length, getBitmapOption(camera));
                    Bitmap scaled = bm;
                    int w = scaled.getWidth();
                    int h = scaled.getHeight();
                    // Setting post rotate to 90
                    Matrix mtx = new Matrix();
                    try{
                        //int rotate = rotateAngle(file.getPath(), currentCameraId);
                        android.hardware.Camera.CameraInfo info =
                                new android.hardware.Camera.CameraInfo();
                        android.hardware.Camera.getCameraInfo(currentCameraId, info);
                        mtx.postRotate((w>h)?info.orientation:0);
                    }
                    catch (Exception ioe){
                        ioe.printStackTrace();
                    }
                    // Rotating Bitmap
                    bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    bm.compress(Bitmap.CompressFormat.JPEG, 60, fos);
                    fos.close();
                }
                else {
                    fos.write(data);
                    fos.close();
                }
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            Intent intent = new Intent();
            intent.setData(Uri.fromFile(pictureFile));
            setResult(RESULT_OK, intent);
            finish();
        }
    };


    private boolean isFrontCameraAvailable(){
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                return true;
        }
        return false;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"<<onDestroy");
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        super.onDestroy();
    }
}
