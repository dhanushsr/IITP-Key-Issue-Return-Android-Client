package com.iitp.iitpkeys;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class IssueReturn extends AppCompatActivity{

    TextView nameTextView, emailTextView, phoneTextView;
    private  String name, phone, email, roll, hash;
    private String qrKeyID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_issue_return);

        nameTextView = findViewById(R.id.textView_name);
        emailTextView = findViewById(R.id.textView_email);
        phoneTextView = findViewById(R.id.textView_phone);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        final String message = extras.getString("EXTRA_MESSAGE");
        name  = message.split("\n")[0];
        roll = message.split("\n")[1];
        hash = message.split("\n")[2];
        getData(message);
        nameTextView.setText(name);


    }

    private void getData(String message) {


        Map<String,String> params =new HashMap<>();
        params.put("APIKey", Constants.Token);
        params.put("message", message);

        CustomRequest jsonObjectRequest = new CustomRequest(Request.Method.POST,
                Constants.PEOPLE_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        try {
                            JSONObject jsonObject = response.getJSONObject("data");
                            name = jsonObject.getString("FullName");
                            phone = jsonObject.getString("Phone");
                            email = jsonObject.getString("Email");
                            nameTextView.setText(name);
                            phoneTextView.setText( phone);
                            emailTextView.setText(email);
                            ImageLoader mImageLoader = MySingleton.getInstance(getApplicationContext()).getImageLoader();
                            NetworkImageView avatar = findViewById(R.id.imageView_photo);
                            avatar.setImageUrl(Constants.ROOT_URL+"/"+jsonObject.getString("ImageURL"), mImageLoader);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });


        RequestQueue queue = MySingleton.getInstance(getApplicationContext()).getRequestQueue();
        queue.add(jsonObjectRequest);

    }

    public void onBackPressed() {
        this.startActivity(new Intent(IssueReturn.this, MainActivity.class));
    }

    private void getKeyID()
    {
        qrKeyID = "";
        IntentIntegrator qrScan = new IntentIntegrator(this);
        qrScan.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        qrScan.setCameraId(0);
        qrScan.setOrientationLocked(true);
        qrScan.setPrompt("Scan User QR Code");
        qrScan.setBeepEnabled(false);
        qrScan.setCaptureActivity(CaptureActivityPortrait.class);
        qrScan.setBarcodeImageEnabled(true);
        qrScan.initiateScan();
    }

    private void issueKey(String message)
    {

        getKeyID();
        Map<String, String> params = new HashMap<>();
        params.put("KeyNo", qrKeyID);
        params.put("message", message);
        params.put("APIKey", Constants.Token);

        CustomRequest jsonObjectRequest = new CustomRequest(Request.Method.POST, Constants.ISSUE_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Toast.makeText(getApplicationContext(),response.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        onBackPressed();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response Error", error.toString());
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        });


        RequestQueue queue = MySingleton.getInstance(getApplicationContext()).getRequestQueue();
        queue.add(jsonObjectRequest);
    }

    private void returnKey(String message) {

        getKeyID();
        Map<String, String> params = new HashMap<>();
        params.put("KeyNo", qrKeyID);
        params.put("message", message);
        params.put("APIKey", Constants.Token);


        CustomRequest jsonObjectRequest = new CustomRequest(Request.Method.POST, Constants.RETURN_URL, params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Toast.makeText(getApplicationContext(),response.getString("message"), Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        onBackPressed();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Response Error", error.toString());
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_LONG).show();
            }
        });


        RequestQueue queue = MySingleton.getInstance(getApplicationContext()).getRequestQueue();
        queue.add(jsonObjectRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this, MainActivity.class));

            } else {
                //if qr contains data
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                qrKeyID = result.getContents();

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}


