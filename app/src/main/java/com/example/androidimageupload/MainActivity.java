package com.example.androidimageupload;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    // Required permissions
    private String[] appPermisssions = {Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    // Intent Request codes
    private static final int PERMISSION_REQUEST_CODE = 3012;
    private static final int PICK_IMAGE = 3012;
    private static final int CAMERA_REQUEST = 1000;


    // Firebase reference variables

    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    private StorageTask uploadTask;

    private Dialog dialog;


    private LinearLayout camCapture, galleryUpload;
    private Uri imageUri;
    private ProgressBar progressBar;
    private TextView progressTitle;
    private Button viewUploadedImages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // check for permissions on launch
        checkAndRequestPermission();

        camCapture = findViewById(R.id.capture_from_camera);
        galleryUpload = findViewById(R.id.upload_from_gallery);
        progressBar = findViewById(R.id.progressBar);
        progressTitle = findViewById(R.id.title_progress);
        viewUploadedImages = findViewById(R.id.view_uploaded_images);
        camCapture.setOnClickListener(this);
        galleryUpload.setOnClickListener(this);
        viewUploadedImages.setOnClickListener(this);


        // Initializing firebase reference variables
        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");

    }


    // Method to capture image from the camera

    private void captureImage() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(chooserIntent, CAMERA_REQUEST);
        } else {
            ContentValues values = new ContentValues();
            imageUri = this.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            PackageManager packageManager = this.getPackageManager();
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            if (cameraIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "No supported application found", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // method for selecting images from the gallery

    private void chooseImageFromGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        switch (requestCode) {
            case CAMERA_REQUEST:
                try {   // sending the captured image to CropActivity for further processing
                    if (resultCode == Activity.RESULT_OK) {
                        CropImage.activity(imageUri)
                                .setFixAspectRatio(true)
                                .start(this);
                    }
                } catch (Exception e) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;
            case PICK_IMAGE:
                try {   // sending the selected image for cropping
                    if (resultCode == Activity.RESULT_OK) {
                        assert data != null;
                        CropImage.activity(data.getData())
                                .setFixAspectRatio(true)
                                .start(this);
//                        uploadFile(data.getData());
                    }
                } catch (Exception e) {
                    Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
                }
                break;


            // Crop Activity receives a full sized image and crops it according to the user
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    assert result != null;
                    Uri resultUri = result.getUri();
                    Toast.makeText(this, resultUri.getPath(), Toast.LENGTH_SHORT).show();
                    if (uploadTask != null && uploadTask.isInProgress()) {
                        Toast.makeText(this, "An upload is still in progress, please wait !!!", Toast.LENGTH_SHORT).show();
                    } else
                        uploadFile(resultUri);
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Toast.makeText(this, "", Toast.LENGTH_SHORT).show();
                }
                break;
        }


    }


    // method for getting the file extension
    public String GetFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }


    // method for uploading files to Firebase Storage and Realtime Database
    private void uploadFile(Uri fileUri) {
        final StorageReference fileReference = storageReference.child(System.currentTimeMillis() + "." + GetFileExtension(fileUri));
        uploadTask = fileReference.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        DateFormat df = new SimpleDateFormat("yyyyMMdd  HH:mm");
                        String sdt = df.format(new Date(System.currentTimeMillis()));
                        String url = uri.toString();
                        UploadModel uploadModel = new UploadModel(sdt, url);
                        String uploadId = databaseReference.push().getKey();
                        assert uploadId != null;
                        databaseReference.child(uploadId).setValue(uploadModel);
                    }
                });

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(0);
                    }
                }, 500);
                progressBar.setVisibility(View.GONE);
                progressTitle.setVisibility(View.GONE);
                progressBar.setProgress(0);
                Toast.makeText(MainActivity.this, "Upload Successful !!", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Something went wrong !!", Toast.LENGTH_SHORT).show();
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                progressBar.setVisibility(View.VISIBLE);
                progressTitle.setVisibility(View.VISIBLE);
                double progress = (100 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressBar.setProgress((int) progress);
            }
        });
    }


    // requesting runtime permissions to avoid ANRs and ambiguous operations
    private boolean checkAndRequestPermission() {
        List<String> appPermissionsNeeded = new ArrayList<>();
        for (String perm : appPermisssions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                appPermissionsNeeded.add(perm);
            }
        }

        // asking for non granted permissions
        if (!appPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, appPermissionsNeeded.toArray(new String[appPermissionsNeeded.size()]), PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.capture_from_camera:
                if (checkAndRequestPermission()) {
                    // do all the processing
                    captureImage();
                } else {
                    Toast.makeText(MainActivity.this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
                    // request permissions
                }
                break;
            case R.id.upload_from_gallery:
                if (checkAndRequestPermission()) {
                    // do all the processing
                    chooseImageFromGallery();
                } else {
                    Toast.makeText(MainActivity.this, "Please grant all permissions", Toast.LENGTH_SHORT).show();
                    // request permissions
                }
                break;
            case R.id.view_uploaded_images:
                goToImageActivity();
                break;
        }

    }


    private void goToImageActivity() {
        Intent intent = new Intent(this, ShowImagesActivity.class);
        startActivity(intent);
    }
}
