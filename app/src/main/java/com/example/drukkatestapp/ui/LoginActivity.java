package com.example.drukkatestapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drukkatestapp.pojo.LoginRequestPOJO;
import com.example.drukkatestapp.pojo.LoginResponsePOJO;
import com.example.drukkatestapp.Utilities;
import com.example.drukkatestapp.R;
import com.example.drukkatestapp.retrofit.APIClient;
import com.example.drukkatestapp.retrofit.APIInterface;

import retrofit2.Call;
import retrofit2.Callback;

public class LoginActivity extends AppCompatActivity implements RegistrationFragment.OnFragmentInteractionListener {

    final String TAG = "LOGIN_ACTIVITY";

    TextView registerTextView;
    EditText emailEditText, passwordEditText;
    String emailString, passwordString;
    Button loginButton;
    Utilities utilities;

    APIInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        emailEditText = findViewById(R.id.input_email);
        passwordEditText = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.login_button);
        registerTextView = findViewById(R.id.create_account_textview);

        utilities = new Utilities(this);

        APIClient apiClient = new APIClient(this);
        apiInterface = apiClient.getClient().create(APIInterface.class);

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailEditText.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordEditText.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emailString = emailEditText.getText().toString();
                passwordString = passwordEditText.getText().toString();

                //check if device is online
                if (!utilities.isOnline()) {
                    Toast.makeText(LoginActivity.this, "No internet connection.", Toast.LENGTH_LONG).show();
                } else {
                    //email is empty
                    if (emailString.equals("")) {
                        emailEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.errorHintTextColor)));
                        emailEditText.setBackgroundColor(getResources().getColor(R.color.errorHintTextColor));
                    }

                    //empty password
                    if (passwordString.equals("")) {
                        passwordEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.errorHintTextColor)));
                        passwordEditText.setBackgroundColor(getResources().getColor(R.color.errorHintTextColor));
                    }

                    if (!emailString.equals("") && !passwordString.equals("")) {

                        attemptLogin(emailString, passwordString);

                    }
                }

            }
        });

        if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RegistrationFragment fragment = new RegistrationFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(R.id.container, fragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void attemptLogin(String email, String password) {

        Call<LoginResponsePOJO> call = apiInterface.login(new LoginRequestPOJO(email, utilities.convertToSHA256(password)));

        call.enqueue(new Callback<LoginResponsePOJO>() {

            @Override
            public void onResponse(Call<LoginResponsePOJO> call, retrofit2.Response<LoginResponsePOJO> response) {

                int responseCode = response.code();
                switch (responseCode) {
                    case 200:
                        Toast.makeText(LoginActivity.this, "200: Login Succesful", Toast.LENGTH_LONG).show();

                        Log.i(TAG, "Login successful");

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);

                        LoginActivity.this.finish();

                        break;
                    case 401:
                        Log.i(TAG, "Wrong username or password");
                        Toast.makeText(LoginActivity.this, "401: Invalid username or password", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Log.i(TAG, "Login failed");
                }
            }


            @Override
            public void onFailure(Call<LoginResponsePOJO> call, Throwable t) {


            }
        });

    }
}
