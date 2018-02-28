package com.example.drukkatestapp;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;


public class RegistrationFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    final String TAG = "REGFRAGMENT";

    EditText emailEditText, passwordEditText, confirmPasswordEditText;
    Button signUpButton;
    String email, password, confirmPassword;
    NetworkTools networkTools;

    public RegistrationFragment() {
        // Required empty public constructor
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

        networkTools = new NetworkTools(getContext());

        emailEditText = registrationView.findViewById(R.id.reg_input_email);
        passwordEditText = registrationView.findViewById(R.id.reg_input_password);
        confirmPasswordEditText = registrationView.findViewById(R.id.reg_input_confirmpassword);
        signUpButton = registrationView.findViewById(R.id.login_button);



        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = emailEditText.getText().toString();
                password = passwordEditText.getText().toString();
                confirmPassword = confirmPasswordEditText.getText().toString();

                //check if device is online
                if (!networkTools.isOnline()){
                    Toast.makeText(getContext(), "No internet connection.", Toast.LENGTH_LONG).show();
                } else {
                    //check if email format is valid
                    if (!networkTools.isValidEmail(email)) {
                        Log.i(TAG, "email: " + email);
                        Toast.makeText(getContext(), "Invalid email address.", Toast.LENGTH_LONG).show();
                    }

                    if (password.equals("")){
                        Toast.makeText(getContext(), "Password is empty", Toast.LENGTH_LONG).show();
                    }

                    if (!confirmPassword.equals(password)) {
                        Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
                    }

                    if (networkTools.isValidEmail(email) && !password.equals("") && confirmPassword.equals(password)){
                        Toast.makeText(getContext(), "ALL GOOD IN DA' HOOD", Toast.LENGTH_LONG).show();

                    }
                }




//                createAccount();

            }
        });

        return registrationView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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

    public void createAccount() {

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            String URL = "http://mockapi.drukka.hu/registration";
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("email", "test3@test3.com");
            jsonBody.put("password", "test3pwd");
            final String mRequestBody = jsonBody.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_VOLLEY", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_VOLLEY", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {

                        responseString = String.valueOf(response.statusCode);

                    }
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

//        Log.d(TAG, "login");
//
//        String url = "http://mockapi.drukka.hu/registration";
//
//        Map<String, String> params = new HashMap();
//        params.put("email", "test2@test2.com");
//        params.put("password", "testpwd");
//
//        JSONObject parameters = new JSONObject(params);
//
//        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, parameters, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                Log.i(TAG, "registration succesful: " + response);
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                Log.i(TAG, "registration unsuccesful: " + error.toString());
//            }
//            @Override
//            protected Response<String> parseNetworkResponse(NetworkResponse response) {
//                String responseString = "";
//                if (response != null) {
//                    responseString = String.valueOf(response.statusCode);
//                }
//                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
//            }
//        });
//
//        Volley.newRequestQueue(getContext()).add(jsonRequest);
//
//    }
}
