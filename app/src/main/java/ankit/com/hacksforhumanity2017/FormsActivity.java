package ankit.com.hacksforhumanity2017;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
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

public class FormsActivity extends AppCompatActivity {

    private final int REQ_CODE_SPEECH_INPUT = 100;
    private TextView txtSpeechInput;
    private TextToSpeech responseSpeaker;
    public static final String TAG = "VolunteerSignUpActivity";
    RequestQueue mRequestQueue;
    private EditText mEditNameText;
    private Spinner mSpinnerCategory;
    private Spinner mSpinnerSubCategory;
    private ImageView profilePic;
    private static final int CAMERA_REQUEST = 1888;
    final String serverURL ="http://ec2-54-172-72-28.compute-1.amazonaws.com:8080/hello-world";
    //final String serverURL ="http://10.142.203.154:8080/hello-world";
    private Bitmap profileData;
    boolean isNameEntered =true;
    boolean isCategorySelected =true;
    boolean isSubCategorySelected =true;
    private String name;
    final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forms);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Volunteer SignUp !!");
        }

        mRequestQueue = Volley.newRequestQueue(this);
        txtSpeechInput = (TextView) findViewById(R.id.showSpeechToText);
        mEditNameText = (EditText) findViewById(R.id.editName);
        mSpinnerCategory = (Spinner) findViewById(R.id.spinner_1);
        mSpinnerSubCategory = (Spinner) findViewById(R.id.spinner_2);
        profilePic=(ImageButton)findViewById(R.id.user_image);

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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        if (ev.getAction() == MotionEvent.ACTION_DOWN &&
                !getLocationOnScreen(mEditNameText).contains(x, y)) {
            InputMethodManager input = (InputMethodManager)
                    this.getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(mEditNameText.getWindowToken(), 0);
        }

        return super.dispatchTouchEvent(ev);
    }

    protected Rect getLocationOnScreen(EditText mEditText) {
        Rect mRect = new Rect();
        int[] location = new int[2];

        mEditText.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + mEditText.getWidth();
        mRect.bottom = location[1] + mEditText.getHeight();

        return mRect;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
        }

        if(responseSpeaker !=null){
            responseSpeaker.stop();
            responseSpeaker.shutdown();
        }
    }

    public void onClickVoiceButton(View v){

        promptSpeechInput("no");

    }

    public void onClickCameraButton(View v){
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    public void onClickSubmitButton(View v){

        isNameEntered =true;
        isCategorySelected =true;
        isSubCategorySelected =true;

        name = mEditNameText.getText().toString();
        if(name.equals("")) {
            isNameEntered = false;
            Toast.makeText(getApplicationContext(), "Please enter your name !!",Toast.LENGTH_SHORT).show();
        }

        String category1 = mSpinnerCategory.getSelectedItem().toString();
        if(category1.equals("Select")) {
            isCategorySelected = false;
            Toast.makeText(getApplicationContext(), "Please Select a Category !!",Toast.LENGTH_SHORT).show();
        }

        String category2 = mSpinnerSubCategory.getSelectedItem().toString();
        if(category2.equals("Select")) {
            isSubCategorySelected = false;
            Toast.makeText(getApplicationContext(), "Please Select a Sub-Category !!",Toast.LENGTH_SHORT).show();
        }

        makePostRequest(name,category1,category2);

    }

    private void speakUp(JSONObject response){
        String speakResponse = null;
        String whereToGO = null;
        Object value1 = null;
        Object value2 = null;
        try {
            value1 = response.get("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            value2 = response.get("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(value1 != null){
            whereToGO = value1.toString();
        }

        if (value2 != null) {
            speakResponse = value2.toString();
        }

        Log.d(TAG, "speakResponse "+ speakResponse);

        txtSpeechInput.setText(speakResponse);
        responseSpeaker.speak(speakResponse, TextToSpeech.QUEUE_FLUSH, null);

        if(whereToGO !=null) {
            if (whereToGO.equals("24")) {
                if(isNameEntered && isCategorySelected && isSubCategorySelected) {
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Context context = FormsActivity.this;
                            Class destinationActivity = VolunteerDetailsActivity.class;
                            Intent startFormActivityIntent = new Intent(context, destinationActivity);
                            startFormActivityIntent.putExtra("BitmapImage", profileData);
                            startFormActivityIntent.putExtra("name", name);
                            startActivity(startFormActivityIntent);
                        }
                    }, 2000);

                }
            }
        }
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput(String speakResponse) {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        if(speakResponse.equals("no")){
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    getString(R.string.speech_prompt));
        }else{
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                    speakResponse);
        }
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
                    makePostRequest(result.get(0), "no","no");
                }
                break;
            }
            case CAMERA_REQUEST: {
                if (resultCode == Activity.RESULT_OK) {
                    profileData = (Bitmap) data.getExtras().get("data");
                    profilePic.setImageBitmap(profileData);
                }
            }
        }
    }

    void makePostRequest(String name,String category1, String category2){

        // Post params to be sent to the server
        HashMap<String, String> params;
        if(category1.equals("no")){
            params = new HashMap<>();
            params.put("id", "2");
            params.put("content", name);
        }else {
            params = new HashMap<>();
            params.put("id", "2");
            params.put("content", name + " category " + category1 + " sub-category " + category2);
        }

        JsonObjectRequest request_json = new JsonObjectRequest(serverURL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Process os success response
                            speakUp(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtSpeechInput.setText("OOPS!! Something's Wrong with your Internet");
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        mRequestQueue.add(request_json);

    }
}