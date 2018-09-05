import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * 循环recycler view pager adapter
 */

public class LooperRecyclerAdapterWrapper extends RecyclerView.Adapter {

    private RecyclerView.Adapter realAdapter;
    private int mLayoutDirection;
    private int mPageMargin;
    private int mPageWidth;
    private int mPageHeight;

    LooperRecyclerAdapterWrapper(RecyclerView.Adapter realAdapter, int layoutDirection) {
        this.realAdapter = realAdapter;
        mLayoutDirection = layoutDirection;
    }

    void setRvPageParams(int pageMargin, int pageWidth, int pageHeight) {
        mPageMargin = pageMargin;
        mPageWidth = pageWidth;
        mPageHeight = pageHeight;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (realAdapter != null) {
            RecyclerView.ViewHolder viewHolder = realAdapter.onCreateViewHolder(parent, viewType);
            RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(mPageWidth, mPageHeight);
            if (mLayoutDirection == 0) {
                layoutParams.rightMargin = mPageMargin / 2;
                layoutParams.leftMargin = mPageMargin / 2;
            } else {
                layoutParams.topMargin = mPageMargin / 2;
                layoutParams.bottomMargin = mPageMargin / 2;
            }
            viewHolder.itemView.setLayoutParams(layoutParams);
            return viewHolder;
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (realAdapter != null && holder != null) {
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
