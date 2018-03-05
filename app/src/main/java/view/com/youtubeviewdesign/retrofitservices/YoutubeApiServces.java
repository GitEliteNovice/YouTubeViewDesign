package view.com.youtubeviewdesign.retrofitservices;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import view.com.youtubeviewdesign.youtubemodel.YoutubeFeedModel;

/**
 * Created by Abhay on 17/11/17.
 */

public interface YoutubeApiServces {

    @GET("search?")
    Call<YoutubeFeedModel> doYoutubeFeed(
            @Query("relatedToVideoId") String relatedToVideoId,
            @Query("maxResults") String maxResults,
            @Query("type") String type,
            @Query("part") String part,
            @Query("key") String key,
            @Query("pageToken") String pageToken);
}

