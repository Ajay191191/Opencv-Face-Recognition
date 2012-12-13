package org.opencv.face;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import org.opencv.objdetect.CascadeClassifier;


import android.content.Context;
import android.graphics.Bitmap;

class Sample3View extends SampleViewBase {
	
	private int mFrameSize;
	private Bitmap mBitmap;
	private int[] mRGBA;
	

	public static File mCascadeFile;
	
    public Sample3View(Context context){
        super(context);
        try{
        InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
        File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
        mCascadeFile = new File(MainActivity.working_Dir, "haarcascade_frontalface_alt2.xml");
        FileOutputStream os = new FileOutputStream(mCascadeFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        is.close();
        os.close();
        }catch(Exception e){
        	e.printStackTrace();
        }

    }

	@Override
	protected void onPreviewStarted(int previewWidtd, int previewHeight) {
		mFrameSize = previewWidtd * previewHeight;
		mRGBA = new int[mFrameSize];
		mBitmap = Bitmap.createBitmap(previewWidtd, previewHeight, Bitmap.Config.ARGB_8888);
	}

	@Override
	protected void onPreviewStopped() {
		if(mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}
		mRGBA = null;	
	}

    @Override
    protected Bitmap processFrame(byte[] data) {
        int[] rgba = mRGBA;

        FindFeatures(getFrameWidth(), getFrameHeight(), data, rgba);

        Bitmap bmp = mBitmap; 
        bmp.setPixels(rgba, 0/* offset */, getFrameWidth() /* stride */, 0, 0, getFrameWidth(), getFrameHeight());
        return bmp;
    }

    public native void FindFeatures(int width, int height, byte yuv[], int[] rgba);
    public native void FindFaces(String imageName,String FileName);
    public native int Find(String imageName,String FileName,String Csv);
}
