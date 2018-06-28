package com.example.manso.imageencryptor;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Picture;
import android.graphics.drawable.BitmapDrawable;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.*;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Random;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static java.lang.System.out;

public class MainActivity extends AppCompatActivity {
    // permission variables
    private static final String TAG_RUNTIME_PERMISSION = "TAG_RUNTIME_PERMISSION";
    private static final int PERMISSION_REQUEST_CODE = 1;

    TextView Info;
    TextView saveURI;
    ImageView targetImage;
    String ImagePath;
    EditText Seed;
    EditText Complexity;
    Uri URI;
    public String textTargetURI;
    public int height, pixel, pixel2, temp;
    public int width;
    public Bitmap bitmap;
    public Bitmap bitmapCopy;
    public int seed = 0, bound = 100, randomHeight, randomWidth;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button LoadImage = (Button) findViewById(R.id.ImageSelect);
        Button Encrypt = (Button) findViewById(R.id.Encypt);
        Button SaveImage = (Button) findViewById(R.id.Save);
        Button Decrypt = (Button) findViewById(R.id.Decrypt);
        Info = (TextView) findViewById(R.id.Info);
        saveURI = (TextView) findViewById(R.id.SaveURI);
        targetImage = (ImageView) findViewById(R.id.ImageView);
        Seed = (EditText) findViewById(R.id.Seed);
        Complexity = (EditText) findViewById(R.id.Complexity);


        /** Loading Image */
        LoadImage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 0);
                //permission stuff
                if (!hasRuntimePermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestRuntimePermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_CODE);
                } else {
                }


            }
        });


        /** ENCRYPTION */
        Encrypt.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if(targetImage.getDrawable() != null) {
                    if (Seed.getText().toString().matches("")) {
                        seed = 0;
                        Toast.makeText(MainActivity.this, "Seed is set to 0", Toast.LENGTH_SHORT).show();
                    } else {
                        seed = Integer.parseInt(Seed.getText().toString());
                    }
                    if (Complexity.getText().toString().matches("")) {
                        bound = 1;
                        Toast.makeText(MainActivity.this, "Please enter scattering coefficient!! No encryption occurred.", Toast.LENGTH_SHORT).show();

                    } else if(Complexity.getText().toString().matches("0")) {
                        bound = 1;
                        Toast.makeText(MainActivity.this, "0 scattering is not allowed! No encryption occurred.", Toast.LENGTH_SHORT).show();
                    } else {
                        bound = Integer.parseInt(Complexity.getText().toString());
                    }
                    Random r = new Random(seed);
                    for (int h = 1; h < height; h++) {
                        for (int w = 1; w < width; w++) {
                            randomHeight = (height*r.nextInt(bound))/bound;
                            randomWidth = (width*r.nextInt(bound))/bound;
                            pixel = bitmapCopy.getPixel(w, h);
                            pixel2 = bitmapCopy.getPixel(randomWidth, randomHeight);
                            temp = pixel;
                            pixel = pixel2;
                            pixel2 = temp;
                            bitmapCopy.setPixel(w, h, pixel);
                            bitmapCopy.setPixel(randomWidth, randomHeight, pixel2);
                        }
                    }
                    targetImage.setImageBitmap(bitmapCopy);
                    if (bound ==1 || Complexity.getText().toString().matches("")) {
                    } else {
                        Toast.makeText(MainActivity.this, "Encrypted!", Toast.LENGTH_SHORT).show();
                    }
                    String info = "Height = " + height + " pixels\nWidth = " + width + " pixels\nLast Operation: Encrypt\nImage URI: " + textTargetURI;
                    Info.setText(info);
                } else{
                    Toast.makeText(MainActivity.this, "Please select an Image first!", Toast.LENGTH_SHORT).show();
                }


            }
        });


        /** Saving image */
        SaveImage.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {


                //saveToInternalStorage(bitmapCopy);
                //saveImage(getApplicationContext(),bitmapCopy,"Image.PNG");
                FileOutputStream outStream = null;
                if(targetImage.getDrawable() != null) {
                    // Write to SD Card
                    try {
                        File sdCard = Environment.getExternalStorageDirectory();
                        File dir = new File(sdCard.getAbsolutePath() + "/DCIM");
                        dir.mkdirs();

                        Random gen = new Random();
                        int n = 1000;
                        n = gen.nextInt(n);
                        String photoname = "photo-" + n + ".jpg";
                        String fileName = String.format(photoname);
                        saveURI.setText("Image saved in: " + dir + "/DCIM \nImage name:" + fileName);
                        File outFile = new File(dir, fileName);
                        outStream = new FileOutputStream(outFile);
                        bitmapCopy.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                        outStream.flush();
                        outStream.close();
                        Toast.makeText(MainActivity.this, "Image saved!", Toast.LENGTH_SHORT).show();
                        String info = "Height = " + height + " pixels\nWidth = " + width + " pixels\nLast Operation: Save Image\nImage URI: " + textTargetURI;
                        Info.setText(info);
                        addImageGallery(outFile);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "FileNotFoundException", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "IOException", Toast.LENGTH_SHORT).show();
                    } finally {
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Please select an Image first!", Toast.LENGTH_SHORT).show();
                }

                //Toast.makeText(MainActivity.this, "Image saved!", Toast.LENGTH_SHORT).show();

            }
        });


        /** DECRYPTION */
        Decrypt.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if(targetImage.getDrawable() != null) {
                    if (Seed.getText().toString().matches("")) {
                        seed = 0;
                        Toast.makeText(MainActivity.this, "Seed is set to 0", Toast.LENGTH_SHORT).show();
                    } else {
                        seed = Integer.parseInt(Seed.getText().toString());
                    }
                    if (Complexity.getText().toString().matches("")) {
                        bound = 1;
                        Toast.makeText(MainActivity.this, "Please enter scattering coefficient! No decryption occurred.", Toast.LENGTH_SHORT).show();
                    } else if(Complexity.getText().toString().matches("0")) {
                        bound = 1;
                        Toast.makeText(MainActivity.this, "0 scattering is not allowed! No decryption occurred.", Toast.LENGTH_SHORT).show();
                    } else {
                        bound = Integer.parseInt(Complexity.getText().toString());
                    }
                    Random r = new Random(seed);


                    int[][] DecryptRandomHeight = new int[width][height];
                    int[][] DecryptRandomWidth = new int[width][height];

                    for (int h = 1; h < height; h++) {
                        for (int w = 1; w < width; w++) {
                            randomHeight = (height*r.nextInt(bound))/bound;
                            randomWidth = (width*r.nextInt(bound))/bound;
                            DecryptRandomHeight[w][h] = randomHeight;
                            DecryptRandomWidth[w][h] = randomWidth;

                        }
                    }
                    for (int h = height - 1; h >= 1; h--) {
                        for (int w = width - 1; w >= 1; w--) {
                            randomWidth = DecryptRandomWidth[w][h];
                            randomHeight = DecryptRandomHeight[w][h];
                            pixel = bitmapCopy.getPixel(w, h);
                            pixel2 = bitmapCopy.getPixel(randomWidth, randomHeight);
                            temp = pixel;
                            pixel = pixel2;
                            pixel2 = temp;
                            bitmapCopy.setPixel(w, h, pixel);
                            bitmapCopy.setPixel(randomWidth, randomHeight, pixel2);
                        }
                    }
                    targetImage.setImageBitmap(bitmapCopy);
                    if (bound ==1 || Complexity.getText().toString().matches("")) {
                    } else {
                        Toast.makeText(MainActivity.this, "Decrypted!", Toast.LENGTH_SHORT).show();
                    }
                    String info = "Height = " + height + " pixels\nWidth = " + width + " pixels\nLast Operation: Decrypt\nImage URI: " + textTargetURI;
                    Info.setText(info);
                } else {
                    Toast.makeText(MainActivity.this, "Please select an Image first!", Toast.LENGTH_SHORT).show();
                }


            }
        });

    }


    /** PERMISSION STUFF*/
    // This method is used to check whether current app has required runtime permission.
    private boolean hasRuntimePermission(Context context, String runtimePermission) {
        boolean ret = false;

        // Get current android os version.
        int currentAndroidVersion = Build.VERSION.SDK_INT;

        // Build.VERSION_CODES.M's value is 23.
        if (currentAndroidVersion > Build.VERSION_CODES.M) {
            // Only android version 23+ need to check runtime permission.
            if (ContextCompat.checkSelfPermission(context, runtimePermission) == PackageManager.PERMISSION_GRANTED) {
                ret = true;
            }
        } else {
            ret = true;
        }
        return ret;
    }

    /* Request app user to allow the needed runtime permission.
       It will popup a confirm dialog , user can click allow or deny. */
    private void requestRuntimePermission(Activity activity, String runtimePermission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{runtimePermission}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // If this is our permission request result.
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                // Construct result message.
                StringBuffer msgBuf = new StringBuffer();
                int grantResult = grantResults[0];
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    //msgBuf.append("You granted below permissions, you can do the action again to use the permission : ");
                } else {
                    msgBuf.append("You denied below permissions : ");
                }

                // Add granted permissions to the message.
                if (permissions != null) {
                    int length = permissions.length;
                    for (int i = 0; i < length; i++) {
                        String permission = permissions[i];
                        msgBuf.append(permission);

                        if (i < length - 1) {
                            msgBuf.append(",");
                        }
                    }
                }

                // Show result message.
                Toast.makeText(getApplicationContext(), msgBuf.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    /**PERMISSION STUFF END*/


    /** IMAGE LOADER*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri targetURI = data.getData();
            textTargetURI = targetURI.toString();
            try {

                bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(targetURI));
                bitmapCopy = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                targetImage.setImageBitmap(bitmap);
                height = bitmap.getHeight();
                width = bitmap.getWidth();
                String info = "Height = " + height + " pixels\nWidth = " + width + " pixels\nLast Operation: Load Image\nImage URI: " + textTargetURI;
                Info.setText(info);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Toast.makeText(MainActivity.this, "Image loaded Successfully", Toast.LENGTH_SHORT).show();
        }
    }


    /** refresh the gallery */
    private void addImageGallery( File file ) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // setar isso
        getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }
}

