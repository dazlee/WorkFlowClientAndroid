package com.nicloud.workflowclient.utility;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

import com.nicloud.workflowclient.R;

/**
 * Divider for vertical/horizontal RecyclerView
 */
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    private boolean mShowFirstDivider = false;
    private boolean mShowLastDivider = false;
    private boolean mShowLastPadding = false;

    private int mLastPaddingSize = 0;


    public DividerItemDecoration(Context context, AttributeSet attrs) {
        final TypedArray a = context
                .obtainStyledAttributes(attrs, new int[]{android.R.attr.listDivider});
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    public DividerItemDecoration(Context context, AttributeSet attrs,
                                 boolean showFirstDivider, boolean showLastDivider, boolean showLastPadding,
                                 int lastPaddingSize) {
        this(context, attrs);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
        mShowLastPadding = showLastPadding;
        mLastPaddingSize = lastPaddingSize;
    }

    public DividerItemDecoration(Drawable divider) {
        mDivider = divider;
    }

    public DividerItemDecoration(Drawable divider,
                                 boolean showFirstDivider, boolean showLastDivider, boolean showLastPadding,
                                 int lastPaddingSize) {
        this(divider);
        mShowFirstDivider = showFirstDivider;
        mShowLastDivider = showLastDivider;
        mShowLastPadding = showLastPadding;
        mLastPaddingSize = lastPaddingSize;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mDivider == null) {
            return;
        }

        int itemCount = parent.getAdapter().getItemCount();
        int itemPosition = parent.getChildAdapterPosition(view);

        if (itemPosition < 1) {
            return;
        }

        if (getOrientation(parent) == LinearLayoutManager.VERTICAL) {
            outRect.top = mDivider.getIntrinsicHeight();

            if (mShowLastPadding && itemPosition == itemCount - 1) {
                outRect.bottom = mLastPaddingSize;
            }

        } else {
            outRect.left = mDivider.getIntrinsicWidth();

            if (mShowLastPadding && itemPosition == itemCount - 1) {
                outRect.right = mLastPaddingSize;
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mDivider == null) {
            super.onDrawOver(c, parent, state);
            return;
        }

        // Initialization needed to avoid compiler warning
        int left = 0, right = 0, top = 0, bottom = 0, size;
        int orientation = getOrientation(parent);
        int childCount = parent.getChildCount();

        // Vertical
        if (orientation == LinearLayoutManager.VERTICAL) {
            size = mDivider.getIntrinsicHeight();
            left = parent.getPaddingLeft();
            right = parent.getWidth() - parent.getPaddingRight();
        } else { // Horizontal
            size = mDivider.getIntrinsicWidth();
            top = parent.getPaddingTop();
            bottom = parent.getHeight() - parent.getPaddingBottom();
        }

        for (int i = mShowFirstDivider ? 0 : 1; i < childCount; i++) {
            View child = parent.getChildAt(i);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.getTop() - params.topMargin - size;
                bottom = top + size;
            } else { //horizontal
                left = child.getLeft() - params.leftMargin - size;
                right = left + size;
            }

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }

        // show last divider
        if (mShowLastDivider && childCount > 0) {
            View child = parent.getChildAt(childCount - 1);
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            if (orientation == LinearLayoutManager.VERTICAL) {
                top = child.getBottom() + params.bottomMargin;
                bottom = top + size;
            } else { // horizontal
                left = child.getRight() + params.rightMargin;
                right = left + size;
            }
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

    private int getOrientation(RecyclerView parent) {
        if (parent.getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) parent.getLayoutManager();
            return layoutManager.getOrientation();
        } else {
            throw new IllegalStateException(
                    "DividerItemDecoration can only be used with a LinearLayoutManager.");
        }
    }
}