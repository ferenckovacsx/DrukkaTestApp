package com.example.drukkatestapp.retrofit;

/**
 * Created by ferenckovacsx on 2018-03-01.
 */

import com.example.drukkatestapp.pojo.FilePOJO;
import com.example.drukkatestapp.pojo.LoginRequestPOJO;
import com.example.drukkatestapp.pojo.LoginResponsePOJO;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface APIInterface {

    @POST("/login")
    Call<LoginResponsePOJO> login(@Body LoginRequestPOJO body);

    @POST("/registration")
    Call<ResponseBody> registration(@Body LoginRequestPOJO body);

    @GET("/list_documents")
    Call<ArrayList<FilePOJO>> list_documents(@Header("Cookie") String cookie);

    @Multipart
    @POST("/add_documents")
    Call<ResponseBody> add_documents(@Header("Cookie") String cookie, @Part MultipartBody.Part file);

    @HTTP(method = "DELETE", path = "/delete_document", hasBody = true)
    Call<ResponseBody> delete_document(@Header("Cookie") String cookie, @Body String uuid);

    @GET("/logout")
    Call<ResponseBody> logout(@Header("Cookie") String cookie);

    @GET("/close")
    Call<ResponseBody> close(@Header("Cookie") String cookie);

}
