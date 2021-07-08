package com.tgd.javasshtest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import static  com.tgd.javasshtest.KeysDictionary.*;

public class LoginActivity extends AppCompatActivity {

    private String host, port, username, password;
    EditText editText;
    EditText portField;
    EditText usernameField;
    EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText = findViewById(R.id.editText);
        portField = findViewById(R.id.portField);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);

        getData();
        editText.setText(host);
        portField.setText(port);
        usernameField.setText(username);
        passwordField.setText(password);
    }
    public void authenticate(View view) {

        // Create an intent for sshActivity
        Intent intent = new Intent(this, MainActivity.class);

        // Get input data from fields
        setData();
        // Pass on data to sshActivity via intent
        intent.putExtra("host", host);
        intent.putExtra("port", port);
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        startActivity(intent);
    }

    private void setData(){
        host = editText.getText().toString();
        port = portField.getText().toString();
        username = usernameField.getText().toString();
        password = passwordField.getText().toString();

        SharedPreferences preferences = getSharedPreferences(CONNECTION_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(HOST, host);
        editor.putString(PORT, port);
        editor.putString(USER, username);
        editor.putString(PASSWORD, password);

        editor.apply();
    }

    private void getData(){
        SharedPreferences preferences = getSharedPreferences(CONNECTION_DATA, Context.MODE_PRIVATE);
        host = preferences.getString(HOST, "");
        port = preferences.getString(PORT, "22");
        username = preferences.getString(USER, "");
        password = preferences.getString(PASSWORD, "");
    }

}