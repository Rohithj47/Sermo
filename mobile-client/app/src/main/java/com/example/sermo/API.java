package com.example.sermo;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface API {

    @Multipart
    @POST("summarize/")
    Call<ResponseBody> generateReport(@Part MultipartBody.Part[] files,
                                      @Part("fullname") RequestBody fullname,
                                      @Part("age") RequestBody age,
                                      @Part("gender") RequestBody gender);

    @POST("search/")
    Call<List<ReportsCollection>> searchReports(@Body QueryBody queryBody);
}
