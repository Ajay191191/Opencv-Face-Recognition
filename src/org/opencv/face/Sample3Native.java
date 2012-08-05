package org.opencv.face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

public class Sample3Native extends Activity {
    private static final String TAG = "Sample::Activity";
    private Sample3View mView;
    Button ok_button,click_button,cancel_button,try_again_button;
    ProgressDialog pg;
    Bitmap bmp;
    int count;
//    ArrayList<String> face_db;
    
    private class ButtonListener implements View.OnClickListener{
   	 
        public void onClick(View v) {
            if(v.equals(findViewById(R.id.capture_button))){
                pg=ProgressDialog.show(Sample3Native.this, null, "Capturing Image..");
                pg.show();
            	mView.mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }else if(v.equals(findViewById(R.id.cancel_button))){
                finish();
            }else if(v.equals(findViewById(R.id.ok_button))){
            	saveImage();
            	Toast.makeText(getApplicationContext(), "Saved File", Toast.LENGTH_SHORT).show();
            	MainActivity.pictureTaken=true;
            	Intent i = new Intent(Sample3Native.this,MainActivity.class);
            	startActivity(i);
            }else if(v.equals(findViewById(R.id.recapture_button))){
                findViewById(R.id.img).setVisibility(View.GONE);
                findViewById(R.id.preview).setVisibility(View.VISIBLE);
               // mView.mCamera.grab();
                ok_button.setVisibility(View.GONE);
                cancel_button.setVisibility(View.VISIBLE);
                try_again_button.setVisibility(View.GONE);
                click_button.setVisibility(View.VISIBLE);
            }
        }
 
    }

    
    
    private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
    	@Override
    	public void onManagerConnected(int status) {
    		switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
					
					// Load native library after(!) OpenCV initialization
					System.loadLibrary("native_sample");
					
					// Create and set View
					mView = new Sample3View(mAppContext);
					//setContentView(mView);
					
					setContentView(R.layout.main);
					((FrameLayout) findViewById(R.id.preview)).addView(mView);
					findViewById(R.id.preview).setVisibility(View.VISIBLE);
			        findViewById(R.id.img).setVisibility(View.GONE);
			 
			        ButtonListener listener=new ButtonListener();
			        click_button=((Button) findViewById(R.id.capture_button));
			        click_button.setVisibility(View.VISIBLE);
			        click_button.setOnClickListener(listener);
			 
			        try_again_button=((Button) findViewById(R.id.recapture_button));
			        try_again_button.setVisibility(View.GONE);
			        try_again_button.setOnClickListener(listener);
			 
			        cancel_button=((Button) findViewById(R.id.cancel_button));
			        cancel_button.setVisibility(View.VISIBLE);
			        cancel_button.setOnClickListener(listener);
			 
			        ok_button=((Button) findViewById(R.id.ok_button));
			        ok_button.setVisibility(View.GONE);
			        ok_button.setOnClickListener(listener);
			
			        count=0;
//			        face_db = MainActivity.faces.get(MainActivity.current_name);
					
					// Check native OpenCV camera
					if( !mView.openCamera() ) {
						AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
						ad.setCancelable(false); // This blocks the 'BACK' button
						ad.setMessage("Fatal error: can't open camera!");
						ad.setButton("OK", new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						    }
						});
						ad.show();
					}
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
    	}
	};

    public Sample3Native() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
	protected void onPause() {
        Log.i(TAG, "onPause");
		super.onPause();
		if (null != mView)
			mView.releaseCamera();
	}

	@Override
	protected void onResume() {
        Log.i(TAG, "onResume");
		super.onResume();
		if((null != mView) && !mView.openCamera() ) {
			AlertDialog ad = new AlertDialog.Builder(this).create();  
			ad.setCancelable(false); // This blocks the 'BACK' button  
			ad.setMessage("Fatal error: can't open camera!");  
			ad.setButton("OK", new DialogInterface.OnClickListener() {  
			    public void onClick(DialogInterface dialog, int which) {  
				dialog.dismiss();
				finish();
			    }  
			});  
			ad.show();
		}
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
        	Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
    }
    
    

    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            //Log.d(TAG, "onShutter'd");
            System.out.println("In ShutterCallback");
        }
    };
 
    /** Handles data for raw picture */
    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if(data!=null){
                bmp=BitmapFactory.decodeByteArray(data,0,data.length);
                findViewById(R.id.img).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.img)).setImageBitmap(bmp);
                findViewById(R.id.preview).setVisibility(View.GONE);
                if(pg!=null)
                    pg.dismiss();
                ok_button.setVisibility(View.VISIBLE);
                click_button.setVisibility(View.GONE);
                try_again_button.setVisibility(View.VISIBLE);
            }
        }
    };
 
    /** Handles data for jpeg picture */
    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            if(data!=null){
                bmp=BitmapFactory.decodeByteArray(data,0,data.length);
                findViewById(R.id.img).setVisibility(View.VISIBLE);
                ((ImageView)findViewById(R.id.img)).setImageBitmap(bmp);
                findViewById(R.id.preview).setVisibility(View.GONE);
 
                if(pg!=null)
                    pg.dismiss();
                ok_button.setVisibility(View.VISIBLE);
                click_button.setVisibility(View.GONE);
                try_again_button.setVisibility(View.VISIBLE);
            }
        }
    };
 
    public void saveImage(){
        FileOutputStream out;
        
        try {
            File file=new File(MainActivity.working_Dir,MainActivity.current_name +".jpg");
//            face_db.add(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+MainActivity.current_name+".jpg");
            if(!file.exists()) file.createNewFile();
            out = new FileOutputStream(file);
            bmp.compress(CompressFormat.JPEG, 90, out);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    
}
