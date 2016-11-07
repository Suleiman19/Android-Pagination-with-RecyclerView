package com.suleiman.pagination;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.suleiman.pagination.models.TopRatedMovies;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Suleiman on 19/10/16.
 */

public class PaginationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;

    private List<TopRatedMovies> topRatedMoviesList;
    private Context context;

    private boolean isLoadingAdded = false;

    public PaginationAdapter(Context context) {
        this.context = context;
        topRatedMoviesList = new ArrayList<>();
    }

    public List<TopRatedMovies> getMovies() {
        return topRatedMoviesList;
    }

    public void setMovies(List<TopRatedMovies> topRatedMoviesList) {
        this.topRatedMoviesList = topRatedMoviesList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.item_progress, parent, false);
                viewHolder = new LoadingVH(v2);
                break;
        }
        return viewHolder;
    }

    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.item_list, parent, false);
        viewHolder = new MovieVH(v1);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        TopRatedMovies topRatedMovies = topRatedMoviesList.get(position);

        switch (getItemViewType(position)) {
            case ITEM:
                MovieVH movieVH = (MovieVH) holder;

//                movieVH.textView.setText(topRatedMoviesList.getTitle());
                break;
            case LOADING:
//                Do nothing
                break;
        }

    }

    @Override
    public int getItemCount() {
        return topRatedMoviesList == null ? 0 : topRatedMoviesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == topRatedMoviesList.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    /*
   Helpers
   _________________________________________________________________________________________________
    */

    public void add(TopRatedMovies mc) {
        topRatedMoviesList.add(mc);
        notifyItemInserted(topRatedMoviesList.size() - 1);
    }

    public void addAll(List<TopRatedMovies> topRatedMoviesList) {
        for (TopRatedMovies topRatedMovies : topRatedMoviesList) {
            add(topRatedMovies);
        }
    }

    public void remove(TopRatedMovies topRatedMovies) {
        int position = topRatedMoviesList.indexOf(topRatedMovies);
        if (position > -1) {
            topRatedMoviesList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public boolean isEmpty() {
        return getItemCount() == 0;
    }


    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new TopRatedMovies());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = topRatedMoviesList.size() - 1;
        TopRatedMovies topRatedMovies = getItem(position);

        if (topRatedMovies != null) {
            topRatedMoviesList.remove(position);
            notifyItemRemoved(position);
        }
    }

    public TopRatedMovies getItem(int position) {
        return topRatedMoviesList.get(position);
    }


   /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Main list's content ViewHolder
     */
    protected class MovieVH extends RecyclerView.ViewHolder {
        private TextView textView;

        public MovieVH(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.item_text);
        }
    }


    protected class LoadingVH extends RecyclerView.ViewHolder {

        public LoadingVH(View itemView) {
            super(itemView);
        }
    }


}
