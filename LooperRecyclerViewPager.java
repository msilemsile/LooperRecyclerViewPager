package me.msile.tran.kotlintrandemo.looprecyclerviewpager;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * 循环RecyclerView Pager (4 * itemTotalCount)
 *
 * 1 --> 2 --> 3 --> 4|startEdge|1 --> 2 --> 3 --> 4|initPosition|1 --> 2 --> 3 --> 4|endEdge|1 --> 2 --> 3 --> 4
 *
 * When current scroll position = startEdge|| endEdge ,Then current scroll position will reset = initPosition
 */

public class LooperRecyclerViewPager extends RecyclerView {

    private static final String TAG = "LooperRecyclerViewPager";

    private LooperRecyclerAdapterWrapper mAdapterWrapper;
    private int mPageWidth;
    private int mPageHeight;
    private OnPageChangeListener mOnPageChangeListener;
    private int mScrollX;
    private int mScrollY;
    private boolean needInitScrollPos;
    private int mCurrentPosition;
    private int mCurrentOffset;
    private LooperPagerSnapHelper mPagerSnapHelper;
    private int mScrollState;

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
        mPageWidth = getMeasuredWidth();
        mPageHeight = getMeasuredHeight();
        if (needInitScrollPos && mPageWidth > 0 && mPageHeight > 0) {
            int realAdapterCount = mAdapterWrapper.getRealAdapterCount();
            mScrollX = mPageWidth * realAdapterCount;
            mScrollY = mPageHeight * realAdapterCount;
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
                calculateScrollPosition();

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
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
            mCurrentPosition = mScrollX / mPageWidth;
            mCurrentOffset = mScrollX - mCurrentPosition * mPageWidth;
        } else {
            mCurrentPosition = mScrollY / mPageHeight;
            mCurrentOffset = mScrollY - mCurrentPosition * mPageHeight;
        }
    }

    public void setOnPageChangeListener(OnPageChangeListener mPageChangeListener) {
        this.mOnPageChangeListener = mPageChangeListener;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            mAdapterWrapper = new LooperRecyclerAdapterWrapper(adapter);
            super.setAdapter(mAdapterWrapper);
            scrollToInitPosition();
        }
    }

    private void scrollToInitPosition() {
        int realAdapterCount = mAdapterWrapper.getRealAdapterCount();
        if (realAdapterCount > 1) {
            scrollToPosition(realAdapterCount * 2);
            if (mPageWidth > 0 && mPageHeight > 0) {
                mScrollX = mPageWidth * realAdapterCount;
                mScrollY = mPageHeight * realAdapterCount;
            } else {
                needInitScrollPos = true;
            }
            Log.d(TAG, "--scrollToInitPosition--");
        }
    }

    public interface OnPageChangeListener {
        void onPageScrolled(int position, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }

}
