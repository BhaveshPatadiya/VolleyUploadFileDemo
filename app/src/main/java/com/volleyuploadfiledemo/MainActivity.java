package com.volleyuploadfiledemo;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.volleyuploadfiledemo.databinding.ActivityMainBinding;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import network.NetworkUtility;
import network.Volley;
import network.VolleyNetworkRequest;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    ActivityMainBinding binding;
    public static final int REQUEST_GET_FILE = 101;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 102;

    ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setTest(this);
    }

    /**
     * On Click of Search
     */
    public void onClickOfChooseFile(View view) {
        Log.i(TAG, "onClickOfChooseFile: ");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);

            } else {

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            }

        } else {
            //Go ahead with file choosing
            startIntentFileChooser();
        }
    }

    /**
     * OnClick of Done
     */
    public void OnClickOfDone(View view) {
        Log.i(TAG, "OnClickOfDone: ");
        if (!TextUtils.isEmpty(binding.txtFilename.getText().toString())) {
            if (Utility.isConnected(MainActivity.this)) {
                askQuestion();
            } else {
                Snackbar.make(binding.root, "Please check internet connection.", 1000).show();
            }

        } else {
            Snackbar.make(binding.root, "Please select file first!!", 1000).show();
        }
    }

    private void askQuestion() {
        HashMap<String, File> mFileContent = new HashMap<>();
        mFileContent.put("file", new File(binding.txtFilename.getText().toString()));

        Map<String, String> mParams = new HashMap<>();
        mParams.put("subjectid", "2");
        mParams.put("questiontext", "This is Test");
        mParams.put("addby", "39");

        showProgressDialog();

        VolleyNetworkRequest mVolleyNetworkRequest = new VolleyNetworkRequest(NetworkUtility.ASK_QUESTION, mErrorListener, mListener, null, mParams, mFileContent, null, false);
        Volley.getInstance(this).addToRequestQueue(mVolleyNetworkRequest);

    }


    public void startIntentFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_GET_FILE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GET_FILE && resultCode == RESULT_OK) {
            Log.i(TAG, "onActivityResult: " + data.getData().toString());
            String path = Utility.getPath(this, data.getData());
            binding.txtFilename.setText(path);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Granted");
                startIntentFileChooser();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Log.i(TAG, "onRequestPermissionsResult: Permission Denied");
                Snackbar.make(binding.root, "You Denied the permission request", 3000).show();
            }
        }
    }

    private Response.ErrorListener mErrorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            Log.d(TAG, "onErrorResponse() called with: " + "error = [" + error + "]");
            Toast.makeText(MainActivity.this, "Ooops..Something went wrong... Error: " + error.toString(), Toast.LENGTH_LONG).show();
        }
    };

    private Response.Listener mListener = new Response.Listener() {
        @Override
        public void onResponse(Object response) {
            Log.d(TAG, "onResponse() called with: " + "response = [" + response + "]");
            hideProgressDialog();
            Toast.makeText(MainActivity.this, "Question Successfully posted!!", Toast.LENGTH_LONG).show();
        }
    };

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Loading..");
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
            mProgressDialog = null;
        }
    }
}
