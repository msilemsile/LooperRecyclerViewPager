package me.msile.tran.kotlintrandemo.looprecyclerviewpager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 循环RecyclerView Pager (4 * itemTotalCount)
 * <p>
 * 1 --> 2 --> 3 --> 4|startEdge|1 --> 2 --> 3 --> 4|initPosition|1 --> 2 --> 3 --> 4|endEdge|1 --> 2 --> 3 --> 4
 * <p>
 * When current scroll position = startEdge|| endEdge ,Then current scroll position will reset = initPosition
 */

public class LooperRecyclerViewPager extends RecyclerView {

    private static final String TAG = "LooperRecyclerViewPager";

    private LooperRecyclerAdapterWrapper mAdapterWrapper;
    private RecyclerView.Adapter mRealAdapter;
    private int mPageWidth, mPageHeight;
    private int mPageMargin;
    private int mPageSpace;
    private OnPageChangeListener mOnPageChangeListener;
    private int mScrollX;
    private int mScrollY;
    private boolean needInitScrollPos;
    private int mCurrentPosition;
    private int mCurrentOffset;
    private LooperPagerSnapHelper mPagerSnapHelper;
    private int mScrollState;
    private int mRvWidth, mRvHeight;

    public LooperRecyclerViewPager(Context context) {
        super(context);
        init();
    }

    public LooperRecyclerViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public LooperRecyclerViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return false;
        }
        if (mScrollState == RecyclerView.SCROLL_STATE_SETTLING) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
        mRvWidth = getMeasuredWidth();
        mRvHeight = getMeasuredHeight();
        if (needInitScrollPos && mRvWidth > 0 && mRvHeight > 0) {
            setRealAdapter();
            needInitScrollPos = false;
        }
    }

    private void init() {
        mPagerSnapHelper = new LooperPagerSnapHelper(this);
        mPagerSnapHelper.attachToRecyclerView(this);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (mPageWidth <= 0 || mPageHeight <= 0) {
                    return;
                }
                mScrollState = newState;
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScrollStateChanged(newState);
                }
                Log.d(TAG, "--onPageScrollStateChanged--state = " + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    calculateScrollPosition();
                    int realPosition = mAdapterWrapper.getRealPosition(mCurrentPosition);
                    Log.d(TAG, "--onPageSelected--selectPos = " + realPosition);
                    if (mOnPageChangeListener != null) {
                        mOnPageChangeListener.onPageSelected(realPosition);
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                Log.d(TAG, "--onScrolled--scrollX = " + mScrollX + " scrollY = " + mScrollY);
                mScrollX += dx;
                mScrollY += dy;
                calculateScrollPosition();
                Log.d(TAG, "--onPageScrolled--currentPos = " + mCurrentPosition + " currentOffset = " + mCurrentOffset);
                if (mOnPageChangeListener != null) {
                    mOnPageChangeListener.onPageScrolled(mCurrentPosition, mCurrentOffset);
                }
                if (mCurrentOffset == 0) {
                    View centerView = mPagerSnapHelper.findSnapView(getLayoutManager());
                    int centerPos = getChildLayoutPosition(centerView);
                    if (centerPos != -1) {
                        mCurrentPosition = centerPos;
                    }
                    int realAdapterCount = mAdapterWrapper.getRealAdapterCount();
                    int endLooperEdge = realAdapterCount * 3;
                    if (realAdapterCount > 1 && (mCurrentPosition == realAdapterCount || mCurrentPosition == endLooperEdge)) {
                        scrollToInitPosition();
                    }
                }
            }
        });
    }

    private void calculateScrollPosition() {
        if (mPageWidth <= 0 || mPageHeight <= 0) {
            return;
        }
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        int layoutDirection = layoutManager.canScrollHorizontally() ? 0 : 1;
        if (layoutDirection == 0) {
            mCurrentPosition = mScrollX / (mPageWidth + mPageMargin);
            mCurrentOffset = mScrollX - mCurrentPosition * (mPageWidth + mPageMargin);
        } else {
            mCurrentPosition = mScrollY / (mPageHeight + mPageMargin);
            mCurrentOffset = mScrollY - mCurrentPosition * (mPageHeight + mPageMargin);
        }
    }

    public void setPageMargin(int pageMargin, int pageSpace) {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        this.mPageMargin = pageMargin;
        this.mPageSpace = pageSpace;
        int layoutDirection = layoutManager.canScrollHorizontally() ? 0 : 1;
        if (layoutDirection == 0) {
            setPadding(pageMargin / 2 + mPageSpace, 0, pageMargin / 2 + mPageSpace, 0);
        } else {
            setPadding(0, pageMargin / 2 + mPageSpace, 0, pageMargin / 2 + mPageSpace);
        }
        setClipToPadding(false);
        setClipChildren(false);
    }

    private void calculatePageParams() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        int layoutDirection = layoutManager.canScrollHorizontally() ? 0 : 1;
        if (mRvWidth > 0) {
            if (layoutDirection == 0) {
                int maxPageWidth = mRvWidth - 2 * mPageMargin - 2 * mPageSpace;
                if (maxPageWidth <= 0) {
                    maxPageWidth = mRvWidth;
                    mPageMargin = 0;
                    mPageSpace = 0;
                }
                mPageWidth = maxPageWidth;
            } else {
                mPageWidth = mRvWidth;
            }
        }
        if (mRvHeight > 0) {
            if (layoutDirection == 1) {
                int maxPageHeight = mRvHeight - 2 * mPageMargin - 2 * mPageSpace;
                if (maxPageHeight <= 0) {
                    maxPageHeight = mRvHeight;
                    mPageMargin = 0;
                    mPageSpace = 0;
                }
                mPageHeight = maxPageHeight;
            } else {
                mPageHeight = mRvHeight;
            }
        }
        Log.d(TAG, "--calculatePageParams--" + "rvWidth = " + mRvWidth + " rvHeight = " + mRvHeight + " pageWidth = " + mPageWidth + " pageHeight = " + mPageHeight + " margin = " + mPageMargin + " space = " + mPageSpace);
    }

    public void setOnPageChangeListener(OnPageChangeListener mPageChangeListener) {
        this.mOnPageChangeListener = mPageChangeListener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            mRealAdapter = adapter;
            setRealAdapter();
        }
    }

    private void setRealAdapter() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        if (mRvWidth > 0 && mRvHeight > 0 && mRealAdapter != null) {
            calculatePageParams();
            int layoutDirection = getLayoutManager().canScrollHorizontally() ? 0 : 1;
            mAdapterWrapper = new LooperRecyclerAdapterWrapper(mRealAdapter, layoutDirection);
            mAdapterWrapper.setRvPageParams(mPageMargin, mPageWidth, mPageHeight);
            super.setAdapter(mAdapterWrapper);
            scrollToInitPosition();
        } else {
            needInitScrollPos = true;
        }
    }

    private void scrollToInitPosition() {
        int realAdapterCount = mAdapterWrapper.getRealAdapterCount();
        if (realAdapterCount > 1) {
            mScrollX = (mPageWidth + mPageMargin) * realAdapterCount;
            mScrollY = (mPageHeight + mPageMargin) * realAdapterCount;
            scrollToPosition(realAdapterCount * 2);
            Log.d(TAG, "--scrollToInitPosition--");
        }
    }

    public interface OnPageChangeListener {
        void onPageScrolled(int position, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }

}
