package com.angtwr31.cameracrop;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ImageView profile_img;
    private static final int REQUEST_CAMERA_CAPTURE = 1;
    private static final int REQUEST_GALLERY_CAPTURE = 2;
    private static final int REQUEST_CROP = 3;
    private String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile_img = (ImageView)findViewById(R.id.profile_img);
        profile_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogStyle);
                builder.setTitle("");
                builder.setMessage("Change Profile Picture");

                builder.setNegativeButton("Take Photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dispatchCameraIntent();
                    }
                });
                builder.setPositiveButton("Choose from Library", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dispatchGalleryIntent();
                    }
                });
                builder.show();
            }
        });
    }

    /**
     * open a camera
     */
    private void dispatchCameraIntent() {

        if ((android.os.Build.VERSION.SDK_INT >= 23) && ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            requestPermissions(new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA_CAPTURE);

            // REQUEST_CAMERA_CAPTURE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            Intent takePictureIntent = new Intent(this, CameraActivity.class);//new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            /*if (takePictureIntent.resolveActivity(this.getPackageManager()) != null) {

                outputFileUri = getCaptureImageOutputUri();
                if (outputFileUri != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                }
                startActivityForResult(takePictureIntent, REQUEST_CAMERA_CAPTURE);
            }*/
            startActivityForResult(takePictureIntent, REQUEST_CAMERA_CAPTURE);
        }
    }

    /**
     * open a phone-gallery
     */
    private void dispatchGalleryIntent() {

        if ((android.os.Build.VERSION.SDK_INT >= 23) && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_GALLERY_CAPTURE);

            // REQUEST_GALLERY_CAPTURE is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else {
            Intent pickPictureIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // startActivityForResult(i, REQUEST_GALLERY_CAPTURE);
            if (pickPictureIntent.resolveActivity(this.getPackageManager()) != null) {
                startActivityForResult(pickPictureIntent, REQUEST_GALLERY_CAPTURE);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(data==null) {
            Log.i(TAG, "onActivityResult: data==null");
            return;
        }

        Log.i(TAG, "onActivityResult: requestcode="+requestCode);
        switch (requestCode){

            case REQUEST_CAMERA_CAPTURE: {

                Uri imageUri = getPickImageResultUri(data);
                File imageFile = new File(imageUri.getPath());
                Intent intent = new Intent(this, CropActivity.class);
                intent.putExtra("file", imageFile.getAbsolutePath());
                startActivityForResult(intent, REQUEST_CROP);

                /*CropImage.activity(imageUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(getActivity(), SettingsFragment.this);*/

                break;
            }
            case REQUEST_GALLERY_CAPTURE: {

                Intent intent = new Intent(this, CropActivity.class);
                intent.putExtra("file", uriToPath(data.getData()));
                startActivityForResult(intent, REQUEST_CROP);

                /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                    try {

                        CropImage.activity(uri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setCropShape(CropImageView.CropShape.RECTANGLE)
                                .start(getActivity(), SettingsFragment.this);

                        isProfilePicUpdated = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                    if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        try {
                            CropImage.activity(uri)
                                    .setGuidelines(CropImageView.Guidelines.ON)
                                    .setFixAspectRatio(true)
                                    .start(getActivity(), SettingsFragment.this);

                            isProfilePicUpdated = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }*/
                break;
            }

            case REQUEST_CROP: {

                profile_img.setImageDrawable(null);
                Uri uri = data.getData();
                profile_img.setImageURI(uri);
                break;
            }

            default: {
                Log.i(TAG, "onActivityResult: default");
                break;
            }
        }
    }

    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
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
}
