package view.com.youtubeviewdesign;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.pedrovgs.DraggablePanel;
import com.google.android.youtube.player.YouTubePlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import view.com.youtubeviewdesign.adapter.YoutubeFeedAdapter;
import view.com.youtubeviewdesign.interfaces.loadRetryListener;
import view.com.youtubeviewdesign.retrofitservices.RetrofitAPi;
import view.com.youtubeviewdesign.retrofitservices.YoutubeApiServces;
import view.com.youtubeviewdesign.utils.MainFragment;
import view.com.youtubeviewdesign.utils.PaginationScrollListener;
import view.com.youtubeviewdesign.youtubemodel.Item;
import view.com.youtubeviewdesign.youtubemodel.YoutubeFeedModel;


/**
 * Created by Abhay Jaiswal on 3/11/17.
 */

@SuppressLint("ValidFragment")
public class BottomYoutubeViewFragment extends MainFragment implements loadRetryListener {


    String TAG="BottomYoutubeViewFragment=";
    RecyclerView recyclelerview;
    LinearLayoutManager  linearLayoutManager;
    view.com.youtubeviewdesign.adapter.YoutubeFeedAdapter youtubeFeedAdapter;
    View view;
    ProgressBar main_progress;
    //use for paging
    private boolean isLastPage;
    private boolean isLoading;
    String nextPageToken=null;
    private int getTotalPageCount;
    YouTubePlayer youtubePlayer;
    //Youtube data
    List<String> playeridss=new ArrayList<>();
   LinkedHashMap<String,Integer> timein_millsec=new LinkedHashMap<>();
    String releventVideoId;
    String youtubeKey="AIzaSyDCvpcOXfSIOiDXwT6xwqUMk45rSTPwS3Y";
    LinkedHashMap<Integer,List<Item>> allPreviousData=new LinkedHashMap();
    int linkedHasMapindex;
    DraggablePanel draggablePanel;
     public BottomYoutubeViewFragment(DraggablePanel draggablePanel) {

            this.draggablePanel=draggablePanel;

        }
        public BottomYoutubeViewFragment(){

    }
    public void setVideoid(String releventVideoId)
    {
        playeridss.add(releventVideoId);
        linkedHasMapindex++;
//        main_progress.setVisibility(View.VISIBLE);
        this.releventVideoId=releventVideoId;

        clearYoutubeListing();
        fetchYoutubeFeed(false);
    }
    @Override
    @Nullable
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

         view=inflater.inflate(R.layout.bottom_fragment,container,false);
        inisilizeComponent();

        return view;
    }
    private void inisilizeComponent()
    {
        main_progress=(ProgressBar) view.findViewById(R.id.main_progress);
        recyclelerview= (RecyclerView) view.findViewById(R.id.recyclelerview);
        bindAdapter();
    }
    public void setYoutubeplayer(YouTubePlayer youtubePlayer){
        this.youtubePlayer=youtubePlayer;
    }
    private void fetchYoutubeFeed(final boolean pagingStatus)
    {
        final YoutubeApiServces youtubeApiServces=  RetrofitAPi.getClient().create(YoutubeApiServces.class);
       Call<YoutubeFeedModel> youtubeFeedModelCall= youtubeApiServces.doYoutubeFeed(releventVideoId,"25","video","snippet",youtubeKey,nextPageToken);

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
        Toast.makeText(getActivity(),"Internet Connection Check !",Toast.LENGTH_LONG).show();
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

    public void bindAdapter() {

        youtubeFeedAdapter=new YoutubeFeedAdapter(getActivity(),this);
        linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
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



    @Override
    public void reLoad() {

        //pagging concept running
        fetchYoutubeFeed(true);
    }

    @Override
    public void rowItemClick(String releventVideoId) {

        //getting videoId for reload listing
int s=youtubePlayer.getCurrentTimeMillis();
        timein_millsec.put(this.releventVideoId,youtubePlayer.getCurrentTimeMillis());
        youtubePlayer.loadVideo(releventVideoId);
        youtubePlayer.setShowFullscreenButton(true);
        clearYoutubeListingWIthReLoad(releventVideoId);
        System.out.println(TAG+releventVideoId);

    }

    private void clearYoutubeListingWIthReLoad(String releventVideoId)
    {

        //save all youtube list data for show when user backPress
//        System.out.println("DataSize Save="+youtubeFeedAdapter.getAllListData().get(0).getSnippet().getTitle());
       ArrayList<Item> youtubeCopyData=new ArrayList<>();
        youtubeCopyData.addAll(youtubeFeedAdapter.getAllListData());

        allPreviousData.put(linkedHasMapindex,youtubeCopyData);


        clearYoutubeListing();
        setVideoid(releventVideoId);

    }
    private void clearYoutubeListing()
    {
        youtubeFeedAdapter.removeAllData();
        nextPageToken=null;
        isLastPage=false;
        isLoading=false;
        nextPageToken=null;
        getTotalPageCount=0;

    }

    @Override
    public void onBackPress() {

        //check loded data index
        System.out.println("PrevIndex="+linkedHasMapindex);
        if(linkedHasMapindex!=1)
        {
            linkedHasMapindex--;


            List<Item> youtubeBackDataList=allPreviousData.get(allPreviousData.size());

            ArrayList <Item> data=new ArrayList<>();
            data.addAll(youtubeBackDataList);
            youtubeFeedAdapter.removeAllData();
            youtubeFeedAdapter.addAll(data);
            youtubePlayer.loadVideo(playeridss.get(linkedHasMapindex-1),timein_millsec.get(playeridss.get(linkedHasMapindex-1)));
            allPreviousData.remove(allPreviousData.size());

        }
        else
        {
            timein_millsec.clear();
            playeridss.clear();
            //when user backpress then exectue this code
            clearYoutubeListing();
            linkedHasMapindex=0;
            draggablePanel.minimize();
//            draggablePanel.maximize();
        }


    }

    @Override
    public void closeDraggablePanel() {

        clearYoutubeListing();
        linkedHasMapindex=0;

    }

}