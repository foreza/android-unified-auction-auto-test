package com.inmobise.myapplication;

import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {

    @POST("api/session/")
    Call<MetricModel> postToMetricEndpoint(@Body HashMap<String, Object> body );



}

