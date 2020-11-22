package idm.idm;

import android.content.Context;
import android.content.Intent;
//import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.Date;
import java.text.SimpleDateFormat;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONException;

//import org.opencv.android.BaseLoaderCallback;
//import org.opencv.android.CameraBridgeViewBase;
//import org.opencv.android.JavaCameraView;
//import org.opencv.android.LoaderCallbackInterface;
//import org.opencv.android.OpenCVLoader;
//import org.opencv.core.Mat;
//import org.opencv.core.MatOfRect;
//import org.opencv.core.Point;
//import org.opencv.core.Rect;
//import org.opencv.core.Scalar;
//import org.opencv.imgproc.Imgproc;
//import org.opencv.objdetect.CascadeClassifier;

import java.io.IOException;

import idm.idm.servercom.FaceRecognizer;
import idm.idm.servercom.Server;

public class LoginActivity extends AppCompatActivity  {

    private EditText Name;
    private EditText Password;
    private TextView Info;
    private Button Login;
    private ImageButton LoginFace;
    private TextView Create;
    private int counter = 3;
    private Date lockTime;
    //JavaCameraView javaCameraView;
    File cascFile;
    //CascadeClassifier faceDetector;
   // private Mat mRgba, mGrey;

    private String currentPhotoPath;
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_login);

        Name = (EditText)findViewById((R.id.loginUserName));
        Password = (EditText) findViewById(R.id.password);
        Info = (TextView)findViewById(R.id.logInfo);

        Login = (Button)findViewById(R.id.loginButton);
        LoginFace = (ImageButton)findViewById((R.id.faceID));
        Create = (TextView) findViewById(R.id.createAccount);

//        javaCameraView = (JavaCameraView)findViewById(R.id.javaCamView);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validID(Name.getText().toString(), Password.getText().toString());

                System.out.println("reached 1");
            }
        });

        LoginFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fileName = "photo";
                File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                try {
                    imageFile = File.createTempFile(fileName, ".jpg", storageDirectory);
                    currentPhotoPath = imageFile.getAbsolutePath();

                    Uri imageUri = FileProvider.getUriForFile(LoginActivity.this,
                            "idm.idm.provider", imageFile);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent,1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent toRegister = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(toRegister);
            }
        });

        //Camera view
//        if(!OpenCVLoader.initDebug()){
//            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION,this,baseCallback);
//        }
//        else{
//            try {
//                baseCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        javaCameraView.setCvCameraViewListener(this);

    }

    private void validID(String userName, String userPass) {

        if(counter > 0 && Server.SERVER.login(userName,userPass)) {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        else {
            counter--;
            Info.setText("ATTEMPTS REMAINING: " + String.valueOf(counter));

            //SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if(counter == 0){
                lockTime = new Date();
                //Login.setEnabled(false);
                Info.setText("Try 2 minutes later.");
            }
            else if (counter < 0) {

                Date currentTime = new Date();
                long remainingTime = currentTime.getTime() - lockTime.getTime();
                int minutes = (int) ((remainingTime / (1000*60)) % 60);

                if (remainingTime > 12000) {
                    counter = 3;
                    if (Server.SERVER.login(userName,userPass)) {
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }else{
                        counter --;
                        Info.setText("ATTEMPTS REMAINING: " + String.valueOf(counter));
                    }
                }
                else {
                    //Login.setEnabled(false);
                    Info.setText("Try " + String.valueOf(minutes) + " minutes later.");
                }
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String username = Name.getText().toString();

        if (requestCode == 1 && resultCode == RESULT_OK ) {

            if (FaceRecognizer.FACERECOGNIZER.Authenticate(imageFile, username)) {
                Intent toHome2 = new Intent(LoginActivity.this, HomeActivity.class);
                toHome2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(toHome2);
            }
            else {
                System.out.println("Error authenticating.");
                counter--;

                Info.setText("Attempts Remaining: " + String.valueOf(counter));
                if(counter == 0){
                    Login.setEnabled(false);
                    Info.setText("Try again later.");
                }
            }
        }
    }

  
      //Camera
//    @Override
//    public void onCameraViewStarted(int width, int height) {
//        mRgba = new Mat();
//        mGrey = new Mat();
//    }
//
//    @Override
//    public void onCameraViewStopped() {
//        mRgba.release();
//        mGrey.release();
//    }
//
//    @Override
//    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
//        mRgba = inputFrame.rgba();
//        mGrey = inputFrame.gray();
//
//        //detect face
//        MatOfRect faceDetections = new MatOfRect();
//        faceDetector.detectMultiScale(mRgba,faceDetections);
//
//
//        for(Rect rect: faceDetections.toArray()){
//            Imgproc.rectangle(mRgba, new Point(rect.x, rect.y),
//                    new Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(255,0,0));
//        }
//        return mRgba;
//    }
//
//    private BaseLoaderCallback baseCallback = new BaseLoaderCallback(this) {
//        @Override
//        public void onManagerConnected(int status) throws IOException {
//            switch (status){
//                case LoaderCallbackInterface.SUCCESS:{
//                    InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
//                    File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
//                    cascFile = new File(cascadeDir,"haarcascade_frontalface_alt2.xml");
//
//                    FileOutputStream fos = new FileOutputStream(cascFile);
//
//                    byte[] buffer = new byte[4096];
//                    int bytesRead;
//
//                    while((bytesRead = is.read(buffer))!=-1){
//                        fos.write(buffer,0,bytesRead);
//                    }
//
//                    is.close();
//                    fos.close();
//
//                    faceDetector = new CascadeClassifier(cascFile.getAbsolutePath());
//
//                    if(faceDetector.empty()){
//                        faceDetector = null;
//                    }
//                    else{
//                        cascadeDir.delete();
//                    }
//                    javaCameraView.enableView();
//                }
//                break;
//
//                default: {
//                    super.onManagerConnected(status);
//                }
//                break;
//            }
//
//        }
//    };

}
