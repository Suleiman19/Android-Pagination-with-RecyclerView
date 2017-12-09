package com.kotlinUtils

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by heinhtet on 12/9/2017.
 */

abstract class PaginationScrollListener(manager: LinearLayoutManager): RecyclerView.OnScrollListener() {

    var layoutManager: LinearLayoutManager = manager

    override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                loadMoreItems()
            }
        }
    }


    protected abstract fun loadMoreItems()

    abstract fun getTotalPageCount(): Int

    abstract fun isLastPage(): Boolean

    abstract fun isLoading(): Boolean

}