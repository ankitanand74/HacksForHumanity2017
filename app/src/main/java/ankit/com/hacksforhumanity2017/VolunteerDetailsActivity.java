package ankit.com.hacksforhumanity2017;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

public class VolunteerDetailsActivity extends AppCompatActivity {

    public static final String TAG = "VolunteerSignUpActivity";
    RequestQueue mRequestQueue;
    //final String serverURL ="http://10.142.203.154:8080/hello-world";
    final String serverURL ="http://ec2-54-172-72-28.compute-1.amazonaws.com:8080/hello-world";
    private int PLACE_PICKER_REQUEST = 1;
    private String volunteerAddress = "";
    private EditText mEditPhoneNum;
    private EditText mEditSkypeID;
    private String name;
    private TextToSpeech responseSpeaker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_volunteer_signup);

        Intent intent = getIntent();
        Bitmap bitmap = intent.getParcelableExtra("BitmapImage");
        name = intent.getStringExtra("name");

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Need Some More Details !!");
        }

        mRequestQueue = Volley.newRequestQueue(this);
        ImageView profilePic=(ImageView)findViewById(R.id.user_image);
        mEditPhoneNum = (EditText) findViewById(R.id.edit_cellPhone);
        mEditSkypeID = (EditText) findViewById(R.id.edit_skypeID);
        profilePic.setImageBitmap(bitmap);
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
                !getLocationOnScreen(mEditPhoneNum).contains(x, y)) {
            InputMethodManager input = (InputMethodManager)
                    this.getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(mEditPhoneNum.getWindowToken(), 0);
        }else if (ev.getAction() == MotionEvent.ACTION_DOWN &&
                !getLocationOnScreen(mEditSkypeID).contains(x, y)) {
            InputMethodManager input = (InputMethodManager)
                    this.getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(mEditSkypeID.getWindowToken(), 0);
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

    public void onClickChooseAddressButton(View v){

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
            Toast.makeText(this, "Google Play Services is not available.",
                    Toast.LENGTH_LONG)
                    .show();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
            Toast.makeText(this, "GooglePlayServicesRepairableException",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG,"requestCode "+ requestCode + "resultCode "+ resultCode);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place selectedPlace = PlacePicker.getPlace(data, this);
                Log.d(TAG,"selectedPlace " + selectedPlace);
                if(selectedPlace != null){
                    volunteerAddress = selectedPlace.getName().toString();
                    String toastMsg = String.format("Place: %s", selectedPlace.getName());
                    Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                    Log.d(TAG,"selectedPlace is " + selectedPlace.getName().toString());
                }else{
                    Toast.makeText(this, "Please select your address", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    public void onClickDoneButton(View v){

        boolean isAddressSelected =true;
        boolean isPhoneNumberEntered =true;
        boolean isSkypeIDEntered =true;

        Log.d(TAG, "volunteerAddress is "+ volunteerAddress);
        if(volunteerAddress.equals("")){
            Log.d(TAG, "volunteerAddress is null");
            isAddressSelected =false;
            Toast.makeText(this, "Please select your address", Toast.LENGTH_LONG).show();
        }

        String phoneNumber = mEditPhoneNum.getText().toString();
        if(phoneNumber.equals("")) {
            isPhoneNumberEntered = false;
            Toast.makeText(getApplicationContext(), "Please enter your phone number !!",Toast.LENGTH_SHORT).show();
        }

        String skypeID = mEditSkypeID.getText().toString();
        if(skypeID.equals("")) {
            isSkypeIDEntered = false;
            Toast.makeText(getApplicationContext(), "Please enter your skype ID!!",Toast.LENGTH_SHORT).show();
        }

        if(isAddressSelected && isPhoneNumberEntered && isSkypeIDEntered) {
            //TODO POST ON SERVER
            makePostRequest(volunteerAddress,phoneNumber,skypeID);
            Toast.makeText(getApplicationContext(), "Success, Thank you for Registering !!", Toast.LENGTH_SHORT).show();
        }
    }

    void makePostRequest(String address,String phoneNo, String skypeID){

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<>();
        params.put("id", "4");
        params.put("content", "the address of "+ name+ " is " + address + " with "+ phoneNo + " having " + skypeID);

        JsonObjectRequest request_json = new JsonObjectRequest(serverURL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            responseSpeaker.speak("Success, Thank you for Registering ", TextToSpeech.QUEUE_FLUSH, null);
                            Toast.makeText(getApplicationContext(), "Success, Thank you for Registering !!", Toast.LENGTH_SHORT).show();
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d(TAG, "Responded");
                                    Context context = VolunteerDetailsActivity.this;
                                    Class destinationActivity = MainActivity.class;
                                    Intent startFormActivityIntent = new Intent(context, destinationActivity);
                                    startActivity(startFormActivityIntent);
                                }
                            }, 2000);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                //txtSpeechInput.setText("OOPS Something's Wrong with your Internet");
                Toast.makeText(getApplicationContext(), "OOPS Something's Wrong with your Internet", Toast.LENGTH_SHORT).show();
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        mRequestQueue.add(request_json);

    }
}