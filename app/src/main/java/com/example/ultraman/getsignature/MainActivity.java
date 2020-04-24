package com.example.ultraman.getsignature;



import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.location.LocationManager.NETWORK_PROVIDER;


public class MainActivity extends AppCompatActivity {
    static String ConvertedBitmap;

    Button btn_get_sign, mClear, mGetSign, mCancel;
    ImageView img_sig;

    Dialog dialog;
    LinearLayout mContent;
    View view;
    MainActivity.signature mSignature;
    Bitmap bitmap;

    AlertDialog.Builder builder;

    LinearLayout layout;

    private int STORAGE_PERMISSION_CODE = 1;
    private int Contact_PERMISSION_CODE = 1;
    private int location_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
//            }
//
//        }
        Storagepermission();
        Contactpermission();

        img_sig = (ImageView) findViewById(R.id.img_sig);

        layout = (LinearLayout) findViewById(R.id.linearLayout);
        layout = (LinearLayout) findViewById(R.id.layout);

        btn_get_sign = (Button) findViewById(R.id.btn_get_sig);
        dialog = new Dialog(MainActivity.this);
        // Removing the features of Normal Dialogs
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_signature);
        dialog.setCancelable(true);

        btn_get_sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConvertedBitmap = "";
                dialog_action();
            }
        });
    }//oncreate


    // Function for Digital Signature
    public void dialog_action() {

        mContent = (LinearLayout) dialog.findViewById(R.id.linearLayout);
        mSignature = new MainActivity.signature(getApplicationContext(), null);
        mSignature.setBackgroundColor(Color.WHITE);
        // Dynamically generating Layout through java code
        mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mClear = (Button) dialog.findViewById(R.id.clear);
        mGetSign = (Button) dialog.findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button) dialog.findViewById(R.id.cancel);
        view = mContent;

        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //Log.v("log_tag", "Panel Cleared");
                mSignature.clear();
                bitmap = null;
                img_sig.setImageDrawable(null);
            }
        });

        mGetSign.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Creating Separate Directory for saving Generated Images
                String DIRECTORY = Environment.getExternalStorageDirectory().getPath() + "/DCIM/CREAMLINE/";
                String pic_name = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String StoredPath = DIRECTORY + pic_name + ".png";
                //save to static string
                ConvertedBitmap = StoredPath;
                // Method to create Directory, if the Directory doesn't exists
                File file = new File(DIRECTORY);
                if (!file.exists()) {
                    file.mkdir();
                    Toast.makeText(getApplicationContext(), "Folder created", Toast.LENGTH_SHORT).show();
                    Snackbar snackbar = Snackbar.make(layout, "Folder created successfully!", Snackbar.LENGTH_LONG);
                    snackbar.show();

                }
                Log.v("log_tag", "Panel Saved");
                view.setDrawingCacheEnabled(true);

                mSignature.save(view, StoredPath);
                dialog.dismiss();

                if (img_sig.equals("")) {

                    builder.setTitle("Reminder!");
                    builder.setMessage("Please make sure all required fields are not empty. Before getting the driver's Signature");
                    builder.setIcon(R.drawable.ic_android_black_24dp);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                        }
                    });
                    builder.show();

                } else {
                    Snackbar snackbar = Snackbar.make(layout, "Signature saved successfully!", Snackbar.LENGTH_LONG);
                    snackbar.show();
                    //  selectImage1();
                    File imgFile = new File(ConvertedBitmap);
                    bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    ImageView myImage = (ImageView) findViewById(R.id.img_sig);
                    myImage.setImageBitmap(bitmap);
                    if (imgFile.exists()) {


                    }
                }


            }
        });

        mCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.v("log_tag", "Panel Canceled");
                if (img_sig.equals(null)) {
                    mSignature.clear();
                    dialog.dismiss();
                } else {
                    dialog.dismiss();

                }
            }
        });
        dialog.show();
    }

    //CONVERTIDOR
    public static byte[] imageViewToByte(ImageView image) {
        Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private String imageToString(Bitmap bitmap) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] imgBytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(imgBytes, Base64.DEFAULT);

    }

    static Canvas canvas;

    protected boolean isLocationEnabled() {
        String le = Context.LOCATION_SERVICE;
        LocationManager locationManager = (LocationManager) getSystemService(le);
        if (!locationManager.isProviderEnabled(NETWORK_PROVIDER)) {
            return false;
        } else {
            return true;
        }
    }

    public class signature extends View {

        private static final float STROKE_WIDTH = 5f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();


        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v, String StoredPath) {
            Log.v("log_tag", "Width: " + v.getWidth());
            Log.v("log_tag", "Height: " + v.getHeight());
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
            }
            canvas = new Canvas(bitmap);
            try {
                // Output the file
                FileOutputStream mFileOutStream = new FileOutputStream(StoredPath);
                v.draw(canvas);

                // Convert the output file to Image such as .png
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();
            } catch (Exception e) {
                Log.v("log_tag", e.toString());
            }

        }

        public void clear() {
            path.reset();
            invalidate();
            mContent.removeAllViews();
            mContent = (LinearLayout) dialog.findViewById(R.id.linearLayout);
            mSignature = new MainActivity.signature(getApplicationContext(), null);
            mSignature.setBackgroundColor(Color.WHITE);
            // Dynamically generating Layout through java code
            mContent.addView(mSignature, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {

            Log.v("log_tag", string);

        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }


    }




    //storage permission
    public void Storagepermission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(MainActivity.this, "storage permission granted", Toast.LENGTH_SHORT).show();
        } else {

            requestStoragePermission();
        }

    }


    public void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this).setTitle("Storage Permission needed!").setMessage("This permission is needed")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);


                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();


        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }

    }


//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        if (requestCode == STORAGE_PERMISSION_CODE){
//            if (grantResults.length > 0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
//                Toast.makeText(MainActivity.this, "storage permission is already granted",Toast.LENGTH_LONG).show();
//            }else {
//
//                Toast.makeText(MainActivity.this, "storage permission is denied",Toast.LENGTH_LONG).show();
//            }
//        }
//    }
    //contact permision

    public void Contactpermission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

             Toast.makeText(MainActivity.this, "Read Contact permission granted",Toast.LENGTH_SHORT).show();
        } else {

            requestContactPermission();
        }

    }
    private String mCurrentPhotoPath;
    private static final int REQUEST_CODE = 1;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*Photo 1*/
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE ) {
            Bitmap  mImageBitmap;
            try {
                mImageBitmap   = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.parse(mCurrentPhotoPath));
                img_sig.setImageBitmap(Bitmap.createScaledBitmap(mImageBitmap,1500,1500,false));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void requestContactPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {

            new AlertDialog.Builder(this).setTitle("Read Contact Permission needed!").setMessage("This permission is needed")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, Contact_PERMISSION_CODE);


                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();


        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, Contact_PERMISSION_CODE);
        }

    }
    //location permission

    //storage permission
    public void Locationpermission() {

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(MainActivity.this, "Location permission granted", Toast.LENGTH_LONG).show();
        } else {

            requestLocationPermission();
        }

    }


    public void requestLocationPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this).setTitle("Location Permission needed!").setMessage("This permission is needed")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, location_PERMISSION_CODE);


                        }
                    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).create().show();


        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, location_PERMISSION_CODE);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {



        if (requestCode == STORAGE_PERMISSION_CODE) {


            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "storage permission is granted", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(MainActivity.this, "storage permission is denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == Contact_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "access contacts permission is granted", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(MainActivity.this, "contact permission is denied", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == location_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "location permission is granted", Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(MainActivity.this, "location permission is denied", Toast.LENGTH_LONG).show();
            }
        }
    }

}

