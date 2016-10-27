package com.example.peter.bully_button;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.net.Uri;
import android.widget.TextView;

import com.clockworksms.ClockWorkSmsService;
import com.clockworksms.ClockworkSmsResult;
import com.clockworksms.SMS;
import com.google.android.gms.vision.text.Text;

import java.io.DataOutputStream;
import java.io.InputStream;
import 	java.net.URL;

import java.io.IOException;
import java.net.HttpURLConnection;

import static android.provider.AlarmClock.EXTRA_MESSAGE;
import static android.provider.ContactsContract.CommonDataKinds.Website.URL;

public class MainActivity extends AppCompatActivity {

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    /**
     * Called when the user clicks the Send button
     */
    public void sendMessage(View view) {
        // Do something in response to button
        EditText editText = (EditText) findViewById(R.id.edit_message);
        String message = editText.getText().toString();
        if (message.isEmpty()) {
            message =
                    "Your child has pressed the bully button, they would like to talk to you about it later.";
        }
        SharedPreferences preferences = getSharedPreferences("bully_prefs", 0);
        String phoneNum = preferences.getString("phone_number", "");
        String apiKey = preferences.getString("api_key", "");
        //Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        //startActivity(intent);
        HttpURLConnection urlConnection = null;
        String messageOut = null;
        try {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            ClockWorkSmsService clockWorkSmsService = new ClockWorkSmsService(apiKey);
            SMS sms = new SMS(phoneNum, message);
            ClockworkSmsResult result = clockWorkSmsService.send(sms);

            if (result.isSuccess()) {
                TextView bob = (TextView) findViewById(R.id.feedback);
                bob.setText("Sent");
                System.out.println("Sent with ID: " + result.getId());
                messageOut = "Message sent to " + phoneNum + " with ID." + result.getId();

            } else {
                TextView bob = (TextView) findViewById(R.id.feedback);
                bob.setText("Failed");
                System.out.println("Error: " + result.getErrorMessage());
                messageOut = "Message Wasn't Sent Because" + result.getErrorMessage();
            }
        } catch (Exception e) {
        }

    }

    public void displaySettings(View view) {
        try {
            setContentView(R.layout.configurationscreen);
            SharedPreferences preferences = getSharedPreferences("bully_prefs", 0);

            String phoneNum = preferences.getString("phone_number", "");
            EditText editText = (EditText) findViewById(R.id.phoneNumber);
            editText.setText(phoneNum);

            String apiKey = preferences.getString("api_key", "");
            EditText apiKeyEditText = (EditText) findViewById(R.id.apiKey);
            apiKeyEditText.setText(apiKey);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void displayMain(View view) {
        setContentView(R.layout.activity_main);
        TextView bob = (TextView) findViewById(R.id.feedback);
        bob.setText("Changes Discarded");
    }

    public void save(View view) {
        try {
            SharedPreferences preferences = getSharedPreferences("bully_prefs", 0);
            SharedPreferences.Editor editor = preferences.edit();

            // phone number
            EditText editText = (EditText) findViewById(R.id.phoneNumber);
            String phoneNum = editText.getText().toString();
            editor.putString("phone_number", phoneNum); // value to store

            // api key
            EditText apiKeyEditText = (EditText) findViewById(R.id.apiKey);
            String apiKey = apiKeyEditText.getText().toString();
            editor.putString("api_key", apiKey); // value to store

            editor.commit();
            setContentView(R.layout.activity_main);
            TextView bob = (TextView) findViewById(R.id.feedback);
            bob.setText("Changes Saved");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}