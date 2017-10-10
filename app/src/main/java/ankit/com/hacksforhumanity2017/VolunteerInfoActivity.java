/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ankit.com.hacksforhumanity2017;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telecom.Call;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class VolunteerInfoActivity extends AppCompatActivity {

    public static final String TAG = "MainActivity";
    RequestQueue mRequestQueue;
    final String serverURL ="http://ec2-54-172-72-28.compute-1.amazonaws.com:8080/hello-world";
    private String youtubeLink = "";
    private String address = "";
    private String phoneNumber= "";
    private String skypeID= "";
    private String subCategory= "";
    private Button yButton;
    private Button aButton;
    private Button cButton;
    private Button sButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_volunteer_info);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("We have all the help right here !!");
        }

        yButton = (Button) findViewById(R.id.youTubeLink);
        aButton = (Button) findViewById(R.id.show_on_map_button);
        cButton = (Button) findViewById(R.id.call_nearest_helping_hand);
        sButton = (Button) findViewById(R.id.skype_call);

        mRequestQueue = Volley.newRequestQueue(this);
        makePostRequest();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    void makePostRequest(){

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<>();
        params.put("id", "5");
        params.put("content", "something");

        JsonObjectRequest request_json = new JsonObjectRequest(serverURL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d(TAG,"response "+response.toString());
                            Object value1 = null;

                            try {
                                value1 = response.get("content");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String splitString = "";

                            if(value1 != null){
                                splitString = value1.toString();
                            }

                            Log.d(TAG, "splitString "+ splitString);//bully, phoneno, skype, address
                            String[] separatedStrings = splitString.split("!");
                            boolean entryNotPresent = false;
                            if(separatedStrings[0].equals("null")) {
                                entryNotPresent =true;
                            }else if(separatedStrings[1].equals("null")) {
                                entryNotPresent =true;
                            }else if(separatedStrings[1].equals("null")) {
                                entryNotPresent =true;
                            }else if(separatedStrings[1].equals("null")) {
                                entryNotPresent =true;
                            }
                            if(entryNotPresent){
                                yButton.setEnabled(false);
                                aButton.setEnabled(false);
                                cButton.setEnabled(false);
                                sButton.setEnabled(false);
                                Toast.makeText(getApplicationContext(), "OOPS!! We don't have a volunteer available in that category",Toast.LENGTH_LONG).show();
                            }else {
                                subCategory = separatedStrings[0];
                                phoneNumber = separatedStrings[1];
                                skypeID = separatedStrings[2];
                                address = separatedStrings[3];
                            }
                            Log.d(TAG, "subCategory " + subCategory + "phoneNumber "+phoneNumber+"skypeID "+skypeID+"address "+address);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), "OOPS!! Something's Wrong with your Internet",Toast.LENGTH_SHORT).show();
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        mRequestQueue.add(request_json);

    }

    public void onClickLaunchYoutubeButton(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //intent.setData(Uri.parse("https://www.youtube.com/watch?v=gZ2AL5pALdU"));
        switch (subCategory) {
            case "bully":
                youtubeLink = "https://www.youtube.com/watch?v=gZ2AL5pALdU";
                break;
            case "science":
                youtubeLink = "https://www.youtube.com/watch?v=MYuh5yErdfA";
                break;
            case "maths":
                youtubeLink = "https://www.youtube.com/watch?v=EPwoM9azzcA";
                break;
            case "theft":
                youtubeLink = "https://www.youtube.com/watch?v=5zImN0GLX4o";
                break;
        }
        intent.setData(Uri.parse(youtubeLink));
        intent.setPackage("com.google.android.youtube");
        startActivity(intent);
    }

    public void onClickShowMapButton(View v){

        //String addressString = "1265 East University Drive, AZ";
        String addressString = address;
        String map = "http://maps.google.co.in/maps?q=" + addressString;

        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(map));
        startActivity(i);
    }

    public void onClickCallButton(View v){

        //String phoneNumberString = "4804344681";
        String phoneNumberString = phoneNumber;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumberString));
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.CALL_PHONE ) != PackageManager.PERMISSION_GRANTED ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ActivityCompat.requestPermissions( this, new String[] {  Manifest.permission.CALL_PHONE  }, Call.STATE_CONNECTING
                );
            }
        }
        startActivity(intent);
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + phoneNumberString));
        startActivity(callIntent);

    }

    public void onClickSkypeButton(View v){

        //String user_name = "pkgishere";
        String user_name = skypeID;
        Intent sky = new Intent("android.intent.action.VIEW");
        sky.setData(Uri.parse("skype:" + user_name));
        startActivity(sky);

    }

}