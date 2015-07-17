package com.joelbryceanderson.dastranslater;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;



import com.googlecode.tesseract.android.TessBaseAPI;
import com.gtranslate.Language;
import com.gtranslate.Translator;


/**
 * Created by JAnderson on 7/14/15.
 */
public class CameraActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mPreview;
    private int translateFrom;
    private int translateTo;
    private String toTranslate;
    private ProgressBar mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);

        }
        translateFrom = getIntent().getIntExtra("fromLang", 2);
        translateTo = getIntent().getIntExtra("toLang", 0);

        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        mProgress.setVisibility(View.GONE);

        initializeCamera();

        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.camera_fab);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
                mProgress.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mPreview.getHolder().removeCallback(mPreview);
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCamera == null) {
            initializeCamera();
        }
    }

    void initializeCamera() {
        mCamera = getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        Camera.Parameters params = mCamera.getParameters();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(params);
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open();
        }
        catch (Exception e){
        }
        return c;
    }

    void findText(Bitmap bitmap){
        String DATA_PATH = Environment.getExternalStorageDirectory() + "";
        String lang = "";
        if (translateFrom == 0){
            DATA_PATH += "/eng";
            lang = "eng";
        } else if (translateFrom == 1) {
            DATA_PATH += "/frn";
            lang = "fra";
        } else if (translateFrom == 2) {
            DATA_PATH += "/spn";
            lang = "spa";
        }
        TessBaseAPI baseApi = new TessBaseAPI();
        baseApi.init(DATA_PATH, lang);
        baseApi.setVariable("tessedit_char_whitelist",
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        baseApi.setImage(bitmap);
        toTranslate = baseApi.getUTF8Text();
        baseApi.end();
        new GetTranslation().execute("");
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options op = new BitmapFactory.Options();
            op.inPreferredConfig = Bitmap.Config.ARGB_8888;
            op.inSampleSize = 6;
            Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length, op);
            findText(bm);
        }
    };

    private class GetTranslation extends AsyncTask<String, Void, String> {

        private String translated = "";

        @Override
        protected String doInBackground(String... params) {
            toTranslate = toTranslate.replace("\n", " ");
            Translator translate = Translator.getInstance();
            String from = Language.SPANISH;
            String to = Language.ENGLISH;
            if (translateFrom == 0) {
                from = Language.ENGLISH;
            } else if (translateFrom == 1) {
                from = Language.FRENCH;
            } else if (translateFrom == 2) {
                from = Language.SPANISH;
            }
            if (translateTo == 0) {
                to = Language.ENGLISH;
            } else if (translateTo == 1) {
                to = Language.FRENCH;
            } else if (translateTo == 2) {
                to = Language.SPANISH;
            }
            try {
                translated = translate.translate(toTranslate, from, to);
            } catch (Exception e){
                translated += e.toString();
            }
            return translated;
        }

        @Override
        protected void onPostExecute(String result) {
            mProgress.setVisibility(View.GONE);
            new AlertDialog.Builder(CameraActivity.this)
                    .setTitle("Translation Complete")
                    .setMessage("Original Text:\n" + toTranslate
                            + "\nTranslated Text:\n"
                            + translated)
                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            mCamera.startPreview();
                        }
                    }).show();
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }
}