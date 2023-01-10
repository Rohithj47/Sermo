package com.example.sermo;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface API {

    @Multipart
    @POST("summarize/")
    Call<ResponseBody> uploadFiles(@Part MultipartBody.Part[] files);

    @POST("search/")
    Call<ResponseBody> queryByTags(@Body QueryBody queryBody);
}
