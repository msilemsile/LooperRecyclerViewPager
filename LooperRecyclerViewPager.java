package me.msile.tran.kotlintrandemo.looprecyclerviewpager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
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
    private int mPageWidth;
    private int mPageHeight;
    private int mPageMargin;
    private OnPageChangeListener mOnPageChangeListener;
    private int mScrollX;
    private int mScrollY;
    private boolean needInitScrollPos;
    private int mCurrentPosition;
    private int mCurrentOffset;
    private LooperPagerSnapHelper mPagerSnapHelper;
    private int mScrollState;
    private int mScreenWidth, mScreenHeight;

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
        if (mPageWidth <= 0 || mPageHeight <= 0) {
            mPageWidth = getMeasuredWidth();
            mPageHeight = getMeasuredHeight();
            if (needInitScrollPos && mPageWidth > 0 && mPageHeight > 0) {
                int realAdapterCount = mAdapterWrapper.getRealAdapterCount();
                mScrollX = mPageWidth * realAdapterCount;
                mScrollY = mPageHeight * realAdapterCount;
            }
        }
    }

    private void init() {
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
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
            mCurrentPosition = mScrollX / mPageWidth;
            mCurrentOffset = mScrollX - mCurrentPosition * mPageWidth;
        } else {
            mCurrentPosition = mScrollY / mPageHeight;
            mCurrentOffset = mScrollY - mCurrentPosition * mPageHeight;
        }
    }

    public void setPageWidth(int mPageWidth) {
        this.mPageWidth = mPageWidth;
    }

    public void setPageHeight(int mPageHeight) {
        this.mPageHeight = mPageHeight;
    }

    public void setPageMargin(int mPageMargin) {
        this.mPageMargin = mPageMargin;
        mPageWidth += 2 * mPageMargin;
        mPageHeight += 2 * mPageMargin;
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
            if (mPageMargin > 0) {
                int scrollOffset = 0;
                LayoutManager layoutManager = getLayoutManager();
                int layoutDirection = layoutManager.canScrollHorizontally() ? 0 : 1;
                if (layoutDirection == 0) {
                    scrollOffset = (mScreenWidth - mPageWidth) / 2;
                } else {
                    scrollOffset = (mScreenHeight - mPageWidth) / 2;
                }
                if (layoutManager instanceof GridLayoutManager) {
                    ((GridLayoutManager) layoutManager).scrollToPositionWithOffset(realAdapterCount * 2, scrollOffset);
                } else if (layoutManager instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) layoutManager).scrollToPositionWithOffset(realAdapterCount * 2, scrollOffset);
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    ((StaggeredGridLayoutManager) layoutManager).scrollToPositionWithOffset(realAdapterCount * 2, scrollOffset);
                }
            } else {
                scrollToPosition(realAdapterCount * 2);
            }
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
