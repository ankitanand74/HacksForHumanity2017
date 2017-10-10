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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView txtSpeechInput;
    private TextToSpeech responseSpeaker;
    public static final String TAG = "MainActivity";
    RequestQueue mRequestQueue;
    //final String serverURL ="http://10.142.203.154:8080/hello-world";
    final String serverURL ="http://ec2-54-172-72-28.compute-1.amazonaws.com:8080/hello-world";
    final Handler handler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle("HelpingHands");

        mRequestQueue = Volley.newRequestQueue(this);
        txtSpeechInput = (TextView) findViewById(R.id.showSpeechToText);

    }

    @Override
    protected void onResume() {
        super.onResume();
        responseSpeaker=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    responseSpeaker.setLanguage(Locale.US);
                }
            }
        });
    }

    @Override
    protected void onStop () {
        super.onStop();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }

        if(responseSpeaker !=null){
            responseSpeaker.stop();
            responseSpeaker.shutdown();
        }
    }

    public void onClickVolunteerButton(View v) {

        //TODO volunteer call to server
        makePostRequest("I want to help");
    }

    public void onClickNeedHelp(View v){

        //TODO volunteer call to server
        makePostRequest("I need help");

    }

    public void onClickVoiceButton(View v){

        promptSpeechInput();

    }

    private void speakUp(JSONObject response){
        String speakResponse = null;
        String whereToGO = null;
        Object value1 = null;
        Object value2 = null;
        try {
            value1 = response.get("id");
            value2 = response.get("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(value1 != null){
            whereToGO = value1.toString();
        }

        Log.d(TAG, "whereToGO "+whereToGO);

        if (value2 != null) {
            speakResponse = value2.toString();
        }
        Log.d(TAG, "speakResponse "+ speakResponse);

        //Toast.makeText(getApplicationContext(), speakResponse,Toast.LENGTH_SHORT).show();
        txtSpeechInput.setText(speakResponse);
        responseSpeaker.speak(speakResponse, TextToSpeech.QUEUE_FLUSH, null);

        if(whereToGO !=null) {
            if (whereToGO.equals("12")) {
                Context context = MainActivity.this;
                Class destinationActivity = FormsActivity.class;
                Intent startFormActivityIntent = new Intent(context, destinationActivity);
                startActivity(startFormActivityIntent);
            } else if (whereToGO.equals("13")) {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Context context = MainActivity.this;
                        Class destinationActivity = GetHelpActivity.class;
                        Intent startFormActivityIntent = new Intent(context, destinationActivity);
                        startActivity(startFormActivityIntent);
                    }
                }, 3000);

            }
        }

    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    //txtSpeechInput.setText(result.get(0));
                    makePostRequest(result.get(0));
                }
                break;
            }

        }
    }

    void makePostRequest(String data){

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<>();
        params.put("id", "1");
        params.put("content", data);

        JsonObjectRequest request_json = new JsonObjectRequest(serverURL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Process os success response
                            //txtSpeechInput.setText(response.toString());
                            speakUp(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtSpeechInput.setText("OOPS Something's Wrong with your Internet");
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        mRequestQueue.add(request_json);

    }

}