package com.qing.widget.horizontallistview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.DataSetObserver;
import android.os.Build.VERSION;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.ViewDebug.CapturedViewProperty;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Adapter;

public abstract class AdapterView<T extends Adapter> extends ViewGroup {
    public static final String LOG_TAG = "AdapterView";
    public static final boolean LOG_ENABLED = false;
    public static final int ITEM_VIEW_TYPE_IGNORE = -1;
    public static final int ITEM_VIEW_TYPE_HEADER_OR_FOOTER = -2;
    @ExportedProperty(
            category = "scrolling"
    )
    protected int mFirstPosition = 0;
    protected int mSpecificLeft;
    protected int mSyncPosition;
    protected long mSyncColId = -9223372036854775808L;
    protected long mSyncWidth;
    protected boolean mNeedSync = false;
    int mSyncMode;
    private int mLayoutWidth;
    static final int SYNC_SELECTED_POSITION = 0;
    static final int SYNC_FIRST_POSITION = 1;
    static final int SYNC_MAX_DURATION_MILLIS = 100;
    protected boolean mInLayout = false;
    AdapterView.OnItemSelectedListener mOnItemSelectedListener;
    AdapterView.OnItemClickListener mOnItemClickListener;
    AdapterView.OnItemLongClickListener mOnItemLongClickListener;
    public boolean mDataChanged;
    @ExportedProperty(
            category = "list"
    )
    protected int mNextSelectedPosition = -1;
    protected long mNextSelectedColId = -9223372036854775808L;
    @ExportedProperty(
            category = "list"
    )
    protected int mSelectedPosition = -1;
    protected long mSelectedColId = -9223372036854775808L;
    private View mEmptyView;
    @ExportedProperty(
            category = "list"
    )
    protected int mItemCount;
    protected int mOldItemCount;
    AccessibilityManager mAccessibilityManager;
    public static final int INVALID_POSITION = -1;
    public static final long INVALID_COL_ID = -9223372036854775808L;
    protected int mOldSelectedPosition = -1;
    protected long mOldSelectedColId = -9223372036854775808L;
    private boolean mDesiredFocusableState;
    private boolean mDesiredFocusableInTouchModeState;
    private AdapterView<T>.SelectionNotifier mSelectionNotifier;
    protected boolean mBlockLayoutRequests = false;

    public AdapterView(Context context) {
        super(context);
        this.initAdapterView();
    }

    public AdapterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.initAdapterView();
    }

    @TargetApi(16)
    public AdapterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.initAdapterView();
    }

    private void initAdapterView() {
        if (VERSION.SDK_INT >= 16 && this.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            this.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        if (!this.isInEditMode() && this.mAccessibilityManager == null) {
            this.mAccessibilityManager = (AccessibilityManager) this.getContext().getSystemService(Context.ACCESSIBILITY_SERVICE);
        }

    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public final AdapterView.OnItemClickListener getOnItemClickListener() {
        return this.mOnItemClickListener;
    }

    public boolean performItemClick(View view, int position, long id) {
        if (this.mOnItemClickListener != null) {
            this.playSoundEffect(0);
            if (view != null) {
                view.sendAccessibilityEvent(1);
            }

            this.mOnItemClickListener.onItemClick(this, view, position, id);
            return true;
        } else {
            return false;
        }
    }

    public void setOnItemLongClickListener(AdapterView.OnItemLongClickListener listener) {
        if (!this.isLongClickable()) {
            this.setLongClickable(true);
        }

        this.mOnItemLongClickListener = listener;
    }

    public final AdapterView.OnItemLongClickListener getOnItemLongClickListener() {
        return this.mOnItemLongClickListener;
    }

    public void setOnItemSelectedListener(AdapterView.OnItemSelectedListener listener) {
        this.mOnItemSelectedListener = listener;
    }

    public final AdapterView.OnItemSelectedListener getOnItemSelectedListener() {
        return this.mOnItemSelectedListener;
    }

    public abstract T getAdapter();

    public abstract void setAdapter(T var1);

    public void addView(View child) {
        throw new UnsupportedOperationException("addView(View) is not supported in AdapterView");
    }

    public void addView(View child, int index) {
        throw new UnsupportedOperationException("addView(View, int) is not supported in AdapterView");
    }

    public void addView(View child, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, LayoutParams) is not supported in AdapterView");
    }

    public void addView(View child, int index, LayoutParams params) {
        throw new UnsupportedOperationException("addView(View, int, LayoutParams) is not supported in AdapterView");
    }

    public void removeView(View child) {
        throw new UnsupportedOperationException("removeView(View) is not supported in AdapterView");
    }

    public void removeViewAt(int index) {
        throw new UnsupportedOperationException("removeViewAt(int) is not supported in AdapterView");
    }

    public void removeAllViews() {
        throw new UnsupportedOperationException("removeAllViews() is not supported in AdapterView");
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.mLayoutWidth = this.getWidth();
    }

    @CapturedViewProperty
    public int getSelectedItemPosition() {
        return this.mNextSelectedPosition;
    }

    @CapturedViewProperty
    public long getSelectedItemId() {
        return this.mNextSelectedColId;
    }

    public abstract View getSelectedView();

    public Object getSelectedItem() {
        Adapter adapter = this.getAdapter();
        int selection = this.getSelectedItemPosition();
        return adapter != null && adapter.getCount() > 0 && selection >= 0 ? adapter.getItem(selection) : null;
    }

    @CapturedViewProperty
    public int getCount() {
        return this.mItemCount;
    }

    public int getPositionForView(View view) {
        View listItem = view;

        View childCount;
        try {
            while (!(childCount = (View) listItem.getParent()).equals(this)) {
                listItem = childCount;
            }
        } catch (ClassCastException var5) {
            return -1;
        }

        int var6 = this.getChildCount();

        for (int i = 0; i < var6; ++i) {
            if (this.getChildAt(i).equals(listItem)) {
                return this.mFirstPosition + i;
            }
        }

        return -1;
    }

    public int getFirstVisiblePosition() {
        return this.mFirstPosition;
    }

    public int getLastVisiblePosition() {
        return this.mFirstPosition + this.getChildCount() - 1;
    }

    public abstract void setSelection(int var1);

    @TargetApi(16)
    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
        if (VERSION.SDK_INT >= 16 && emptyView != null && emptyView.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            emptyView.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
        }

        Adapter adapter = this.getAdapter();
        boolean empty = adapter == null || adapter.isEmpty();
        this.updateEmptyStatus(empty);
    }

    public View getEmptyView() {
        return this.mEmptyView;
    }

    boolean isInFilterMode() {
        return false;
    }

    public void setFocusable(boolean focusable) {
        Adapter adapter = this.getAdapter();
        boolean empty = adapter == null || adapter.getCount() == 0;
        this.mDesiredFocusableState = focusable;
        if (!focusable) {
            this.mDesiredFocusableInTouchModeState = false;
        }

        super.setFocusable(focusable && (!empty || this.isInFilterMode()));
    }

    public void setFocusableInTouchMode(boolean focusable) {
        Adapter adapter = this.getAdapter();
        boolean empty = adapter == null || adapter.getCount() == 0;
        this.mDesiredFocusableInTouchModeState = focusable;
        if (focusable) {
            this.mDesiredFocusableState = true;
        }

        super.setFocusableInTouchMode(focusable && (!empty || this.isInFilterMode()));
    }

    protected void checkFocus() {
        Adapter adapter = this.getAdapter();
        boolean empty = adapter == null || adapter.getCount() == 0;
        boolean focusable = !empty || this.isInFilterMode();
        super.setFocusableInTouchMode(focusable && this.mDesiredFocusableInTouchModeState);
        super.setFocusable(focusable && this.mDesiredFocusableState);
        if (this.mEmptyView != null) {
            this.updateEmptyStatus(adapter == null || adapter.isEmpty());
        }

    }

    @SuppressLint({"WrongCall"})
    private void updateEmptyStatus(boolean empty) {
        if (this.isInFilterMode()) {
            empty = false;
        }

        if (empty) {
            if (this.mEmptyView != null) {
                this.mEmptyView.setVisibility(View.VISIBLE);
                this.setVisibility(View.GONE);
            } else {
                this.setVisibility(View.VISIBLE);
            }

            if (this.mDataChanged) {
                this.onLayout(false, this.getLeft(), this.getTop(), this.getRight(), this.getBottom());
            }
        } else {
            if (this.mEmptyView != null) {
                this.mEmptyView.setVisibility(View.GONE);
            }
            this.setVisibility(View.VISIBLE);
        }

    }

    public Object getItemAtPosition(int position) {
        Adapter adapter = this.getAdapter();
        return adapter != null && position >= 0 ? adapter.getItem(position) : null;
    }

    public long getItemIdAtPosition(int position) {
        Adapter adapter = this.getAdapter();
        return adapter != null && position >= 0 ? adapter.getItemId(position) : -9223372036854775808L;
    }

    public void setOnClickListener(OnClickListener l) {
        throw new RuntimeException("Don\'t call setOnClickListener for an AdapterView. You probably want setOnItemClickListener instead");
    }

    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        this.dispatchFreezeSelfOnly(container);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        this.dispatchThawSelfOnly(container);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.removeCallbacks(this.mSelectionNotifier);
    }

    void selectionChanged() {
        if (this.mOnItemSelectedListener != null || this.mAccessibilityManager.isEnabled()) {
            if (!this.mInLayout && !this.mBlockLayoutRequests) {
                this.fireOnSelected();
                this.performAccessibilityActionsOnSelected();
            } else {
                if (this.mSelectionNotifier == null) {
                    this.mSelectionNotifier = new AdapterView.SelectionNotifier();
                }

                this.post(this.mSelectionNotifier);
            }
        }

    }

    private void fireOnSelected() {
        if (this.mOnItemSelectedListener != null) {
            int selection = this.getSelectedItemPosition();
            if (selection >= 0) {
                View v = this.getSelectedView();
                this.mOnItemSelectedListener.onItemSelected(this, v, selection, this.getAdapter().getItemId(selection));
            } else {
                this.mOnItemSelectedListener.onNothingSelected(this);
            }

        }
    }

    private void performAccessibilityActionsOnSelected() {
        if (this.mAccessibilityManager.isEnabled()) {
            int position = this.getSelectedItemPosition();
            if (position >= 0) {
                this.sendAccessibilityEvent(4);
            }

        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        View selectedView = this.getSelectedView();
        return selectedView != null && selectedView.getVisibility() == View.VISIBLE && selectedView.dispatchPopulateAccessibilityEvent(event);
    }

    @TargetApi(14)
    public boolean onRequestSendAccessibilityEvent(View child, AccessibilityEvent event) {
        if (super.onRequestSendAccessibilityEvent(child, event)) {
            AccessibilityEvent record = AccessibilityEvent.obtain();
            this.onInitializeAccessibilityEvent(record);
            child.dispatchPopulateAccessibilityEvent(record);
            event.appendRecord(record);
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(14)
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AdapterView.class.getName());
        info.setScrollable(this.isScrollableForAccessibility());
        View selectedView = this.getSelectedView();
        if (selectedView != null) {
            info.setEnabled(selectedView.isEnabled());
        }

    }

    @TargetApi(14)
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AdapterView.class.getName());
        event.setScrollable(this.isScrollableForAccessibility());
        View selectedView = this.getSelectedView();
        if (selectedView != null) {
            event.setEnabled(selectedView.isEnabled());
        }

        event.setCurrentItemIndex(this.getSelectedItemPosition());
        event.setFromIndex(this.getFirstVisiblePosition());
        event.setToIndex(this.getLastVisiblePosition());
        event.setItemCount(this.getCount());
    }

    private boolean isScrollableForAccessibility() {
        Adapter adapter = this.getAdapter();
        if (adapter == null) {
            return false;
        } else {
            int itemCount = adapter.getCount();
            return itemCount > 0 && (this.getFirstVisiblePosition() > 0 || this.getLastVisiblePosition() < itemCount - 1);
        }
    }

    protected boolean canAnimate() {
        return super.canAnimate() && this.mItemCount > 0;
    }

    void handleDataChanged() {
        int count = this.mItemCount;
        boolean found = false;
        if (count > 0) {
            int newPos;
            int selectablePos;
            if (this.mNeedSync) {
                this.mNeedSync = false;
                newPos = this.findSyncPosition();
                if (newPos >= 0) {
                    selectablePos = this.lookForSelectablePosition(newPos, true);
                    if (selectablePos == newPos) {
                        this.setNextSelectedPositionInt(newPos);
                        found = true;
                    }
                }
            }

            if (!found) {
                newPos = this.getSelectedItemPosition();
                if (newPos >= count) {
                    newPos = count - 1;
                }

                if (newPos < 0) {
                    newPos = 0;
                }

                selectablePos = this.lookForSelectablePosition(newPos, true);
                if (selectablePos < 0) {
                    selectablePos = this.lookForSelectablePosition(newPos, false);
                }

                if (selectablePos >= 0) {
                    this.setNextSelectedPositionInt(selectablePos);
                    this.checkSelectionChanged();
                    found = true;
                }
            }
        }

        if (!found) {
            this.mSelectedPosition = -1;
            this.mSelectedColId = -9223372036854775808L;
            this.mNextSelectedPosition = -1;
            this.mNextSelectedColId = -9223372036854775808L;
            this.mNeedSync = false;
            this.checkSelectionChanged();
        }

    }

    protected void checkSelectionChanged() {
        if (this.mSelectedPosition != this.mOldSelectedPosition || this.mSelectedColId != this.mOldSelectedColId) {
            this.selectionChanged();
            this.mOldSelectedPosition = this.mSelectedPosition;
            this.mOldSelectedColId = this.mSelectedColId;
        }

    }

    int findSyncPosition() {
        int count = this.mItemCount;
        if (count == 0) {
            return -1;
        } else {
            long idToMatch = this.mSyncColId;
            int seed = this.mSyncPosition;
            if (idToMatch == -9223372036854775808L) {
                return -1;
            } else {
                seed = Math.max(0, seed);
                seed = Math.min(count - 1, seed);
                long endTime = SystemClock.uptimeMillis() + 100L;
                int first = seed;
                int last = seed;
                boolean next = false;
                Adapter adapter = this.getAdapter();
                if (adapter == null) {
                    return -1;
                } else {
                    while (true) {
                        if (SystemClock.uptimeMillis() <= endTime) {
                            long colId = adapter.getItemId(seed);
                            if (colId == idToMatch) {
                                return seed;
                            }

                            boolean hitLast = last == count - 1;
                            boolean hitFirst = first == 0;
                            if (!hitLast || !hitFirst) {
                                if (!hitFirst && (!next || hitLast)) {
                                    if (hitLast || !next && !hitFirst) {
                                        --first;
                                        seed = first;
                                        next = true;
                                    }
                                } else {
                                    ++last;
                                    seed = last;
                                    next = false;
                                }
                                continue;
                            }
                        }

                        return -1;
                    }
                }
            }
        }
    }

    protected int lookForSelectablePosition(int position, boolean lookDown) {
        return position;
    }

    protected void setSelectedPositionInt(int position) {
        this.mSelectedPosition = position;
        this.mSelectedColId = this.getItemIdAtPosition(position);
    }

    protected void setNextSelectedPositionInt(int position) {
        this.mNextSelectedPosition = position;
        this.mNextSelectedColId = this.getItemIdAtPosition(position);
        if (this.mNeedSync && this.mSyncMode == 0 && position >= 0) {
            this.mSyncPosition = position;
            this.mSyncColId = this.mNextSelectedColId;
        }

    }

    public void rememberSyncState() {
        if (this.getChildCount() > 0) {
            this.mNeedSync = true;
            this.mSyncWidth = (long) this.mLayoutWidth;
            View v;
            if (this.mSelectedPosition >= 0) {
                v = this.getChildAt(this.mSelectedPosition - this.mFirstPosition);
                this.mSyncColId = this.mNextSelectedColId;
                this.mSyncPosition = this.mNextSelectedPosition;
                if (v != null) {
                    this.mSpecificLeft = v.getLeft();
                }

                this.mSyncMode = 0;
            } else {
                v = this.getChildAt(0);
                Adapter adapter = this.getAdapter();
                if (this.mFirstPosition >= 0 && this.mFirstPosition < adapter.getCount()) {
                    this.mSyncColId = adapter.getItemId(this.mFirstPosition);
                } else {
                    this.mSyncColId = -1L;
                }

                this.mSyncPosition = this.mFirstPosition;
                if (v != null) {
                    this.mSpecificLeft = v.getLeft();
                }

                this.mSyncMode = 1;
            }
        }

    }

    private class SelectionNotifier implements Runnable {
        private SelectionNotifier() {
        }

        public void run() {
            if (AdapterView.this.mDataChanged) {
                if (AdapterView.this.getAdapter() != null) {
                    AdapterView.this.post(this);
                }
            } else {
                AdapterView.this.fireOnSelected();
                AdapterView.this.performAccessibilityActionsOnSelected();
            }

        }
    }

    public class AdapterDataSetObserver extends DataSetObserver {
        private Parcelable mInstanceState = null;

        public AdapterDataSetObserver() {
        }

        public void onChanged() {
            AdapterView.this.mDataChanged = true;
            AdapterView.this.mOldItemCount = AdapterView.this.mItemCount;
            AdapterView.this.mItemCount = AdapterView.this.getAdapter().getCount();
            if (AdapterView.this.getAdapter().hasStableIds() && this.mInstanceState != null && AdapterView.this.mOldItemCount == 0 && AdapterView.this.mItemCount > 0) {
                AdapterView.this.onRestoreInstanceState(this.mInstanceState);
                this.mInstanceState = null;
            } else {
                AdapterView.this.rememberSyncState();
            }

            AdapterView.this.checkFocus();
            AdapterView.this.requestLayout();
        }

        public void onInvalidated() {
            AdapterView.this.mDataChanged = true;
            if (AdapterView.this.getAdapter().hasStableIds()) {
                this.mInstanceState = AdapterView.this.onSaveInstanceState();
            }

            AdapterView.this.mOldItemCount = AdapterView.this.mItemCount;
            AdapterView.this.mItemCount = 0;
            AdapterView.this.mSelectedPosition = -1;
            AdapterView.this.mSelectedColId = -9223372036854775808L;
            AdapterView.this.mNextSelectedPosition = -1;
            AdapterView.this.mNextSelectedColId = -9223372036854775808L;
            AdapterView.this.mNeedSync = false;
            AdapterView.this.checkFocus();
            AdapterView.this.requestLayout();
        }

        public void clearSavedState() {
            this.mInstanceState = null;
        }
    }

    public static class AdapterContextMenuInfo implements ContextMenuInfo {
        public View targetView;
        public int position;
        public long id;

        public AdapterContextMenuInfo(View targetView, int position, long id) {
            this.targetView = targetView;
            this.position = position;
            this.id = id;
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(AdapterView<?> var1, View var2, int var3, long var4);

        void onNothingSelected(AdapterView<?> var1);
    }

    public interface OnItemLongClickListener {
        boolean onItemLongClick(AdapterView<?> var1, View var2, int var3, long var4);
    }

    public interface OnItemClickListener {
        void onItemClick(AdapterView<?> var1, View var2, int var3, long var4);
    }
}
