package com.example.drukkatestapp.retrofit;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by ferenckovacsx on 2018-03-01.
 */

public class APIClient {

    private static Retrofit retrofit = null;

    private Context context;
    private SharedPreferences sharedpreferences;
    private String cookieValue;


    public APIClient(Context context) {
        this.context = context;
    }

    public Retrofit getClient() {

        String baseURL = "http://mockapi.drukka.hu";

        sharedpreferences = context.getSharedPreferences("cookiePref", Context.MODE_PRIVATE);

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder().
                addInterceptor(interceptor).
                cookieJar(new CookieJar() {
                    private final HashMap<HttpUrl, List<Cookie>> cookieStore = new HashMap<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url, cookies);

                        //retrieve cookies and save them to sharedpreferences
                        for (int i = 0; i < cookies.size(); i++) {

                            cookieValue = cookies.get(i).toString();

                            SharedPreferences.Editor editor = sharedpreferences.edit();
                            editor.putString("cookieValue", cookieValue);
                            editor.apply();
                        }

                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {

                        List<Cookie> cookies = cookieStore.get(url);
                        return cookies != null ? cookies : new ArrayList<Cookie>();
                    }
                }).build();


        retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();


        return retrofit;
    }
}