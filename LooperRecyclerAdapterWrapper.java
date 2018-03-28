package me.msile.tran.kotlintrandemo.looprecyclerviewpager;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * 循环recycler view pager adapter
 */

public class LooperRecyclerAdapterWrapper extends RecyclerView.Adapter {

    private RecyclerView.Adapter realAdapter;

    public LooperRecyclerAdapterWrapper(RecyclerView.Adapter realAdapter) {
        this.realAdapter = realAdapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (realAdapter != null) {
            return realAdapter.onCreateViewHolder(parent, viewType);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (realAdapter != null) {
            realAdapter.onBindViewHolder(holder, getRealPosition(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (realAdapter == null) {
            return super.getItemViewType(position);
        }
        int realAdapterCount = getRealAdapterCount();
        if (realAdapterCount <= 1) {
            return realAdapter.getItemViewType(position);
        }
        int realPos = position % realAdapterCount;
        return realAdapter.getItemViewType(realPos);
    }

    @Override
    public int getItemCount() {
        if (realAdapter == null) {
            return 0;
        }
        int realAdapterCount = getRealAdapterCount();
        if (realAdapterCount <= 1) {
            return realAdapterCount;
        }
        return realAdapterCount * 4;
    }

    public int getRealPosition(int position) {
        int realAdapterCount = getRealAdapterCount();
        if (realAdapterCount <= 1) {
            return position;
        }
        return position % realAdapterCount;
    }

    public int getRealAdapterCount() {
        if (realAdapter == null) {
            return 0;
        }
        return realAdapter.getItemCount();
    }

}
