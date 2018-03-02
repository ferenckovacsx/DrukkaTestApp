package com.example.drukkatestapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drukkatestapp.R;
import com.example.drukkatestapp.Utilities;
import com.example.drukkatestapp.pojo.LoginRequestPOJO;
import com.example.drukkatestapp.retrofit.APIClient;
import com.example.drukkatestapp.retrofit.APIInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;


public class RegistrationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    final String TAG = "REGFRAGMENT";

    EditText emailEditText, passwordEditText, confirmPasswordEditText;
    TextView invalidEmailTv, emptyPasswrodTv, passwordMismatchTv;
    ImageView cancelButton;
    Button signUpButton;
    String email, password, confirmPassword;
    Utilities utilities;
    APIInterface apiInterface;

    public RegistrationFragment() {
    }

    public static RegistrationFragment newInstance(String param1, String param2) {
        RegistrationFragment fragment = new RegistrationFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View registrationView = inflater.inflate(R.layout.fragment_registration, container, false);

        utilities = new Utilities(getContext());

        APIClient apiClient = new APIClient(getContext());
        apiInterface = apiClient.getClient().create(APIInterface.class);

        emailEditText = registrationView.findViewById(R.id.reg_input_email);
        passwordEditText = registrationView.findViewById(R.id.reg_input_password);
        confirmPasswordEditText = registrationView.findViewById(R.id.reg_input_confirmpassword);
        invalidEmailTv = registrationView.findViewById(R.id.invalid_email_textview);
        emptyPasswrodTv = registrationView.findViewById(R.id.empty_password_textview);
        passwordMismatchTv = registrationView.findViewById(R.id.password_mismatch_textview);
        signUpButton = registrationView.findViewById(R.id.login_button);
        cancelButton = registrationView.findViewById(R.id.cancel);

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
                invalidEmailTv.setVisibility(View.GONE);
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
                confirmPasswordEditText.setBackgroundColor(Color.TRANSPARENT);
                emptyPasswrodTv.setVisibility(View.GONE);
                passwordMismatchTv.setVisibility(View.GONE);
            }
        });

        confirmPasswordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordEditText.setBackgroundColor(Color.TRANSPARENT);
                passwordEditText.setBackgroundColor(Color.TRANSPARENT);
                passwordMismatchTv.setVisibility(View.GONE);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                confirmPassword = confirmPasswordEditText.getText().toString();

                //check if device is online
                if (!utilities.isOnline()) {
                    Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                } else {
                    //invalid email address
                    if (!utilities.isValidEmail(email)) {
                        invalidEmailTv.setVisibility(View.VISIBLE);
                        emailEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.errorHintTextColor)));
                        emailEditText.setBackgroundColor(getResources().getColor(R.color.errorHintTextColor));
                    }

                    //empty password
                    if (password.equals("")) {
                        emptyPasswrodTv.setVisibility(View.VISIBLE);
                        passwordEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.errorHintTextColor)));
                        confirmPasswordEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.errorHintTextColor)));
                        passwordEditText.setBackgroundColor(getResources().getColor(R.color.errorHintTextColor));
                        confirmPasswordEditText.setBackgroundColor(getResources().getColor(R.color.errorHintTextColor));
                    }

                    //passwords do not match
                    if (!confirmPassword.equals(password)) {
                        passwordMismatchTv.setVisibility(View.VISIBLE);
                        passwordEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.errorHintTextColor)));
                        confirmPasswordEditText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.errorHintTextColor)));
                        passwordEditText.setBackgroundColor(getResources().getColor(R.color.errorHintTextColor));
                        confirmPasswordEditText.setBackgroundColor(getResources().getColor(R.color.errorHintTextColor));
                    }

                    if (utilities.isValidEmail(email) && !password.equals("") && confirmPassword.equals(password)) {
                        invalidEmailTv.setVisibility(View.GONE);
                        emptyPasswrodTv.setVisibility(View.GONE);
                        passwordMismatchTv.setVisibility(View.GONE);

                        createAccount(email, password);

                    }
                }

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        return registrationView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void createAccount(String email, String password) {

        Call<ResponseBody> call = apiInterface.registration(new LoginRequestPOJO(email, utilities.convertToSHA256(password)));

        call.enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {

                int responseCode = response.code();
                switch (responseCode) {
                    case 201:
                        Toast.makeText(getContext(), "201: Registration successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(getActivity(), MainActivity.class);
                        startActivity(intent);

                        getActivity().finish();

                        break;
                    case 406:
                        Toast.makeText(getContext(), "406: Email already in use", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Log.i(TAG, "Login failed");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
            }
        });
    }

}
