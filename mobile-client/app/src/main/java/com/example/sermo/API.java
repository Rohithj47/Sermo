package com.example.sermo;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface API {
    // POST request to upload an image from storage
    @Multipart
    @POST("summarize/")
    Call<ResponseBody> uploadImage(@Part MultipartBody.Part[] files);

    @POST("values/")
    Call<ResponseBody> queryByTags(@Body QueryBody queryBody);
}
