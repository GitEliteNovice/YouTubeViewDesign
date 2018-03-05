package view.com.youtubeviewdesign;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.pedrovgs.DraggableListener;
import com.github.pedrovgs.DraggablePanel;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import view.com.youtubeviewdesign.adapter.*;
import view.com.youtubeviewdesign.interfaces.loadRetryListener;
import view.com.youtubeviewdesign.player.BasePlayer;
import view.com.youtubeviewdesign.player.MainVideoPlayer;
import view.com.youtubeviewdesign.player.VideoPlayer;
import view.com.youtubeviewdesign.player.model.AudioStream;
import view.com.youtubeviewdesign.player.model.VideoStream;
import view.com.youtubeviewdesign.retrofitservices.RetrofitAPi;
import view.com.youtubeviewdesign.retrofitservices.YoutubeApiServces;
import view.com.youtubeviewdesign.utils.PaginationScrollListener;
import view.com.youtubeviewdesign.youtubemodel.Item;
import view.com.youtubeviewdesign.youtubemodel.YoutubeFeedModel;

import static view.com.youtubeviewdesign.BuildConfig.DEBUG;

public class MainActivity extends AppCompatActivity implements loadRetryListener {
    private static final String YOUTUBE_API_KEY = "AIzaSyC1rMU-mkhoyTvBIdTnYU0dss0tU9vtK48";
    private static String VIDEO_KEY = "gsjtg7m1MMM";
    DraggablePanel draggablePanel;
    BottomYoutubeViewFragment bottomYoutubeViewFragment;
    VideoYoutubeViewFragment topFragment;
ViewGroup playerParent;
    MainVideoPlayer mainVideoPlayer;
    Button button,button2;
    private YouTubePlayer youtubePlayer;
    private YouTubePlayerSupportFragment youtubeFragment;
    RecyclerView recyclelerview;
    LinearLayoutManager linearLayoutManager;
    view.com.youtubeviewdesign.adapter.MainYoutubeFeedAdapter youtubeFeedAdapter;
    ProgressBar main_progress;
    String youtubeKey="AIzaSyDCvpcOXfSIOiDXwT6xwqUMk45rSTPwS3Y";
    private boolean isLastPage;
    private boolean isLoading;
    private int getTotalPageCount;
    String releventVideoId="";
    String nextPageToken=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    //    button=(Button) findViewById(R.id.button);
    //    button2= (Button) findViewById(R.id.button2);

        draggablePanel= (DraggablePanel) findViewById(R.id.draggable_panel);

        hookDraggableListener();
        initializeFragments();
        initializeYoutubeFragment();
        initializeDraggablePanel();
        fetchYoutubeFeed(false);
/*
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hookDraggablePanelListeners();
            }
        });*/
    }

    /**
     * Initialize PlaceFragment and SupportMapFragment.
     */
    private void initializeFragments() {

        mainVideoPlayer=new MainVideoPlayer(draggablePanel);
         bottomYoutubeViewFragment = new BottomYoutubeViewFragment(draggablePanel);
        topFragment=new VideoYoutubeViewFragment(1);
        main_progress=(ProgressBar)findViewById(R.id.main_progress);
        recyclelerview= (RecyclerView) findViewById(R.id.mainRecycleview);
        bindAdapter();
    }


    public void bindAdapter() {

        youtubeFeedAdapter=new view.com.youtubeviewdesign.adapter.MainYoutubeFeedAdapter(MainActivity.this,this);
        linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        recyclelerview.setLayoutManager(linearLayoutManager);
        recyclelerview.setItemAnimator(new DefaultItemAnimator());
        recyclelerview.setAdapter(youtubeFeedAdapter);

        recyclelerview.addOnScrollListener(new PaginationScrollListener(linearLayoutManager) {
            @Override
            public boolean isLastPage() {
                return isLastPage;
            }
            @Override
            public boolean isLoading() {

                return isLoading;
            }

            @Override
            public void loadMoreItems() {
                isLoading=true;

                if(nextPageToken!=null)
                {

                    //pagging concept running
                    fetchYoutubeFeed(true);
                }

            }

            @Override
            public int getTotalPageCount() {
                return getTotalPageCount;
            }
        });



    }
    private void fetchYoutubeFeed(final boolean pagingStatus)
    {
        final YoutubeApiServces youtubeApiServces=  RetrofitAPi.getClient().create(YoutubeApiServces.class);
        Call<YoutubeFeedModel> youtubeFeedModelCall= youtubeApiServces.doYoutubeFeed(VIDEO_KEY,"25","video","snippet",youtubeKey,nextPageToken);

        youtubeFeedModelCall.enqueue(new Callback<YoutubeFeedModel>() {
            @Override
            public void onResponse(Call<YoutubeFeedModel> call, Response<YoutubeFeedModel> response) {


                if(pagingStatus)
                {
                    //if paging concept execute then
                    youtubeFeedAdapter.removeLoadingFooter();
                    isLoading=false;
                }
                else
                {
                    main_progress.setVisibility(View.GONE);
                }


                YoutubeFeedModel youtubeFeedModel=response.body();
                nextPageToken=youtubeFeedModel.getNextPageToken();
                List<Item> youtubeDataList=youtubeFeedModel.getItems();

                youtubeFeedAdapter.addAll(youtubeDataList);

                if(nextPageToken==null)
                {
                    isLastPage=true;
                }
                else
                {
                    youtubeFeedAdapter.addLodingFooter();
                }

            }

            @Override
            public void onFailure(Call<YoutubeFeedModel> call, Throwable t) {

                if(pagingStatus)
                {
                    youtubeFeedAdapter.showRetry(true,fetchErrorMessage(t));
                }
                else
                {
                    showErrorView(t);
                }

            }
        });
    }
    private void showErrorView(Throwable throwable) {

        main_progress.setVisibility(View.GONE);
        Toast.makeText(MainActivity.this,"Internet Connection Check !",Toast.LENGTH_LONG).show();
    }
    public String fetchErrorMessage(Throwable throwable)
    {

        String errorMsg=null;
        if(true)
        {
            //internet connection error
            errorMsg=getResources().getString(R.string.error_msg_no_internet);
        }
        else if(throwable instanceof TimeoutException)
        {
            errorMsg=getResources().getString(R.string.error_msg_timeout);
        }
        return errorMsg;
    }

    private void initializeYoutubeFragment() {
        youtubeFragment = new YouTubePlayerSupportFragment();
        youtubeFragment.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {

            @Override public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                          YouTubePlayer player, boolean wasRestored) {
                if (!wasRestored) {
                    youtubePlayer = player;
                    youtubePlayer.loadVideo(VIDEO_KEY);
                    youtubePlayer.setShowFullscreenButton(true);
                    bottomYoutubeViewFragment.setYoutubeplayer(youtubePlayer);
                }
            }

            @Override public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                          YouTubeInitializationResult error) {
            }
        });
    }

    /**
     * Initialize the DraggablePanel with top and bottom Fragments and apply all the configuration.
     */
    private void initializeDraggablePanel() {
        draggablePanel.setFragmentManager(getSupportFragmentManager());
        draggablePanel.setTopFragment(youtubeFragment);
        draggablePanel.setBottomFragment(bottomYoutubeViewFragment);
        draggablePanel.setClickToMaximizeEnabled(true);
        //draggable_view:top_view_resize="true"

        TypedValue typedValue = new TypedValue();
        getResources().getValue(R.dimen.x_scale_factor, typedValue, true);
        float xScaleFactor = typedValue.getFloat();
        typedValue = new TypedValue();
        getResources().getValue(R.dimen.y_scale_factor, typedValue, true);
        float yScaleFactor = typedValue.getFloat();
        draggablePanel.setXScaleFactor(xScaleFactor);
        draggablePanel.setYScaleFactor(yScaleFactor);
        draggablePanel.setTopViewHeight(
                getResources().getDimensionPixelSize(R.dimen.top_fragment_height));
        draggablePanel.setTopFragmentMarginRight(
                getResources().getDimensionPixelSize(R.dimen.top_fragment_margin));
        draggablePanel.setTopFragmentMarginBottom(
                getResources().getDimensionPixelSize(R.dimen.top_fragment_margin));
        draggablePanel.initializeView();
        draggablePanel.setVisibility(View.GONE);
    }
    private void showPlace() {
        draggablePanel.setVisibility(View.VISIBLE);
        draggablePanel.maximize();

        bottomYoutubeViewFragment.setVideoid("bTV67SQzJng");

        sendPlayer();
    }

    private void hookDraggableListener() {
        // TODO Auto-generated method stub
        draggablePanel.setDraggableListener(new DraggableListener() {

            @Override
            public void onMinimized() {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, "Minimized", Toast.LENGTH_SHORT).show();

//        surfaceView.getHolder().setFixedSize(surfaceView.getWidth(), surfaceView.getHeight());
            }

            @Override
            public void onMaximized() {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, "Maximized", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClosedToRight() {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, "Closed to Right", Toast.LENGTH_SHORT).show();
                bottomYoutubeViewFragment.closeDraggablePanel();

            }

            @Override
            public void onClosedToLeft() {
                // TODO Auto-generated method stub
                Toast.makeText(MainActivity.this, "Closed to Left", Toast.LENGTH_SHORT).show();
                bottomYoutubeViewFragment.closeDraggablePanel();
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            View playerView = youtubeFragment.getView();
           playerParent = (ViewGroup) findViewById(R.id.drag_view);
           playerParent.removeView(playerView);
            draggablePanel.addView(playerView, playerView.getLayoutParams());
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            View playerView = youtubeFragment.getView();
            draggablePanel.removeView(playerView);
            playerParent = (ViewGroup) findViewById(R.id.drag_view);
            playerParent.addView(playerView, playerView.getLayoutParams());
        }

     //   playerFragment.onOrientationChanged(configuration.orientation);



        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        setRequestedOrientation(getResources().getDisplayMetrics().heightPixels > getResources().getDisplayMetrics().widthPixels
                ? ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                : ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        if(draggablePanel.isShown())
        {
            bottomYoutubeViewFragment.onBackPress();
        }
        else
        {
            super.onBackPressed();
        }

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }

    private void sendPlayer()
    {

        String videoUrl="https://r9---sn-ci5gup-qxad.googlevideo.com/videoplayback?requiressl=yes&source=youtube&initcwndbps=366250&ei=hY4rWrmjOY-iowOArrjwBw&sparams=dur%2Cei%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpcm2%2Cpl%2Cratebypass%2Crequiressl%2Csource%2Cexpire&lmt=1512103067875094&ipbits=0&pcm2=no&beids=%5B9466593%5D&dur=233.569&ratebypass=yes&expire=1512825574&key=yt6&mime=video%2Fmp4&id=o-ALfGqnXuWd8_PWeO3bgClBxC-uYOCEmehKzHl7bb2OyZ&mt=1512803861&mv=m&ms=au&mm=31&ip=122.177.232.12&mn=sn-ci5gup-qxad&pl=20&itag=22&signature=B3A3F3BC502FD0405CBD76AE8D49774E1BF503AB.43F5F9D3B7973F1B4DD4CB55DA55B2CA9BEEEE56";


        String audioUrl="https://redirector.googlevideo.com/videoplayback?requiressl=yes&itag=140&lmt=1496070292413168&keepalive=yes&ip=104.131.144.251&sparams=clen%2Cdur%2Cei%2Cgir%2Cid%2Cinitcwndbps%2Cip%2Cipbits%2Citag%2Ckeepalive%2Clmt%2Cmime%2Cmm%2Cmn%2Cms%2Cmv%2Cpl%2Crequiressl%2Csource%2Cexpire&dur=235.589&source=youtube&id=o-ANZCSLH3L79kuw6paB2tqRJ0TKmQ4p0vs_FdDbHKNt8L&mime=audio%2Fmp4&expire=1511437538&ipbits=0&initcwndbps=231250&gir=yes&mm=31&mn=sn-n4v7sn7z&key=yt6&clen=3742404&ei=gmAWWsC3JpWz8wTGk6eAAg&ms=au&mt=1511415861&mv=m&pl=20&signature=1761799B2EE9677A996C2A2D92F4D22914F14F62.61D80F04801F5B00FA6757B14E8D7081F7FC89D9&ratebypass=yes&title=3+Doors+Down+-+Here+Without+You&title=3+Doors+Down+-+Here+Without+You";


        VideoStream videoData=new VideoStream(videoUrl,1024,"Mp4");
        final AudioStream audioData=new AudioStream(audioUrl,128,128);

        final ArrayList<VideoStream> videoListt=new  ArrayList();
        ArrayList<AudioStream> audioList=new  ArrayList();

        videoListt.add(videoData);
        audioList.add(audioData);

        Bundle intent=new Bundle();
        intent.putString(BasePlayer.VIDEO_TITLE,"YoutubeSongsTitle");

        intent.putString(BasePlayer.VIDEO_URL,"https://www.youtube.com/watch?v=SR9Ly4IpIdQ");
        intent.putString(BasePlayer.VIDEO_THUMBNAIL_URL,"http://i.ytimg.com/vi/SR9Ly4IpIdQ/hqdefault.jpg");
        intent.putString(BasePlayer.CHANNEL_NAME, "UploaderName");
        intent.putInt(VideoPlayer.INDEX_SEL_VIDEO_STREAM, 0);
        intent.putSerializable(VideoPlayer.VIDEO_STREAMS_LIST, videoListt);
        intent.putSerializable(VideoPlayer.VIDEO_ONLY_AUDIO_STREAM, audioData);
        mainVideoPlayer.reciveIntentValue(intent);



    }
    private void hookDraggablePanelListeners() {
        draggablePanel.setVisibility(View.VISIBLE);
        draggablePanel.maximize();

        bottomYoutubeViewFragment.setVideoid(VIDEO_KEY);
        draggablePanel.setDraggableListener(new DraggableListener() {
            @Override public void onMaximized() {
                playVideo();
            }

            @Override public void onMinimized() {
                //Empty
            }

            @Override public void onClosedToLeft() {
                pauseVideo();
            }

            @Override public void onClosedToRight() {
                pauseVideo();
            }
        });
    }
    private void pauseVideo() {
        if (youtubePlayer.isPlaying()) {
            youtubePlayer.pause();
        }
    }

    /**
     * Resume the video reproduced in the YouTubePlayer.
     */
    private void playVideo() {
        if (!youtubePlayer.isPlaying()) {
            youtubePlayer.play();
        }
    }

    @Override
    public void reLoad() {

    }

    @Override
    public void rowItemClick(String videoId) {
        VIDEO_KEY=videoId;
        youtubePlayer.loadVideo(videoId);
        hookDraggablePanelListeners();
    }

//--------------------------------------------Video Player--------------------------------------


}
