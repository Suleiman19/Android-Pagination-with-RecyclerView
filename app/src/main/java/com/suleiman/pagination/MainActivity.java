package com.suleiman.pagination;

import android.content.ClipData;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.kotlinUtils.RecyclerItemTouchHelper;
import com.suleiman.pagination.api.MovieApi;
import com.suleiman.pagination.api.MovieService;
import com.suleiman.pagination.models.Result;
import com.suleiman.pagination.models.TopRatedMovies;
import com.suleiman.pagination.utils.PaginationAdapterCallback;
import com.suleiman.pagination.utils.PaginationScrollListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements PaginationAdapterCallback, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG = "MainActivity";

    com.kotlinUtils.PaginationAdapter adapter;
    LinearLayoutManager linearLayoutManager;
    GridLayoutManager gridLayoutManager;
    List<Result> movieList;
    RecyclerView rv;
    ProgressBar progressBar;
    LinearLayout errorLayout;
    Button btnRetry;
    TextView txtError;
    SwipeRefreshLayout swipeRefreshLayout;
    private static final int PAGE_START = 1;

    private boolean isLoading = false;
    private boolean isLastPage = false;
    // limiting to 5 for this tutorial, since total pages in actual API is very large. Feel free to modify.
    private int TOTAL_PAGES = 5;
    private int currentPage = PAGE_START;

    private MovieService movieService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        movieList = new ArrayList<>();
        rv = (RecyclerView) findViewById(R.id.main_recycler);
        progressBar = (ProgressBar) findViewById(R.id.main_progress);
        errorLayout = (LinearLayout) findViewById(R.id.error_layout);
        btnRetry = (Button) findViewById(R.id.error_btn_retry);
        txtError = (TextView) findViewById(R.id.error_txt_cause);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeColors(Color.BLACK, Color.BLUE, Color.YELLOW, Color.GREEN);
        swipeRefreshLayout.setOnRefreshListener(this);
        adapter = new com.kotlinUtils.PaginationAdapter(this);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        gridLayoutManager = new GridLayoutManager(this, 2
        );
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (adapter.getItemViewType(position)) {
                    case PaginationAdapter.HERO:
                        return 2;
                    case PaginationAdapter.ITEM:
                        return 1;
                    case PaginationAdapter.LOADING:
                        return 2;
                    default:
                        return -1;
                }
            }
        });
        rv.setLayoutManager(gridLayoutManager);
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(adapter);


        rv.addOnScrollListener(new com.kotlinUtils.PaginationScrollListener(gridLayoutManager) {
            @Override
            protected void loadMoreItems() {
                Log.d("load more ", "call back");
                isLoading = true;
                currentPage += 1;
                loadApi("next");
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        //init service and load data
        movieService = MovieApi.getClient().create(MovieService.class);
        loadApi("first");
        btnRetry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadApi("first");
            }
        });

    }


    private void loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ");

        // To ensure list is visible when retry button in error view is clicked
        hideErrorView();

        callTopRatedMoviesApi().enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                // Got data. Send it to adapter

                hideErrorView();

                List<Result> results = fetchResults(response);
                progressBar.setVisibility(View.GONE);
                adapter.addAll(results);

                if (currentPage <= TOTAL_PAGES) {
                    Log.d("load need", "more");
                    isLoading = true;
                    adapter.addLoadingFooter();
                } else {
                    isLastPage = true;
                }

            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
                showErrorView(t);
            }
        });
    }

    /**
     * @param response extracts List<{@link Result>} from response
     * @return
     */
    private List<Result> fetchResults(Response<TopRatedMovies> response) {
        TopRatedMovies topRatedMovies = response.body();
        return topRatedMovies.getResults();
    }

    private void loadNextPage(String type) {
        if (type.equals("first")) {
            hideErrorView();
        }
        Log.d(TAG, "loadNextPage: " + currentPage);

        callTopRatedMoviesApi().enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                adapter.removeLoadingFooter();
                isLoading = false;

                List<Result> results = fetchResults(response);
                adapter.addAll(results);

                if (currentPage != TOTAL_PAGES) adapter.addLoadingFooter();
                else isLastPage = true;
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
                adapter.showRetry(true, fetchErrorMessage(t));
            }
        });
    }

    /*adding type and call api method write once*/
    private void loadApi(final String type) {

        if (type.equals("first")) {
            hideErrorView();
        }

        callTopRatedMoviesApi().enqueue(new Callback<TopRatedMovies>() {
            @Override
            public void onResponse(Call<TopRatedMovies> call, Response<TopRatedMovies> response) {
                if (type.equals("first")) {
                    hideSwipeRefresh();
                    Log.d(TAG, "loadFirstPage: " + currentPage);
                    hideErrorView();
                    List<Result> results = fetchResults(response);
                    progressBar.setVisibility(View.GONE);
                    adapter.addAll(results);
                } else {
                    Log.d(TAG, "loadNextPage: " + currentPage);
                    adapter.removeLoadingFooter();
                    isLoading = false;
                    List<Result> results = fetchResults(response);
                    adapter.addAll(results);
                }
                if (currentPage != TOTAL_PAGES) {
                    Log.d("load need", "");
                    adapter.addLoadingFooter();
                } else isLastPage = true;
            }

            @Override
            public void onFailure(Call<TopRatedMovies> call, Throwable t) {
                t.printStackTrace();
                if (type.equals("first")) {
                    showErrorView(t);
                } else {
                    adapter.showRetry(true, fetchErrorMessage(t));
                }
            }
        });
    }


    /**
     * Performs a Retrofit call to the top rated movies API.
     * Same API call for Pagination.
     * As {@link #currentPage} will be incremented automatically
     * by @{@link PaginationScrollListener} to load next page.
     */
    private Call<TopRatedMovies> callTopRatedMoviesApi() {
        return movieService.getTopRatedMovies(
                getString(R.string.my_api_key),
                "en_US",
                currentPage
        );
    }


    @Override
    public void retryPageLoad() {
        loadApi("more");
    }

    @Override
    public void onItemsClickListener(Result result, int position) {
        Toast.makeText(this, "click position " + position, Toast.LENGTH_SHORT).show();
        Result result1 = new Result();
        result1.setId(19);
        result1.setAdult(true);
        result1.setOriginalLanguage("MM");
        result1.setOriginalTitle("");
        result1.setReleaseDate("1993");
        result1.setBackdropPath("https://pbs.twimg.com/profile_images/737889751034920960/ATs6TR-T_400x400.jpg");
        result1.setPosterPath("https://pbs.twimg.com/profile_images/737889751034920960/ATs6TR-T_400x400.jpg");
        result1.setTitle("Hein Htet");
        result1.setGenreIds(new ArrayList<Integer>());
        adapter.updateItemsAtPosition(position, result1);
    }

    @Override
    public void emptyLayout() {

    }

    private void hideSwipeRefresh() {
        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }


    /**
     * @param throwable required for {@link #fetchErrorMessage(Throwable)}
     * @return
     */
    private void showErrorView(Throwable throwable) {

        if (errorLayout.getVisibility() == View.GONE) {
            hideSwipeRefresh();
            errorLayout.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            txtError.setText(fetchErrorMessage(throwable));
        }
    }

    /**
     * @param throwable to identify the type of error
     * @return appropriate error message
     */
    private String fetchErrorMessage(Throwable throwable) {
        String errorMsg = getResources().getString(R.string.error_msg_unknown);

        if (!isNetworkConnected()) {
            errorMsg = getResources().getString(R.string.error_msg_no_internet);
        } else if (throwable instanceof TimeoutException) {
            errorMsg = getResources().getString(R.string.error_msg_timeout);
        }

        return errorMsg;
    }

    // Helpers -------------------------------------------------------------------------------------


    private void hideErrorView() {
        if (errorLayout.getVisibility() == View.VISIBLE) {
            errorLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Remember to add android.permission.ACCESS_NETWORK_STATE permission.
     *
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }

    @Override
    public void onRefresh() {
        isLoading = false;
        isLastPage = false;
        currentPage = PAGE_START;
        adapter.clear();
        loadApi("first");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_row: {
                Result r = new Result();
                r.setPosterPath("https://scontent.xx.fbcdn.net/v/t1.0-9/s720x720/24862150_10154896375856035_2285275458086808430_n.jpg?oh=b8a8d3c097ad859ee24a2c1818003688&oe=5A8BE160");
                r.setBackdropPath("https://scontent.xx.fbcdn.net/v/t1.0-9/s720x720/24862150_10154896375856035_2285275458086808430_n.jpg?oh=b8a8d3c097ad859ee24a2c1818003688&oe=5A8BE160");
                r.setTitle("add row");
                r.setOriginalLanguage("En");
                r.setReleaseDate("13131");
                r.setId(242);
                r.setGenreIds(new ArrayList<Integer>());
                adapter.addRow(r);
            }
        }
        return super.onOptionsItemSelected(item);
    }

}
