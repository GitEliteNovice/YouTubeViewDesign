package view.com.youtubeviewdesign.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Abhay on 7/6/17.
 */

public abstract  class PaginationScrollListener extends RecyclerView.OnScrollListener {

    LinearLayoutManager linearLayoutManager;
    public PaginationScrollListener(LinearLayoutManager linearLayoutManager)
    {
        this.linearLayoutManager=linearLayoutManager;
    }
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

        super.onScrolled(recyclerView, dx, dy);

        int visibleCount= linearLayoutManager.getChildCount();
         int totalItemCount=linearLayoutManager.getItemCount();

        int firstVisibleItemPosition=linearLayoutManager.findFirstVisibleItemPosition();
        if(!isLoading() && !isLastPage())
        {
            if ((visibleCount + firstVisibleItemPosition) >= totalItemCount
                    && firstVisibleItemPosition >= 0) {
                loadMoreItems();
            }
        }

    }

    public abstract boolean isLastPage();
    public abstract boolean isLoading();
    public abstract  void loadMoreItems();
    public  abstract int getTotalPageCount();

}
