package com.example.sermo;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface API {
    @Multipart
    // POST request to upload an image from storage
    @POST("test/")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part[] files);
}
