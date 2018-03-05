package view.com.youtubeviewdesign.retrofitservices;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Abhay on 30/5/17.
 */

public class RetrofitAPi {

    static Retrofit retrofit;
    public static OkHttpClient buildClient()
    {
        return new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();
    }
    public static Retrofit getClient()
    {
        if(retrofit==null)
        {
            retrofit=new Retrofit.Builder().client(buildClient()).addConverterFactory(GsonConverterFactory.create()).baseUrl("https://www.googleapis.com/youtube/v3/").build();

        }
        return retrofit;
    }

}
