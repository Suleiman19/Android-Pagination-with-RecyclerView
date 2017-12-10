package com.kotlinUtils

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log

/**
 * Created by heinhtet on 12/9/2017.
 */

abstract class PaginationScrollListener(manager: LinearLayoutManager) : RecyclerView.OnScrollListener() {

    var layoutManager: LinearLayoutManager = manager

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
        Log.i(" scroll change ", "visible count " + visibleItemCount)
        Log.i(" scroll change ", "total items count " + totalItemCount)
        Log.i(" scroll change ", "first visible items count " + firstVisibleItemPosition)

        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                Log.i("scroll load more items","")
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()

    abstract fun getTotalPageCount(): Int

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean

}