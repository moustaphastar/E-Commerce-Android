package dev.atharvakulkarni.e_commerce.net;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface PayoneerApi {
    @GET("listUrl/")
    Call<Map<String, Object>> getListUrl();
}
