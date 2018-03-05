package view.com.youtubeviewdesign.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import view.com.youtubeviewdesign.R;
import view.com.youtubeviewdesign.interfaces.loadRetryListener;
import view.com.youtubeviewdesign.youtubemodel.Item;

/**
 * Created by dell on 3/5/2018.
 */

public class MainYoutubeFeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
        {
        Context context;
        List<Item> youtubeFeedData;

public static final int ITEM=0;
public static final int LOADING=1;
private boolean isLoadingAdded=false;
private boolean showRetry=false;
        String errorMsg=null;
        loadRetryListener loadRetryListeners;

public MainYoutubeFeedAdapter(Context context,loadRetryListener loadRetry)
        {

        this.context=context;
        youtubeFeedData=new ArrayList<>();
        this.loadRetryListeners= loadRetry;

        }
@Override
public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder=null;
        switch (viewType)
        {
        case ITEM:
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_feed_row,parent,false);
        viewHolder=  new YoutubeFeedViewHolder(view);
        break;
        case LOADING:
        View viewloder = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_progress, parent, false);
        viewHolder=new YoutubeFeedViewHolderLoding(viewloder);
        break;
        }
        return viewHolder;
        }

@Override
public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        Item youtubeItem= youtubeFeedData.get(position);

        switch (getItemViewType(position))
        {
        case ITEM:


final YoutubeFeedViewHolder youtubeFeedViewHolder = (YoutubeFeedViewHolder) holder;
        youtubeFeedViewHolder.title.setText(youtubeItem.getSnippet().getTitle());


        Glide.with(context)
        .load(youtubeItem.getSnippet().getThumbnails().getDefault().getUrl())
        .centerCrop()
        .placeholder(R.mipmap.ic_launcher)
        .into(youtubeFeedViewHolder.thumb);
        break;
        case LOADING:
        YoutubeFeedViewHolderLoding feedViewHolderLoding= (YoutubeFeedViewHolderLoding) holder;
        if(showRetry)
        {
        feedViewHolderLoding.mErrorLayout.setVisibility(View.VISIBLE);
        feedViewHolderLoding.mProgressBar.setVisibility(View.GONE);
        }
        else
        {
        feedViewHolderLoding.mErrorLayout.setVisibility(View.GONE);
        feedViewHolderLoding.mProgressBar.setVisibility(View.VISIBLE);
        }

        break;
        }


        }

@Override
public int getItemCount() {
        return youtubeFeedData.size();
        }
@Override
public int getItemViewType(int position) {

        if(position==youtubeFeedData.size()-1 && isLoadingAdded)
        {
        return LOADING;
        }
        else
        return ITEM;

        }



class YoutubeFeedViewHolder extends RecyclerView.ViewHolder
{
    TextView title;
    ImageView thumb;
    public YoutubeFeedViewHolder(View itemView) {
        super(itemView);
        title= (TextView) itemView.findViewById(R.id.title);
        thumb= (ImageView) itemView.findViewById(R.id.thumb);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadRetryListeners.rowItemClick(youtubeFeedData.get(getAdapterPosition()).getId().getVideoId());
            }
        });
    }


}


public class YoutubeFeedViewHolderLoding extends  RecyclerView.ViewHolder implements View.OnClickListener
{
    private ProgressBar mProgressBar;
    private ImageButton mRetryBtn;
    private TextView mErrorTxt;
    private LinearLayout mErrorLayout;

    public YoutubeFeedViewHolderLoding(View itemView) {
        super(itemView);
        mProgressBar = (ProgressBar) itemView.findViewById(R.id.loadmore_progress);
        mRetryBtn = (ImageButton) itemView.findViewById(R.id.loadmore_retry);
        mErrorTxt = (TextView) itemView.findViewById(R.id.loadmore_errortxt);
        mErrorLayout = (LinearLayout) itemView.findViewById(R.id.loadmore_errorlayout);
        mRetryBtn.setOnClickListener(this);
        mErrorLayout.setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.loadmore_retry:
            case R.id.loadmore_errorlayout:
                showRetry(false,null);
                loadRetryListeners.reLoad();
                break;

        }
    }
}




//--------------------------------List Operation functions----------------------------------------------------



    public void addAll(List<Item> youtubeItems) {

        for (Item result : youtubeItems) {
            add(result);
        }

    }
    public void removeAllData()
    {
        youtubeFeedData.clear();
        notifyDataSetChanged();
    }
    public List<Item> getAllListData()
    {
        return youtubeFeedData;
    }
    public void addLodingFooter()
    {
        isLoadingAdded=true;
        add(new Item());
    }
    public void add(Item result)
    {
        youtubeFeedData.add(result);
        notifyItemInserted(youtubeFeedData.size()-1);
    }
    public void removeLoadingFooter()
    {
        isLoadingAdded=false;
        int pos=youtubeFeedData.size()-1;
        Item result=  getItem(pos);

        if(result!=null)
        {
            youtubeFeedData.remove(pos);
            notifyItemRemoved(pos);
        }

    }
    public Item getItem(int position)
    {
        return youtubeFeedData.get(position);

    }

    public void showRetry(boolean status,String errorMsg)
    {
        showRetry=status;
        notifyItemChanged(youtubeFeedData.size()-1);
        if(errorMsg!=null)
            this.errorMsg=errorMsg;

    }
}
