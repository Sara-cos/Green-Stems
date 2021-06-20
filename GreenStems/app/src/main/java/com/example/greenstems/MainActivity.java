package com.example.greenstems;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.tensorflow.lite.Interpreter;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {

    Button button;
    LinearLayout classification, soil, health, diseases;
    private static final int PERMISSION_NEW_REQUEST_CODE = 200;
    String[] permissionsRequired = new String[]
            {
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };

    Interpreter tflitemodel1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button=findViewById(R.id.gardening);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainActivity.this,GardenResource.class);
                startActivity(intent);
            }
        });

        classification=findViewById(R.id.classification);
        soil=findViewById(R.id.soil);
        health=findViewById(R.id.leafhealth);
        diseases=findViewById(R.id.diseases);

        classification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checknewpermission())
                {
                    addImage();
                }
                else
                {
                    requestnewpermission();
                }
            }
        });

        soil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checknewpermission())
                {
                    addImage();
                }
                else
                {
                    requestnewpermission();
                }
            }
        });

        health.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checknewpermission())
                {
                    addImage();
                }
                else
                {
                    requestnewpermission();
                }
            }
        });

        diseases.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checknewpermission())
                {
                    addImage();
                }
                else
                {
                    requestnewpermission();
                }
            }
        });


        try {
            //tflitemodel1=new Interpreter(loadModelFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checknewpermission()
    {
        int result = ContextCompat.checkSelfPermission(Objects.requireNonNull(MainActivity.this), Manifest.permission.CAMERA);
        int result1 = ContextCompat.checkSelfPermission(MainActivity.this, WRITE_EXTERNAL_STORAGE);
        int result2 = ContextCompat.checkSelfPermission(MainActivity.this,READ_EXTERNAL_STORAGE );
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
                && result2 == PackageManager.PERMISSION_GRANTED;
    }

    private void addImage()
    {
        final CharSequence[] options = { "Take Photo", "Choose From Gallery" ,"Cancel" };
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.
                Builder(Objects.requireNonNull(MainActivity.this));
        builder.setTitle("Choose a picture");
        builder.setItems(options, (dialog, which) -> {
            if (options[which].equals("Take Photo"))
            {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 10);
            }
            else if (options[which].equals("Choose From Gallery"))
            {
                Intent pickphoto = new Intent();
                pickphoto.setType("image/*");
                pickphoto.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(pickphoto,11);
            }
            else if(options[which].equals("Cancel"))
            {

                dialog.dismiss();
            }
        });
        builder.show();
    }

    private void requestnewpermission()
    {
        ActivityCompat.requestPermissions(Objects.requireNonNull(MainActivity.this),permissionsRequired,PERMISSION_NEW_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            assert data != null;
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            //Starting activity (ImageViewActivity in my code) to preview image
            Intent intent = new Intent(MainActivity.this, ActivityResultFlower.class);
            intent.putExtra("sendImage", photo);
            startActivity(intent);
        }
        else if(requestCode == 11)
        {

            assert data != null;
            if (data.getData() != null) {
                Uri imageUri = data.getData();

                //Starting activity (ImageViewActivity in my code) to preview image
                Intent intent = new Intent(MainActivity.this, ActivityResultFlower.class);
                intent.putExtra("selectedImage", imageUri.toString());
                startActivity(intent);
            }
        }
    }
}