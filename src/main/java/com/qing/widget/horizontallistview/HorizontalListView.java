package com.qing.widget.horizontallistview;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.support.v4.util.SparseArrayCompat;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.ListAdapter;
import android.widget.RemoteViews.RemoteView;

import java.util.ArrayList;

/**
 * if you invoke addHeaderView() or addFooterView(), you invoke setAdapter() must be in the end
 */
@RemoteView
public class HorizontalListView extends AbsHorizontalListView {

    static final int NO_POSITION = -1;
    private static final float MAX_SCROLL_FACTOR = 0.33F;
    private static final int MIN_SCROLL_PREVIEW_PIXELS = 2;
    private static final String LOG_TAG = HorizontalListView.class.getSimpleName();
    private ArrayList<FixedViewInfo> mHeaderViewInfos = new ArrayList();
    private ArrayList<FixedViewInfo> mFooterViewInfos = new ArrayList();
    Drawable mDivider;
    int mDividerWidth;
    int mMeasureWithChild;
    Drawable mOverScrollHeader;
    Drawable mOverScrollFooter;
    private boolean mIsCacheColorOpaque;
    private boolean mDividerIsOpaque;
    private boolean mHeaderDividersEnabled;
    private boolean mFooterDividersEnabled;
    private boolean mAreAllItemsSelectable = true;
    private boolean mItemsCanFocus = false;
    private final Rect mTempRect = new Rect();
    private Paint mDividerPaint;
    private final HorizontalListView.ArrowScrollFocusResult mArrowScrollFocusResult = new HorizontalListView.ArrowScrollFocusResult();
    private HorizontalListView.FocusSelector mFocusSelector;

    public HorizontalListView(Context context) {
        super(context);
        Theme theme = context.getTheme();
        Object entries = null;
        Object dividerDrawable = null;
        Object overscrollHeader = null;
        Object overscrollFooter = null;
        byte dividerWidth = 0;
        boolean headerDividersEnabled = true;
        boolean footerDividersEnabled = true;
        byte measureWithChild = -1;
        if (entries != null) {
            this.setAdapter((ListAdapter) (new ArrayAdapter(context, 17367043, (Object[]) entries)));
        }

        if (dividerDrawable != null) {
            this.setDivider((Drawable) dividerDrawable);
        }

        if (overscrollHeader != null) {
            this.setOverscrollHeader((Drawable) overscrollHeader);
        }

        if (overscrollFooter != null) {
            this.setOverscrollFooter((Drawable) overscrollFooter);
        }

        if (dividerWidth != 0) {
            this.setDividerWidth(dividerWidth);
        }

        this.mHeaderDividersEnabled = headerDividersEnabled;
        this.mFooterDividersEnabled = footerDividersEnabled;
        this.mMeasureWithChild = measureWithChild;
    }

    public int getMaxScrollAmount() {
        return (int) (0.33F * (float) (this.getRight() - this.getLeft()));
    }

    private void adjustViewsLeftOrRight() {
        int childCount = this.getChildCount();
        if (childCount > 0) {
            int delta;
            View child;
            if (!this.mStackFromRight) {
                child = this.getChildAt(0);
                delta = child.getLeft() - this.mListPadding.left;
                if (this.mFirstPosition != 0) {
                    delta -= this.mDividerWidth;
                }

                if (delta < 0) {
                    delta = 0;
                }
            } else {
                child = this.getChildAt(childCount - 1);
                delta = child.getRight() - (this.getWidth() - this.mListPadding.right);
                if (this.mFirstPosition + childCount < this.mItemCount) {
                    delta += this.mDividerWidth;
                }

                if (delta > 0) {
                    delta = 0;
                }
            }

            if (delta != 0) {
                this.offsetChildrenLeftAndRight(-delta);
            }
        }

    }

    public void addHeaderView(View v, Object data, boolean isSelectable) {
        if (this.mAdapter != null && !(this.mAdapter instanceof HeaderViewListAdapter)) {
            throw new IllegalStateException("Cannot add header view to list -- setAdapter has already been called.");
        } else {
            HorizontalListView.FixedViewInfo info = new HorizontalListView.FixedViewInfo();
            info.view = v;
            info.data = data;
            info.isSelectable = isSelectable;
            this.mHeaderViewInfos.add(info);
            if (this.mAdapter != null && this.mDataSetObserver != null) {
                this.mDataSetObserver.onChanged();
            }

        }
    }

    public void addHeaderView(View v) {
        this.addHeaderView(v, (Object) null, true);
    }

    public int getHeaderViewsCount() {
        return this.mHeaderViewInfos.size();
    }

    public boolean removeHeaderView(View v) {
        if (this.mHeaderViewInfos.size() > 0) {
            boolean result = false;
            if (this.mAdapter != null && ((HeaderViewListAdapter) this.mAdapter).removeHeader(v)) {
                if (this.mDataSetObserver != null) {
                    this.mDataSetObserver.onChanged();
                }

                result = true;
            }

            this.removeFixedViewInfo(v, this.mHeaderViewInfos);
            return result;
        } else {
            return false;
        }
    }

    private void removeFixedViewInfo(View v, ArrayList<FixedViewInfo> where) {
        int len = where.size();

        for (int i = 0; i < len; ++i) {
            HorizontalListView.FixedViewInfo info = (HorizontalListView.FixedViewInfo) where.get(i);
            if (info.view == v) {
                where.remove(i);
                break;
            }
        }

    }

    public void addFooterView(View v, Object data, boolean isSelectable) {
        HorizontalListView.FixedViewInfo info = new HorizontalListView.FixedViewInfo();
        info.view = v;
        info.data = data;
        info.isSelectable = isSelectable;
        this.mFooterViewInfos.add(info);
        if (this.mAdapter != null && this.mDataSetObserver != null) {
            this.mDataSetObserver.onChanged();
        }

    }

    public void addFooterView(View v) {
        this.addFooterView(v, (Object) null, true);
    }

    public int getFooterViewsCount() {
        return this.mFooterViewInfos.size();
    }

    public boolean removeFooterView(View v) {
        if (this.mFooterViewInfos.size() > 0) {
            boolean result = false;
            if (this.mAdapter != null && ((HeaderViewListAdapter) this.mAdapter).removeFooter(v)) {
                if (this.mDataSetObserver != null) {
                    this.mDataSetObserver.onChanged();
                }

                result = true;
            }

            this.removeFixedViewInfo(v, this.mFooterViewInfos);
            return result;
        } else {
            return false;
        }
    }

    public ListAdapter getAdapter() {
        return this.mAdapter;
    }

    public void setAdapter(ListAdapter adapter) {
        if (this.mAdapter != null && this.mDataSetObserver != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
        }

        this.resetList();
        this.mRecycler.clear();
        if (this.mHeaderViewInfos.size() <= 0 && this.mFooterViewInfos.size() <= 0) {
            this.mAdapter = adapter;
        } else {
            this.mAdapter = new HeaderViewListAdapter(this.mHeaderViewInfos, this.mFooterViewInfos, adapter);
        }

        this.mOldSelectedPosition = -1;
        this.mOldSelectedColId = -9223372036854775808L;
        super.setAdapter(adapter);
        if (this.mAdapter != null) {
            this.mAreAllItemsSelectable = this.mAdapter.areAllItemsEnabled();
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
            this.checkFocus();
            this.mDataSetObserver = new AdapterDataSetObserver(this);
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            this.mRecycler.setViewTypeCount(this.mAdapter.getViewTypeCount());
            int position;
            if (this.mStackFromRight) {
                position = this.lookForSelectablePosition(this.mItemCount - 1, false);
            } else {
                position = this.lookForSelectablePosition(0, true);
            }

            this.setSelectedPositionInt(position);
            this.setNextSelectedPositionInt(position);
            if (this.mItemCount == 0) {
                this.checkSelectionChanged();
            }
        } else {
            this.mAreAllItemsSelectable = true;
            this.checkFocus();
            this.checkSelectionChanged();
        }

        this.requestLayout();
    }

    protected void resetList() {
        this.clearRecycledState(this.mHeaderViewInfos);
        this.clearRecycledState(this.mFooterViewInfos);
        super.resetList();
        this.mLayoutMode = 0;
    }

    private void clearRecycledState(ArrayList<FixedViewInfo> infos) {
        if (infos != null) {
            int count = infos.size();

            for (int i = 0; i < count; ++i) {
                View child = ((HorizontalListView.FixedViewInfo) infos.get(i)).view;
                LayoutParams p = (LayoutParams) child.getLayoutParams();
                if (p != null) {
                    p.recycledHeaderFooter = false;
                }
            }
        }

    }

    private boolean showingLeftFadingEdge() {
        int listLeft = this.getScrollX() + this.mListPadding.left;
        return this.mFirstPosition > 0 || this.getChildAt(0).getLeft() > listLeft;
    }

    private boolean showingRightFadingEdge() {
        int childCount = this.getChildCount();
        int rightOfRightChild = this.getChildAt(childCount - 1).getRight();
        int lastVisiblePosition = this.mFirstPosition + childCount - 1;
        int listRight = this.getScrollX() + this.getWidth() - this.mListPadding.right;
        return lastVisiblePosition < this.mItemCount - 1 || rightOfRightChild < listRight;
    }

    public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate) {
        int rectLeftWithinChild = rect.left;
        rect.offset(child.getLeft(), child.getTop());
        rect.offset(-child.getScrollX(), -child.getScrollY());
        int width = this.getWidth();
        int listUnfadedLeft = this.getScrollX();
        int listUnfadedRight = listUnfadedLeft + width;
        int fadingEdge = this.getHorizontalFadingEdgeLength();
        if (this.showingLeftFadingEdge() && (this.mSelectedPosition > 0 || rectLeftWithinChild > fadingEdge)) {
            listUnfadedLeft += fadingEdge;
        }

        int childCount = this.getChildCount();
        int rightOfRightChild = this.getChildAt(childCount - 1).getRight();
        if (this.showingRightFadingEdge() && (this.mSelectedPosition < this.mItemCount - 1 || rect.right < rightOfRightChild - fadingEdge)) {
            listUnfadedRight -= fadingEdge;
        }

        int scrollXDelta = 0;
        int scroll;
        if (rect.right > listUnfadedRight && rect.left > listUnfadedLeft) {
            if (rect.width() > width) {
                scrollXDelta += rect.left - listUnfadedLeft;
            } else {
                scrollXDelta += rect.right - listUnfadedRight;
            }

            scroll = rightOfRightChild - listUnfadedRight;
            scrollXDelta = Math.min(scrollXDelta, scroll);
        } else if (rect.left < listUnfadedLeft && rect.right < listUnfadedRight) {
            if (rect.width() > width) {
                scrollXDelta -= listUnfadedRight - rect.right;
            } else {
                scrollXDelta -= listUnfadedLeft - rect.left;
            }

            scroll = this.getChildAt(0).getLeft();
            int deltaToLeft = scroll - listUnfadedLeft;
            scrollXDelta = Math.max(scrollXDelta, deltaToLeft);
        }

        boolean scroll1 = scrollXDelta != 0;
        if (scroll1) {
            this.scrollListItemsBy(-scrollXDelta);
            this.positionSelector(-1, child);
            this.mSelectedLeft = child.getTop();
            this.invalidate();
        }

        return scroll1;
    }

    protected void fillGap(boolean down) {
        int count = this.getChildCount();
        byte paddingRight;
        int startOffset;
        if (down) {
            paddingRight = 0;
            startOffset = count > 0 ? this.getChildAt(count - 1).getRight() + this.mDividerWidth : paddingRight;
            this.fillRight(this.mFirstPosition + count, startOffset);
            this.correctTooWide(this.getChildCount());
        } else {
            paddingRight = 0;
            startOffset = count > 0 ? this.getChildAt(0).getLeft() - this.mDividerWidth : this.getWidth() - paddingRight;
            this.fillLeft(this.mFirstPosition - 1, startOffset);
            this.correctTooSmall(this.getChildCount());
        }

    }

    private View fillRight(int pos, int nextLeft) {
        View selectedView = null;

        for (int end = this.getRight() - this.getLeft(); nextLeft < end && pos < this.mItemCount; ++pos) {
            boolean selected = pos == this.mSelectedPosition;
            View child = this.makeAndAddView(pos, nextLeft, true, this.mListPadding.top, selected);
            nextLeft = child.getRight() + this.mDividerWidth;
            if (selected) {
                selectedView = child;
            }
        }

        this.setVisibleRangeHint(this.mFirstPosition, this.mFirstPosition + this.getChildCount() - 1);
        return selectedView;
    }

    private View fillLeft(int pos, int nextRight) {
        View selectedView = null;

        for (byte end = 0; nextRight > end && pos >= 0; --pos) {
            boolean selected = pos == this.mSelectedPosition;
            View child = this.makeAndAddView(pos, nextRight, false, this.mListPadding.top, selected);
            nextRight = child.getLeft() - this.mDividerWidth;
            if (selected) {
                selectedView = child;
            }
        }

        this.mFirstPosition = pos + 1;
        this.setVisibleRangeHint(this.mFirstPosition, this.mFirstPosition + this.getChildCount() - 1);
        return selectedView;
    }

    private View fillFromLeft(int nextLeft) {
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mSelectedPosition);
        this.mFirstPosition = Math.min(this.mFirstPosition, this.mItemCount - 1);
        if (this.mFirstPosition < 0) {
            this.mFirstPosition = 0;
        }

        return this.fillRight(this.mFirstPosition, nextLeft);
    }

    private View fillFromMiddle(int childrenLeft, int childrenRight) {
        int width = childrenRight - childrenLeft;
        int position = this.reconcileSelectedPosition();
        View sel = this.makeAndAddView(position, childrenLeft, true, this.mListPadding.top, true);
        this.mFirstPosition = position;
        int selWidth = sel.getMeasuredWidth();
        if (selWidth <= width) {
            sel.offsetLeftAndRight((width - selWidth) / 2);
        }

        this.fillBeforeAndAfter(sel, position);
        if (!this.mStackFromRight) {
            this.correctTooWide(this.getChildCount());
        } else {
            this.correctTooSmall(this.getChildCount());
        }

        return sel;
    }

    private void fillBeforeAndAfter(View sel, int position) {
        int dividerWidth = this.mDividerWidth;
        if (!this.mStackFromRight) {
            this.fillLeft(position - 1, sel.getLeft() - dividerWidth);
            this.adjustViewsLeftOrRight();
            this.fillRight(position + 1, sel.getRight() + dividerWidth);
        } else {
            this.fillRight(position + 1, sel.getRight() + dividerWidth);
            this.adjustViewsLeftOrRight();
            this.fillLeft(position - 1, sel.getLeft() - dividerWidth);
        }

    }

    private View fillFromSelection(int selectedLeft, int childrenLeft, int childrenRight) {
        int fadingEdgeLength = this.getHorizontalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int leftSelectionPixel = this.getLeftSelectionPixel(childrenLeft, fadingEdgeLength, selectedPosition);
        int rightSelectionPixel = this.getRightSelectionPixel(childrenRight, fadingEdgeLength, selectedPosition);
        View sel = this.makeAndAddView(selectedPosition, selectedLeft, true, this.mListPadding.top, true);
        int spaceBefore;
        int spaceAfter;
        int offset;
        if (sel.getRight() > rightSelectionPixel) {
            spaceBefore = sel.getLeft() - leftSelectionPixel;
            spaceAfter = sel.getRight() - rightSelectionPixel;
            offset = Math.min(spaceBefore, spaceAfter);
            sel.offsetLeftAndRight(-offset);
        } else if (sel.getLeft() < leftSelectionPixel) {
            spaceBefore = leftSelectionPixel - sel.getLeft();
            spaceAfter = rightSelectionPixel - sel.getRight();
            offset = Math.min(spaceBefore, spaceAfter);
            sel.offsetLeftAndRight(offset);
        }

        this.fillBeforeAndAfter(sel, selectedPosition);
        if (!this.mStackFromRight) {
            this.correctTooWide(this.getChildCount());
        } else {
            this.correctTooSmall(this.getChildCount());
        }

        return sel;
    }

    private int getRightSelectionPixel(int childrenRight, int fadingEdgeLength, int selectedPosition) {
        int rightSelectionPixel = childrenRight;
        if (selectedPosition != this.mItemCount - 1) {
            rightSelectionPixel = childrenRight - fadingEdgeLength;
        }

        return rightSelectionPixel;
    }

    private int getLeftSelectionPixel(int childrenLeft, int fadingEdgeLength, int selectedPosition) {
        int leftSelectionPixel = childrenLeft;
        if (selectedPosition > 0) {
            leftSelectionPixel = childrenLeft + fadingEdgeLength;
        }

        return leftSelectionPixel;
    }

    public void smoothScrollToPosition(int position) {
        super.smoothScrollToPosition(position);
    }

    public void smoothScrollByOffset(int offset) {
        super.smoothScrollByOffset(offset);
    }

    private View moveSelection(View oldSel, View newSel, int delta, int childrenLeft, int childrenRight) {
        int fadingEdgeLength = this.getHorizontalFadingEdgeLength();
        int selectedPosition = this.mSelectedPosition;
        int leftSelectionPixel = this.getLeftSelectionPixel(childrenLeft, fadingEdgeLength, selectedPosition);
        int rightSelectionPixel = this.getRightSelectionPixel(childrenLeft, fadingEdgeLength, selectedPosition);
        View sel;
        int oldLeft;
        int newRight;
        int halfHorizontalSpace;
        int offset;
        if (delta > 0) {
            oldSel = this.makeAndAddView(selectedPosition - 1, oldSel.getLeft(), true, this.mListPadding.top, false);
            oldLeft = this.mDividerWidth;
            sel = this.makeAndAddView(selectedPosition, oldSel.getRight() + oldLeft, true, this.mListPadding.top, true);
            if (sel.getRight() > rightSelectionPixel) {
                newRight = sel.getLeft() - leftSelectionPixel;
                halfHorizontalSpace = sel.getRight() - rightSelectionPixel;
                offset = (childrenRight - childrenLeft) / 2;
                int offset1 = Math.min(newRight, halfHorizontalSpace);
                offset1 = Math.min(offset1, offset);
                oldSel.offsetLeftAndRight(-offset1);
                sel.offsetLeftAndRight(-offset1);
            }

            if (!this.mStackFromRight) {
                this.fillLeft(this.mSelectedPosition - 2, sel.getLeft() - oldLeft);
                this.adjustViewsLeftOrRight();
                this.fillRight(this.mSelectedPosition + 1, sel.getRight() + oldLeft);
            } else {
                this.fillRight(this.mSelectedPosition + 1, sel.getRight() + oldLeft);
                this.adjustViewsLeftOrRight();
                this.fillLeft(this.mSelectedPosition - 2, sel.getLeft() - oldLeft);
            }
        } else if (delta < 0) {
            if (newSel != null) {
                sel = this.makeAndAddView(selectedPosition, newSel.getLeft(), true, this.mListPadding.top, true);
            } else {
                sel = this.makeAndAddView(selectedPosition, oldSel.getLeft(), false, this.mListPadding.top, true);
            }

            if (sel.getLeft() < leftSelectionPixel) {
                oldLeft = leftSelectionPixel - sel.getLeft();
                newRight = rightSelectionPixel - sel.getRight();
                halfHorizontalSpace = (childrenRight - childrenLeft) / 2;
                offset = Math.min(oldLeft, newRight);
                offset = Math.min(offset, halfHorizontalSpace);
                sel.offsetLeftAndRight(offset);
            }

            this.fillBeforeAndAfter(sel, selectedPosition);
        } else {
            oldLeft = oldSel.getLeft();
            sel = this.makeAndAddView(selectedPosition, oldLeft, true, this.mListPadding.top, true);
            if (oldLeft < childrenLeft) {
                newRight = sel.getRight();
                if (newRight < childrenLeft + 20) {
                    sel.offsetLeftAndRight(childrenLeft - sel.getLeft());
                }
            }

            this.fillBeforeAndAfter(sel, selectedPosition);
        }

        return sel;
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (this.getChildCount() > 0) {
            View focusedChild = this.getFocusedChild();
            if (focusedChild != null) {
                int childPosition = this.mFirstPosition + this.indexOfChild(focusedChild);
                int childRight = focusedChild.getRight();
                int offset = Math.max(0, childRight - (w - this.getPaddingLeft()));
                int left = focusedChild.getLeft() - offset;
                if (this.mFocusSelector == null) {
                    this.mFocusSelector = new HorizontalListView.FocusSelector();
                }

                this.post(this.mFocusSelector.setup(childPosition, left));
            }
        }

        super.onSizeChanged(w, h, oldw, oldh);
    }

    @TargetApi(11)
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int childWidth = 0;
        int childHeight = 0;
        int childState = 0;
        this.mItemCount = this.mAdapter == null ? 0 : this.mAdapter.getCount();
        if (this.mItemCount > 0 && (widthMode == 0 || heightMode == 0)) {
            View result = this.obtainView(0, this.mIsScrap);
            this.measureScrapChildWidth(result, 0, heightMeasureSpec);
            childWidth = result.getMeasuredWidth();
            childHeight = result.getMeasuredHeight();
            if (VERSION.SDK_INT >= 11) {
                childState = combineMeasuredStates(childState, result.getMeasuredState());
            }

            if (this.recycleOnMeasure() && this.mRecycler.shouldRecycleViewType(((LayoutParams) result.getLayoutParams()).viewType)) {
                this.mRecycler.addScrapView(result, -1);
            }
        }

        if (heightMode == 0) {
            heightSize = this.mListPadding.top + this.mListPadding.bottom + childHeight + this.getHorizontalScrollbarHeight();
        } else if (heightMode == -2147483648 && this.mItemCount > 0 && this.mMeasureWithChild > -1) {
            int[] result1 = this.measureWithLargeChildren(heightMeasureSpec, this.mMeasureWithChild, this.mMeasureWithChild, widthSize, heightSize, -1);
            heightSize = result1[1];
        } else if (VERSION.SDK_INT >= 11) {
            heightSize |= childState & -16777216;
        }

        if (widthMode == 0) {
            widthSize = this.mListPadding.left + this.mListPadding.right + childWidth + this.getHorizontalFadingEdgeLength() * 2;
        }

        if (widthMode == -2147483648) {
            widthSize = this.measureWidthOfChildren(heightMeasureSpec, 0, -1, widthSize, -1);
        }

        this.setMeasuredDimension(widthSize, heightSize);
        this.mHeightMeasureSpec = heightMeasureSpec;
    }

    private void measureScrapChildWidth(View child, int position, int heightMeasureSpec) {
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) this.generateDefaultLayoutParams();
            child.setLayoutParams(p);
        }

        p.viewType = this.mAdapter.getItemViewType(position);
        p.forceAdd = true;
        int childHeightSpec = ViewGroup.getChildMeasureSpec(heightMeasureSpec, this.mListPadding.top + this.mListPadding.bottom, p.height);
        int lpWidth = p.width;
        int childWidthSpec;
        if (lpWidth > 0) {
            childWidthSpec = MeasureSpec.makeMeasureSpec(lpWidth, MeasureSpec.EXACTLY);
        } else {
            childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        child.measure(childWidthSpec, childHeightSpec);
    }

    public int[] measureChild(View child) {
        this.measureItem(child);
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        return new int[]{w, h};
    }

    @ExportedProperty(
            category = "list"
    )
    protected boolean recycleOnMeasure() {
        return true;
    }

    final int measureWidthOfChildren(int heightMeasureSpec, int startPosition, int endPosition, int maxWidth, int disallowPartialChildPosition) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null) {
            return this.mListPadding.left + this.mListPadding.right;
        } else {
            int returnedWidth = this.mListPadding.left + this.mListPadding.right;
            int dividerWidth = this.mDividerWidth > 0 && this.mDivider != null ? this.mDividerWidth : 0;
            int prevWidthWithoutPartialChild = 0;
            endPosition = endPosition == -1 ? adapter.getCount() - 1 : endPosition;
            RecycleBin recycleBin = this.mRecycler;
            boolean recyle = this.recycleOnMeasure();
            boolean[] isScrap = this.mIsScrap;

            for (int i = startPosition; i <= endPosition; ++i) {
                View child = this.obtainView(i, isScrap);
                this.measureScrapChildWidth(child, i, heightMeasureSpec);
                if (i > 0) {
                    returnedWidth += dividerWidth;
                }

                if (recyle && recycleBin.shouldRecycleViewType(((LayoutParams) child.getLayoutParams()).viewType)) {
                    recycleBin.addScrapView(child, -1);
                }

                returnedWidth += child.getMeasuredWidth();
                if (returnedWidth >= maxWidth) {
                    return disallowPartialChildPosition >= 0 && i > disallowPartialChildPosition && prevWidthWithoutPartialChild > 0 && returnedWidth != maxWidth ? prevWidthWithoutPartialChild : maxWidth;
                }

                if (disallowPartialChildPosition >= 0 && i >= disallowPartialChildPosition) {
                    prevWidthWithoutPartialChild = returnedWidth;
                }
            }

            return returnedWidth;
        }
    }

    final int[] measureWithLargeChildren(int heightMeasureSpec, int startPosition, int endPosition, int maxWidth, int maxHeight, int disallowPartialChildPosition) {
        ListAdapter adapter = this.mAdapter;
        if (adapter == null) {
            return new int[]{this.mListPadding.left + this.mListPadding.right, this.mListPadding.top + this.mListPadding.bottom};
        } else {
            int returnedWidth = this.mListPadding.left + this.mListPadding.right;
            int returnedHeight = this.mListPadding.top + this.mListPadding.bottom;
            int dividerWidth = this.mDividerWidth > 0 && this.mDivider != null ? this.mDividerWidth : 0;
            int childWidth = 0;
            int childHeight = 0;
            endPosition = endPosition == -1 ? adapter.getCount() - 1 : endPosition;
            RecycleBin recycleBin = this.mRecycler;
            boolean recyle = this.recycleOnMeasure();
            boolean[] isScrap = this.mIsScrap;

            for (int i = startPosition; i <= endPosition; ++i) {
                View child = this.obtainView(i, isScrap);
                this.measureScrapChildWidth(child, i, heightMeasureSpec);
                if (recyle && recycleBin.shouldRecycleViewType(((LayoutParams) child.getLayoutParams()).viewType)) {
                    recycleBin.addScrapView(child, -1);
                }

                childWidth = Math.max(childWidth, child.getMeasuredWidth() + dividerWidth);
                childHeight = Math.max(childHeight, child.getMeasuredHeight());
            }

            returnedWidth += childWidth;
            returnedHeight += childHeight;
            return new int[]{Math.min(returnedWidth, maxWidth), Math.min(returnedHeight, maxHeight)};
        }
    }

    protected int findMotionCol(int x) {
        int childCount = this.getChildCount();
        if (childCount > 0) {
            int i;
            View v;
            if (!this.mStackFromRight) {
                for (i = 0; i < childCount; ++i) {
                    v = this.getChildAt(i);
                    if (x <= v.getRight()) {
                        return this.mFirstPosition + i;
                    }
                }
            } else {
                for (i = childCount - 1; i >= 0; --i) {
                    v = this.getChildAt(i);
                    if (x >= v.getLeft()) {
                        return this.mFirstPosition + i;
                    }
                }
            }
        }

        return -1;
    }

    private View fillSpecific(int position, int left) {
        boolean tempIsSelected = position == this.mSelectedPosition;
        View temp = this.makeAndAddView(position, left, true, this.mListPadding.top, tempIsSelected);
        this.mFirstPosition = position;
        int dividerWidth = this.mDividerWidth;
        View before;
        View after;
        int childCount;
        if (!this.mStackFromRight) {
            before = this.fillLeft(position - 1, temp.getLeft() - dividerWidth);
            this.adjustViewsLeftOrRight();
            after = this.fillRight(position + 1, temp.getRight() + dividerWidth);
            childCount = this.getChildCount();
            if (childCount > 0) {
                this.correctTooWide(childCount);
            }
        } else {
            after = this.fillRight(position + 1, temp.getRight() + dividerWidth);
            this.adjustViewsLeftOrRight();
            before = this.fillLeft(position - 1, temp.getLeft() - dividerWidth);
            childCount = this.getChildCount();
            if (childCount > 0) {
                this.correctTooSmall(childCount);
            }
        }

        return tempIsSelected ? temp : (before != null ? before : after);
    }

    private void correctTooWide(int childCount) {
        int lastPosition = this.mFirstPosition + childCount - 1;
        if (lastPosition == this.mItemCount - 1 && childCount > 0) {
            View lastChild = this.getChildAt(childCount - 1);
            int lastRight = lastChild.getRight();
            int end = this.getRight() - this.getLeft() - this.mListPadding.right;
            int rightOffset = end - lastRight;
            View firstChild = this.getChildAt(0);
            int firstLeft = firstChild.getLeft();
            if (rightOffset > 0 && (this.mFirstPosition > 0 || firstLeft < this.mListPadding.top)) {
                if (this.mFirstPosition == 0) {
                    rightOffset = Math.min(rightOffset, this.mListPadding.top - firstLeft);
                }

                this.offsetChildrenLeftAndRight(rightOffset);
                if (this.mFirstPosition > 0) {
                    this.fillLeft(this.mFirstPosition - 1, firstChild.getLeft() - this.mDividerWidth);
                    this.adjustViewsLeftOrRight();
                }
            }
        }

    }

    private void correctTooSmall(int childCount) {
        if (this.mFirstPosition == 0 && childCount > 0) {
            View firstChild = this.getChildAt(0);
            int firstLeft = firstChild.getLeft();
            int start = this.mListPadding.left;
            int end = this.getRight() - this.getLeft() - this.mListPadding.right;
            int leftOffset = firstLeft - start;
            View lastChild = this.getChildAt(childCount - 1);
            int lastRight = lastChild.getRight();
            int lastPosition = this.mFirstPosition + childCount - 1;
            if (leftOffset > 0) {
                if (lastPosition >= this.mItemCount - 1 && lastRight <= end) {
                    if (lastPosition == this.mItemCount - 1) {
                        this.adjustViewsLeftOrRight();
                    }
                } else {
                    if (lastPosition == this.mItemCount - 1) {
                        leftOffset = Math.min(leftOffset, lastRight - end);
                    }

                    this.offsetChildrenLeftAndRight(-leftOffset);
                    if (lastPosition < this.mItemCount - 1) {
                        this.fillRight(lastPosition + 1, lastChild.getRight() + this.mDividerWidth);
                        this.adjustViewsLeftOrRight();
                    }
                }
            }
        }

    }

    protected void layoutChildren() {
        boolean blockLayoutRequests = this.mBlockLayoutRequests;
        if (!blockLayoutRequests) {
            this.mBlockLayoutRequests = true;

            try {
                super.layoutChildren();
                this.invalidate();
                if (this.mAdapter == null) {
                    this.resetList();
                    this.invokeOnItemScrollListener();
                    return;
                }

                int childrenLeft = this.mListPadding.left;
                int childrenRight = this.getRight() - this.getLeft() - this.mListPadding.right;
                int childCount = this.getChildCount();
                boolean index = false;
                int delta = 0;
                View oldSel = null;
                View oldFirst = null;
                View newSel = null;
                View focusLayoutRestoreView = null;
                int var22;
                switch (this.mLayoutMode) {
                    case 1:
                    case 3:
                    case 4:
                    case 5:
                        break;
                    case 2:
                        var22 = this.mNextSelectedPosition - this.mFirstPosition;
                        if (var22 >= 0 && var22 < childCount) {
                            newSel = this.getChildAt(var22);
                        }
                        break;
                    case 6:
                    default:
                        var22 = this.mSelectedPosition - this.mFirstPosition;
                        if (var22 >= 0 && var22 < childCount) {
                            oldSel = this.getChildAt(var22);
                        }

                        oldFirst = this.getChildAt(0);
                        if (this.mNextSelectedPosition >= 0) {
                            delta = this.mNextSelectedPosition - this.mSelectedPosition;
                        }

                        newSel = this.getChildAt(var22 + delta);
                }

                boolean dataChanged = this.mDataChanged;
                if (dataChanged) {
                    this.handleDataChanged();
                }

                if (this.mItemCount != 0) {
                    if (this.mItemCount != this.mAdapter.getCount()) {
                        throw new IllegalStateException("The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread. [in ListView(" + this.getId() + ", " + this.getClass() + ") with Adapter(" + this.mAdapter.getClass() + ")]");
                    }

                    this.setSelectedPositionInt(this.mNextSelectedPosition);
                    int firstPosition = this.mFirstPosition;
                    RecycleBin recycleBin = this.mRecycler;
                    View focusLayoutRestoreDirectChild = null;
                    if (dataChanged) {
                        for (int focusedChild = 0; focusedChild < childCount; ++focusedChild) {
                            recycleBin.addScrapView(this.getChildAt(focusedChild), firstPosition + focusedChild);
                        }
                    } else {
                        recycleBin.fillActiveViews(childCount, firstPosition);
                    }

                    View var23 = this.getFocusedChild();
                    if (var23 != null) {
                        if (!dataChanged || this.isDirectChildHeaderOrFooter(var23)) {
                            focusLayoutRestoreDirectChild = var23;
                            focusLayoutRestoreView = this.findFocus();
                            if (focusLayoutRestoreView != null) {
                                focusLayoutRestoreView.onStartTemporaryDetach();
                            }
                        }

                        this.requestFocus();
                    }

                    this.detachAllViewsFromParent();
                    recycleBin.removeSkippedScrap();
                    View sel;
                    switch (this.mLayoutMode) {
                        case 1:
                            this.mFirstPosition = 0;
                            sel = this.fillFromLeft(childrenLeft);
                            this.adjustViewsLeftOrRight();
                            break;
                        case 2:
                            if (newSel != null) {
                                sel = this.fillFromSelection(newSel.getLeft(), childrenLeft, childrenRight);
                            } else {
                                sel = this.fillFromMiddle(childrenLeft, childrenRight);
                            }
                            break;
                        case 3:
                            sel = this.fillLeft(this.mItemCount - 1, childrenRight);
                            this.adjustViewsLeftOrRight();
                            break;
                        case 4:
                            sel = this.fillSpecific(this.reconcileSelectedPosition(), this.mSpecificLeft);
                            break;
                        case 5:
                            sel = this.fillSpecific(this.mSyncPosition, this.mSpecificLeft);
                            break;
                        case 6:
                            sel = this.moveSelection(oldSel, newSel, delta, childrenLeft, childrenRight);
                            break;
                        default:
                            if (childCount == 0) {
                                int child;
                                if (!this.mStackFromRight) {
                                    child = this.lookForSelectablePosition(0, true);
                                    this.setSelectedPositionInt(child);
                                    sel = this.fillFromLeft(childrenLeft);
                                } else {
                                    child = this.lookForSelectablePosition(this.mItemCount - 1, false);
                                    this.setSelectedPositionInt(child);
                                    sel = this.fillLeft(this.mItemCount - 1, childrenRight);
                                }
                            } else if (this.mSelectedPosition >= 0 && this.mSelectedPosition < this.mItemCount) {
                                sel = this.fillSpecific(this.mSelectedPosition, oldSel == null ? childrenLeft : oldSel.getLeft());
                            } else if (this.mFirstPosition < this.mItemCount) {
                                sel = this.fillSpecific(this.mFirstPosition, oldFirst == null ? childrenLeft : oldFirst.getLeft());
                            } else {
                                sel = this.fillSpecific(0, childrenLeft);
                            }
                    }

                    recycleBin.scrapActiveViews();
                    if (sel != null) {
                        if (this.mItemsCanFocus && this.hasFocus() && !sel.hasFocus()) {
                            boolean var25 = sel == focusLayoutRestoreDirectChild && focusLayoutRestoreView != null && focusLayoutRestoreView.requestFocus() || sel.requestFocus();
                            if (!var25) {
                                View focused = this.getFocusedChild();
                                if (focused != null) {
                                    focused.clearFocus();
                                }

                                this.positionSelector(-1, sel);
                            } else {
                                sel.setSelected(false);
                                this.mSelectorRect.setEmpty();
                            }
                        } else {
                            this.positionSelector(-1, sel);
                        }

                        this.mSelectedLeft = sel.getLeft();
                    } else {
                        if (this.mTouchMode > 0 && this.mTouchMode < 3) {
                            View var24 = this.getChildAt(this.mMotionPosition - this.mFirstPosition);
                            if (var24 != null) {
                                this.positionSelector(this.mMotionPosition, var24);
                            }
                        } else {
                            this.mSelectedLeft = 0;
                            this.mSelectorRect.setEmpty();
                        }

                        if (this.hasFocus() && focusLayoutRestoreView != null) {
                            focusLayoutRestoreView.requestFocus();
                        }
                    }

                    if (focusLayoutRestoreView != null && focusLayoutRestoreView.getWindowToken() != null) {
                        focusLayoutRestoreView.onFinishTemporaryDetach();
                    }

                    this.mLayoutMode = 0;
                    this.mDataChanged = false;
                    if (this.mPositionScrollAfterLayout != null) {
                        this.post(this.mPositionScrollAfterLayout);
                        this.mPositionScrollAfterLayout = null;
                    }

                    this.mNeedSync = false;
                    this.setNextSelectedPositionInt(this.mSelectedPosition);
                    this.updateScrollIndicators();
                    if (this.mItemCount > 0) {
                        this.checkSelectionChanged();
                    }

                    this.invokeOnItemScrollListener();
                    this.resetInCenter();
                    return;
                }

                this.resetList();
                this.invokeOnItemScrollListener();
            } finally {
                if (!blockLayoutRequests) {
                    this.mBlockLayoutRequests = false;
                }

            }

        }
    }

    private View findAccessibilityFocusedChild(View focusedView) {
        ViewParent viewParent;
        for (viewParent = focusedView.getParent(); viewParent instanceof View && viewParent != this; viewParent = viewParent.getParent()) {
            focusedView = (View) viewParent;
        }

        return !(viewParent instanceof View) ? null : focusedView;
    }

    private boolean isDirectChildHeaderOrFooter(View child) {
        ArrayList headers = this.mHeaderViewInfos;
        int numHeaders = headers.size();

        for (int footers = 0; footers < numHeaders; ++footers) {
            if (child == ((HorizontalListView.FixedViewInfo) headers.get(footers)).view) {
                return true;
            }
        }

        ArrayList var7 = this.mFooterViewInfos;
        int numFooters = var7.size();

        for (int i = 0; i < numFooters; ++i) {
            if (child == ((HorizontalListView.FixedViewInfo) var7.get(i)).view) {
                return true;
            }
        }

        return false;
    }

    private View makeAndAddView(int position, int x, boolean flow, int childrenTop, boolean selected) {
        View child;
        if (!this.mDataChanged) {
            child = this.mRecycler.getActiveView(position);
            if (child != null) {
                this.setupChild(child, position, x, flow, childrenTop, selected, true);
                return child;
            }
        }

        child = this.obtainView(position, this.mIsScrap);
        this.setupChild(child, position, x, flow, childrenTop, selected, this.mIsScrap[0]);
        return child;
    }

    @TargetApi(11)
    private void setupChild(View child, int position, int x, boolean flowDown, int childrenTop, boolean selected, boolean recycled) {
        boolean isSelected = selected && this.shouldShowSelector();
        boolean updateChildSelected = isSelected != child.isSelected();
        int mode = this.mTouchMode;
        boolean isPressed = mode > 0 && mode < 3 && this.mMotionPosition == position;
        boolean updateChildPressed = isPressed != child.isPressed();
        boolean needToMeasure = !recycled || updateChildSelected || child.isLayoutRequested();
        LayoutParams p = (LayoutParams) child.getLayoutParams();
        if (p == null) {
            p = (LayoutParams) this.generateDefaultLayoutParams();
        }

        p.viewType = this.mAdapter.getItemViewType(position);
        if ((!recycled || p.forceAdd) && (!p.recycledHeaderFooter || p.viewType != -2)) {
            p.forceAdd = false;
            if (p.viewType == -2) {
                p.recycledHeaderFooter = true;
            }

            this.addViewInLayout(child, flowDown ? -1 : 0, p, true);
        } else {
            this.attachViewToParent(child, flowDown ? -1 : 0, p);
        }

        if (updateChildSelected) {
            child.setSelected(isSelected);
        }

        if (updateChildPressed) {
            child.setPressed(isPressed);
        }

        if (this.mChoiceMode != 0 && this.mCheckStates != null) {
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(((Boolean) this.mCheckStates.get(position, Boolean.valueOf(false))).booleanValue());
            } else if (VERSION.SDK_INT >= 11) {
                child.setActivated(((Boolean) this.mCheckStates.get(position, Boolean.valueOf(false))).booleanValue());
            }
        }

        int w;
        int childLeft;
        int h;
        if (needToMeasure) {
            w = ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mListPadding.top + this.mListPadding.bottom, p.height);
            h = p.width;
            if (h > 0) {
                childLeft = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            } else {
                childLeft = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
            }

            child.measure(childLeft, w);
        } else {
            this.cleanupLayoutState(child);
        }

        w = child.getMeasuredWidth();
        h = child.getMeasuredHeight();
        childLeft = flowDown ? x : x - w;
        if (needToMeasure) {
            int childBottom = childrenTop + h;
            int childRight = childLeft + w;
            child.layout(childLeft, childrenTop, childRight, childBottom);
        } else {
            child.offsetLeftAndRight(childLeft - child.getLeft());
            child.offsetTopAndBottom(childrenTop - child.getTop());
        }

        if (this.mCachingStarted && !child.isDrawingCacheEnabled()) {
            child.setDrawingCacheEnabled(true);
        }

        if (VERSION.SDK_INT >= 11 && recycled && ((LayoutParams) child.getLayoutParams()).scrappedFromPosition != position) {
            child.jumpDrawablesToCurrentState();
        }

    }

    protected boolean canAnimate() {
        return super.canAnimate() && this.mItemCount > 0;
    }

    public void setSelection(int position) {
        this.setSelectionFromLeft(position, 0);
    }

    public void setSelectionFromLeft(int position, int x) {
        if (this.mAdapter != null) {
            if (!this.isInTouchMode()) {
                position = this.lookForSelectablePosition(position, true);
                if (position >= 0) {
                    this.setNextSelectedPositionInt(position);
                }
            } else {
                this.mResurrectToPosition = position;
            }

            if (position >= 0) {
                this.mLayoutMode = 4;
                this.mSpecificLeft = this.mListPadding.left + x;
                if (this.mNeedSync) {
                    this.mSyncPosition = position;
                    this.mSyncColId = this.mAdapter.getItemId(position);
                }

                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }

                this.requestLayout();
            }

        }
    }

    public void setSelectionInt(int position) {
        this.setNextSelectedPositionInt(position);
        boolean awakeScrollbars = false;
        int selectedPosition = this.mSelectedPosition;
        if (selectedPosition >= 0) {
            if (position == selectedPosition - 1) {
                awakeScrollbars = true;
            } else if (position == selectedPosition + 1) {
                awakeScrollbars = true;
            }
        }

        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }

        this.layoutChildren();
        if (awakeScrollbars) {
            this.awakenScrollBars();
        }

    }

    protected int lookForSelectablePosition(int position, boolean lookDown) {
        ListAdapter adapter = this.mAdapter;
        if (adapter != null && !this.isInTouchMode()) {
            int count = adapter.getCount();
            if (this.mAreAllItemsSelectable) {
                return position >= 0 && position < count ? position : -1;
            } else {
                if (lookDown) {
                    for (position = Math.max(0, position); position < count && !adapter.isEnabled(position); ++position) {
                        ;
                    }
                } else {
                    for (position = Math.min(position, count - 1); position >= 0 && !adapter.isEnabled(position); --position) {
                        ;
                    }
                }

                return position >= 0 && position < count ? position : -1;
            }
        } else {
            return -1;
        }
    }

    public void setSelectionAfterHeaderView() {
        int count = this.mHeaderViewInfos.size();
        if (count > 0) {
            this.mNextSelectedPosition = 0;
        } else {
            if (this.mAdapter != null) {
                this.setSelection(count);
            } else {
                this.mNextSelectedPosition = count;
                this.mLayoutMode = 2;
            }

        }
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        boolean handled = super.dispatchKeyEvent(event);
        if (!handled) {
            View focused = this.getFocusedChild();
            if (focused != null && event.getAction() == 0) {
                handled = this.onKeyDown(event.getKeyCode(), event);
            }
        }

        return handled;
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return this.commonKey(keyCode, 1, event);
    }

    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return this.commonKey(keyCode, repeatCount, event);
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return this.commonKey(keyCode, 1, event);
    }

    @TargetApi(11)
    private boolean commonKey(int keyCode, int count, KeyEvent event) {
        if (this.mAdapter != null && this.mIsAttached) {
            if (this.mDataChanged) {
                this.layoutChildren();
            }

            if (VERSION.SDK_INT < 11) {
                return false;
            } else {
                boolean handled = false;
                int action = event.getAction();
                if (action != 1) {
                    switch (keyCode) {
                        case 19:
                            if (event.hasNoModifiers()) {
                                handled = this.handleHorizontalFocusWithinListItem(33);
                            }
                            break;
                        case 20:
                            if (event.hasNoModifiers()) {
                                handled = this.handleHorizontalFocusWithinListItem(130);
                            }
                            break;
                        case 21:
                            if (event.hasNoModifiers()) {
                                handled = this.resurrectSelectionIfNeeded();
                                if (!handled) {
                                    while (count-- > 0 && this.arrowScroll(33)) {
                                        handled = true;
                                    }
                                }
                            } else if (event.hasModifiers(2)) {
                                handled = this.resurrectSelectionIfNeeded() || this.fullScroll(33);
                            }
                            break;
                        case 22:
                            if (event.hasNoModifiers()) {
                                handled = this.resurrectSelectionIfNeeded();
                                if (!handled) {
                                    while (count-- > 0 && this.arrowScroll(130)) {
                                        handled = true;
                                    }
                                }
                            } else if (event.hasModifiers(2)) {
                                handled = this.resurrectSelectionIfNeeded() || this.fullScroll(130);
                            }
                            break;
                        case 23:
                        case 66:
                            if (event.hasNoModifiers()) {
                                handled = this.resurrectSelectionIfNeeded();
                                if (!handled && event.getRepeatCount() == 0 && this.getChildCount() > 0) {
                                    this.keyPressed();
                                    handled = true;
                                }
                            }
                        case 61:
                        default:
                            break;
                        case 62:
                            boolean var10000;
                            if (event.hasNoModifiers()) {
                                if (!this.resurrectSelectionIfNeeded() && !this.pageScroll(130)) {
                                    var10000 = false;
                                } else {
                                    var10000 = true;
                                }
                            } else if (event.hasModifiers(1)) {
                                if (!this.resurrectSelectionIfNeeded() && !this.pageScroll(33)) {
                                    var10000 = false;
                                } else {
                                    var10000 = true;
                                }
                            }

                            handled = true;
                            break;
                        case 92:
                            if (event.hasNoModifiers()) {
                                handled = this.resurrectSelectionIfNeeded() || this.pageScroll(33);
                            } else if (event.hasModifiers(2)) {
                                handled = this.resurrectSelectionIfNeeded() || this.fullScroll(33);
                            }
                            break;
                        case 93:
                            if (event.hasNoModifiers()) {
                                handled = this.resurrectSelectionIfNeeded() || this.pageScroll(130);
                            } else if (event.hasModifiers(2)) {
                                handled = this.resurrectSelectionIfNeeded() || this.fullScroll(130);
                            }
                            break;
                        case 122:
                            if (event.hasNoModifiers()) {
                                handled = this.resurrectSelectionIfNeeded() || this.fullScroll(33);
                            }
                            break;
                        case 123:
                            if (event.hasNoModifiers()) {
                                handled = this.resurrectSelectionIfNeeded() || this.fullScroll(130);
                            }
                    }
                }

                if (handled) {
                    return true;
                } else {
                    switch (action) {
                        case 0:
                            return super.onKeyDown(keyCode, event);
                        case 1:
                            return super.onKeyUp(keyCode, event);
                        case 2:
                            return super.onKeyMultiple(keyCode, count, event);
                        default:
                            return false;
                    }
                }
            }
        } else {
            return false;
        }
    }

    boolean pageScroll(int direction) {
        int nextPage = -1;
        boolean down = false;
        if (direction == 33) {
            nextPage = Math.max(0, this.mSelectedPosition - this.getChildCount() - 1);
        } else if (direction == 130) {
            nextPage = Math.min(this.mItemCount - 1, this.mSelectedPosition + this.getChildCount() - 1);
            down = true;
        }

        if (nextPage >= 0) {
            int position = this.lookForSelectablePosition(nextPage, down);
            if (position >= 0) {
                this.mLayoutMode = 4;
                this.mSpecificLeft = this.getPaddingLeft() + this.getHorizontalFadingEdgeLength();
                if (down && position > this.mItemCount - this.getChildCount()) {
                    this.mLayoutMode = 3;
                }

                if (!down && position < this.getChildCount()) {
                    this.mLayoutMode = 1;
                }

                this.setSelectionInt(position);
                this.invokeOnItemScrollListener();
                if (!this.awakenScrollBars()) {
                    this.invalidate();
                }

                return true;
            }
        }

        return false;
    }

    boolean fullScroll(int direction) {
        boolean moved = false;
        int position;
        if (direction == 33) {
            if (this.mSelectedPosition != 0) {
                position = this.lookForSelectablePosition(0, true);
                if (position >= 0) {
                    this.mLayoutMode = 1;
                    this.setSelectionInt(position);
                    this.invokeOnItemScrollListener();
                }

                moved = true;
            }
        } else if (direction == 130 && this.mSelectedPosition < this.mItemCount - 1) {
            position = this.lookForSelectablePosition(this.mItemCount - 1, true);
            if (position >= 0) {
                this.mLayoutMode = 3;
                this.setSelectionInt(position);
                this.invokeOnItemScrollListener();
            }

            moved = true;
        }

        if (moved && !this.awakenScrollBars()) {
            this.awakenScrollBars();
            this.invalidate();
        }

        return moved;
    }

    private boolean handleHorizontalFocusWithinListItem(int direction) {
        if (direction != 33 && direction != 130) {
            throw new IllegalArgumentException("direction must be one of {View.FOCUS_UP, View.FOCUS_DOWN}");
        } else {
            int numChildren = this.getChildCount();
            if (this.mItemsCanFocus && numChildren > 0 && this.mSelectedPosition != -1) {
                View selectedView = this.getSelectedView();
                if (selectedView != null && selectedView.hasFocus() && selectedView instanceof ViewGroup) {
                    View currentFocus = selectedView.findFocus();
                    View nextFocus = FocusFinder.getInstance().findNextFocus((ViewGroup) selectedView, currentFocus, direction);
                    if (nextFocus != null) {
                        currentFocus.getFocusedRect(this.mTempRect);
                        this.offsetDescendantRectToMyCoords(currentFocus, this.mTempRect);
                        this.offsetRectIntoDescendantCoords(nextFocus, this.mTempRect);
                        if (nextFocus.requestFocus(direction, this.mTempRect)) {
                            return true;
                        }
                    }

                    View globalNextFocus = FocusFinder.getInstance().findNextFocus((ViewGroup) this.getRootView(), currentFocus, direction);
                    if (globalNextFocus != null) {
                        return this.isViewAncestorOf(globalNextFocus, this);
                    }
                }
            }

            return false;
        }
    }

    boolean arrowScroll(int direction) {
        boolean var3;
        try {
            this.mInLayout = true;
            boolean handled = this.arrowScrollImpl(direction);
            if (handled) {
                this.playSoundEffect(SoundEffectConstants.getContantForFocusDirection(direction));
            }

            var3 = handled;
        } finally {
            this.mInLayout = false;
        }

        return var3;
    }

    private boolean arrowScrollImpl(int direction) {
        if (this.getChildCount() <= 0) {
            return false;
        } else {
            View selectedView = this.getSelectedView();
            int selectedPos = this.mSelectedPosition;
            int nextSelectedPosition = this.lookForSelectablePositionOnScreen(direction);
            int amountToScroll = this.amountToScroll(direction, nextSelectedPosition);
            HorizontalListView.ArrowScrollFocusResult focusResult = this.mItemsCanFocus ? this.arrowScrollFocused(direction) : null;
            if (focusResult != null) {
                nextSelectedPosition = focusResult.getSelectedPosition();
                amountToScroll = focusResult.getAmountToScroll();
            }

            boolean needToRedraw = focusResult != null;
            View focused;
            if (nextSelectedPosition != -1) {
                this.handleNewSelectionChange(selectedView, direction, nextSelectedPosition, focusResult != null);
                this.setSelectedPositionInt(nextSelectedPosition);
                this.setNextSelectedPositionInt(nextSelectedPosition);
                selectedView = this.getSelectedView();
                selectedPos = nextSelectedPosition;
                if (this.mItemsCanFocus && focusResult == null) {
                    focused = this.getFocusedChild();
                    if (focused != null) {
                        focused.clearFocus();
                    }
                }

                needToRedraw = true;
                this.checkSelectionChanged();
            }

            if (amountToScroll > 0) {
                this.scrollListItemsBy(direction == 33 ? amountToScroll : -amountToScroll);
                needToRedraw = true;
            }

            if (this.mItemsCanFocus && focusResult == null && selectedView != null && selectedView.hasFocus()) {
                focused = selectedView.findFocus();
                if (!this.isViewAncestorOf(focused, this) || this.distanceToView(focused) > 0) {
                    focused.clearFocus();
                }
            }

            if (nextSelectedPosition == -1 && selectedView != null && !this.isViewAncestorOf(selectedView, this)) {
                selectedView = null;
                this.hideSelector();
                this.mResurrectToPosition = -1;
            }

            if (needToRedraw) {
                if (selectedView != null) {
                    this.positionSelector(selectedPos, selectedView);
                    this.mSelectedLeft = selectedView.getLeft();
                }

                if (!this.awakenScrollBars()) {
                    this.invalidate();
                }

                this.invokeOnItemScrollListener();
                return true;
            } else {
                return false;
            }
        }
    }

    private void handleNewSelectionChange(View selectedView, int direction, int newSelectedPosition, boolean newFocusAssigned) {
        if (newSelectedPosition == -1) {
            throw new IllegalArgumentException("newSelectedPosition needs to be valid");
        } else {
            boolean leftSelected = false;
            int selectedIndex = this.mSelectedPosition - this.mFirstPosition;
            int nextSelectedIndex = newSelectedPosition - this.mFirstPosition;
            View leftView;
            View rightView;
            int leftViewIndex;
            int rightViewIndex;
            if (direction == 33) {
                leftViewIndex = nextSelectedIndex;
                rightViewIndex = selectedIndex;
                leftView = this.getChildAt(nextSelectedIndex);
                rightView = selectedView;
                leftSelected = true;
            } else {
                leftViewIndex = selectedIndex;
                rightViewIndex = nextSelectedIndex;
                leftView = selectedView;
                rightView = this.getChildAt(nextSelectedIndex);
            }

            int numChildren = this.getChildCount();
            if (leftView != null) {
                leftView.setSelected(!newFocusAssigned && leftSelected);
                this.measureAndAdjustRight(leftView, leftViewIndex, numChildren);
            }

            if (rightView != null) {
                rightView.setSelected(!newFocusAssigned && !leftSelected);
                this.measureAndAdjustRight(rightView, rightViewIndex, numChildren);
            }

        }
    }

    private void measureAndAdjustRight(View child, int childIndex, int numChildren) {
        int oldWidth = child.getWidth();
        this.measureItem(child);
        if (child.getMeasuredWidth() != oldWidth) {
            this.relayoutMeasuredItem(child);
            int widthDelta = child.getMeasuredWidth() - oldWidth;

            for (int i = childIndex + 1; i < numChildren; ++i) {
                this.getChildAt(i).offsetLeftAndRight(widthDelta);
            }
        }

    }

    private void measureItem(View child) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = new ViewGroup.LayoutParams(-2, -1);
        }

        int childHeightSpec = ViewGroup.getChildMeasureSpec(this.mHeightMeasureSpec, this.mListPadding.top + this.mListPadding.bottom, p.height);
        int lpWidth = p.width;
        int childWidthSpec;
        if (lpWidth > 0) {
            childWidthSpec = MeasureSpec.makeMeasureSpec(lpWidth, MeasureSpec.EXACTLY);
        } else {
            childWidthSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        child.measure(childWidthSpec, childHeightSpec);
    }

    private void relayoutMeasuredItem(View child) {
        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();
        int childTop = this.mListPadding.top;
        int childBottom = childTop + h;
        int childLeft = child.getLeft();
        int childRight = childLeft + w;
        child.layout(childLeft, childTop, childRight, childBottom);
    }

    private int getArrowScrollPreviewLength() {
        return Math.max(2, this.getHorizontalFadingEdgeLength());
    }

    private int amountToScroll(int direction, int nextSelectedPosition) {
        int listRight = this.getWidth() - this.mListPadding.right;
        int listLeft = this.mListPadding.left;
        int numChildren = this.getChildCount();
        int indexToMakeVisible;
        int positionToMakeVisible;
        View viewToMakeVisible;
        int goalLeft;
        int amountToScroll;
        int max;
        if (direction == 130) {
            indexToMakeVisible = numChildren - 1;
            if (nextSelectedPosition != -1) {
                indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
            }

            positionToMakeVisible = this.mFirstPosition + indexToMakeVisible;
            viewToMakeVisible = this.getChildAt(indexToMakeVisible);
            goalLeft = listRight;
            if (positionToMakeVisible < this.mItemCount - 1) {
                goalLeft = listRight - this.getArrowScrollPreviewLength();
            }

            if (viewToMakeVisible.getRight() <= goalLeft) {
                return 0;
            } else if (nextSelectedPosition != -1 && goalLeft - viewToMakeVisible.getLeft() >= this.getMaxScrollAmount()) {
                return 0;
            } else {
                amountToScroll = viewToMakeVisible.getRight() - goalLeft;
                if (this.mFirstPosition + numChildren == this.mItemCount) {
                    max = this.getChildAt(numChildren - 1).getRight() - listRight;
                    amountToScroll = Math.min(amountToScroll, max);
                }

                return Math.min(amountToScroll, this.getMaxScrollAmount());
            }
        } else {
            indexToMakeVisible = 0;
            if (nextSelectedPosition != -1) {
                indexToMakeVisible = nextSelectedPosition - this.mFirstPosition;
            }

            positionToMakeVisible = this.mFirstPosition + indexToMakeVisible;
            viewToMakeVisible = this.getChildAt(indexToMakeVisible);
            goalLeft = listLeft;
            if (positionToMakeVisible > 0) {
                goalLeft = listLeft + this.getArrowScrollPreviewLength();
            }

            if (viewToMakeVisible.getLeft() >= goalLeft) {
                return 0;
            } else if (nextSelectedPosition != -1 && viewToMakeVisible.getRight() - goalLeft >= this.getMaxScrollAmount()) {
                return 0;
            } else {
                amountToScroll = goalLeft - viewToMakeVisible.getLeft();
                if (this.mFirstPosition == 0) {
                    max = listLeft - this.getChildAt(0).getLeft();
                    amountToScroll = Math.min(amountToScroll, max);
                }

                return Math.min(amountToScroll, this.getMaxScrollAmount());
            }
        }
    }

    private int lookForSelectablePositionOnScreen(int direction) {
        int firstPosition = this.mFirstPosition;
        int last;
        int startPos;
        ListAdapter adapter;
        int pos;
        if (direction == 130) {
            last = this.mSelectedPosition != -1 ? this.mSelectedPosition + 1 : firstPosition;
            if (last >= this.mAdapter.getCount()) {
                return -1;
            }

            if (last < firstPosition) {
                last = firstPosition;
            }

            startPos = this.getLastVisiblePosition();
            adapter = this.getAdapter();

            for (pos = last; pos <= startPos; ++pos) {
                if (adapter.isEnabled(pos) && this.getChildAt(pos - firstPosition).getVisibility() == View.VISIBLE) {
                    return pos;
                }
            }
        } else {
            last = firstPosition + this.getChildCount() - 1;
            startPos = this.mSelectedPosition != -1 ? this.mSelectedPosition - 1 : firstPosition + this.getChildCount() - 1;
            if (startPos < 0 || startPos >= this.mAdapter.getCount()) {
                return -1;
            }

            if (startPos > last) {
                startPos = last;
            }

            adapter = this.getAdapter();

            for (pos = startPos; pos >= firstPosition; --pos) {
                if (adapter.isEnabled(pos) && this.getChildAt(pos - firstPosition).getVisibility() == View.VISIBLE) {
                    return pos;
                }
            }
        }

        return -1;
    }

    private HorizontalListView.ArrowScrollFocusResult arrowScrollFocused(int direction) {
        View selectedView = this.getSelectedView();
        View newFocus;
        int focusScroll;
        int maxScrollAmount;
        if (selectedView != null && selectedView.hasFocus()) {
            View positionOfNewFocus1 = selectedView.findFocus();
            newFocus = FocusFinder.getInstance().findNextFocus(this, positionOfNewFocus1, direction);
        } else {
            boolean positionOfNewFocus;
            if (direction == 130) {
                positionOfNewFocus = this.mFirstPosition > 0;
                focusScroll = this.mListPadding.left + (positionOfNewFocus ? this.getArrowScrollPreviewLength() : 0);
                maxScrollAmount = selectedView != null && selectedView.getLeft() > focusScroll ? selectedView.getLeft() : focusScroll;
                this.mTempRect.set(maxScrollAmount, 0, maxScrollAmount, 0);
            } else {
                positionOfNewFocus = this.mFirstPosition + this.getChildCount() - 1 < this.mItemCount;
                focusScroll = this.getWidth() - this.mListPadding.right - (positionOfNewFocus ? this.getArrowScrollPreviewLength() : 0);
                maxScrollAmount = selectedView != null && selectedView.getRight() < focusScroll ? selectedView.getRight() : focusScroll;
                this.mTempRect.set(maxScrollAmount, 0, maxScrollAmount, 0);
            }

            newFocus = FocusFinder.getInstance().findNextFocusFromRect(this, this.mTempRect, direction);
        }

        if (newFocus != null) {
            int positionOfNewFocus2 = this.positionOfNewFocus(newFocus);
            if (this.mSelectedPosition != -1 && positionOfNewFocus2 != this.mSelectedPosition) {
                focusScroll = this.lookForSelectablePositionOnScreen(direction);
                if (focusScroll != -1 && (direction == 130 && focusScroll < positionOfNewFocus2 || direction == 33 && focusScroll > positionOfNewFocus2)) {
                    return null;
                }
            }

            focusScroll = this.amountToScrollToNewFocus(direction, newFocus, positionOfNewFocus2);
            maxScrollAmount = this.getMaxScrollAmount();
            if (focusScroll < maxScrollAmount) {
                newFocus.requestFocus(direction);
                this.mArrowScrollFocusResult.populate(positionOfNewFocus2, focusScroll);
                return this.mArrowScrollFocusResult;
            }

            if (this.distanceToView(newFocus) < maxScrollAmount) {
                newFocus.requestFocus(direction);
                this.mArrowScrollFocusResult.populate(positionOfNewFocus2, maxScrollAmount);
                return this.mArrowScrollFocusResult;
            }
        }

        return null;
    }

    private int positionOfNewFocus(View newFocus) {
        int numChildren = this.getChildCount();

        for (int i = 0; i < numChildren; ++i) {
            View child = this.getChildAt(i);
            if (this.isViewAncestorOf(newFocus, child)) {
                return this.mFirstPosition + i;
            }
        }

        throw new IllegalArgumentException("newFocus is not a child of any of the children of the list!");
    }

    private boolean isViewAncestorOf(View child, View parent) {
        if (child == parent) {
            return true;
        } else {
            ViewParent theParent = child.getParent();
            return theParent instanceof ViewGroup && this.isViewAncestorOf((View) theParent, parent);
        }
    }

    private int amountToScrollToNewFocus(int direction, View newFocus, int positionOfNewFocus) {
        int amountToScroll = 0;
        newFocus.getDrawingRect(this.mTempRect);
        this.offsetDescendantRectToMyCoords(newFocus, this.mTempRect);
        if (direction == 33) {
            if (this.mTempRect.left < this.mListPadding.left) {
                amountToScroll = this.mListPadding.left - this.mTempRect.left;
                if (positionOfNewFocus > 0) {
                    amountToScroll += this.getArrowScrollPreviewLength();
                }
            }
        } else {
            int listRight = this.getWidth() - this.mListPadding.right;
            if (this.mTempRect.bottom > listRight) {
                amountToScroll = this.mTempRect.right - listRight;
                if (positionOfNewFocus < this.mItemCount - 1) {
                    amountToScroll += this.getArrowScrollPreviewLength();
                }
            }
        }

        return amountToScroll;
    }

    private int distanceToView(View descendant) {
        int distance = 0;
        descendant.getDrawingRect(this.mTempRect);
        this.offsetDescendantRectToMyCoords(descendant, this.mTempRect);
        int listRight = this.getRight() - this.getLeft() - this.mListPadding.right;
        if (this.mTempRect.right < this.mListPadding.left) {
            distance = this.mListPadding.left - this.mTempRect.right;
        } else if (this.mTempRect.left > listRight) {
            distance = this.mTempRect.left - listRight;
        }

        return distance;
    }

    private void scrollListItemsBy(int amount) {
        this.offsetChildrenLeftAndRight(amount);
        int listRight = this.getWidth() - this.mListPadding.right;
        int listLeft = this.mListPadding.left;
        RecycleBin recycleBin = this.mRecycler;
        LayoutParams layoutParams;
        View var11;
        if (amount < 0) {
            int first = this.getChildCount();

            View lastIndex;
            for (lastIndex = this.getChildAt(first - 1); lastIndex.getRight() < listRight; ++first) {
                int last = this.mFirstPosition + first - 1;
                if (last >= this.mItemCount - 1) {
                    break;
                }

                lastIndex = this.addViewAfter(lastIndex, last);
            }

            if (lastIndex.getBottom() < listRight) {
                this.offsetChildrenLeftAndRight(listRight - lastIndex.getRight());
            }

            for (var11 = this.getChildAt(0); var11.getRight() < listLeft; ++this.mFirstPosition) {
                layoutParams = (LayoutParams) var11.getLayoutParams();
                if (recycleBin.shouldRecycleViewType(layoutParams.viewType)) {
                    this.detachViewFromParent(var11);
                    recycleBin.addScrapView(var11, this.mFirstPosition);
                } else {
                    this.removeViewInLayout(var11);
                }

                var11 = this.getChildAt(0);
            }
        } else {
            View var9;
            for (var9 = this.getChildAt(0); var9.getLeft() > listLeft && this.mFirstPosition > 0; --this.mFirstPosition) {
                var9 = this.addViewBefore(var9, this.mFirstPosition);
            }

            if (var9.getLeft() > listLeft) {
                this.offsetChildrenLeftAndRight(listLeft - var9.getLeft());
            }

            int var10 = this.getChildCount() - 1;

            for (var11 = this.getChildAt(var10); var11.getLeft() > listRight; var11 = this.getChildAt(var10)) {
                layoutParams = (LayoutParams) var11.getLayoutParams();
                if (recycleBin.shouldRecycleViewType(layoutParams.viewType)) {
                    this.detachViewFromParent(var11);
                    recycleBin.addScrapView(var11, this.mFirstPosition + var10);
                } else {
                    this.removeViewInLayout(var11);
                }

                --var10;
            }
        }

    }

    private View addViewBefore(View theView, int position) {
        int abovePosition = position - 1;
        View view = this.obtainView(abovePosition, this.mIsScrap);
        int edgeOfNewChild = theView.getLeft() - this.mDividerWidth;
        this.setupChild(view, abovePosition, edgeOfNewChild, false, this.mListPadding.top, false, this.mIsScrap[0]);
        return view;
    }

    private View addViewAfter(View theView, int position) {
        int belowPosition = position + 1;
        View view = this.obtainView(belowPosition, this.mIsScrap);
        int edgeOfNewChild = theView.getRight() + this.mDividerWidth;
        this.setupChild(view, belowPosition, edgeOfNewChild, true, this.mListPadding.top, false, this.mIsScrap[0]);
        return view;
    }

    public void setItemsCanFocus(boolean itemsCanFocus) {
        this.mItemsCanFocus = itemsCanFocus;
        if (!itemsCanFocus) {
            this.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }

    }

    public boolean getItemsCanFocus() {
        return this.mItemsCanFocus;
    }

    public boolean isOpaque() {
        boolean retValue = this.mCachingActive && this.mIsCacheColorOpaque && this.mDividerIsOpaque || super.isOpaque();
        if (retValue) {
            int listLeft = this.mListPadding != null ? this.mListPadding.left : this.getPaddingLeft();
            View first = this.getChildAt(0);
            if (first == null || first.getLeft() > listLeft) {
                return false;
            }

            int listRight = this.getWidth() - (this.mListPadding != null ? this.mListPadding.right : this.getPaddingRight());
            View last = this.getChildAt(this.getChildCount() - 1);
            if (last == null || last.getRight() < listRight) {
                return false;
            }
        }

        return retValue;
    }

    public void setCacheColorHint(int color) {
        boolean opaque = color >>> 24 == 255;
        this.mIsCacheColorOpaque = opaque;
        if (opaque) {
            if (this.mDividerPaint == null) {
                this.mDividerPaint = new Paint();
            }

            this.mDividerPaint.setColor(color);
        }

        super.setCacheColorHint(color);
    }

    void drawOverscrollHeader(Canvas canvas, Drawable drawable, Rect bounds) {
        int width = drawable.getMinimumWidth();
        canvas.save();
        canvas.clipRect(bounds);
        int span = bounds.right - bounds.left;
        if (span < width) {
            bounds.left = bounds.right - width;
        }

        drawable.setBounds(bounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    void drawOverscrollFooter(Canvas canvas, Drawable drawable, Rect bounds) {
        int width = drawable.getMinimumWidth();
        canvas.save();
        canvas.clipRect(bounds);
        int span = bounds.right - bounds.left;
        if (span < width) {
            bounds.right = bounds.left + width;
        }

        drawable.setBounds(bounds);
        drawable.draw(canvas);
        canvas.restore();
    }

    public boolean isListViewReachTopEdge() {
        boolean result = false;
        if (this.mFirstPosition == 0 && this.getChildCount() > 0) {
            View topChildView = this.getChildAt(0);
            result = topChildView.getTop() == 0;
        }

        return result;
    }

    public boolean isListViewReachBottomEdge() {
        boolean result = false;
        if (this.getLastVisiblePosition() == this.getCount() - 1) {
            View bottomChildView = this.getChildAt(this.getLastVisiblePosition() - this.getFirstVisiblePosition());
            result = this.getHeight() >= bottomChildView.getBottom();
        }

        return result;
    }

    protected void dispatchDraw(Canvas canvas) {
        if (this.mCachingStarted) {
            this.mCachingActive = true;
        }

        int dividerWidth = this.mDividerWidth;
        Drawable overscrollHeader = this.mOverScrollHeader;
        Drawable overscrollFooter = this.mOverScrollFooter;
        boolean drawOverscrollHeader = overscrollHeader != null;
        boolean drawOverscrollFooter = overscrollFooter != null;
        boolean drawDividers = dividerWidth > 0 && this.mDivider != null;
        if (drawDividers || drawOverscrollHeader || drawOverscrollFooter) {
            Rect bounds = this.mTempRect;
            bounds.top = this.getPaddingTop();
            bounds.bottom = this.getBottom() - this.getTop() - this.getPaddingBottom();
            int count = this.getChildCount();
            int headerCount = this.mHeaderViewInfos.size();
            int itemCount = this.mItemCount;
            int footerLimit = itemCount - this.mFooterViewInfos.size() - 1;
            boolean headerDividers = this.mHeaderDividersEnabled;
            boolean footerDividers = this.mFooterDividersEnabled;
            int first = this.mFirstPosition;
            boolean areAllItemsSelectable = this.mAreAllItemsSelectable;
            ListAdapter adapter = this.mAdapter;
            boolean fillForMissingDividers = this.isOpaque() && !super.isOpaque();
            if (fillForMissingDividers && this.mDividerPaint == null && this.mIsCacheColorOpaque) {
                this.mDividerPaint = new Paint();
                this.mDividerPaint.setColor(this.getCacheColorHint());
            }

            Paint paint = this.mDividerPaint;
            byte effectivePaddingLeft = 0;
            byte effectivePaddingRight = 0;
            int listRight = this.getRight() - this.getLeft() - effectivePaddingRight + this.getScrollX();
            int left;
            int start;
            int scrollX;
            if (!this.mStackFromRight) {
                left = 0;
                scrollX = this.getScrollX();
                if (count > 0 && scrollX < 0) {
                    if (drawOverscrollHeader) {
                        bounds.right = 0;
                        bounds.left = scrollX;
                        this.drawOverscrollHeader(canvas, overscrollHeader, bounds);
                    } else if (drawDividers) {
                        bounds.right = 0;
                        bounds.left = -dividerWidth;
                        this.drawDivider(canvas, bounds, -1);
                    }
                }

                for (start = 0; start < count; ++start) {
                    if ((headerDividers || first + start >= headerCount) && (footerDividers || first + start < footerLimit)) {
                        View absListRight = this.getChildAt(start);
                        left = absListRight.getRight();
                        if (drawDividers && left < listRight && (!drawOverscrollFooter || start != count - 1)) {
                            if (areAllItemsSelectable || adapter.isEnabled(first + start) && (start == count - 1 || adapter.isEnabled(first + start + 1))) {
                                bounds.left = left;
                                bounds.right = left + dividerWidth;
                                this.drawDivider(canvas, bounds, start);
                            } else if (fillForMissingDividers) {
                                bounds.left = left;
                                bounds.right = left + dividerWidth;
                                canvas.drawRect(bounds, paint);
                            }
                        }
                    }
                }

                start = this.getRight() + this.getScrollX();
                if (drawOverscrollFooter && first + count == itemCount && start > left) {
                    bounds.left = left;
                    bounds.right = start;
                    this.drawOverscrollFooter(canvas, overscrollFooter, bounds);
                }
            } else {
                scrollX = this.getScrollX();
                if (count > 0 && drawOverscrollHeader) {
                    bounds.left = scrollX;
                    bounds.right = this.getChildAt(0).getLeft();
                    this.drawOverscrollHeader(canvas, overscrollHeader, bounds);
                }

                start = drawOverscrollHeader ? 1 : 0;

                int var28;
                for (var28 = start; var28 < count; ++var28) {
                    if ((headerDividers || first + var28 >= headerCount) && (footerDividers || first + var28 < footerLimit)) {
                        View child = this.getChildAt(var28);
                        left = child.getLeft();
                        if (left > effectivePaddingLeft) {
                            if (!areAllItemsSelectable && (!adapter.isEnabled(first + var28) || var28 != count - 1 && !adapter.isEnabled(first + var28 + 1))) {
                                if (fillForMissingDividers) {
                                    bounds.left = left - dividerWidth;
                                    bounds.right = left;
                                    canvas.drawRect(bounds, paint);
                                }
                            } else {
                                bounds.left = left - dividerWidth;
                                bounds.right = left;
                                this.drawDivider(canvas, bounds, var28 - 1);
                            }
                        }
                    }
                }

                if (count > 0 && scrollX > 0) {
                    if (drawOverscrollFooter) {
                        var28 = this.getRight();
                        bounds.left = var28;
                        bounds.right = var28 + scrollX;
                        this.drawOverscrollFooter(canvas, overscrollFooter, bounds);
                    } else if (drawDividers) {
                        bounds.left = listRight;
                        bounds.right = listRight + dividerWidth;
                        this.drawDivider(canvas, bounds, -1);
                    }
                }
            }
        }

        super.dispatchDraw(canvas);
    }

    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        boolean more = super.drawChild(canvas, child, drawingTime);
        if (this.mCachingActive) {
            this.mCachingActive = false;
        }

        return more;
    }

    void drawDivider(Canvas canvas, Rect bounds, int childIndex) {
        Drawable divider = this.mDivider;
        divider.setBounds(bounds);
        divider.draw(canvas);
    }

    public Drawable getDivider() {
        return this.mDivider;
    }

    public void setDivider(Drawable divider) {
        if (divider != null) {
            this.mDividerWidth = divider.getIntrinsicWidth();
        } else {
            this.mDividerWidth = 0;
        }

        this.mDivider = divider;
        this.mDividerIsOpaque = divider == null || divider.getOpacity() == -1;
        this.requestLayout();
        this.invalidate();
    }

    public int getDividerWidth() {
        return this.mDividerWidth;
    }

    public void setDividerWidth(int width) {
        this.mDividerWidth = width;
        this.requestLayout();
        this.invalidate();
    }

    public void setHeaderDividersEnabled(boolean headerDividersEnabled) {
        this.mHeaderDividersEnabled = headerDividersEnabled;
        this.invalidate();
    }

    public void setFooterDividersEnabled(boolean footerDividersEnabled) {
        this.mFooterDividersEnabled = footerDividersEnabled;
        this.invalidate();
    }

    public void setOverscrollHeader(Drawable header) {
        this.mOverScrollHeader = header;
        if (this.getScrollX() < 0) {
            this.invalidate();
        }

    }

    public Drawable getOverscrollHeader() {
        return this.mOverScrollHeader;
    }

    public void setOverscrollFooter(Drawable footer) {
        this.mOverScrollFooter = footer;
        this.invalidate();
    }

    public Drawable getOverscrollFooter() {
        return this.mOverScrollFooter;
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        ListAdapter adapter = this.mAdapter;
        int closetChildIndex = -1;
        int closestChildLeft = 0;
        if (adapter != null && gainFocus && previouslyFocusedRect != null) {
            previouslyFocusedRect.offset(this.getScrollX(), this.getScrollY());
            if (adapter.getCount() < this.getChildCount() + this.mFirstPosition) {
                this.mLayoutMode = 0;
                this.layoutChildren();
            }

            Rect otherRect = this.mTempRect;
            int minDistance = 2147483647;
            int childCount = this.getChildCount();
            int firstPosition = this.mFirstPosition;

            for (int i = 0; i < childCount; ++i) {
                if (adapter.isEnabled(firstPosition + i)) {
                    View other = this.getChildAt(i);
                    other.getDrawingRect(otherRect);
                    this.offsetDescendantRectToMyCoords(other, otherRect);
                    int distance = getDistance(previouslyFocusedRect, otherRect, direction);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closetChildIndex = i;
                        closestChildLeft = other.getLeft();
                    }
                }
            }
        }

        if (closetChildIndex >= 0) {
            this.setSelectionFromLeft(closetChildIndex + this.mFirstPosition, closestChildLeft);
        } else {
            this.requestLayout();
        }

    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = this.getChildCount();
        if (count > 0) {
            for (int i = 0; i < count; ++i) {
                this.addHeaderView(this.getChildAt(i));
            }

            this.removeAllViews();
        }

    }

    /**
     * @deprecated
     */
    @Deprecated
    public long[] getCheckItemIds() {
        if (this.mAdapter != null && this.mAdapter.hasStableIds()) {
            return this.getCheckedItemIds();
        } else if (this.mChoiceMode != 0 && this.mCheckStates != null && this.mAdapter != null) {
            SparseArrayCompat states = this.mCheckStates;
            int count = states.size();
            long[] ids = new long[count];
            ListAdapter adapter = this.mAdapter;
            int checkedCount = 0;

            for (int result = 0; result < count; ++result) {
                if (((Boolean) states.valueAt(result)).booleanValue()) {
                    ids[checkedCount++] = adapter.getItemId(states.keyAt(result));
                }
            }

            if (checkedCount == count) {
                return ids;
            } else {
                long[] var7 = new long[checkedCount];
                System.arraycopy(ids, 0, var7, 0, checkedCount);
                return var7;
            }
        } else {
            return new long[0];
        }
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(HorizontalListView.class.getName());
    }

    @TargetApi(14)
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(HorizontalListView.class.getName());
    }

    public void onGlobalLayout() {
    }

    private static class ArrowScrollFocusResult {
        private int mSelectedPosition;
        private int mAmountToScroll;

        private ArrowScrollFocusResult() {
        }

        void populate(int selectedPosition, int amountToScroll) {
            this.mSelectedPosition = selectedPosition;
            this.mAmountToScroll = amountToScroll;
        }

        public int getSelectedPosition() {
            return this.mSelectedPosition;
        }

        public int getAmountToScroll() {
            return this.mAmountToScroll;
        }
    }

    private class FocusSelector implements Runnable {
        private int mPosition;
        private int mPositionLeft;

        private FocusSelector() {
        }

        public HorizontalListView.FocusSelector setup(int position, int left) {
            this.mPosition = position;
            this.mPositionLeft = left;
            return this;
        }

        public void run() {
            HorizontalListView.this.setSelectionFromLeft(this.mPosition, this.mPositionLeft);
        }
    }

    public static class FixedViewInfo {
        public View view;
        public Object data;
        public boolean isSelectable;

        public FixedViewInfo() {
        }
    }
}
