package com.suleiman.pagination.utils;


import com.suleiman.pagination.models.Result;

public interface PaginationAdapterCallback {

    void retryPageLoad();

    void onItemsClickListener(Result result, int position);

    void emptyLayout();
}
