package dev.atharvakulkarni.e_commerce.net;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PayoneerApiClient {
    private static final String BASE_URL = "https://payment-gateway-integrations.herokuapp.com/";
    private static PayoneerApiClient mInstance;
    private Retrofit retrofit;

    private PayoneerApiClient()
    {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized PayoneerApiClient getInstance()
    {
        if (mInstance == null)
            mInstance = new PayoneerApiClient();
        return mInstance;
    }

    public PayoneerApi getApi()
    {
        return retrofit.create(PayoneerApi.class);
    }
}
