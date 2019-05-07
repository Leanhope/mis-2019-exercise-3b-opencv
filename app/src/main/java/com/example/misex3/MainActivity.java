package com.example.misex3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

//Oriented at https://www.mirkosertic.de/blog/2013/07/realtime-face-detection-on-android-using-opencv/

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OCVSample::Activity";

    private CameraBridgeViewBase    mOpenCvCameraView;
    private CascadeClassifier cal;
    private int absoluteFaceSize;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    //using just a haarcascade to detect faces from the front
                    String asset = initAssetFile("haarcascade_frontalface_default.xml");

                    try{
                    cal =  new CascadeClassifier(asset);
                    } catch (Exception e) {
                        Log.e("OpenCVActivity", "Error loading cascade", e);
                    }
                    cal.load(asset);
                    if( !cal.load( asset ) ){ System.out.println("--(!)Error loading\n"); return; };
                    mOpenCvCameraView.enableView();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_main);

        // before opening the CameraBridge, we need the Camera Permission on newer Android versions

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("REQUESTING PERMISSION");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0x123);
        }
        init();
    }

    public void init(){
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.misExerciseView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        //using front camera for easier testing
        mOpenCvCameraView.setCameraIndex(1);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        absoluteFaceSize = (int) (height * 0.2);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "OpenCV library found inside package. Using it!");
                mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            }
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat col = inputFrame.rgba();
        Mat gray = inputFrame.gray();
        Imgproc.cvtColor(col, gray, Imgproc.COLOR_RGBA2RGB);

        MatOfRect faces = new MatOfRect();
        // Use the classifier to detect faces
        cal.detectMultiScale(gray, faces, 1.1, 2, 2,
                new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        // If there are any faces found, draw a red circle slightly below the center
        Rect[] facesArray = faces.toArray();
        System.out.println(facesArray.length);
        for(int i = 0; i < facesArray.length; i++) {
            //Imgproc.rectangle(col, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
            double edgeLength = facesArray[i].br().x - facesArray[i].tl().x;
            Point center = new Point((facesArray[i].tl().x + facesArray[i].br().x) / 2, (facesArray[i].tl().y + facesArray[i].br().y) / 2 + 0.1*edgeLength);
            int r = (int)(edgeLength)/10;
            Imgproc.circle(col, center, r, new Scalar(255, 0, 0), -1);
        }
        return col;
    }

    public String initAssetFile(String filename)  {
        File file = new File(getFilesDir(), filename);
        if (!file.exists()) try {
            InputStream is = getAssets().open(filename);
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[is.available()];
            is.read(data); os.write(data); is.close(); os.close();
        } catch (IOException e) { e.printStackTrace(); }
        Log.d(TAG,"prepared local file: "+filename);
        return file.getAbsolutePath();
    }
}
