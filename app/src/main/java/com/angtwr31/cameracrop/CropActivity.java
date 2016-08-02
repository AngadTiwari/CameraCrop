package com.angtwr31.cameracrop;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.angtwr31.cameracrop.cropper.view.ImageCropView;

import java.io.File;
import java.io.FileOutputStream;
import com.angtwr31.cameracrop.R;

public class CropActivity extends AppCompatActivity {

    public static final String TAG = "CropActivity";
    private ImageCropView imageCropView;
    private TextView tvSave, tvCancel;
    private float[] positionInfo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        imageCropView = (ImageCropView) findViewById(R.id.image);
        tvSave = (TextView)findViewById(R.id.save);
        tvCancel = (TextView)findViewById(R.id.cancel);

        Intent i = getIntent();

        try{
            imageCropView.setImageFilePath(this,i.getExtras().getString("file"));
            imageCropView.setAspectRatio(1,1);
        }
        catch (NullPointerException npe){
            npe.printStackTrace();
            finish();
        }

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtility.disableDoubleClick(v);

                if(isPossibleCrop(1,1)) {
                    //finish();
                    //startActivity(new Intent(CropActivity.this,MainActivity.class));
                    finish();
                }
            }
        });

        tvSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtility.disableDoubleClick(v);

                if(!imageCropView.isChangingScale()) {
                    Bitmap b = imageCropView.getCroppedImage();
                    if(b!=null)
                    {
                        bitmapConvertToFile(b);
                    }
                    else{
                        Toast.makeText(CropActivity.this, "Cropping failed !!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private boolean isPossibleCrop(int widthRatio, int heightRatio){

        int bitmapWidth = imageCropView.getViewBitmap().getWidth();
        int bitmapHeight = imageCropView.getViewBitmap().getHeight();
        return !(bitmapWidth < widthRatio && bitmapHeight < heightRatio);
    }

    public File bitmapConvertToFile(Bitmap bitmap) {

        FileOutputStream fileOutputStream = null;
        File bitmapFile = null;
        try {
            bitmapFile = new File(getExternalFilesDir(null), "pic_crop.jpg");
            if (!bitmapFile.exists()) {
                Log.i(TAG, "bitmapConvertToFile: file not exist");
            }

            fileOutputStream = new FileOutputStream(bitmapFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 40, fileOutputStream);
            MediaScannerConnection.scanFile(this, new String[]{bitmapFile.getAbsolutePath()}, null, new MediaScannerConnection.MediaScannerConnectionClient() {
                @Override
                public void onMediaScannerConnected() {

                }
                @Override
                public void onScanCompleted(final String path, final Uri uri) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            Intent data = new Intent();
                            data.setData(uri);
                            setResult(RESULT_OK, data);
                            finish();
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return bitmapFile;
    }



    private String uriToPath(Uri imageUri) {

        String uriPath = null;

        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(imageUri,filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        uriPath = cursor.getString(columnIndex);
        cursor.close();
        if (!TextUtils.isEmpty(uriPath)) {
            Log.v(TAG, "SELECT_PICTURE... " + uriPath);
        }

        return uriPath;
    }

    public void onClickSaveButton(View v) {

        positionInfo = imageCropView.getPositionInfo();
        View restoreButton = findViewById(R.id.restore_btn);
        if(restoreButton!=null)
            if (!restoreButton.isEnabled()) {
                restoreButton.setEnabled(true);
            }
    }

    public void onClickRestoreButton(View v) {

        imageCropView.applyPositionInfo(positionInfo);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
