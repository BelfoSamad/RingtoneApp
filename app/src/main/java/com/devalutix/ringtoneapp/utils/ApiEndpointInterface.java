package com.devalutix.ringtoneapp.utils;

import com.devalutix.ringtoneapp.pojo.Category;
import com.devalutix.ringtoneapp.pojo.Ringtone;

import java.util.ArrayList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface ApiEndpointInterface {

    @GET("ringtones/type={type}")
    Call<ArrayList<Ringtone>> getRingtones(@Header("Authorization") String auth, @Path("type") String type);

    @GET("categories")
    Call<ArrayList<Category>> getCategories(@Header("Authorization") String auth);

    @GET("ringtones/category={category}")
    Call<ArrayList<Ringtone>> getCategoryRingtones(@Header("Authorization") String auth, @Path("category") String category);

    @GET("search={query}")
    Call<ArrayList<Ringtone>> searchRingtones(@Header("Authorization") String auth, @Path("query") String query);
}
