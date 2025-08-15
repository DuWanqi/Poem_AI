package com.example.poemai.network;

import com.example.poemai.model.ApiResponse;
import com.example.poemai.model.CiPai;
import com.example.poemai.model.LoginRequest;
import com.example.poemai.model.LoginResponse;
import com.example.poemai.model.RegisterRequest;
import com.example.poemai.model.RhymeResponse;
import com.example.poemai.model.WorkSaveResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

public interface ApiService {
    // 认证相关接口
    @POST("api/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);

    @POST("api/auth/register")
    Call<LoginResponse> register(@Body RegisterRequest request);

    // 词牌相关接口
    @GET("api/cipai/")
    Call<List<CiPai>> getAllCiPais();

    @POST("api/cipai/match")
    Call<List<CiPai>> matchCiPai(@Body Map<String, Object> requestBody);

    // 押韵相关接口
    @GET("api/rhyme/words")
    Call<RhymeResponse> getRhymeWords(@Query("query") String query);

    // 作品相关接口
    @POST("api/work/save")
    Call<WorkSaveResponse> saveWork(@Header("Authorization") String token, @Body Map<String, Object> requestBody);

    @GET("api/work/")
    Call<ApiResponse> getAllWorks(@Header("Authorization") String token);

    @GET("api/work/{id}")
    Call<Map<String, Object>> getWorkById(@Header("Authorization") String token, @retrofit2.http.Path("id") Long id);

    @DELETE("api/work/{id}")
    Call<Void> deleteWork(@Header("Authorization") String token, @retrofit2.http.Path("id") Long id);

    @PUT("api/work/{id}")
    Call<Map<String, Object>> updateWork(@Header("Authorization") String token, @retrofit2.http.Path("id") Long id, @Body Map<String, Object> requestBody);
}