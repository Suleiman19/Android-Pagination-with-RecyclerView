package com.kotlinUtils

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bumptech.glide.DrawableRequestBuilder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.suleiman.pagination.R
import com.suleiman.pagination.models.Result
import com.suleiman.pagination.utils.PaginationAdapterCallback
import java.util.ArrayList

/**
 * Created by heinhtet on 12/9/2017.
 */


class PaginationAdapter(private val context: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var movieResults: MutableList<Result>? = null

    private var isLoadingAdded = false
    private var retryPageLoad = false

    private val mCallback: PaginationAdapterCallback

    private var errorMsg: String? = null

    var movies: MutableList<Result>?
        get() = movieResults
        set(movieResults) {
            this.movieResults = movieResults
        }

    val isEmpty: Boolean
        get() = itemCount == 0

    init {
        this.mCallback = context as PaginationAdapterCallback
        movieResults = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder? {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ITEM -> {
                val viewItem = inflater.inflate(R.layout.item_list, parent, false)
                viewHolder = MovieVH(viewItem)
            }
            LOADING -> {
                val viewLoading = inflater.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingVH(viewLoading)
            }
            HERO -> {
                val viewHero = inflater.inflate(R.layout.item_hero, parent, false)
                viewHolder = HeroVH(viewHero)
            }
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val result = movieResults!![position] // Movie

        when (getItemViewType(position)) {
            HERO -> {
                val heroVh = holder as HeroVH
                heroVh.mMovieTitle.text = result.title
                if (result.title != null) {
                    heroVh.mYear.text = formatYearLabel(result)
                } else {
                    heroVh.mYear.text = "null"
                }
                heroVh.mMovieDesc.text = result.overview
                loadImage(result.backdropPath)
                        .into(heroVh.mPosterImg)
            }
            ITEM -> {
                val movieVH = holder as MovieVH
                movieVH.mMovieTitle.text = result.title
                if (result.title != null) {
                    movieVH.mYear.text = formatYearLabel(result)
                }
                movieVH.mMovieDesc.text = result.overview
                // load movie thumbnail
                loadImage(result.posterPath)
                        .listener(object : RequestListener<String, GlideDrawable> {
                            override fun onException(e: Exception, model: String, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                                // TODO: 08/11/16 handle failure
                                movieVH.mProgress.visibility = View.GONE
                                return false
                            }

                            override fun onResourceReady(resource: GlideDrawable, model: String, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                                // image ready, hide progress now
                                movieVH.mProgress.visibility = View.GONE
                                return false   // return false if you want Glide to handle everything else.
                            }
                        }).into(movieVH.mPosterImg)
            }

            LOADING -> {
                val loadingVH = holder as LoadingVH

                if (retryPageLoad) {
                    loadingVH.mErrorLayout.visibility = View.VISIBLE
                    loadingVH.mProgressBar.visibility = View.GONE

                    loadingVH.mErrorTxt.text = if (errorMsg != null)
                        errorMsg
                    else
                        context.getString(R.string.error_msg_unknown)

                } else {
                    loadingVH.mErrorLayout.visibility = View.GONE
                    loadingVH.mProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun getItemCount(): Int {
        if (movieResults == null) {
            mCallback.emptyLayout()
            return 0
        } else {
            return movieResults!!.size
        }
        //        return movieResults == null ? 0 : movieResults.size()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 3 == 0) {
            HERO
        } else {
            if (position == movieResults!!.size - 1 && isLoadingAdded) LOADING else ITEM

        }
        //        if (position == 0) {
        //            return HERO;
        //        } else {
        //            return (position == movieResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
        //        }
    }

    /*
        Helpers - bind Views
   _________________________________________________________________________________________________
    */

    /**
     * @param result
     * @return [releasedate] | [2letterlangcode]
     */
    private fun formatYearLabel(result: Result?): String {
        return if (result != null) {
            (result.releaseDate.substring(0, 4)  // we want the year only

                    + " | "
                    + result.originalLanguage.toUpperCase())

        } else {
            ""
        }
    }

    /**
     * Using Glide to handle image loading.
     * Learn more about Glide here:
     * [](http://blog.grafixartist.com/image-gallery-app-android-studio-1-4-glide/)
     *
     * @param posterPath from [Result.getPosterPath]
     * @return Glide builder
     */
    var imageUrl: String? = null

    private fun loadImage(posterPath: String?): DrawableRequestBuilder<String> {

        if (posterPath != null) {
            if (posterPath.contains("profile_image") || posterPath.contains("scontent")) {
                imageUrl = posterPath
            } else {
                imageUrl = BASE_URL_IMG + posterPath
            }
        } else {
            imageUrl = BASE_URL_IMG + posterPath
        }

        return Glide
                .with(context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)   // cache both original & resized image
                .centerCrop()
                .crossFade()
    }

    /*
        Helpers - Pagination
   _________________________________________________________________________________________________
    */

    fun add(r: Result) {
        movieResults!!.add(r)
        notifyItemInserted(movieResults!!.size - 1)
    }

    fun addAll(moveResults: List<Result>) {
        for (result in moveResults) {
            add(result)
        }
    }

    fun remove(r: Result?) {
        val position = movieResults!!.indexOf(r)
        if (position > -1) {
            movieResults!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    /*remove specific row*/
    fun removeAtItemsPosition(position: Int) {
        movieResults!!.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, movieResults!!.size)
    }

    /*update row */
    fun updateItemsAtPosition(position: Int, result: Result) {
        movieResults!!.removeAt(position)
        notifyItemChanged(position)
        movieResults!!.add(position, result)
        notifyItemChanged(position, movieResults!!.size)

    }

    /*add new row*/
    fun addRow(result: Result) {
        movieResults!!.add(0, result)
        notifyItemRangeChanged(0, movieResults!!.size)
    }


    fun addLoadingFooter() {
        isLoadingAdded = true
        add(Result())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false

        val position = movieResults!!.size - 1
        val result = getItem(position)

        if (result != null) {
            movieResults!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): Result? {
        return movieResults!![position]
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(movieResults!!.size - 1)

        if (errorMsg != null) this.errorMsg = errorMsg
    }


    /*
   View Holders
   _________________________________________________________________________________________________
    */

    /**
     * Header ViewHolder
     */
    protected inner class HeroVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mMovieTitle: TextView
        val mMovieDesc: TextView
        val mYear: TextView // displays "year | language"
        val mPosterImg: ImageView

        init {
            mMovieTitle = itemView.findViewById(R.id.movie_title) as TextView
            mMovieDesc = itemView.findViewById(R.id.movie_desc) as TextView
            mYear = itemView.findViewById(R.id.movie_year) as TextView
            mPosterImg = itemView.findViewById(R.id.movie_poster) as ImageView
            itemView.setOnClickListener { mCallback.onItemsClickListener(movieResults!![adapterPosition], adapterPosition) }
        }
    }

    override fun getItemId(position: Int): Long {
        return movieResults!![position].id.toLong()
    }

    /**
     * Main list's content ViewHolder
     */
    protected inner class MovieVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mMovieTitle: TextView
        val mMovieDesc: TextView
        val mYear: TextView // displays "year | language"
        val mPosterImg: ImageView
        val mProgress: ProgressBar

        init {

            mMovieTitle = itemView.findViewById(R.id.movie_title) as TextView
            mMovieDesc = itemView.findViewById(R.id.movie_desc) as TextView
            mYear = itemView.findViewById(R.id.movie_year) as TextView
            mPosterImg = itemView.findViewById(R.id.movie_poster) as ImageView
            mProgress = itemView.findViewById(R.id.movie_progress) as ProgressBar
            itemView.setOnClickListener { mCallback.onItemsClickListener(movieResults!![adapterPosition], adapterPosition) }
        }
    }


    protected inner class LoadingVH(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mProgressBar: ProgressBar
        val mRetryBtn: ImageButton
        val mErrorTxt: TextView
        val mErrorLayout: LinearLayout

        init {

            mProgressBar = itemView.findViewById(R.id.loadmore_progress) as ProgressBar
            mRetryBtn = itemView.findViewById(R.id.loadmore_retry) as ImageButton
            mErrorTxt = itemView.findViewById(R.id.loadmore_errortxt) as TextView
            mErrorLayout = itemView.findViewById(R.id.loadmore_errorlayout) as LinearLayout

            mRetryBtn.setOnClickListener(this)
            mErrorLayout.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            when (view.id) {
                R.id.loadmore_retry, R.id.loadmore_errorlayout -> {
                    showRetry(false, null)
                    mCallback.retryPageLoad()
                }
            }
        }
    }

    companion object {
        // View Types
        val ITEM = 0
        val LOADING = 1
        val HERO = 2

        private val BASE_URL_IMG = "https://image.tmdb.org/t/p/w150"
    }

}
