package com.qing.widget.horizontallistview;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.EdgeEffectCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug.ExportedProperty;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.Checkable;
import android.widget.ListAdapter;

import com.qing.widget.horizontallistview.v11.MultiChoiceModeListener;
import com.qing.widget.horizontallistview.v11.MultiChoiceModeWrapper;

import java.util.ArrayList;
import java.util.List;

@TargetApi(11)
public abstract class AbsHorizontalListView extends AdapterView<ListAdapter> implements OnGlobalLayoutListener, OnTouchModeChangeListener {
    private static final String TAG = "AbsListView";
    public static final int TRANSCRIPT_MODE_DISABLED = 0;
    public static final int TRANSCRIPT_MODE_NORMAL = 1;
    public static final int TRANSCRIPT_MODE_ALWAYS_SCROLL = 2;
    public static final int TOUCH_MODE_REST = -1;
    public static final int TOUCH_MODE_DOWN = 0;
    public static final int TOUCH_MODE_TAP = 1;
    public static final int TOUCH_MODE_DONE_WAITING = 2;
    public static final int TOUCH_MODE_SCROLL = 3;
    public static final int TOUCH_MODE_FLING = 4;
    public static final int TOUCH_MODE_OVERSCROLL = 5;
    public static final int TOUCH_MODE_OVERFLING = 6;
    public static final int LAYOUT_NORMAL = 0;
    public static final int LAYOUT_FORCE_LEFT = 1;
    public static final int LAYOUT_SET_SELECTION = 2;
    public static final int LAYOUT_FORCE_RIGHT = 3;
    public static final int LAYOUT_SPECIFIC = 4;
    public static final int LAYOUT_SYNC = 5;
    public static final int LAYOUT_MOVE_SELECTION = 6;
    ViewHelperFactory.ViewHelper mViewHelper;
    protected int mChoiceMode = 0;
    public Object mChoiceActionMode;
    Object mMultiChoiceModeCallback;
    int mCheckedItemCount;
    protected SparseArrayCompat<Boolean> mCheckStates;
    LongSparseArray<Integer> mCheckedIdStates;
    protected int mLayoutMode = 0;
    protected AbsHorizontalListView.AdapterDataSetObserver mDataSetObserver;
    protected ListAdapter mAdapter;
    boolean mAdapterHasStableIds;
    boolean mDrawSelectorOnTop = false;
    Drawable mSelector;
    int mSelectorPosition = -1;
    protected Rect mSelectorRect = new Rect();
    protected final AbsHorizontalListView.RecycleBin mRecycler = new AbsHorizontalListView.RecycleBin();
    int mSelectionLeftPadding = 0;
    int mSelectionTopPadding = 0;
    int mSelectionRightPadding = 0;
    int mSelectionBottomPadding = 0;
    protected Rect mListPadding = new Rect();
    protected int mHeightMeasureSpec = 0;
    View mScrollLeft;
    View mScrollRight;
    protected boolean mCachingStarted;
    protected boolean mCachingActive;
    protected int mMotionPosition;
    int mMotionViewOriginalLeft;
    int mMotionViewNewLeft;
    int mMotionX;
    int mMotionY;
    protected int mTouchMode = -1;
    int mLastX;
    int mMotionCorrection;
    private VelocityTracker mVelocityTracker;
    private AbsHorizontalListView.FlingRunnable mFlingRunnable;
    protected AbsHorizontalListView.PositionScroller mPositionScroller;
    protected int mSelectedLeft = 0;
    protected boolean mStackFromRight;
    boolean mScrollingCacheEnabled;
    boolean mFastScrollEnabled;
    private AbsHorizontalListView.OnScrollListener mOnScrollListener;
    private boolean mSmoothScrollbarEnabled = true;
    private Rect mTouchFrame;
    protected int mResurrectToPosition = -1;
    private ContextMenuInfo mContextMenuInfo = null;
    protected int mOverscrollMax;
    protected static final int OVERSCROLL_LIMIT_DIVISOR = 3;
    private static final int CHECK_POSITION_SEARCH_DISTANCE = 20;
    private static final int TOUCH_MODE_UNKNOWN = -1;
    private static final int TOUCH_MODE_ON = 0;
    private static final int TOUCH_MODE_OFF = 1;
    private int mLastTouchMode = -1;
    private AbsHorizontalListView.CheckForLongPress mPendingCheckForLongPress;
    private Runnable mPendingCheckForTap;
    private AbsHorizontalListView.CheckForKeyLongPress mPendingCheckForKeyLongPress;
    private AbsHorizontalListView.PerformClick mPerformClick;
    private Runnable mTouchModeReset;
    private int mTranscriptMode;
    private int mCacheColorHint;
    private boolean mIsChildViewEnabled;
    private int mLastScrollState = 0;
    private int mTouchSlop;
    private Runnable mClearScrollingCache;
    protected Runnable mPositionScrollAfterLayout;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private float mVelocityScale = 1.0F;
    protected final boolean[] mIsScrap = new boolean[1];
    private int mActivePointerId = -1;
    private static final int INVALID_POINTER = -1;
    int mOverscrollDistance;
    int mOverflingDistance;
    private EdgeEffectCompat mEdgeGlowLeft;
    private EdgeEffectCompat mEdgeGlowRight;
    private int mFirstPositionDistanceGuess;
    private int mLastPositionDistanceGuess;
    private int mDirection = 0;
    private boolean mForceTranscriptScroll;
    private int mGlowPaddingTop;
    private int mGlowPaddingBottom;
    private AbsHorizontalListView.ListItemAccessibilityDelegate mAccessibilityDelegate;
    private int mLastAccessibilityScrollEventFromIndex;
    private int mLastAccessibilityScrollEventToIndex;
    protected boolean mIsAttached;
    private int mLastHandledItemCount;
    static final Interpolator sLinearInterpolator = new LinearInterpolator();
    private AbsHorizontalListView.SavedState mPendingSync;
    public static final int[] STATESET_NOTHING = new int[]{0};
    private float mHorizontalScrollFactor;

    public AbsHorizontalListView(Context context) {
        this(context, null);
    }

    public AbsHorizontalListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AbsHorizontalListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        this.initAbsListView();
        Object listSelector = null;
        boolean drawSelectorOnTop = false;
        boolean stackFromRight = false;
        boolean scrollingCacheEnabled = true;
        byte transcriptMode = 0;
        byte color = 0;
        boolean smoothScrollbar = true;
        byte choiceMode = 0;
        if (listSelector != null) {
            this.setSelector((Drawable) listSelector);
        }

        this.mDrawSelectorOnTop = drawSelectorOnTop;
        this.setStackFromRight(stackFromRight);
        this.setScrollingCacheEnabled(scrollingCacheEnabled);
        this.setTranscriptMode(transcriptMode);
        this.setCacheColorHint(color);
        this.setSmoothScrollbarEnabled(smoothScrollbar);
        this.setChoiceMode(choiceMode);
    }

    private void initAbsListView() {
        this.setClickable(true);
        this.setFocusableInTouchMode(true);
        this.setWillNotDraw(false);
        this.setAlwaysDrawnWithCacheEnabled(false);
        this.setScrollingCacheEnabled(true);
        ViewConfiguration configuration = ViewConfiguration.get(this.getContext());
        this.mTouchSlop = configuration.getScaledTouchSlop();
        this.mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        this.mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        this.mOverscrollDistance = configuration.getScaledOverscrollDistance();
        this.mOverflingDistance = configuration.getScaledOverflingDistance();
        this.mViewHelper = ViewHelperFactory.create(this);
    }

    public void setOverScrollMode(int mode) {
        if (mode != 2) {
            if (this.mEdgeGlowLeft == null) {
                Context context = this.getContext();
                this.mEdgeGlowLeft = new EdgeEffectCompat(context);
                this.mEdgeGlowRight = new EdgeEffectCompat(context);
            }
        } else {
            this.mEdgeGlowLeft = null;
            this.mEdgeGlowRight = null;
        }

        super.setOverScrollMode(mode);
    }

    public void setAdapter(ListAdapter adapter) {
        if (adapter != null) {
            this.mAdapterHasStableIds = this.mAdapter.hasStableIds();
            if (this.mChoiceMode != 0 && this.mAdapterHasStableIds && this.mCheckedIdStates == null) {
                this.mCheckedIdStates = new LongSparseArray();
            }
        }

        if (this.mCheckStates != null) {
            this.mCheckStates.clear();
        }

        if (this.mCheckedIdStates != null) {
            this.mCheckedIdStates.clear();
        }

    }

    public int getCheckedItemCount() {
        return this.mCheckedItemCount;
    }

    public boolean isItemChecked(int position) {
        return this.mChoiceMode != 0 && this.mCheckStates != null ? ((Boolean) this.mCheckStates.get(position, Boolean.valueOf(false))).booleanValue() : false;
    }

    public int getCheckedItemPosition() {
        return this.mChoiceMode == 1 && this.mCheckStates != null && this.mCheckStates.size() == 1 ? this.mCheckStates.keyAt(0) : -1;
    }

    public SparseArrayCompat<Boolean> getCheckedItemPositions() {
        return this.mChoiceMode != 0 ? this.mCheckStates : null;
    }

    public long[] getCheckedItemIds() {
        if (this.mChoiceMode != 0 && this.mCheckedIdStates != null && this.mAdapter != null) {
            LongSparseArray idStates = this.mCheckedIdStates;
            int count = idStates.size();
            long[] ids = new long[count];

            for (int i = 0; i < count; ++i) {
                ids[i] = idStates.keyAt(i);
            }

            return ids;
        } else {
            return new long[0];
        }
    }

    public void clearChoices() {
        if (this.mCheckStates != null) {
            this.mCheckStates.clear();
        }

        if (this.mCheckedIdStates != null) {
            this.mCheckedIdStates.clear();
        }

        this.mCheckedItemCount = 0;
    }

    public void setItemChecked(int position, boolean value) {
        if (this.mChoiceMode != 0) {
            if (VERSION.SDK_INT >= 11 && value && this.mChoiceMode == 3 && this.mChoiceActionMode == null) {
                if (this.mMultiChoiceModeCallback == null || !((MultiChoiceModeWrapper) this.mMultiChoiceModeCallback).hasWrappedCallback()) {
                    throw new IllegalStateException("AbsListView: attempted to start selection mode for CHOICE_MODE_MULTIPLE_MODAL but no choice mode callback was supplied. Call setMultiChoiceModeListener to set a callback.");
                }

                if (this.mMultiChoiceModeCallback instanceof MultiChoiceModeWrapper) {
                    this.mChoiceActionMode = this.startActionMode((MultiChoiceModeWrapper) this.mMultiChoiceModeCallback);
                }
            }

            boolean updateIds;
            if (this.mChoiceMode == 2 || VERSION.SDK_INT >= 11 && this.mChoiceMode == 3) {
                updateIds = ((Boolean) this.mCheckStates.get(position, Boolean.valueOf(false))).booleanValue();
                this.mCheckStates.put(position, Boolean.valueOf(value));
                if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                    if (value) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    } else {
                        this.mCheckedIdStates.delete(this.mAdapter.getItemId(position));
                    }
                }

                if (updateIds != value) {
                    if (value) {
                        ++this.mCheckedItemCount;
                    } else {
                        --this.mCheckedItemCount;
                    }
                }

                if (this.mChoiceActionMode != null) {
                    long id = this.mAdapter.getItemId(position);
                    ((MultiChoiceModeWrapper) this.mMultiChoiceModeCallback).onItemCheckedStateChanged((ActionMode) this.mChoiceActionMode, position, id, value);
                }
            } else {
                updateIds = this.mCheckedIdStates != null && this.mAdapter.hasStableIds();
                if (value || this.isItemChecked(position)) {
                    this.mCheckStates.clear();
                    if (updateIds) {
                        this.mCheckedIdStates.clear();
                    }
                }

                if (value) {
                    this.mCheckStates.put(position, Boolean.valueOf(true));
                    if (updateIds) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    }

                    this.mCheckedItemCount = 1;
                } else if (this.mCheckStates.size() == 0 || !((Boolean) this.mCheckStates.valueAt(0)).booleanValue()) {
                    this.mCheckedItemCount = 0;
                }
            }

            if (!this.mInLayout && !this.mBlockLayoutRequests) {
                this.mDataChanged = true;
                this.rememberSyncState();
                this.requestLayout();
            }

        }
    }

    public boolean performItemClick(View view, int position, long id) {
        boolean handled = false;
        boolean dispatchItemClick = true;
        if (this.mChoiceMode != 0) {
            handled = true;
            boolean checkedStateChanged = false;
            boolean checked;
            if (this.mChoiceMode == 2 || VERSION.SDK_INT >= 11 && this.mChoiceMode == 3 && this.mChoiceActionMode != null) {
                checked = !((Boolean) this.mCheckStates.get(position, Boolean.valueOf(false))).booleanValue();
                this.mCheckStates.put(position, Boolean.valueOf(checked));
                if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                    if (checked) {
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    } else {
                        this.mCheckedIdStates.delete(this.mAdapter.getItemId(position));
                    }
                }

                if (checked) {
                    ++this.mCheckedItemCount;
                } else {
                    --this.mCheckedItemCount;
                }

                if (this.mChoiceActionMode != null) {
                    ((MultiChoiceModeWrapper) this.mMultiChoiceModeCallback).onItemCheckedStateChanged((ActionMode) this.mChoiceActionMode, position, id, checked);
                    dispatchItemClick = false;
                }

                checkedStateChanged = true;
            } else if (this.mChoiceMode == 1) {
                checked = !((Boolean) this.mCheckStates.get(position, Boolean.valueOf(false))).booleanValue();
                if (checked) {
                    this.mCheckStates.clear();
                    this.mCheckStates.put(position, Boolean.valueOf(true));
                    if (this.mCheckedIdStates != null && this.mAdapter.hasStableIds()) {
                        this.mCheckedIdStates.clear();
                        this.mCheckedIdStates.put(this.mAdapter.getItemId(position), Integer.valueOf(position));
                    }

                    this.mCheckedItemCount = 1;
                } else if (this.mCheckStates.size() == 0 || !((Boolean) this.mCheckStates.valueAt(0)).booleanValue()) {
                    this.mCheckedItemCount = 0;
                }

                checkedStateChanged = true;
            }

            if (checkedStateChanged) {
                this.updateOnScreenCheckedViews();
            }
        }

        if (dispatchItemClick) {
            handled |= super.performItemClick(view, position, id);
        }

        return handled;
    }

    private void updateOnScreenCheckedViews() {
        int firstPos = this.mFirstPosition;
        int count = this.getChildCount();
        boolean useActivated = VERSION.SDK_INT >= 11;

        for (int i = 0; i < count; ++i) {
            View child = this.getChildAt(i);
            int position = firstPos + i;
            if (child instanceof Checkable) {
                ((Checkable) child).setChecked(((Boolean) this.mCheckStates.get(position, Boolean.valueOf(false))).booleanValue());
            } else if (useActivated) {
                child.setActivated(((Boolean) this.mCheckStates.get(position, Boolean.valueOf(false))).booleanValue());
            }
        }

    }

    public int getChoiceMode() {
        return this.mChoiceMode;
    }

    @TargetApi(11)
    public void setChoiceMode(int choiceMode) {
        this.mChoiceMode = choiceMode;
        if (VERSION.SDK_INT >= 11 && this.mChoiceActionMode != null) {
            if (VERSION.SDK_INT >= 11) {
                ((ActionMode) this.mChoiceActionMode).finish();
            }

            this.mChoiceActionMode = null;
        }

        if (this.mChoiceMode != 0) {
            if (this.mCheckStates == null) {
                this.mCheckStates = new SparseArrayCompat();
            }

            if (this.mCheckedIdStates == null && this.mAdapter != null && this.mAdapter.hasStableIds()) {
                this.mCheckedIdStates = new LongSparseArray();
            }

            if (VERSION.SDK_INT >= 11 && this.mChoiceMode == 3) {
                this.clearChoices();
                this.setLongClickable(true);
            }
        }

    }

    @TargetApi(11)
    public void setMultiChoiceModeListener(MultiChoiceModeListener listener) {
        if (VERSION.SDK_INT >= 11) {
            if (this.mMultiChoiceModeCallback == null) {
                this.mMultiChoiceModeCallback = new MultiChoiceModeWrapper(this);
            }

            ((MultiChoiceModeWrapper) this.mMultiChoiceModeCallback).setWrapped(listener);
        } else {
            Log.e("AbsListView", "setMultiChoiceModeListener not supported for this version of Android");
        }

    }

    private boolean contentFits() {
        int childCount = this.getChildCount();
        return childCount == 0 ? true : (childCount != this.mItemCount ? false : this.getChildAt(0).getLeft() >= this.mListPadding.left && this.getChildAt(childCount - 1).getRight() <= this.getWidth() - this.mListPadding.right);
    }

    protected int getHorizontalScrollbarHeight() {
        return super.getHorizontalScrollbarHeight();
    }

    public void setSmoothScrollbarEnabled(boolean enabled) {
        this.mSmoothScrollbarEnabled = enabled;
    }

    @ExportedProperty
    public boolean isSmoothScrollbarEnabled() {
        return this.mSmoothScrollbarEnabled;
    }

    public void setOnScrollListener(AbsHorizontalListView.OnScrollListener l) {
        this.mOnScrollListener = l;
        this.invokeOnItemScrollListener();
    }

    protected void invokeOnItemScrollListener() {
        if (this.mOnScrollListener != null) {
            this.mOnScrollListener.onScroll(this, this.mFirstPosition, this.getChildCount(), this.mItemCount);
        }

        this.onScrollChanged(0, 0, 0, 0);
    }

    public void sendAccessibilityEvent(int eventType) {
        if (eventType == 4096) {
            int firstVisiblePosition = this.getFirstVisiblePosition();
            int lastVisiblePosition = this.getLastVisiblePosition();
            if (this.mLastAccessibilityScrollEventFromIndex == firstVisiblePosition && this.mLastAccessibilityScrollEventToIndex == lastVisiblePosition) {
                return;
            }

            this.mLastAccessibilityScrollEventFromIndex = firstVisiblePosition;
            this.mLastAccessibilityScrollEventToIndex = lastVisiblePosition;
        }

        super.sendAccessibilityEvent(eventType);
    }

    @TargetApi(14)
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(AbsHorizontalListView.class.getName());
    }

    @TargetApi(14)
    @SuppressLint({"Override"})
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(AbsHorizontalListView.class.getName());
        if (this.isEnabled()) {
            if (this.getFirstVisiblePosition() > 0) {
                info.addAction(8192);
            }

            if (this.getLastVisiblePosition() < this.getCount() - 1) {
                info.addAction(4096);
            }
        }

    }

    @TargetApi(16)
    public boolean performAccessibilityAction(int action, Bundle arguments) {
        if (super.performAccessibilityAction(action, arguments)) {
            return true;
        } else {
            int viewportWidth;
            switch (action) {
                case 4096:
                    if (this.isEnabled() && this.getLastVisiblePosition() < this.getCount() - 1) {
                        viewportWidth = this.getWidth() - this.mListPadding.left - this.mListPadding.right;
                        this.smoothScrollBy(viewportWidth, 200);
                        return true;
                    }

                    return false;
                case 8192:
                    if (this.isEnabled() && this.mFirstPosition > 0) {
                        viewportWidth = this.getWidth() - this.mListPadding.left - this.mListPadding.right;
                        this.smoothScrollBy(-viewportWidth, 200);
                        return true;
                    }

                    return false;
                default:
                    return false;
            }
        }
    }

    @ExportedProperty
    public boolean isScrollingCacheEnabled() {
        return this.mScrollingCacheEnabled;
    }

    public void setScrollingCacheEnabled(boolean enabled) {
        if (this.mScrollingCacheEnabled && !enabled) {
            this.clearScrollingCache();
        }

        this.mScrollingCacheEnabled = enabled;
    }

    public void getFocusedRect(Rect r) {
        View view = this.getSelectedView();
        if (view != null && view.getParent() == this) {
            view.getFocusedRect(r);
            this.offsetDescendantRectToMyCoords(view, r);
        } else {
            super.getFocusedRect(r);
        }

    }

    private void useDefaultSelector() {
        this.setSelector(new ColorDrawable(0x00000000));//this.getResources().getDrawable(com.android.internal.R.drawable.list_selector_background));//17301602
    }

    public boolean isStackFromRight() {
        return this.mStackFromRight;
    }

    public void setStackFromRight(boolean stackFromRight) {
        if (this.mStackFromRight != stackFromRight) {
            this.mStackFromRight = stackFromRight;
            this.requestLayoutIfNecessary();
        }

    }

    void requestLayoutIfNecessary() {
        if (this.getChildCount() > 0) {
            this.resetList();
            this.requestLayout();
            this.invalidate();
        }

    }

    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        AbsHorizontalListView.SavedState ss = new AbsHorizontalListView.SavedState(superState);
        if (this.mPendingSync != null) {
            ss.selectedId = this.mPendingSync.selectedId;
            ss.firstId = this.mPendingSync.firstId;
            ss.viewLeft = this.mPendingSync.viewLeft;
            ss.position = this.mPendingSync.position;
            ss.width = this.mPendingSync.width;
            ss.filter = this.mPendingSync.filter;
            ss.inActionMode = this.mPendingSync.inActionMode;
            ss.checkedItemCount = this.mPendingSync.checkedItemCount;
            ss.checkState = this.mPendingSync.checkState;
            ss.checkIdState = this.mPendingSync.checkIdState;
            return ss;
        } else {
            boolean haveChildren = this.getChildCount() > 0 && this.mItemCount > 0;
            long selectedId = this.getSelectedItemId();
            ss.selectedId = selectedId;
            ss.width = this.getWidth();
            int count;
            if (selectedId >= 0L) {
                ss.viewLeft = this.mSelectedLeft;
                ss.position = this.getSelectedItemPosition();
                ss.firstId = -1L;
            } else if (haveChildren && this.mFirstPosition > 0) {
                View idState = this.getChildAt(0);
                ss.viewLeft = idState.getLeft();
                count = this.mFirstPosition;
                if (count >= this.mItemCount) {
                    count = this.mItemCount - 1;
                }

                ss.position = count;
                ss.firstId = this.mAdapter.getItemId(count);
            } else {
                ss.viewLeft = 0;
                ss.firstId = -1L;
                ss.position = 0;
            }

            ss.filter = null;
            ss.inActionMode = VERSION.SDK_INT >= 11 && this.mChoiceMode == 3 && this.mChoiceActionMode != null;
            if (this.mCheckStates != null) {
                try {
                    ss.checkState = this.mCheckStates.clone();
                } catch (NoSuchMethodError var9) {
                    var9.printStackTrace();
                    ss.checkState = new SparseArrayCompat();
                }
            }

            if (this.mCheckedIdStates != null) {
                LongSparseArray var10 = new LongSparseArray();
                count = this.mCheckedIdStates.size();

                for (int i = 0; i < count; ++i) {
                    var10.put(this.mCheckedIdStates.keyAt(i), this.mCheckedIdStates.valueAt(i));
                }

                ss.checkIdState = var10;
            }

            ss.checkedItemCount = this.mCheckedItemCount;
            return ss;
        }
    }

    public void onRestoreInstanceState(Parcelable state) {
        AbsHorizontalListView.SavedState ss = (AbsHorizontalListView.SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mDataChanged = true;
        this.mSyncWidth = (long) ss.width;
        if (ss.selectedId >= 0L) {
            this.mNeedSync = true;
            this.mPendingSync = ss;
            this.mSyncColId = ss.selectedId;
            this.mSyncPosition = ss.position;
            this.mSpecificLeft = ss.viewLeft;
            this.mSyncMode = 0;
        } else if (ss.firstId >= 0L) {
            this.setSelectedPositionInt(-1);
            this.setNextSelectedPositionInt(-1);
            this.mSelectorPosition = -1;
            this.mNeedSync = true;
            this.mPendingSync = ss;
            this.mSyncColId = ss.firstId;
            this.mSyncPosition = ss.position;
            this.mSpecificLeft = ss.viewLeft;
            this.mSyncMode = 1;
        }

        if (ss.checkState != null) {
            this.mCheckStates = ss.checkState;
        }

        if (ss.checkIdState != null) {
            this.mCheckedIdStates = ss.checkIdState;
        }

        this.mCheckedItemCount = ss.checkedItemCount;
        if (VERSION.SDK_INT >= 11 && ss.inActionMode && this.mChoiceMode == 3 && this.mMultiChoiceModeCallback != null) {
            this.mChoiceActionMode = this.startActionMode((MultiChoiceModeWrapper) this.mMultiChoiceModeCallback);
        }

        this.requestLayout();
    }

    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if (gainFocus && this.mSelectedPosition < 0 && !this.isInTouchMode()) {
            if (!this.mIsAttached && this.mAdapter != null) {
                this.mDataChanged = true;
                this.mOldItemCount = this.mItemCount;
                this.mItemCount = this.mAdapter.getCount();
            }

            this.resurrectSelection();
        }

    }

    public void requestLayout() {
        if (!this.mBlockLayoutRequests && !this.mInLayout) {
            super.requestLayout();
        }

    }

    protected void resetList() {
        this.removeAllViewsInLayout();
        this.mFirstPosition = 0;
        this.mDataChanged = false;
        this.mPositionScrollAfterLayout = null;
        this.mNeedSync = false;
        this.mPendingSync = null;
        this.mOldSelectedPosition = -1;
        this.mOldSelectedColId = -9223372036854775808L;
        this.setSelectedPositionInt(-1);
        this.setNextSelectedPositionInt(-1);
        this.mSelectedLeft = 0;
        this.mSelectorPosition = -1;
        this.mSelectorRect.setEmpty();
        this.invalidate();
    }

    protected int computeHorizontalScrollExtent() {
        int count = this.getChildCount();
        if (count > 0) {
            if (this.mSmoothScrollbarEnabled) {
                int extent = count * 100;
                View view = this.getChildAt(0);
                int left = view.getLeft();
                int width = view.getWidth();
                if (width > 0) {
                    extent += left * 100 / width;
                }

                view = this.getChildAt(count - 1);
                int right = view.getRight();
                width = view.getWidth();
                if (width > 0) {
                    extent -= (right - this.getWidth()) * 100 / width;
                }

                return extent;
            } else {
                return 1;
            }
        } else {
            return 0;
        }
    }

    protected int computeHorizontalScrollOffset() {
        int firstPosition = this.mFirstPosition;
        int childCount = this.getChildCount();
        if (firstPosition >= 0 && childCount > 0) {
            int count;
            if (!this.mSmoothScrollbarEnabled) {
                count = this.mItemCount;
                int index1;
                if (firstPosition == 0) {
                    index1 = 0;
                } else if (firstPosition + childCount == count) {
                    index1 = count;
                } else {
                    index1 = firstPosition + childCount / 2;
                }

                return (int) ((float) firstPosition + (float) childCount * ((float) index1 / (float) count));
            }

            View index = this.getChildAt(0);
            count = index.getLeft();
            int width = index.getWidth();
            if (width > 0) {
                return Math.max(firstPosition * 100 - count * 100 / width + (int) ((float) this.getScrollX() / (float) this.getWidth() * (float) this.mItemCount * 100.0F), 0);
            }
        }

        return 0;
    }

    protected int computeHorizontalScrollRange() {
        int result;
        if (this.mSmoothScrollbarEnabled) {
            result = Math.max(this.mItemCount * 100, 0);
            if (this.getScrollX() != 0) {
                result += Math.abs((int) ((float) this.getScrollX() / (float) this.getWidth() * (float) this.mItemCount * 100.0F));
            }
        } else {
            result = this.mItemCount;
        }

        return result;
    }

    protected float getLeftFadingEdgeStrength() {
        int count = this.getChildCount();
        float fadeEdge = super.getLeftFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        } else if (this.mFirstPosition > 0) {
            return 1.0F;
        } else {
            int left = this.getChildAt(0).getLeft();
            float fadeLength = (float) this.getHorizontalFadingEdgeLength();
            return left < this.getPaddingLeft() ? (float) (-(left - this.getPaddingLeft())) / fadeLength : fadeEdge;
        }
    }

    protected float getRightFadingEdgeStrength() {
        int count = this.getChildCount();
        float fadeEdge = super.getRightFadingEdgeStrength();
        if (count == 0) {
            return fadeEdge;
        } else if (this.mFirstPosition + count - 1 < this.mItemCount - 1) {
            return 1.0F;
        } else {
            int right = this.getChildAt(count - 1).getRight();
            int width = this.getWidth();
            float fadeLength = (float) this.getHorizontalFadingEdgeLength();
            return right > width - this.getPaddingRight() ? (float) (right - width + this.getPaddingRight()) / fadeLength : fadeEdge;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this.mSelector == null) {
            this.useDefaultSelector();
        }

        Rect listPadding = this.mListPadding;
        listPadding.left = this.mSelectionLeftPadding + this.getPaddingLeft();
        listPadding.top = this.mSelectionTopPadding + this.getPaddingTop();
        listPadding.right = this.mSelectionRightPadding + this.getPaddingRight();
        listPadding.bottom = this.mSelectionBottomPadding + this.getPaddingBottom();
        if (this.mTranscriptMode == 1) {
            int childCount = this.getChildCount();
            int listRight = this.getWidth() - this.getPaddingRight();
            View lastChild = this.getChildAt(childCount - 1);
            int lastRight = lastChild != null ? lastChild.getRight() : listRight;
            this.mForceTranscriptScroll = this.mFirstPosition + childCount >= this.mLastHandledItemCount && lastRight <= listRight;
        }

    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mInLayout = true;
        if (changed) {
            int childCount = this.getChildCount();

            for (int i = 0; i < childCount; ++i) {
                this.getChildAt(i).forceLayout();
            }

            this.mRecycler.markChildrenDirty();
        }

        this.layoutChildren();
        this.mInLayout = false;
        this.mOverscrollMax = (r - l) / 3;
    }

    protected void layoutChildren() {
    }

    protected void updateScrollIndicators() {
        boolean canScrollRight;
        if (this.mScrollLeft != null) {
            canScrollRight = this.mFirstPosition > 0;
            if (!canScrollRight && this.getChildCount() > 0) {
                View count = this.getChildAt(0);
                canScrollRight = count.getLeft() < this.mListPadding.left;
            }

            this.mScrollLeft.setVisibility(canScrollRight ? View.VISIBLE : View.INVISIBLE);
        }

        if (this.mScrollRight != null) {
            int count1 = this.getChildCount();
            canScrollRight = this.mFirstPosition + count1 < this.mItemCount;
            if (!canScrollRight && count1 > 0) {
                View child = this.getChildAt(count1 - 1);
                canScrollRight = child.getRight() > this.getRight() - this.mListPadding.right;
            }

            this.mScrollRight.setVisibility(canScrollRight ? View.VISIBLE : View.INVISIBLE);
        }

    }

    @ExportedProperty
    public View getSelectedView() {
        return this.mItemCount > 0 && this.mSelectedPosition >= 0 ? this.getChildAt(this.mSelectedPosition - this.mFirstPosition) : null;
    }

    public int getListPaddingTop() {
        return this.mListPadding.top;
    }

    public int getListPaddingBottom() {
        return this.mListPadding.bottom;
    }

    public int getListPaddingLeft() {
        return this.mListPadding.left;
    }

    public int getListPaddingRight() {
        return this.mListPadding.right;
    }

    @SuppressLint({"NewApi"})
    protected View obtainView(int position, boolean[] isScrap) {
        isScrap[0] = false;
        View scrapView = this.mRecycler.getTransientStateView(position);
        if (scrapView != null) {
            return scrapView;
        } else {
            scrapView = this.mRecycler.getScrapView(position);
            View child;
            if (scrapView != null) {
                child = this.mAdapter.getView(position, scrapView, this);
                if (VERSION.SDK_INT >= 16 && child.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                    child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                }

                if (child != scrapView) {
                    this.mRecycler.addScrapView(scrapView, position);
                    if (this.mCacheColorHint != 0) {
                        child.setDrawingCacheBackgroundColor(this.mCacheColorHint);
                    }
                } else {
                    isScrap[0] = true;
                    child.onFinishTemporaryDetach();
                }
            } else {
                child = this.mAdapter.getView(position, (View) null, this);
                if (VERSION.SDK_INT >= 16 && child.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
                    child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
                }

                if (this.mCacheColorHint != 0) {
                    child.setDrawingCacheBackgroundColor(this.mCacheColorHint);
                }
            }

            if (this.mAdapterHasStableIds) {
                android.view.ViewGroup.LayoutParams vlp = child.getLayoutParams();
                AbsHorizontalListView.LayoutParams lp;
                if (vlp == null) {
                    lp = (AbsHorizontalListView.LayoutParams) this.generateDefaultLayoutParams();
                } else if (!this.checkLayoutParams(vlp)) {
                    lp = (AbsHorizontalListView.LayoutParams) this.generateLayoutParams(vlp);
                } else {
                    lp = (AbsHorizontalListView.LayoutParams) vlp;
                }

                lp.itemId = this.mAdapter.getItemId(position);
                child.setLayoutParams(lp);
            }

            if (this.mAccessibilityManager.isEnabled() && this.mAccessibilityDelegate == null) {
                this.mAccessibilityDelegate = new AbsHorizontalListView.ListItemAccessibilityDelegate();
            }

            return child;
        }
    }

    protected void positionSelector(int position, View sel) {
        if (position != -1) {
            this.mSelectorPosition = position;
        }

        Rect selectorRect = this.mSelectorRect;
        selectorRect.set(sel.getLeft(), sel.getTop(), sel.getRight(), sel.getBottom());
        if (sel instanceof AbsHorizontalListView.SelectionBoundsAdjuster) {
            ((AbsHorizontalListView.SelectionBoundsAdjuster) sel).adjustListItemSelectionBounds(selectorRect);
        }

        this.positionSelector(selectorRect.left, selectorRect.top, selectorRect.right, selectorRect.bottom);
        boolean isChildViewEnabled = this.mIsChildViewEnabled;
        if (sel.isEnabled() != isChildViewEnabled) {
            this.mIsChildViewEnabled = !isChildViewEnabled;
            if (this.getSelectedItemPosition() != -1) {
                this.refreshDrawableState();
            }
        }

    }

    private void positionSelector(int l, int t, int r, int b) {
        this.mSelectorRect.set(l - this.mSelectionLeftPadding, t - this.mSelectionTopPadding, r + this.mSelectionRightPadding, b + this.mSelectionBottomPadding);
    }

    protected void dispatchDraw(Canvas canvas) {
        boolean drawSelectorOnTop = this.mDrawSelectorOnTop;
        if (!drawSelectorOnTop) {
            this.drawSelector(canvas);
        }

        this.dispatchCustomDraw(canvas);
        if (drawSelectorOnTop) {
            this.drawSelector(canvas);
        }

    }

    public void dispatchCustomDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    public void resetInCenter() {
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (this.getChildCount() > 0) {
            this.mDataChanged = true;
            this.rememberSyncState();
        }

    }

    boolean touchModeDrawsInPressedState() {
        switch (this.mTouchMode) {
            case 1:
            case 2:
                return true;
            default:
                return false;
        }
    }

    protected boolean shouldShowSelector() {
        return this.hasFocus() && !this.isInTouchMode() || this.touchModeDrawsInPressedState();
    }

    private void drawSelector(Canvas canvas) {
        if (!this.mSelectorRect.isEmpty()) {
            Drawable selector = this.mSelector;
            selector.setBounds(this.mSelectorRect);
            selector.draw(canvas);
        }

    }

    public void setDrawSelectorOnTop(boolean onTop) {
        this.mDrawSelectorOnTop = onTop;
    }

    public void setSelector(int resID) {
        this.setSelector(this.getResources().getDrawable(resID));
    }

    public void setSelector(Drawable sel) {
        if (this.mSelector != null) {
            this.mSelector.setCallback((Callback) null);
            this.unscheduleDrawable(this.mSelector);
        }

        this.mSelector = sel;
        Rect padding = new Rect();
        sel.getPadding(padding);
        this.mSelectionLeftPadding = padding.left;
        this.mSelectionTopPadding = padding.top;
        this.mSelectionRightPadding = padding.right;
        this.mSelectionBottomPadding = padding.bottom;
        sel.setCallback(this);
        this.updateSelectorState();
    }

    public Drawable getSelector() {
        return this.mSelector;
    }

    protected void keyPressed() {
        if (this.isEnabled() && this.isClickable()) {
            Drawable selector = this.mSelector;
            Rect selectorRect = this.mSelectorRect;
            if (selector != null && (this.isFocused() || this.touchModeDrawsInPressedState()) && !selectorRect.isEmpty()) {
                View v = this.getChildAt(this.mSelectedPosition - this.mFirstPosition);
                if (v != null) {
                    if (v.hasFocusable()) {
                        return;
                    }

                    v.setPressed(true);
                }

                this.setPressed(true);
                boolean longClickable = this.isLongClickable();
                Drawable d = selector.getCurrent();
                if (d != null && d instanceof TransitionDrawable) {
                    if (longClickable) {
                        ((TransitionDrawable) d).startTransition(ViewConfiguration.getLongPressTimeout());
                    } else {
                        ((TransitionDrawable) d).resetTransition();
                    }
                }

                if (longClickable && !this.mDataChanged) {
                    if (this.mPendingCheckForKeyLongPress == null) {
                        this.mPendingCheckForKeyLongPress = new AbsHorizontalListView.CheckForKeyLongPress();
                    }

                    this.mPendingCheckForKeyLongPress.rememberWindowAttachCount();
                    this.postDelayed(this.mPendingCheckForKeyLongPress, (long) ViewConfiguration.getLongPressTimeout());
                }
            }

        }
    }

    public void setScrollIndicators(View left, View right) {
        this.mScrollLeft = left;
        this.mScrollRight = right;
    }

    void updateSelectorState() {
        if (this.mSelector != null) {
            if (this.shouldShowSelector()) {
                this.mSelector.setState(this.getDrawableState());
            } else {
                this.mSelector.setState(STATESET_NOTHING);
            }
        }

    }

    protected void drawableStateChanged() {
        super.drawableStateChanged();
        this.updateSelectorState();
    }

    @SuppressLint({"Override"})
    protected int[] onCreateDrawableState(int extraSpace) {
        if (this.mIsChildViewEnabled) {
            return super.onCreateDrawableState(extraSpace);
        } else {
            int enabledState = ENABLED_STATE_SET[0];
            int[] state = super.onCreateDrawableState(extraSpace + 1);
            int enabledPos = -1;

            for (int i = state.length - 1; i >= 0; --i) {
                if (state[i] == enabledState) {
                    enabledPos = i;
                    break;
                }
            }

            if (enabledPos >= 0) {
                System.arraycopy(state, enabledPos + 1, state, enabledPos, state.length - enabledPos - 1);
            }

            return state;
        }
    }

    public boolean verifyDrawable(Drawable dr) {
        return this.mSelector == dr || super.verifyDrawable(dr);
    }

    @TargetApi(11)
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (this.mSelector != null) {
            this.mSelector.jumpToCurrentState();
        }

    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewTreeObserver treeObserver = this.getViewTreeObserver();
        treeObserver.addOnTouchModeChangeListener(this);
        if (this.mAdapter != null && this.mDataSetObserver == null) {
            this.mDataSetObserver = new AbsHorizontalListView.AdapterDataSetObserver(null);
            this.mAdapter.registerDataSetObserver(this.mDataSetObserver);
            this.mDataChanged = true;
            this.mOldItemCount = this.mItemCount;
            this.mItemCount = this.mAdapter.getCount();
        }

        this.mIsAttached = true;
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mRecycler.clear();
        ViewTreeObserver treeObserver = this.getViewTreeObserver();
        treeObserver.removeOnTouchModeChangeListener(this);
        if (this.mAdapter != null && this.mDataSetObserver != null) {
            this.mAdapter.unregisterDataSetObserver(this.mDataSetObserver);
            this.mDataSetObserver = null;
        }

        if (this.mFlingRunnable != null) {
            this.removeCallbacks(this.mFlingRunnable);
        }

        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }

        if (this.mClearScrollingCache != null) {
            this.removeCallbacks(this.mClearScrollingCache);
        }

        if (this.mPerformClick != null) {
            this.removeCallbacks(this.mPerformClick);
        }

        if (this.mTouchModeReset != null) {
            this.removeCallbacks(this.mTouchModeReset);
            this.mTouchModeReset = null;
        }

        this.mIsAttached = false;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int touchMode = this.isInTouchMode() ? 0 : 1;
        if (!hasWindowFocus) {
            this.setChildrenDrawingCacheEnabled(false);
            if (this.mFlingRunnable != null) {
                this.removeCallbacks(this.mFlingRunnable);
                this.mFlingRunnable.endFling();
                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }

                if (this.getScrollX() != 0) {
                    this.mViewHelper.setScrollX(0);
                    this.finishGlows();
                    this.invalidate();
                }
            }

            if (touchMode == 1) {
                this.mResurrectToPosition = this.mSelectedPosition;
            }
        } else if (touchMode != this.mLastTouchMode && this.mLastTouchMode != -1) {
            if (touchMode == 1) {
                this.resurrectSelection();
            } else {
                this.hideSelector();
                this.mLayoutMode = 0;
                this.layoutChildren();
            }
        }

        this.mLastTouchMode = touchMode;
    }

    ContextMenuInfo createContextMenuInfo(View view, int position, long id) {
        return new AdapterContextMenuInfo(view, position, id);
    }

    boolean performLongPress(View child, int longPressPosition, long longPressId) {
        if (VERSION.SDK_INT >= 11 && this.mChoiceMode == 3) {
            if (this.mMultiChoiceModeCallback instanceof MultiChoiceModeWrapper && this.mChoiceActionMode == null && (this.mChoiceActionMode = this.startActionMode((MultiChoiceModeWrapper) this.mMultiChoiceModeCallback)) != null) {
                this.setItemChecked(longPressPosition, true);
                this.performHapticFeedback(0);
            }

            return true;
        } else {
            boolean handled = false;
            if (this.mOnItemLongClickListener != null) {
                handled = this.mOnItemLongClickListener.onItemLongClick(this, child, longPressPosition, longPressId);
            }

            if (!handled) {
                this.mContextMenuInfo = this.createContextMenuInfo(child, longPressPosition, longPressId);
                handled = super.showContextMenuForChild(this);
            }

            if (handled) {
                this.performHapticFeedback(0);
            }

            return handled;
        }
    }

    protected ContextMenuInfo getContextMenuInfo() {
        return this.mContextMenuInfo;
    }

    public boolean showContextMenu(float x, float y, int metaState) {
        int position = this.pointToPosition((int) x, (int) y);
        if (position != -1) {
            long id = this.mAdapter.getItemId(position);
            View child = this.getChildAt(position - this.mFirstPosition);
            if (child != null) {
                this.mContextMenuInfo = this.createContextMenuInfo(child, position, id);
                return super.showContextMenuForChild(this);
            }
        }

        return this.showContextMenu(x, y, metaState);
    }

    public boolean showContextMenuForChild(View originalView) {
        int longPressPosition = this.getPositionForView(originalView);
        if (longPressPosition >= 0) {
            long longPressId = this.mAdapter.getItemId(longPressPosition);
            boolean handled = false;
            if (this.mOnItemLongClickListener != null) {
                handled = this.mOnItemLongClickListener.onItemLongClick(this, originalView, longPressPosition, longPressId);
            }

            if (!handled) {
                this.mContextMenuInfo = this.createContextMenuInfo(this.getChildAt(longPressPosition - this.mFirstPosition), longPressPosition, longPressId);
                handled = super.showContextMenuForChild(originalView);
            }

            return handled;
        } else {
            return false;
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return false;
    }

    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case 23:
            case 66:
                if (!this.isEnabled()) {
                    return true;
                } else if (this.isClickable() && this.isPressed() && this.mSelectedPosition >= 0 && this.mAdapter != null && this.mSelectedPosition < this.mAdapter.getCount()) {
                    View view = this.getChildAt(this.mSelectedPosition - this.mFirstPosition);
                    if (view != null) {
                        this.performItemClick(view, this.mSelectedPosition, this.mSelectedColId);
                        view.setPressed(false);
                    }

                    this.setPressed(false);
                    return true;
                }
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    protected void dispatchSetPressed(boolean pressed) {
    }

    public int pointToPosition(int x, int y) {
        Rect frame = this.mTouchFrame;
        if (frame == null) {
            this.mTouchFrame = new Rect();
            frame = this.mTouchFrame;
        }

        int count = this.getChildCount();

        for (int i = count - 1; i >= 0; --i) {
            View child = this.getChildAt(i);
            if (child.getVisibility() == View.VISIBLE) {
                child.getHitRect(frame);
                if (frame.contains(x, y)) {
                    return this.mFirstPosition + i;
                }
            }
        }

        return -1;
    }

    public long pointToColId(int x, int y) {
        int position = this.pointToPosition(x, y);
        return position >= 0 ? this.mAdapter.getItemId(position) : -9223372036854775808L;
    }

    private boolean startScrollIfNeeded(int x) {
        int deltaX = x - this.mMotionX;
        int distance = Math.abs(deltaX);
        boolean overscroll = this.getScrollX() != 0;
        if (!overscroll && distance <= this.mTouchSlop) {
            return false;
        } else {
            this.createScrollingCache();
            if (overscroll) {
                this.mTouchMode = 5;
                this.mMotionCorrection = 0;
            } else {
                this.mTouchMode = 3;
                this.mMotionCorrection = deltaX > 0 ? this.mTouchSlop : -this.mTouchSlop;
            }

            Handler handler = this.getHandler();
            if (handler != null) {
                handler.removeCallbacks(this.mPendingCheckForLongPress);
            }

            this.setPressed(false);
            View motionView = this.getChildAt(this.mMotionPosition - this.mFirstPosition);
            if (motionView != null) {
                motionView.setPressed(false);
            }

            this.reportScrollStateChange(1);
            ViewParent parent = this.getParent();
            if (parent != null) {
                parent.requestDisallowInterceptTouchEvent(true);
            }

            this.scrollIfNeeded(x);
            return true;
        }
    }

    private void scrollIfNeeded(int x) {
        int rawDeltaX = x - this.mMotionX;
        int deltaX = rawDeltaX - this.mMotionCorrection;
        int incrementalDeltaX = this.mLastX != -2147483648 ? x - this.mLastX : deltaX;
        int newScroll;
        int motionPosition;
        int oldScroll1;
        if (this.mTouchMode == 3) {
            if (x != this.mLastX) {
                if (Math.abs(rawDeltaX) > this.mTouchSlop) {
                    ViewParent oldScroll = this.getParent();
                    if (oldScroll != null) {
                        oldScroll.requestDisallowInterceptTouchEvent(true);
                    }
                }

                if (this.mMotionPosition >= 0) {
                    oldScroll1 = this.mMotionPosition - this.mFirstPosition;
                } else {
                    oldScroll1 = this.getChildCount() / 2;
                }

                newScroll = 0;
                View newDirection = this.getChildAt(oldScroll1);
                if (newDirection != null) {
                    newScroll = newDirection.getLeft();
                }

                boolean overScrollDistance = false;
                if (incrementalDeltaX != 0) {
                    overScrollDistance = this.trackMotionScroll(deltaX, incrementalDeltaX);
                }

                newDirection = this.getChildAt(oldScroll1);
                if (newDirection != null) {
                    motionPosition = newDirection.getLeft();
                    if (overScrollDistance) {
                        int motionView = -incrementalDeltaX - (motionPosition - newScroll);
                        this.overScrollBy(motionView, 0, this.getScrollX(), 0, 0, 0, this.mOverscrollDistance, 0, true);
                        if (Math.abs(this.mOverscrollDistance) == Math.abs(this.getScrollX()) && this.mVelocityTracker != null) {
                            this.mVelocityTracker.clear();
                        }

                        int overscrollMode = this.getOverScrollMode();
                        if (overscrollMode == 0 || overscrollMode == 1 && !this.contentFits()) {
                            this.mDirection = 0;
                            this.mTouchMode = 5;
                            if (rawDeltaX > 0) {
                                this.mEdgeGlowLeft.onPull((float) motionView / (float) this.getWidth());
                                if (!this.mEdgeGlowRight.isFinished()) {
                                    this.mEdgeGlowRight.onRelease();
                                }

                                this.invalidate();
                            } else if (rawDeltaX < 0) {
                                this.mEdgeGlowRight.onPull((float) motionView / (float) this.getWidth());
                                if (!this.mEdgeGlowLeft.isFinished()) {
                                    this.mEdgeGlowLeft.onRelease();
                                }

                                this.invalidate();
                            }
                        }
                    }

                    this.mMotionX = x;
                }

                this.mLastX = x;
            }
        } else if (this.mTouchMode == 5 && x != this.mLastX) {
            oldScroll1 = this.getScrollX();
            newScroll = oldScroll1 - incrementalDeltaX;
            int newDirection1 = x > this.mLastX ? 1 : -1;
            if (this.mDirection == 0) {
                this.mDirection = newDirection1;
            }

            int overScrollDistance1 = -incrementalDeltaX;
            if (newScroll < 0 && oldScroll1 >= 0 || newScroll > 0 && oldScroll1 <= 0) {
                overScrollDistance1 = -oldScroll1;
                incrementalDeltaX += overScrollDistance1;
            } else {
                incrementalDeltaX = 0;
            }

            if (overScrollDistance1 != 0) {
                this.overScrollBy(overScrollDistance1, 0, this.getScrollX(), 0, 0, 0, this.mOverscrollDistance, 0, true);
                motionPosition = this.getOverScrollMode();
                if (motionPosition == 0 || motionPosition == 1 && !this.contentFits()) {
                    if (rawDeltaX > 0) {
                        this.mEdgeGlowLeft.onPull((float) overScrollDistance1 / (float) this.getWidth());
                        if (!this.mEdgeGlowRight.isFinished()) {
                            this.mEdgeGlowRight.onRelease();
                        }

                        this.invalidate();
                    } else if (rawDeltaX < 0) {
                        this.mEdgeGlowRight.onPull((float) overScrollDistance1 / (float) this.getWidth());
                        if (!this.mEdgeGlowLeft.isFinished()) {
                            this.mEdgeGlowLeft.onRelease();
                        }

                        this.invalidate();
                    }
                }
            }

            if (incrementalDeltaX != 0) {
                if (this.getScrollX() != 0) {
                    this.mViewHelper.setScrollX(0);
                    this.invalidateParentIfNeeded();
                }

                this.trackMotionScroll(incrementalDeltaX, incrementalDeltaX);
                this.mTouchMode = 3;
                motionPosition = this.findClosestMotionCol(x);
                this.mMotionCorrection = 0;
                View motionView1 = this.getChildAt(motionPosition - this.mFirstPosition);
                this.mMotionViewOriginalLeft = motionView1 != null ? motionView1.getLeft() : 0;
                this.mMotionX = x;
                this.mMotionPosition = motionPosition;
            }

            this.mLastX = x;
            this.mDirection = newDirection1;
        }

    }

    @TargetApi(11)
    protected void invalidateParentIfNeeded() {
        if (this.mViewHelper.isHardwareAccelerated() && this.getParent() instanceof View) {
            ((View) this.getParent()).invalidate();
        }

    }

    public void onTouchModeChanged(boolean isInTouchMode) {
        if (isInTouchMode) {
            this.hideSelector();
            if (this.getWidth() > 0 && this.getChildCount() > 0) {
                this.layoutChildren();
            }

            this.updateSelectorState();
        } else {
            int touchMode = this.mTouchMode;
            if (touchMode == 5 || touchMode == 6) {
                if (this.mFlingRunnable != null) {
                    this.mFlingRunnable.endFling();
                }

                if (this.mPositionScroller != null) {
                    this.mPositionScroller.stop();
                }

                if (this.getScrollX() != 0) {
                    this.mViewHelper.setScrollX(0);
                    this.finishGlows();
                    this.invalidate();
                }
            }
        }

    }

    @TargetApi(14)
    protected boolean performButtonActionOnTouchDown(MotionEvent event) {
        return VERSION.SDK_INT >= 14 && (event.getButtonState() & 2) != 0 && this.showContextMenu(event.getX(), event.getY(), event.getMetaState());
    }

    @SuppressLint({"Override"})
    public boolean onTouchEvent(MotionEvent ev) {
        if (!this.isEnabled()) {
            return this.isClickable() || this.isLongClickable();
        } else {
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }

            if (!this.mIsAttached) {
                return false;
            } else {
                int action = ev.getAction();
                this.initVelocityTrackerIfNotExists();
                this.mVelocityTracker.addMovement(ev);
                View v;
                int index;
                int id;
                int x;
                int motionPosition;
                switch (action & 255) {
                    case 0:
                        switch (this.mTouchMode) {
                            case 6:
                                this.mFlingRunnable.endFling();
                                if (this.mPositionScroller != null) {
                                    this.mPositionScroller.stop();
                                }

                                this.mTouchMode = 5;
                                this.mMotionY = (int) ev.getY();
                                this.mMotionX = this.mLastX = (int) ev.getX();
                                this.mMotionCorrection = 0;
                                this.mActivePointerId = ev.getPointerId(0);
                                this.mDirection = 0;
                                break;
                            default:
                                this.mActivePointerId = ev.getPointerId(0);
                                index = (int) ev.getX();
                                id = (int) ev.getY();
                                x = this.pointToPosition(index, id);
                                if (!this.mDataChanged) {
                                    if (this.mTouchMode != 4 && x >= 0 && ((ListAdapter) this.getAdapter()).isEnabled(x)) {
                                        this.mTouchMode = 0;
                                        if (this.mPendingCheckForTap == null) {
                                            this.mPendingCheckForTap = new AbsHorizontalListView.CheckForTap();
                                        }

                                        this.postDelayed(this.mPendingCheckForTap, (long) ViewConfiguration.getTapTimeout());
                                    } else if (this.mTouchMode == 4) {
                                        this.createScrollingCache();
                                        this.mTouchMode = 3;
                                        this.mMotionCorrection = 0;
                                        x = this.findMotionCol(index);
                                        this.mFlingRunnable.flywheelTouch();
                                    }
                                }

                                if (x >= 0) {
                                    v = this.getChildAt(x - this.mFirstPosition);
                                    this.mMotionViewOriginalLeft = v.getLeft();
                                }

                                this.mMotionX = index;
                                this.mMotionY = id;
                                this.mMotionPosition = x;
                                this.mLastX = -2147483648;
                        }

                        if (this.performButtonActionOnTouchDown(ev) && this.mTouchMode == 0) {
                            this.removeCallbacks(this.mPendingCheckForTap);
                        }
                        break;
                    case 1:
                        int initialVelocity;
                        switch (this.mTouchMode) {
                            case 0:
                            case 1:
                            case 2:
                                index = this.mMotionPosition;
                                final View id2 = this.getChildAt(index - this.mFirstPosition);
                                float x1 = ev.getX();
                                boolean y1 = x1 > (float) this.mListPadding.left && x1 < (float) (this.getWidth() - this.mListPadding.right);
                                if (id2 != null && !id2.hasFocusable() && y1) {
                                    if (this.mTouchMode != 0) {
                                        id2.setPressed(false);
                                    }

                                    if (this.mPerformClick == null) {
                                        this.mPerformClick = new AbsHorizontalListView.PerformClick();
                                    }

                                    final AbsHorizontalListView.PerformClick motionPosition1 = this.mPerformClick;
                                    motionPosition1.mClickMotionPosition = index;
                                    motionPosition1.rememberWindowAttachCount();
                                    this.mResurrectToPosition = index;
                                    if (this.mTouchMode == 0 || this.mTouchMode == 1) {
                                        Handler velocityTracker3 = this.getHandler();
                                        if (velocityTracker3 != null) {
                                            velocityTracker3.removeCallbacks((Runnable) (this.mTouchMode == 0 ? this.mPendingCheckForTap : this.mPendingCheckForLongPress));
                                        }

                                        this.mLayoutMode = 0;
                                        if (!this.mDataChanged && this.mAdapter.isEnabled(index)) {
                                            this.mTouchMode = 1;
                                            this.setSelectedPositionInt(this.mMotionPosition);
                                            this.layoutChildren();
                                            id2.setPressed(true);
                                            this.positionSelector(this.mMotionPosition, id2);
                                            this.setPressed(true);
                                            if (this.mSelector != null) {
                                                Drawable initialVelocity2 = this.mSelector.getCurrent();
                                                if (initialVelocity2 != null && initialVelocity2 instanceof TransitionDrawable) {
                                                    ((TransitionDrawable) initialVelocity2).resetTransition();
                                                }
                                            }

                                            if (this.mTouchModeReset != null) {
                                                this.removeCallbacks(this.mTouchModeReset);
                                            }

                                            this.mTouchModeReset = new Runnable() {
                                                public void run() {
                                                    AbsHorizontalListView.this.mTouchMode = -1;
                                                    id2.setPressed(false);
                                                    AbsHorizontalListView.this.setPressed(false);
                                                    if (!AbsHorizontalListView.this.mDataChanged) {
                                                        motionPosition1.run();
                                                    }

                                                }
                                            };
                                            this.postDelayed(this.mTouchModeReset, (long) ViewConfiguration.getPressedStateDuration());
                                        } else {
                                            this.mTouchMode = -1;
                                            this.updateSelectorState();
                                        }

                                        return true;
                                    }

                                    if (!this.mDataChanged && this.mAdapter.isEnabled(index)) {
                                        motionPosition1.run();
                                    }
                                }

                                this.mTouchMode = -1;
                                this.updateSelectorState();
                                break;
                            case 3:
                                motionPosition = this.getChildCount();
                                if (motionPosition > 0) {
                                    int velocityTracker2 = this.getChildAt(0).getLeft();
                                    initialVelocity = this.getChildAt(motionPosition - 1).getRight();
                                    int contentLeft = this.mListPadding.left;
                                    int contentRight = this.getWidth() - this.mListPadding.right;
                                    if (this.mFirstPosition == 0 && velocityTracker2 >= contentLeft && this.mFirstPosition + motionPosition < this.mItemCount && initialVelocity <= this.getWidth() - contentRight) {
                                        this.mTouchMode = -1;
                                        this.reportScrollStateChange(0);
                                    } else {
                                        VelocityTracker velocityTracker1 = this.mVelocityTracker;
                                        velocityTracker1.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                                        int initialVelocity1 = (int) (velocityTracker1.getXVelocity(this.mActivePointerId) * this.mVelocityScale);
                                        if (Math.abs(initialVelocity1) <= this.mMinimumVelocity || this.mFirstPosition == 0 && velocityTracker2 == contentLeft - this.mOverscrollDistance || this.mFirstPosition + motionPosition == this.mItemCount && initialVelocity == contentRight + this.mOverscrollDistance) {
                                            this.mTouchMode = -1;
                                            this.reportScrollStateChange(0);
                                            if (this.mFlingRunnable != null) {
                                                this.mFlingRunnable.endFling();
                                            } else {
                                                this.resetInCenter();
                                            }

                                            if (this.mPositionScroller != null) {
                                                this.mPositionScroller.stop();
                                            }
                                        } else {
                                            if (this.mFlingRunnable == null) {
                                                this.mFlingRunnable = new AbsHorizontalListView.FlingRunnable();
                                            }

                                            this.reportScrollStateChange(2);
                                            this.mFlingRunnable.start(-initialVelocity1);
                                        }
                                    }
                                } else {
                                    this.mTouchMode = -1;
                                    this.reportScrollStateChange(0);
                                }
                            case 4:
                            default:
                                break;
                            case 5:
                                if (this.mFlingRunnable == null) {
                                    this.mFlingRunnable = new AbsHorizontalListView.FlingRunnable();
                                }

                                VelocityTracker velocityTracker = this.mVelocityTracker;
                                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                                initialVelocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
                                this.reportScrollStateChange(2);
                                if (Math.abs(initialVelocity) > this.mMinimumVelocity) {
                                    this.mFlingRunnable.startOverfling(-initialVelocity);
                                } else {
                                    this.mFlingRunnable.startSpringback();
                                }
                        }

                        this.setPressed(false);
                        if (this.mEdgeGlowLeft != null) {
                            this.mEdgeGlowLeft.onRelease();
                            this.mEdgeGlowRight.onRelease();
                        }

                        this.invalidate();
                        Handler index2 = this.getHandler();
                        if (index2 != null) {
                            index2.removeCallbacks(this.mPendingCheckForLongPress);
                        }

                        this.recycleVelocityTracker();
                        this.mActivePointerId = -1;
                        break;
                    case 2:
                        index = ev.findPointerIndex(this.mActivePointerId);
                        if (index == -1) {
                            index = 0;
                            this.mActivePointerId = ev.getPointerId(index);
                        }

                        id = (int) ev.getX(index);
                        if (this.mDataChanged) {
                            this.layoutChildren();
                        }

                        switch (this.mTouchMode) {
                            case 0:
                            case 1:
                            case 2:
                                this.startScrollIfNeeded(id);
                                return true;
                            case 3:
                            case 5:
                                this.scrollIfNeeded(id);
                                return true;
                            case 4:
                            default:
                                return true;
                        }
                    case 3:
                        switch (this.mTouchMode) {
                            case 5:
                                if (this.mFlingRunnable == null) {
                                    this.mFlingRunnable = new AbsHorizontalListView.FlingRunnable();
                                }

                                this.mFlingRunnable.startSpringback();
                            case 6:
                                break;
                            default:
                                this.mTouchMode = -1;
                                this.setPressed(false);
                                View index1 = this.getChildAt(this.mMotionPosition - this.mFirstPosition);
                                if (index1 != null) {
                                    index1.setPressed(false);
                                }

                                this.clearScrollingCache();
                                Handler id1 = this.getHandler();
                                if (id1 != null) {
                                    id1.removeCallbacks(this.mPendingCheckForLongPress);
                                }

                                this.recycleVelocityTracker();
                        }

                        if (this.mEdgeGlowLeft != null) {
                            this.mEdgeGlowLeft.onRelease();
                            this.mEdgeGlowRight.onRelease();
                        }

                        this.mActivePointerId = -1;
                    case 4:
                    default:
                        break;
                    case 5:
                        index = ev.getActionIndex();
                        id = ev.getPointerId(index);
                        x = (int) ev.getX(index);
                        int y = (int) ev.getY(index);
                        this.mMotionCorrection = 0;
                        this.mActivePointerId = id;
                        this.mMotionX = x;
                        this.mMotionY = y;
                        motionPosition = this.pointToPosition(x, y);
                        if (motionPosition >= 0) {
                            v = this.getChildAt(motionPosition - this.mFirstPosition);
                            this.mMotionViewOriginalLeft = v.getLeft();
                            this.mMotionPosition = motionPosition;
                        }

                        this.mLastX = x;
                        break;
                    case 6:
                        this.onSecondaryPointerUp(ev);
                        index = this.mMotionX;
                        id = this.mMotionY;
                        x = this.pointToPosition(index, id);
                        if (x >= 0) {
                            v = this.getChildAt(x - this.mFirstPosition);
                            this.mMotionViewOriginalLeft = v.getLeft();
                            this.mMotionPosition = x;
                        }

                        this.mLastX = index;
                }

                return true;
            }
        }
    }

    protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
        if (this.getScrollX() != scrollX) {
            this.onScrollChanged(scrollX, this.getScrollY(), this.getScrollX(), this.getScrollY());
            this.mViewHelper.setScrollX(scrollX);
            this.invalidateParentIfNeeded();
            this.awakenScrollBars();
        }

    }

    @TargetApi(12)
    public boolean onGenericMotionEvent(MotionEvent event) {
        if ((event.getSource() & 2) != 0) {
            switch (event.getAction()) {
                case 8:
                    if (this.mTouchMode == -1) {
                        float hscroll = event.getAxisValue(10);
                        if (hscroll != 0.0F) {
                            int delta = (int) (hscroll * this.getHorizontalScrollFactor());
                            if (!this.trackMotionScroll(delta, delta)) {
                                return true;
                            }
                        }
                    }
            }
        }

        return super.onGenericMotionEvent(event);
    }

    protected float getHorizontalScrollFactor() {
        return this.mHorizontalScrollFactor;
    }

    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (this.mEdgeGlowLeft != null) {
            int scrollX = this.getScrollX();
            int restoreCount;
            int topPadding;
            int bottomPadding;
            int height;
            int width;
            if (!this.mEdgeGlowLeft.isFinished()) {
                restoreCount = canvas.save();
                topPadding = this.mListPadding.top + this.mGlowPaddingTop;
                bottomPadding = this.mListPadding.bottom + this.mGlowPaddingBottom;
                height = this.getHeight() - topPadding - bottomPadding;
                width = Math.min(0, scrollX + this.mFirstPositionDistanceGuess);
                canvas.rotate(-90.0F);
                canvas.translate((float) (-this.getHeight() + topPadding), (float) width);
                this.mEdgeGlowLeft.setSize(height, height);
                if (this.mEdgeGlowLeft.draw(canvas)) {
                    this.invalidate();
                }

                canvas.restoreToCount(restoreCount);
            }

            if (!this.mEdgeGlowRight.isFinished()) {
                restoreCount = canvas.save();
                topPadding = this.mListPadding.left + this.mGlowPaddingTop;
                bottomPadding = this.mListPadding.right + this.mGlowPaddingBottom;
                height = this.getHeight() - topPadding - bottomPadding;
                width = this.getWidth();
                int edgeX = Math.max(width, scrollX + this.mLastPositionDistanceGuess);
                canvas.rotate(90.0F);
                canvas.translate((float) (-topPadding), (float) (-edgeX));
                this.mEdgeGlowRight.setSize(height, height);
                if (this.mEdgeGlowRight.draw(canvas)) {
                    this.invalidate();
                }

                canvas.restoreToCount(restoreCount);
            }
        }

    }

    public void setOverScrollEffectPadding(int topPadding, int bottomPadding) {
        this.mGlowPaddingTop = topPadding;
        this.mGlowPaddingBottom = bottomPadding;
    }

    private void initOrResetVelocityTracker() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            this.mVelocityTracker.clear();
        }

    }

    private void initVelocityTrackerIfNotExists() {
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }

    }

    private void recycleVelocityTracker() {
        if (this.mVelocityTracker != null) {
            this.mVelocityTracker.recycle();
            this.mVelocityTracker = null;
        }

    }

    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            this.recycleVelocityTracker();
        }

        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int action = ev.getAction();
        if (this.mPositionScroller != null) {
            this.mPositionScroller.stop();
        }

        if (!this.mIsAttached) {
            return false;
        } else {
            int pointerIndex;
            int x;
            switch (action & 255) {
                case 0:
                    pointerIndex = this.mTouchMode;
                    if (pointerIndex != 6 && pointerIndex != 5) {
                        x = (int) ev.getX();
                        int y = (int) ev.getY();
                        this.mActivePointerId = ev.getPointerId(0);
                        int motionPosition = this.findMotionCol(x);
                        if (pointerIndex != 4 && motionPosition >= 0) {
                            View v = this.getChildAt(motionPosition - this.mFirstPosition);
                            this.mMotionViewOriginalLeft = v.getLeft();
                            this.mMotionX = x;
                            this.mMotionY = y;
                            this.mMotionPosition = motionPosition;
                            this.mTouchMode = 0;
                            this.clearScrollingCache();
                        }

                        this.mLastX = -2147483648;
                        this.initOrResetVelocityTracker();
                        this.mVelocityTracker.addMovement(ev);
                        if (pointerIndex == 4) {
                            return true;
                        }
                        break;
                    }

                    this.mMotionCorrection = 0;
                    return true;
                case 1:
                case 3:
                    this.mTouchMode = -1;
                    this.mActivePointerId = -1;
                    this.recycleVelocityTracker();
                    this.reportScrollStateChange(0);
                    break;
                case 2:
                    switch (this.mTouchMode) {
                        case 0:
                            pointerIndex = ev.findPointerIndex(this.mActivePointerId);
                            if (pointerIndex == -1) {
                                pointerIndex = 0;
                                this.mActivePointerId = ev.getPointerId(pointerIndex);
                            }

                            x = (int) ev.getX(pointerIndex);
                            this.initVelocityTrackerIfNotExists();
                            this.mVelocityTracker.addMovement(ev);
                            if (this.startScrollIfNeeded(x)) {
                                return true;
                            }
                    }
                case 4:
                case 5:
                default:
                    break;
                case 6:
                    this.onSecondaryPointerUp(ev);
            }

            return false;
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        int pointerIndex = (ev.getAction() & '\uff00') >> 8;
        int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == this.mActivePointerId) {
            int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            this.mMotionX = (int) ev.getX(newPointerIndex);
            this.mMotionY = (int) ev.getY(newPointerIndex);
            this.mMotionCorrection = 0;
            this.mActivePointerId = ev.getPointerId(newPointerIndex);
        }

    }

    public void addTouchables(ArrayList<View> views) {
        int count = this.getChildCount();
        int firstPosition = this.mFirstPosition;
        ListAdapter adapter = this.mAdapter;
        if (adapter != null) {
            for (int i = 0; i < count; ++i) {
                View child = this.getChildAt(i);
                if (adapter.isEnabled(firstPosition + i)) {
                    views.add(child);
                }

                child.addTouchables(views);
            }

        }
    }

    void reportScrollStateChange(int newState) {
        if (newState != this.mLastScrollState && this.mOnScrollListener != null) {
            this.mLastScrollState = newState;
            this.mOnScrollListener.onScrollStateChanged(this, newState);
        }

    }

    public void setFriction(float friction) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new AbsHorizontalListView.FlingRunnable();
        }

        this.mFlingRunnable.mScroller.setFriction(friction);
    }

    public void setVelocityScale(float scale) {
        this.mVelocityScale = scale;
    }

    public void smoothScrollToPosition(int position) {
        if (this.mPositionScroller == null) {
            this.mPositionScroller = new AbsHorizontalListView.PositionScroller();
        }

        this.mPositionScroller.start(position);
    }

    public void smoothScrollToPositionFromLeft(int position, int offset, int duration) {
        if (this.mPositionScroller == null) {
            this.mPositionScroller = new AbsHorizontalListView.PositionScroller();
        }

        this.mPositionScroller.startWithOffset(position, offset, duration);
    }

    public void smoothScrollToPositionFromLeft(int position, int offset) {
        if (this.mPositionScroller == null) {
            this.mPositionScroller = new AbsHorizontalListView.PositionScroller();
        }

        this.mPositionScroller.startWithOffset(position, offset);
    }

    public void smoothScrollToPosition(int position, int boundPosition) {
        if (this.mPositionScroller == null) {
            this.mPositionScroller = new AbsHorizontalListView.PositionScroller();
        }

        this.mPositionScroller.start(position, boundPosition);
    }

    public void smoothScrollBy(int distance, int duration) {
        this.smoothScrollBy(distance, duration, false);
    }

    public void smoothScrollBy(int distance, int duration, boolean linear) {
        if (this.mFlingRunnable == null) {
            this.mFlingRunnable = new AbsHorizontalListView.FlingRunnable();
        }

        int firstPos = this.mFirstPosition;
        int childCount = this.getChildCount();
        int lastPos = firstPos + childCount;
        int leftLimit = this.getPaddingLeft();
        int rightLimit = this.getWidth() - this.getPaddingRight();
        if (distance != 0 && this.mItemCount != 0 && childCount != 0 && (firstPos != 0 || this.getChildAt(0).getLeft() != leftLimit || distance >= 0) && (lastPos != this.mItemCount || this.getChildAt(childCount - 1).getRight() != rightLimit || distance <= 0)) {
            this.reportScrollStateChange(2);
            this.mFlingRunnable.startScroll(distance, duration, linear);
        } else {
            this.mFlingRunnable.endFling();
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }
        }

    }

    protected void smoothScrollByOffset(int position) {
        int index = -1;
        if (position < 0) {
            index = this.getFirstVisiblePosition();
        } else if (position > 0) {
            index = this.getLastVisiblePosition();
        }

        if (index > -1) {
            View child = this.getChildAt(index - this.getFirstVisiblePosition());
            if (child != null) {
                Rect visibleRect = new Rect();
                if (child.getGlobalVisibleRect(visibleRect)) {
                    int childRectArea = child.getWidth() * child.getHeight();
                    int visibleRectArea = visibleRect.width() * visibleRect.height();
                    float visibleArea = (float) visibleRectArea / (float) childRectArea;
                    float visibleThreshold = 0.75F;
                    if (position < 0 && visibleArea < 0.75F) {
                        ++index;
                    } else if (position > 0 && visibleArea < 0.75F) {
                        --index;
                    }
                }

                this.smoothScrollToPosition(Math.max(0, Math.min(this.getCount(), index + position)));
            }
        }

    }

    private void createScrollingCache() {
        if (this.mScrollingCacheEnabled && !this.mCachingStarted && !this.mViewHelper.isHardwareAccelerated()) {
            this.setChildrenDrawnWithCacheEnabled(true);
            this.setChildrenDrawingCacheEnabled(true);
            this.mCachingStarted = this.mCachingActive = true;
        }

    }

    private void clearScrollingCache() {
        if (!this.mViewHelper.isHardwareAccelerated()) {
            if (this.mClearScrollingCache == null) {
                this.mClearScrollingCache = new Runnable() {
                    public void run() {
                        if (AbsHorizontalListView.this.mCachingStarted) {
                            AbsHorizontalListView.this.mCachingStarted = AbsHorizontalListView.this.mCachingActive = false;
                            AbsHorizontalListView.this.setChildrenDrawnWithCacheEnabled(false);
                            if ((AbsHorizontalListView.this.getPersistentDrawingCache() & 2) == 0) {
                                AbsHorizontalListView.this.setChildrenDrawingCacheEnabled(false);
                            }

                            if (!AbsHorizontalListView.this.isAlwaysDrawnWithCacheEnabled()) {
                                AbsHorizontalListView.this.invalidate();
                            }
                        }

                    }
                };
            }

            this.post(this.mClearScrollingCache);
        }

    }

    boolean trackMotionScroll(int deltaX, int incrementalDeltaX) {
        int childCount = this.getChildCount();
        if (childCount == 0) {
            return true;
        } else {
            int firstLeft = this.getChildAt(0).getLeft();
            int lastRight = this.getChildAt(childCount - 1).getRight();
            Rect listPadding = this.mListPadding;
            byte effectivePaddingLeft = 0;
            byte effectivePaddingRight = 0;
            int spaceBefore = effectivePaddingLeft - firstLeft;
            int end = this.getWidth() - effectivePaddingRight;
            int spaceAfter = lastRight - end;
            int width = this.getWidth() - this.getPaddingRight() - this.getPaddingLeft();
            if (deltaX < 0) {
                deltaX = Math.max(-(width - 1), deltaX);
            } else {
                deltaX = Math.min(width - 1, deltaX);
            }

            if (incrementalDeltaX < 0) {
                incrementalDeltaX = Math.max(-(width - 1), incrementalDeltaX);
            } else {
                incrementalDeltaX = Math.min(width - 1, incrementalDeltaX);
            }

            int firstPosition = this.mFirstPosition;
            if (firstPosition == 0) {
                this.mFirstPositionDistanceGuess = firstLeft - listPadding.left;
            } else {
                this.mFirstPositionDistanceGuess += incrementalDeltaX;
            }

            if (firstPosition + childCount == this.mItemCount) {
                this.mLastPositionDistanceGuess = lastRight + listPadding.right;
            } else {
                this.mLastPositionDistanceGuess += incrementalDeltaX;
            }

            boolean cannotScrollRight = firstPosition == 0 && firstLeft >= listPadding.left && incrementalDeltaX >= 0;
            boolean cannotScrollLeft = firstPosition + childCount == this.mItemCount && lastRight <= this.getWidth() - listPadding.right && incrementalDeltaX <= 0;
            if (!cannotScrollRight && !cannotScrollLeft) {
                boolean down = incrementalDeltaX < 0;
                boolean inTouchMode = this.isInTouchMode();
                if (inTouchMode) {
                    this.hideSelector();
                }

                int headerViewsCount = this.getHeaderViewsCount();
                int footerViewsStart = this.mItemCount - this.getFooterViewsCount();
                int start = 0;
                int count = 0;
                int childIndex;
                int absIncrementalDeltaX;
                int position;
                View child;
                if (down) {
                    absIncrementalDeltaX = -incrementalDeltaX;

                    for (childIndex = 0; childIndex < childCount; ++childIndex) {
                        child = this.getChildAt(childIndex);
                        if (child.getRight() >= absIncrementalDeltaX) {
                            break;
                        }

                        ++count;
                        position = firstPosition + childIndex;
                        if (position >= headerViewsCount && position < footerViewsStart) {
                            this.mRecycler.addScrapView(child, position);
                        }
                    }
                } else {
                    absIncrementalDeltaX = this.getWidth() - incrementalDeltaX;

                    for (childIndex = childCount - 1; childIndex >= 0; --childIndex) {
                        child = this.getChildAt(childIndex);
                        if (child.getLeft() <= absIncrementalDeltaX) {
                            break;
                        }

                        start = childIndex;
                        ++count;
                        position = firstPosition + childIndex;
                        if (position >= headerViewsCount && position < footerViewsStart) {
                            this.mRecycler.addScrapView(child, position);
                        }
                    }
                }

                this.mMotionViewNewLeft = this.mMotionViewOriginalLeft + deltaX;
                this.mBlockLayoutRequests = true;
                if (count > 0) {
                    this.detachViewsFromParent(start, count);
                    this.mRecycler.removeSkippedScrap();
                }

                if (!this.awakenScrollBars()) {
                    this.invalidate();
                }

                this.offsetChildrenLeftAndRight(incrementalDeltaX);
                if (down) {
                    this.mFirstPosition += count;
                }

                absIncrementalDeltaX = Math.abs(incrementalDeltaX);
                if (spaceBefore < absIncrementalDeltaX || spaceAfter < absIncrementalDeltaX) {
                    this.fillGap(down);
                }

                if (!inTouchMode && this.mSelectedPosition != -1) {
                    childIndex = this.mSelectedPosition - this.mFirstPosition;
                    if (childIndex >= 0 && childIndex < this.getChildCount()) {
                        this.positionSelector(this.mSelectedPosition, this.getChildAt(childIndex));
                    }
                } else if (this.mSelectorPosition != -1) {
                    childIndex = this.mSelectorPosition - this.mFirstPosition;
                    if (childIndex >= 0 && childIndex < this.getChildCount()) {
                        this.positionSelector(-1, this.getChildAt(childIndex));
                    }
                } else {
                    this.mSelectorRect.setEmpty();
                }

                this.mBlockLayoutRequests = false;
                this.invokeOnItemScrollListener();
                return false;
            } else {
                return incrementalDeltaX != 0;
            }
        }
    }

    public void offsetChildrenLeftAndRight(int offset) {
        int count = this.getChildCount();

        for (int i = 0; i < count; ++i) {
            View v = this.getChildAt(i);
            v.offsetLeftAndRight(offset);
        }

    }

    protected int getHeaderViewsCount() {
        return 0;
    }

    protected int getFooterViewsCount() {
        return 0;
    }

    protected abstract void fillGap(boolean var1);

    protected void hideSelector() {
        if (this.mSelectedPosition != -1) {
            if (this.mLayoutMode != 4) {
                this.mResurrectToPosition = this.mSelectedPosition;
            }

            if (this.mNextSelectedPosition >= 0 && this.mNextSelectedPosition != this.mSelectedPosition) {
                this.mResurrectToPosition = this.mNextSelectedPosition;
            }

            this.setSelectedPositionInt(-1);
            this.setNextSelectedPositionInt(-1);
            this.mSelectedLeft = 0;
        }

    }

    protected int reconcileSelectedPosition() {
        int position = this.mSelectedPosition;
        if (position < 0) {
            position = this.mResurrectToPosition;
        }

        position = Math.max(0, position);
        position = Math.min(position, this.mItemCount - 1);
        return position;
    }

    protected abstract int findMotionCol(int var1);

    protected int findClosestMotionCol(int x) {
        int childCount = this.getChildCount();
        if (childCount == 0) {
            return -1;
        } else {
            int motionCol = this.findMotionCol(x);
            return motionCol != -1 ? motionCol : this.mFirstPosition + childCount - 1;
        }
    }

    public void invalidateViews() {
        this.mDataChanged = true;
        this.rememberSyncState();
        this.requestLayout();
        this.invalidate();
    }

    protected boolean resurrectSelectionIfNeeded() {
        if (this.mSelectedPosition < 0 && this.resurrectSelection()) {
            this.updateSelectorState();
            return true;
        } else {
            return false;
        }
    }

    public abstract void setSelectionInt(int var1);

    boolean resurrectSelection() {
        int childCount = this.getChildCount();
        if (childCount <= 0) {
            return false;
        } else {
            int selectedLeft = 0;
            int childrenLeft = this.mListPadding.left;
            int childrenRight = this.getRight() - this.getLeft() - this.mListPadding.right;
            int firstPosition = this.mFirstPosition;
            int toPosition = this.mResurrectToPosition;
            boolean down = true;
            int var16;
            int selectedPos;
            if (toPosition >= firstPosition && toPosition < firstPosition + childCount) {
                selectedPos = toPosition;
                View var14 = this.getChildAt(toPosition - this.mFirstPosition);
                selectedLeft = var14.getLeft();
                var16 = var14.getRight();
                if (selectedLeft < childrenLeft) {
                    selectedLeft = childrenLeft + this.getHorizontalFadingEdgeLength();
                } else if (var16 > childrenRight) {
                    selectedLeft = childrenRight - var14.getMeasuredWidth() - this.getHorizontalFadingEdgeLength();
                }
            } else {
                int itemCount;
                if (toPosition < firstPosition) {
                    selectedPos = firstPosition;

                    for (itemCount = 0; itemCount < childCount; ++itemCount) {
                        View i = this.getChildAt(itemCount);
                        int v = i.getLeft();
                        if (itemCount == 0) {
                            selectedLeft = v;
                            if (firstPosition > 0 || v < childrenLeft) {
                                childrenLeft += this.getHorizontalFadingEdgeLength();
                            }
                        }

                        if (v >= childrenLeft) {
                            selectedPos = firstPosition + itemCount;
                            selectedLeft = v;
                            break;
                        }
                    }
                } else {
                    itemCount = this.mItemCount;
                    down = false;
                    selectedPos = firstPosition + childCount - 1;

                    for (var16 = childCount - 1; var16 >= 0; --var16) {
                        View var15 = this.getChildAt(var16);
                        int left = var15.getLeft();
                        int right = var15.getRight();
                        if (var16 == childCount - 1) {
                            selectedLeft = left;
                            if (firstPosition + childCount < itemCount || right > childrenRight) {
                                childrenRight -= this.getHorizontalFadingEdgeLength();
                            }
                        }

                        if (right <= childrenRight) {
                            selectedPos = firstPosition + var16;
                            selectedLeft = left;
                            break;
                        }
                    }
                }
            }

            this.mResurrectToPosition = -1;
            this.removeCallbacks(this.mFlingRunnable);
            if (this.mPositionScroller != null) {
                this.mPositionScroller.stop();
            }

            this.mTouchMode = -1;
            this.clearScrollingCache();
            this.mSpecificLeft = selectedLeft;
            selectedPos = this.lookForSelectablePosition(selectedPos, down);
            if (selectedPos >= firstPosition && selectedPos <= this.getLastVisiblePosition()) {
                this.mLayoutMode = 4;
                this.updateSelectorState();
                this.setSelectionInt(selectedPos);
                this.invokeOnItemScrollListener();
            } else {
                selectedPos = -1;
            }

            this.reportScrollStateChange(0);
            return selectedPos >= 0;
        }
    }

    void confirmCheckedPositionsById() {
        this.mCheckStates.clear();
        boolean checkedCountChanged = false;

        for (int checkedIndex = 0; checkedIndex < this.mCheckedIdStates.size(); ++checkedIndex) {
            long id = this.mCheckedIdStates.keyAt(checkedIndex);
            int lastPos = ((Integer) this.mCheckedIdStates.valueAt(checkedIndex)).intValue();
            long lastPosId = this.mAdapter.getItemId(lastPos);
            if (id == lastPosId) {
                this.mCheckStates.put(lastPos, Boolean.valueOf(true));
            } else {
                int start = Math.max(0, lastPos - 20);
                int end = Math.min(lastPos + 20, this.mItemCount);
                boolean found = false;

                for (int searchPos = start; searchPos < end; ++searchPos) {
                    long searchId = this.mAdapter.getItemId(searchPos);
                    if (id == searchId) {
                        found = true;
                        this.mCheckStates.put(searchPos, Boolean.valueOf(true));
                        this.mCheckedIdStates.setValueAt(checkedIndex, Integer.valueOf(searchPos));
                        break;
                    }
                }

                if (!found) {
                    this.mCheckedIdStates.delete(id);
                    --checkedIndex;
                    --this.mCheckedItemCount;
                    checkedCountChanged = true;
                    if (VERSION.SDK_INT > 11 && this.mChoiceActionMode != null && this.mMultiChoiceModeCallback != null) {
                        ((MultiChoiceModeWrapper) this.mMultiChoiceModeCallback).onItemCheckedStateChanged((ActionMode) this.mChoiceActionMode, lastPos, id, false);
                    }
                }
            }
        }

        if (checkedCountChanged && this.mChoiceActionMode != null && VERSION.SDK_INT > 11) {
            ((ActionMode) this.mChoiceActionMode).invalidate();
        }

    }

    protected void handleDataChanged() {
        int count = this.mItemCount;
        int lastHandledItemCount = this.mLastHandledItemCount;
        this.mLastHandledItemCount = this.mItemCount;
        if (this.mChoiceMode != 0 && this.mAdapter != null && this.mAdapter.hasStableIds()) {
            this.confirmCheckedPositionsById();
        }

        this.mRecycler.clearTransientStateViews();
        if (count > 0) {
            int newPos;
            int selectablePos;
            if (this.mNeedSync) {
                this.mNeedSync = false;
                this.mPendingSync = null;
                if (this.mTranscriptMode == 2) {
                    this.mLayoutMode = 3;
                    return;
                }

                if (this.mTranscriptMode == 1) {
                    if (this.mForceTranscriptScroll) {
                        this.mForceTranscriptScroll = false;
                        this.mLayoutMode = 3;
                        return;
                    }

                    int childCount = this.getChildCount();
                    int listRight = this.getWidth() - this.getPaddingRight();
                    View lastChild = this.getChildAt(childCount - 1);
                    int lastRight = lastChild != null ? lastChild.getBottom() : listRight;
                    if (this.mFirstPosition + childCount >= lastHandledItemCount && lastRight <= listRight) {
                        this.mLayoutMode = 3;
                        return;
                    }

                    this.awakenScrollBars();
                }

                switch (this.mSyncMode) {
                    case 0:
                        if (this.isInTouchMode()) {
                            this.mLayoutMode = 5;
                            this.mSyncPosition = Math.min(Math.max(0, this.mSyncPosition), count - 1);
                            return;
                        }

                        newPos = this.findSyncPosition();
                        if (newPos >= 0) {
                            selectablePos = this.lookForSelectablePosition(newPos, true);
                            if (selectablePos == newPos) {
                                this.mSyncPosition = newPos;
                                if (this.mSyncWidth == (long) this.getWidth()) {
                                    this.mLayoutMode = 5;
                                } else {
                                    this.mLayoutMode = 2;
                                }

                                this.setNextSelectedPositionInt(newPos);
                                return;
                            }
                        }
                        break;
                    case 1:
                        this.mLayoutMode = 5;
                        this.mSyncPosition = Math.min(Math.max(0, this.mSyncPosition), count - 1);
                        return;
                }
            }

            if (!this.isInTouchMode()) {
                newPos = this.getSelectedItemPosition();
                if (newPos >= count) {
                    newPos = count - 1;
                }

                if (newPos < 0) {
                    newPos = 0;
                }

                selectablePos = this.lookForSelectablePosition(newPos, true);
                if (selectablePos >= 0) {
                    this.setNextSelectedPositionInt(selectablePos);
                    return;
                }

                selectablePos = this.lookForSelectablePosition(newPos, false);
                if (selectablePos >= 0) {
                    this.setNextSelectedPositionInt(selectablePos);
                    return;
                }
            } else if (this.mResurrectToPosition >= 0) {
                return;
            }
        }

        this.mLayoutMode = this.mStackFromRight ? 3 : 1;
        this.mSelectedPosition = -1;
        this.mSelectedColId = -9223372036854775808L;
        this.mNextSelectedPosition = -1;
        this.mNextSelectedColId = -9223372036854775808L;
        this.mNeedSync = false;
        this.mPendingSync = null;
        this.mSelectorPosition = -1;
        this.checkSelectionChanged();
    }

    public static int getDistance(Rect source, Rect dest, int direction) {
        int sX;
        int sY;
        int dX;
        int dY;
        switch (direction) {
            case 1:
            case 2:
                sX = source.right + source.width() / 2;
                sY = source.top + source.height() / 2;
                dX = dest.left + dest.width() / 2;
                dY = dest.top + dest.height() / 2;
                break;
            case 17:
                sX = source.left;
                sY = source.top + source.height() / 2;
                dX = dest.right;
                dY = dest.top + dest.height() / 2;
                break;
            case 33:
                sX = source.left + source.width() / 2;
                sY = source.top;
                dX = dest.left + dest.width() / 2;
                dY = dest.bottom;
                break;
            case 66:
                sX = source.right;
                sY = source.top + source.height() / 2;
                dX = dest.left;
                dY = dest.top + dest.height() / 2;
                break;
            case 130:
                sX = source.left + source.width() / 2;
                sY = source.bottom;
                dX = dest.left + dest.width() / 2;
                dY = dest.top;
                break;
            default:
                throw new IllegalArgumentException("direction must be one of {FOCUS_UP, FOCUS_DOWN, FOCUS_LEFT, FOCUS_RIGHT, FOCUS_FORWARD, FOCUS_BACKWARD}.");
        }

        int deltaX = dX - sX;
        int deltaY = dY - sY;
        return deltaY * deltaY + deltaX * deltaX;
    }

    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return null;
    }

    public boolean checkInputConnectionProxy(View view) {
        return false;
    }

    protected android.view.ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new AbsHorizontalListView.LayoutParams(-2, -1, 0);
    }

    protected android.view.ViewGroup.LayoutParams generateLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return new AbsHorizontalListView.LayoutParams(p);
    }

    public AbsHorizontalListView.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new AbsHorizontalListView.LayoutParams(this.getContext(), attrs);
    }

    protected boolean checkLayoutParams(android.view.ViewGroup.LayoutParams p) {
        return p instanceof AbsHorizontalListView.LayoutParams;
    }

    public void setTranscriptMode(int mode) {
        this.mTranscriptMode = mode;
    }

    public int getTranscriptMode() {
        return this.mTranscriptMode;
    }

    public int getSolidColor() {
        return this.mCacheColorHint;
    }

    public void setCacheColorHint(int color) {
        if (color != this.mCacheColorHint) {
            this.mCacheColorHint = color;
            int count = this.getChildCount();

            for (int i = 0; i < count; ++i) {
                this.getChildAt(i).setDrawingCacheBackgroundColor(color);
            }

            this.mRecycler.setCacheColorHint(color);
        }

    }

    @ExportedProperty(
            category = "drawing"
    )
    public int getCacheColorHint() {
        return this.mCacheColorHint;
    }

    @SuppressLint({"NewApi"})
    public void reclaimViews(List<View> views) {
        int childCount = this.getChildCount();
        AbsHorizontalListView.RecyclerListener listener = this.mRecycler.mRecyclerListener;

        for (int i = 0; i < childCount; ++i) {
            View child = this.getChildAt(i);
            AbsHorizontalListView.LayoutParams lp = (AbsHorizontalListView.LayoutParams) child.getLayoutParams();
            if (lp != null && this.mRecycler.shouldRecycleViewType(lp.viewType)) {
                views.add(child);
                if (VERSION.SDK_INT >= 14) {
                    child.setAccessibilityDelegate((AccessibilityDelegate) null);
                }

                if (listener != null) {
                    listener.onMovedToScrapHeap(child);
                }
            }
        }

        this.mRecycler.reclaimScrapViews(views);
        this.removeAllViewsInLayout();
    }

    private void finishGlows() {
        if (this.mEdgeGlowLeft != null) {
            this.mEdgeGlowLeft.finish();
            this.mEdgeGlowRight.finish();
        }

    }

    protected void setVisibleRangeHint(int start, int end) {
    }

    public void setRecyclerListener(AbsHorizontalListView.RecyclerListener listener) {
        this.mRecycler.mRecyclerListener = listener;
    }

    static View retrieveFromScrap(ArrayList<View> scrapViews, int position) {
        int size = scrapViews.size();
        if (size > 0) {
            for (int i = 0; i < size; ++i) {
                View view = (View) scrapViews.get(i);
                if (((AbsHorizontalListView.LayoutParams) view.getLayoutParams()).scrappedFromPosition == position) {
                    scrapViews.remove(i);
                    return view;
                }
            }

            return (View) scrapViews.remove(size - 1);
        } else {
            return null;
        }
    }

    public class RecycleBin {
        private AbsHorizontalListView.RecyclerListener mRecyclerListener;
        private int mFirstActivePosition;
        private View[] mActiveViews = new View[0];
        private ArrayList<View>[] mScrapViews;
        private int mViewTypeCount;
        private ArrayList<View> mCurrentScrap;
        private ArrayList<View> mSkippedScrap;
        private SparseArrayCompat<View> mTransientStateViews;

        public RecycleBin() {
        }

        public void setViewTypeCount(int viewTypeCount) {
            if (viewTypeCount < 1) {
                throw new IllegalArgumentException("Can\'t have a viewTypeCount < 1");
            } else {
                ArrayList[] scrapViews = new ArrayList[viewTypeCount];

                for (int i = 0; i < viewTypeCount; ++i) {
                    scrapViews[i] = new ArrayList();
                }

                this.mViewTypeCount = viewTypeCount;
                this.mCurrentScrap = scrapViews[0];
                this.mScrapViews = scrapViews;
            }
        }

        public void markChildrenDirty() {
            int i;
            int var6;
            if (this.mViewTypeCount == 1) {
                ArrayList count = this.mCurrentScrap;
                i = count.size();

                for (int scrap = 0; scrap < i; ++scrap) {
                    ((View) count.get(scrap)).forceLayout();
                }
            } else {
                var6 = this.mViewTypeCount;

                for (i = 0; i < var6; ++i) {
                    ArrayList var7 = this.mScrapViews[i];
                    int scrapCount = var7.size();

                    for (int j = 0; j < scrapCount; ++j) {
                        ((View) var7.get(j)).forceLayout();
                    }
                }
            }

            if (this.mTransientStateViews != null) {
                var6 = this.mTransientStateViews.size();

                for (i = 0; i < var6; ++i) {
                    ((View) this.mTransientStateViews.valueAt(i)).forceLayout();
                }
            }

        }

        public boolean shouldRecycleViewType(int viewType) {
            return viewType >= 0;
        }

        public void clear() {
            int i;
            if (this.mViewTypeCount == 1) {
                ArrayList typeCount = this.mCurrentScrap;
                i = typeCount.size();

                for (int scrap = 0; scrap < i; ++scrap) {
                    AbsHorizontalListView.this.removeDetachedView((View) typeCount.remove(i - 1 - scrap), false);
                }
            } else {
                int var6 = this.mViewTypeCount;

                for (i = 0; i < var6; ++i) {
                    ArrayList var7 = this.mScrapViews[i];
                    int scrapCount = var7.size();

                    for (int j = 0; j < scrapCount; ++j) {
                        AbsHorizontalListView.this.removeDetachedView((View) var7.remove(scrapCount - 1 - j), false);
                    }
                }
            }

            if (this.mTransientStateViews != null) {
                this.mTransientStateViews.clear();
            }

        }

        public void fillActiveViews(int childCount, int firstActivePosition) {
            if (this.mActiveViews.length < childCount) {
                this.mActiveViews = new View[childCount];
            }

            this.mFirstActivePosition = firstActivePosition;
            View[] activeViews = this.mActiveViews;

            for (int i = 0; i < childCount; ++i) {
                View child = AbsHorizontalListView.this.getChildAt(i);
                AbsHorizontalListView.LayoutParams lp = (AbsHorizontalListView.LayoutParams) child.getLayoutParams();
                if (lp != null && lp.viewType != -2) {
                    activeViews[i] = child;
                }
            }

        }

        public View getActiveView(int position) {
            int index = position - this.mFirstActivePosition;
            View[] activeViews = this.mActiveViews;
            if (index >= 0 && index < activeViews.length) {
                View match = activeViews[index];
                activeViews[index] = null;
                return match;
            } else {
                return null;
            }
        }

        View getTransientStateView(int position) {
            if (this.mTransientStateViews == null) {
                return null;
            } else {
                int index = this.mTransientStateViews.indexOfKey(position);
                if (index < 0) {
                    return null;
                } else {
                    View result = (View) this.mTransientStateViews.valueAt(index);
                    this.mTransientStateViews.removeAt(index);
                    return result;
                }
            }
        }

        void clearTransientStateViews() {
            if (this.mTransientStateViews != null) {
                this.mTransientStateViews.clear();
            }

        }

        View getScrapView(int position) {
            if (this.mViewTypeCount == 1) {
                return AbsHorizontalListView.retrieveFromScrap(this.mCurrentScrap, position);
            } else {
                int whichScrap = AbsHorizontalListView.this.mAdapter.getItemViewType(position);
                return whichScrap >= 0 && whichScrap < this.mScrapViews.length ? AbsHorizontalListView.retrieveFromScrap(this.mScrapViews[whichScrap], position) : null;
            }
        }

        @SuppressLint({"NewApi"})
        public void addScrapView(View scrap, int position) {
            AbsHorizontalListView.LayoutParams lp = (AbsHorizontalListView.LayoutParams) scrap.getLayoutParams();
            if (lp != null) {
                lp.scrappedFromPosition = position;
                int viewType = lp.viewType;
                boolean scrapHasTransientState = VERSION.SDK_INT >= 16 ? scrap.hasTransientState() : false;
                if (this.shouldRecycleViewType(viewType) && !scrapHasTransientState) {
                    scrap.onStartTemporaryDetach();
                    if (this.mViewTypeCount == 1) {
                        this.mCurrentScrap.add(scrap);
                    } else {
                        this.mScrapViews[viewType].add(scrap);
                    }

                    if (VERSION.SDK_INT >= 14) {
                        scrap.setAccessibilityDelegate((AccessibilityDelegate) null);
                    }

                    if (this.mRecyclerListener != null) {
                        this.mRecyclerListener.onMovedToScrapHeap(scrap);
                    }

                } else {
                    if (viewType != -2 || scrapHasTransientState) {
                        if (this.mSkippedScrap == null) {
                            this.mSkippedScrap = new ArrayList();
                        }

                        this.mSkippedScrap.add(scrap);
                    }

                    if (scrapHasTransientState) {
                        if (this.mTransientStateViews == null) {
                            this.mTransientStateViews = new SparseArrayCompat();
                        }

                        scrap.onStartTemporaryDetach();
                        this.mTransientStateViews.put(position, scrap);
                    }

                }
            }
        }

        public void removeSkippedScrap() {
            if (this.mSkippedScrap != null) {
                int count = this.mSkippedScrap.size();

                for (int i = 0; i < count; ++i) {
                    AbsHorizontalListView.this.removeDetachedView((View) this.mSkippedScrap.get(i), false);
                }

                this.mSkippedScrap.clear();
            }
        }

        @SuppressLint({"NewApi"})
        public void scrapActiveViews() {
            View[] activeViews = this.mActiveViews;
            boolean hasListener = this.mRecyclerListener != null;
            boolean multipleScraps = this.mViewTypeCount > 1;
            ArrayList scrapViews = this.mCurrentScrap;
            int count = activeViews.length;

            for (int i = count - 1; i >= 0; --i) {
                View victim = activeViews[i];
                if (victim != null) {
                    AbsHorizontalListView.LayoutParams lp = (AbsHorizontalListView.LayoutParams) victim.getLayoutParams();
                    int whichScrap = lp.viewType;
                    activeViews[i] = null;
                    boolean scrapHasTransientState = VERSION.SDK_INT >= 16 ? victim.hasTransientState() : false;
                    if (this.shouldRecycleViewType(whichScrap) && !scrapHasTransientState) {
                        if (multipleScraps) {
                            scrapViews = this.mScrapViews[whichScrap];
                        }

                        victim.onStartTemporaryDetach();
                        lp.scrappedFromPosition = this.mFirstActivePosition + i;
                        scrapViews.add(victim);
                        if (VERSION.SDK_INT >= 14) {
                            victim.setAccessibilityDelegate((AccessibilityDelegate) null);
                        }

                        if (hasListener) {
                            this.mRecyclerListener.onMovedToScrapHeap(victim);
                        }
                    } else {
                        if (whichScrap != -2 || scrapHasTransientState) {
                            AbsHorizontalListView.this.removeDetachedView(victim, false);
                        }

                        if (scrapHasTransientState) {
                            if (this.mTransientStateViews == null) {
                                this.mTransientStateViews = new SparseArrayCompat();
                            }

                            this.mTransientStateViews.put(this.mFirstActivePosition + i, victim);
                        }
                    }
                }
            }

            this.pruneScrapViews();
        }

        @SuppressLint({"NewApi"})
        private void pruneScrapViews() {
            int maxViews = this.mActiveViews.length;
            int viewTypeCount = this.mViewTypeCount;
            ArrayList[] scrapViews = this.mScrapViews;

            int i;
            for (i = 0; i < viewTypeCount; ++i) {
                ArrayList v = scrapViews[i];
                int size = v.size();
                int extras = size - maxViews;
                --size;

                for (int j = 0; j < extras; ++j) {
                    AbsHorizontalListView.this.removeDetachedView((View) v.remove(size--), false);
                }
            }

            if (this.mTransientStateViews != null) {
                for (i = 0; i < this.mTransientStateViews.size(); ++i) {
                    View var9 = (View) this.mTransientStateViews.valueAt(i);
                    if (!var9.hasTransientState()) {
                        this.mTransientStateViews.removeAt(i);
                        --i;
                    }
                }
            }

        }

        void reclaimScrapViews(List<View> views) {
            if (this.mViewTypeCount == 1) {
                views.addAll(this.mCurrentScrap);
            } else {
                int viewTypeCount = this.mViewTypeCount;
                ArrayList[] scrapViews = this.mScrapViews;

                for (int i = 0; i < viewTypeCount; ++i) {
                    ArrayList scrapPile = scrapViews[i];
                    views.addAll(scrapPile);
                }
            }

        }

        void setCacheColorHint(int color) {
            int count;
            int i;
            if (this.mViewTypeCount == 1) {
                ArrayList activeViews = this.mCurrentScrap;
                count = activeViews.size();

                for (i = 0; i < count; ++i) {
                    ((View) activeViews.get(i)).setDrawingCacheBackgroundColor(color);
                }
            } else {
                int var7 = this.mViewTypeCount;

                for (count = 0; count < var7; ++count) {
                    ArrayList var9 = this.mScrapViews[count];
                    int victim = var9.size();

                    for (int j = 0; j < victim; ++j) {
                        ((View) var9.get(j)).setDrawingCacheBackgroundColor(color);
                    }
                }
            }

            View[] var8 = this.mActiveViews;
            count = var8.length;

            for (i = 0; i < count; ++i) {
                View var10 = var8[i];
                if (var10 != null) {
                    var10.setDrawingCacheBackgroundColor(color);
                }
            }

        }
    }

    public interface RecyclerListener {
        void onMovedToScrapHeap(View var1);
    }

    public static class LayoutParams extends android.view.ViewGroup.LayoutParams {
        public int viewType;
        public boolean recycledHeaderFooter;
        public boolean forceAdd;
        public int scrappedFromPosition;
        public long itemId = -1L;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int w, int h) {
            super(w, h);
        }

        public LayoutParams(int w, int h, int viewType) {
            super(w, h);
            this.viewType = viewType;
        }

        public LayoutParams(android.view.ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    public class AdapterDataSetObserver extends com.qing.widget.horizontallistview.AdapterView<ListAdapter>.AdapterDataSetObserver {
        public AdapterDataSetObserver(AbsHorizontalListView view) {
            super();//AbsHorizontalListView.this
        }

        public void onChanged() {
            super.onChanged();
        }

        public void onInvalidated() {
            super.onInvalidated();
        }
    }

    public class PositionScroller implements Runnable {
        private static final int SCROLL_DURATION = 200;
        private static final int MOVE_DOWN_POS = 1;
        private static final int MOVE_UP_POS = 2;
        private static final int MOVE_DOWN_BOUND = 3;
        private static final int MOVE_UP_BOUND = 4;
        private static final int MOVE_OFFSET = 5;
        private int mMode;
        private int mTargetPos;
        private int mBoundPos;
        private int mLastSeenPos;
        private int mScrollDuration;
        private final int mExtraScroll = ViewConfiguration.get(AbsHorizontalListView.this.getContext()).getScaledFadingEdgeLength();
        private int mOffsetFromLeft;

        PositionScroller() {
        }

        public void start(final int position) {
            this.stop();
            if (AbsHorizontalListView.this.mDataChanged) {
                AbsHorizontalListView.this.mPositionScrollAfterLayout = new Runnable() {
                    public void run() {
                        PositionScroller.this.start(position);
                    }
                };
            } else {
                int childCount = AbsHorizontalListView.this.getChildCount();
                if (childCount != 0) {
                    int firstPos = AbsHorizontalListView.this.mFirstPosition;
                    int lastPos = firstPos + childCount - 1;
                    int clampedPosition = Math.max(0, Math.min(AbsHorizontalListView.this.getCount() - 1, position));
                    int viewTravelCount;
                    if (clampedPosition < firstPos) {
                        viewTravelCount = firstPos - clampedPosition + 1;
                        this.mMode = 2;
                    } else {
                        if (clampedPosition <= lastPos) {
                            this.scrollToVisible(clampedPosition, -1, 200);
                            return;
                        }

                        viewTravelCount = clampedPosition - lastPos + 1;
                        this.mMode = 1;
                    }

                    if (viewTravelCount > 0) {
                        this.mScrollDuration = 200 / viewTravelCount;
                    } else {
                        this.mScrollDuration = 200;
                    }

                    this.mTargetPos = clampedPosition;
                    this.mBoundPos = -1;
                    this.mLastSeenPos = -1;
                    AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                }
            }
        }

        public void start(final int position, final int boundPosition) {
            this.stop();
            if (boundPosition == -1) {
                this.start(position);
            } else if (AbsHorizontalListView.this.mDataChanged) {
                AbsHorizontalListView.this.mPositionScrollAfterLayout = new Runnable() {
                    public void run() {
                        PositionScroller.this.start(position, boundPosition);
                    }
                };
            } else {
                int childCount = AbsHorizontalListView.this.getChildCount();
                if (childCount != 0) {
                    int firstPos = AbsHorizontalListView.this.mFirstPosition;
                    int lastPos = firstPos + childCount - 1;
                    int clampedPosition = Math.max(0, Math.min(AbsHorizontalListView.this.getCount() - 1, position));
                    int viewTravelCount;
                    int boundPosFromFirst;
                    int posTravel;
                    int boundTravel;
                    if (clampedPosition < firstPos) {
                        boundPosFromFirst = lastPos - boundPosition;
                        if (boundPosFromFirst < 1) {
                            return;
                        }

                        posTravel = firstPos - clampedPosition + 1;
                        boundTravel = boundPosFromFirst - 1;
                        if (boundTravel < posTravel) {
                            viewTravelCount = boundTravel;
                            this.mMode = 4;
                        } else {
                            viewTravelCount = posTravel;
                            this.mMode = 2;
                        }
                    } else {
                        if (clampedPosition <= lastPos) {
                            this.scrollToVisible(clampedPosition, boundPosition, 200);
                            return;
                        }

                        boundPosFromFirst = boundPosition - firstPos;
                        if (boundPosFromFirst < 1) {
                            return;
                        }

                        posTravel = clampedPosition - lastPos + 1;
                        boundTravel = boundPosFromFirst - 1;
                        if (boundTravel < posTravel) {
                            viewTravelCount = boundTravel;
                            this.mMode = 3;
                        } else {
                            viewTravelCount = posTravel;
                            this.mMode = 1;
                        }
                    }

                    if (viewTravelCount > 0) {
                        this.mScrollDuration = 200 / viewTravelCount;
                    } else {
                        this.mScrollDuration = 200;
                    }

                    this.mTargetPos = clampedPosition;
                    this.mBoundPos = boundPosition;
                    this.mLastSeenPos = -1;
                    AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                }
            }
        }

        void startWithOffset(int position, int offset) {
            this.startWithOffset(position, offset, 200);
        }

        void startWithOffset(final int position, int offset, final int duration) {
            this.stop();
            if (AbsHorizontalListView.this.mDataChanged) {
                final int offsetTemp = offset;
                AbsHorizontalListView.this.mPositionScrollAfterLayout = new Runnable() {
                    public void run() {
                        PositionScroller.this.startWithOffset(position, offsetTemp, duration);
                    }
                };
            } else {
                int childCount = AbsHorizontalListView.this.getChildCount();
                if (childCount != 0) {
                    offset += AbsHorizontalListView.this.getPaddingLeft();
                    this.mTargetPos = Math.max(0, Math.min(AbsHorizontalListView.this.getCount() - 1, position));
                    this.mOffsetFromLeft = offset;
                    this.mBoundPos = -1;
                    this.mLastSeenPos = -1;
                    this.mMode = 5;
                    int firstPos = AbsHorizontalListView.this.mFirstPosition;
                    int lastPos = firstPos + childCount - 1;
                    int viewTravelCount;
                    if (this.mTargetPos < firstPos) {
                        viewTravelCount = firstPos - this.mTargetPos;
                    } else {
                        if (this.mTargetPos <= lastPos) {
                            int screenTravelCount1 = AbsHorizontalListView.this.getChildAt(this.mTargetPos - firstPos).getLeft();
                            AbsHorizontalListView.this.smoothScrollBy(screenTravelCount1 - offset, duration, false);
                            return;
                        }

                        viewTravelCount = this.mTargetPos - lastPos;
                    }

                    float screenTravelCount = (float) viewTravelCount / (float) childCount;
                    this.mScrollDuration = screenTravelCount < 1.0F ? duration : (int) ((float) duration / screenTravelCount);
                    this.mLastSeenPos = -1;
                    AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                }
            }
        }

        void scrollToVisible(int targetPos, int boundPos, int duration) {
            int firstPos = AbsHorizontalListView.this.mFirstPosition;
            int childCount = AbsHorizontalListView.this.getChildCount();
            int lastPos = firstPos + childCount - 1;
            int paddedLeft = AbsHorizontalListView.this.mListPadding.left;
            int paddedRight = AbsHorizontalListView.this.getWidth() - AbsHorizontalListView.this.mListPadding.right;
            if (targetPos < firstPos || targetPos > lastPos) {
                Log.w("AbsListView", "scrollToVisible called with targetPos " + targetPos + " not visible [" + firstPos + ", " + lastPos + "]");
            }

            if (boundPos < firstPos || boundPos > lastPos) {
                boundPos = -1;
            }

            View targetChild = AbsHorizontalListView.this.getChildAt(targetPos - firstPos);
            int targetLeft = targetChild.getLeft();
            int targetRight = targetChild.getRight();
            int scrollBy = 0;
            if (targetRight > paddedRight) {
                scrollBy = targetRight - paddedRight;
            }

            if (targetLeft < paddedLeft) {
                scrollBy = targetLeft - paddedLeft;
            }

            if (scrollBy != 0) {
                if (boundPos >= 0) {
                    View boundChild = AbsHorizontalListView.this.getChildAt(boundPos - firstPos);
                    int boundLeft = boundChild.getLeft();
                    int boundRight = boundChild.getRight();
                    int absScroll = Math.abs(scrollBy);
                    if (scrollBy < 0 && boundRight + absScroll > paddedRight) {
                        scrollBy = Math.max(0, boundRight - paddedRight);
                    } else if (scrollBy > 0 && boundLeft - absScroll < paddedLeft) {
                        scrollBy = Math.min(0, boundLeft - paddedLeft);
                    }
                }

                AbsHorizontalListView.this.smoothScrollBy(scrollBy, duration);
            }
        }

        public void stop() {
            AbsHorizontalListView.this.removeCallbacks(this);
        }

        public void run() {
            int listWidth = AbsHorizontalListView.this.getWidth();
            int firstPos = AbsHorizontalListView.this.mFirstPosition;
            int childCount;
            int position;
            int lastPos;
            int viewTravelCount;
            int targetLeft;
            int distance;
            int duration;
            View lastPos1;
            int screenTravelCount1;
            int modifier1;
            switch (this.mMode) {
                case 1:
                    childCount = AbsHorizontalListView.this.getChildCount() - 1;
                    position = firstPos + childCount;
                    if (childCount < 0) {
                        return;
                    }

                    if (position == this.mLastSeenPos) {
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                        return;
                    }

                    lastPos1 = AbsHorizontalListView.this.getChildAt(childCount);
                    viewTravelCount = lastPos1.getWidth();
                    screenTravelCount1 = lastPos1.getLeft();
                    modifier1 = listWidth - screenTravelCount1;
                    targetLeft = position < AbsHorizontalListView.this.mItemCount - 1 ? Math.max(AbsHorizontalListView.this.mListPadding.right, this.mExtraScroll) : AbsHorizontalListView.this.mListPadding.right;
                    distance = viewTravelCount - modifier1 + targetLeft;
                    AbsHorizontalListView.this.smoothScrollBy(distance, this.mScrollDuration, true);
                    this.mLastSeenPos = position;
                    if (position < this.mTargetPos) {
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                    }
                    break;
                case 2:
                    if (firstPos == this.mLastSeenPos) {
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                        return;
                    }

                    View childCount2 = AbsHorizontalListView.this.getChildAt(0);
                    if (childCount2 == null) {
                        return;
                    }

                    position = childCount2.getLeft();
                    lastPos = firstPos > 0 ? Math.max(this.mExtraScroll, AbsHorizontalListView.this.mListPadding.left) : AbsHorizontalListView.this.mListPadding.left;
                    AbsHorizontalListView.this.smoothScrollBy(position - lastPos, this.mScrollDuration, true);
                    this.mLastSeenPos = firstPos;
                    if (firstPos > this.mTargetPos) {
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                    }
                    break;
                case 3:
                    boolean childCount1 = true;
                    position = AbsHorizontalListView.this.getChildCount();
                    if (firstPos != this.mBoundPos && position > 1 && firstPos + position < AbsHorizontalListView.this.mItemCount) {
                        lastPos = firstPos + 1;
                        if (lastPos == this.mLastSeenPos) {
                            AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                            return;
                        }

                        View viewTravelCount1 = AbsHorizontalListView.this.getChildAt(1);
                        screenTravelCount1 = viewTravelCount1.getWidth();
                        modifier1 = viewTravelCount1.getLeft();
                        targetLeft = Math.max(AbsHorizontalListView.this.mListPadding.right, this.mExtraScroll);
                        if (lastPos < this.mBoundPos) {
                            AbsHorizontalListView.this.smoothScrollBy(Math.max(0, screenTravelCount1 + modifier1 - targetLeft), this.mScrollDuration, true);
                            this.mLastSeenPos = lastPos;
                            AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                        } else if (modifier1 > targetLeft) {
                            AbsHorizontalListView.this.smoothScrollBy(modifier1 - targetLeft, this.mScrollDuration, true);
                        }
                        break;
                    }

                    return;
                case 4:
                    childCount = AbsHorizontalListView.this.getChildCount() - 2;
                    if (childCount < 0) {
                        return;
                    }

                    position = firstPos + childCount;
                    if (position == this.mLastSeenPos) {
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                        return;
                    }

                    lastPos1 = AbsHorizontalListView.this.getChildAt(childCount);
                    viewTravelCount = lastPos1.getWidth();
                    screenTravelCount1 = lastPos1.getLeft();
                    modifier1 = listWidth - screenTravelCount1;
                    targetLeft = Math.max(AbsHorizontalListView.this.mListPadding.left, this.mExtraScroll);
                    this.mLastSeenPos = position;
                    if (position > this.mBoundPos) {
                        AbsHorizontalListView.this.smoothScrollBy(-(modifier1 - targetLeft), this.mScrollDuration, true);
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                    } else {
                        distance = listWidth - targetLeft;
                        duration = screenTravelCount1 + viewTravelCount;
                        if (distance > duration) {
                            AbsHorizontalListView.this.smoothScrollBy(-(distance - duration), this.mScrollDuration, true);
                        }
                    }
                    break;
                case 5:
                    if (this.mLastSeenPos == firstPos) {
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                        return;
                    }

                    this.mLastSeenPos = firstPos;
                    childCount = AbsHorizontalListView.this.getChildCount();
                    position = this.mTargetPos;
                    lastPos = firstPos + childCount - 1;
                    viewTravelCount = 0;
                    if (position < firstPos) {
                        viewTravelCount = firstPos - position + 1;
                    } else if (position > lastPos) {
                        viewTravelCount = position - lastPos;
                    }

                    float screenTravelCount = (float) viewTravelCount / (float) childCount;
                    float modifier = Math.min(Math.abs(screenTravelCount), 1.0F);
                    if (position < firstPos) {
                        targetLeft = (int) ((float) (-AbsHorizontalListView.this.getWidth()) * modifier);
                        distance = (int) ((float) this.mScrollDuration * modifier);
                        AbsHorizontalListView.this.smoothScrollBy(targetLeft, distance, true);
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                    } else if (position > lastPos) {
                        targetLeft = (int) ((float) AbsHorizontalListView.this.getWidth() * modifier);
                        distance = (int) ((float) this.mScrollDuration * modifier);
                        AbsHorizontalListView.this.smoothScrollBy(targetLeft, distance, true);
                        AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                    } else {
                        targetLeft = AbsHorizontalListView.this.getChildAt(position - firstPos).getLeft();
                        distance = targetLeft - this.mOffsetFromLeft;
                        duration = (int) ((float) this.mScrollDuration * ((float) Math.abs(distance) / (float) AbsHorizontalListView.this.getWidth()));
                        AbsHorizontalListView.this.smoothScrollBy(distance, duration, true);
                    }
            }
        }
    }

    private class FlingRunnable implements Runnable {
        private final OverScroller mScroller = new OverScroller(AbsHorizontalListView.this.getContext());
        private int mLastFlingX;
        private final Runnable mCheckFlywheel = new Runnable() {
            public void run() {
                int activeId = AbsHorizontalListView.this.mActivePointerId;
                VelocityTracker vt = AbsHorizontalListView.this.mVelocityTracker;
                OverScroller scroller = FlingRunnable.this.mScroller;
                if (vt != null && activeId != -1) {
                    vt.computeCurrentVelocity(1000, (float) AbsHorizontalListView.this.mMaximumVelocity);
                    float xvel = -vt.getXVelocity(activeId);
                    if (Math.abs(xvel) >= (float) AbsHorizontalListView.this.mMinimumVelocity && scroller.isScrollingInDirection(xvel, 0.0F)) {
                        AbsHorizontalListView.this.postDelayed(this, 40L);
                    } else {
                        FlingRunnable.this.endFling();
                        AbsHorizontalListView.this.mTouchMode = 3;
                        AbsHorizontalListView.this.reportScrollStateChange(1);
                    }

                }
            }
        };
        private static final int FLYWHEEL_TIMEOUT = 40;

        FlingRunnable() {
        }

        void start(int initialVelocity) {
            int initialX = initialVelocity < 0 ? 2147483647 : 0;
            this.mLastFlingX = initialX;
            this.mScroller.setInterpolator((Interpolator) null);
            this.mScroller.fling(initialX, 0, initialVelocity, 0, 0, 2147483647, 0, 2147483647);
            AbsHorizontalListView.this.mTouchMode = 4;
            AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
        }

        void startSpringback() {
            if (this.mScroller.springBack(AbsHorizontalListView.this.getScrollX(), 0, 0, 0, 0, 0)) {
                AbsHorizontalListView.this.mTouchMode = 6;
                AbsHorizontalListView.this.invalidate();
                AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
            } else {
                AbsHorizontalListView.this.mTouchMode = -1;
                AbsHorizontalListView.this.reportScrollStateChange(0);
            }

        }

        void startOverfling(int initialVelocity) {
            this.mScroller.setInterpolator((Interpolator) null);
            this.mScroller.fling(AbsHorizontalListView.this.getScrollX(), 0, initialVelocity, 0, -2147483648, 2147483647, 0, 0, AbsHorizontalListView.this.getWidth(), 0);
            AbsHorizontalListView.this.mTouchMode = 6;
            AbsHorizontalListView.this.invalidate();
            AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
        }

        void edgeReached(int delta) {
            this.mScroller.notifyHorizontalEdgeReached(AbsHorizontalListView.this.getScrollX(), 0, AbsHorizontalListView.this.mOverflingDistance);
            int overscrollMode = AbsHorizontalListView.this.getOverScrollMode();
            if (overscrollMode == 0 || overscrollMode == 1 && !AbsHorizontalListView.this.contentFits()) {
                AbsHorizontalListView.this.mTouchMode = 6;
                int vel = (int) this.mScroller.getCurrVelocity();
                if (delta > 0) {
                    AbsHorizontalListView.this.mEdgeGlowLeft.onAbsorb(vel);
                } else {
                    AbsHorizontalListView.this.mEdgeGlowRight.onAbsorb(vel);
                }
            } else {
                AbsHorizontalListView.this.mTouchMode = -1;
                if (AbsHorizontalListView.this.mPositionScroller != null) {
                    AbsHorizontalListView.this.mPositionScroller.stop();
                }
            }

            AbsHorizontalListView.this.invalidate();
            AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
        }

        void startScroll(int distance, int duration, boolean linear) {
            int initialX = distance < 0 ? 2147483647 : 0;
            this.mLastFlingX = initialX;
            this.mScroller.setInterpolator(linear ? AbsHorizontalListView.sLinearInterpolator : null);
            this.mScroller.startScroll(initialX, 0, distance, 0, duration);
            AbsHorizontalListView.this.mTouchMode = 4;
            AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
        }

        void endFling() {
            AbsHorizontalListView.this.mTouchMode = -1;
            AbsHorizontalListView.this.removeCallbacks(this);
            AbsHorizontalListView.this.removeCallbacks(this.mCheckFlywheel);
            AbsHorizontalListView.this.reportScrollStateChange(0);
            AbsHorizontalListView.this.clearScrollingCache();
            this.mScroller.abortAnimation();
            AbsHorizontalListView.this.overScrollBy(0, 0, 0, 0, 0, 0, 0, 0, false);
            AbsHorizontalListView.this.resetInCenter();
        }

        void flywheelTouch() {
            AbsHorizontalListView.this.postDelayed(this.mCheckFlywheel, 40L);
        }

        public void run() {
            OverScroller scroller;
            int currX;
            int deltaX;
            switch (AbsHorizontalListView.this.mTouchMode) {
                case 3:
                    if (this.mScroller.isFinished()) {
                        return;
                    }
                case 4:
                    if (AbsHorizontalListView.this.mDataChanged) {
                        AbsHorizontalListView.this.layoutChildren();
                    }

                    if (AbsHorizontalListView.this.mItemCount != 0 && AbsHorizontalListView.this.getChildCount() != 0) {
                        scroller = this.mScroller;
                        boolean scrollX1 = scroller.computeScrollOffset();
                        currX = scroller.getCurrX();
                        deltaX = this.mLastFlingX - currX;
                        View crossRight1;
                        if (deltaX > 0) {
                            AbsHorizontalListView.this.mMotionPosition = AbsHorizontalListView.this.mFirstPosition;
                            crossRight1 = AbsHorizontalListView.this.getChildAt(0);
                            AbsHorizontalListView.this.mMotionViewOriginalLeft = crossRight1.getLeft();
                            deltaX = Math.min(AbsHorizontalListView.this.getWidth() - AbsHorizontalListView.this.getPaddingRight() - AbsHorizontalListView.this.getPaddingLeft() - 1, deltaX);
                        } else {
                            int crossRight2 = AbsHorizontalListView.this.getChildCount() - 1;
                            AbsHorizontalListView.this.mMotionPosition = AbsHorizontalListView.this.mFirstPosition + crossRight2;
                            View crossLeft2 = AbsHorizontalListView.this.getChildAt(crossRight2);
                            AbsHorizontalListView.this.mMotionViewOriginalLeft = crossLeft2.getLeft();
                            deltaX = Math.max(-(AbsHorizontalListView.this.getWidth() - AbsHorizontalListView.this.getPaddingRight() - AbsHorizontalListView.this.getPaddingLeft() - 1), deltaX);
                        }

                        crossRight1 = AbsHorizontalListView.this.getChildAt(AbsHorizontalListView.this.mMotionPosition - AbsHorizontalListView.this.mFirstPosition);
                        int crossLeft1 = 0;
                        if (crossRight1 != null) {
                            crossLeft1 = crossRight1.getLeft();
                        }

                        boolean velocity1 = AbsHorizontalListView.this.trackMotionScroll(deltaX, deltaX);
                        boolean atEnd = velocity1 && deltaX != 0;
                        if (atEnd) {
                            if (crossRight1 != null) {
                                int overshoot = -(deltaX - (crossRight1.getLeft() - crossLeft1));
                                AbsHorizontalListView.this.overScrollBy(overshoot, 0, AbsHorizontalListView.this.getScrollX(), 0, 0, 0, AbsHorizontalListView.this.mOverflingDistance, 0, false);
                            }

                            if (scrollX1) {
                                this.edgeReached(deltaX);
                            }
                        } else if (scrollX1 && !atEnd) {
                            if (velocity1) {
                                AbsHorizontalListView.this.invalidate();
                            }

                            this.mLastFlingX = currX;
                            AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                        } else {
                            this.endFling();
                        }
                        break;
                    }

                    this.endFling();
                    return;
                case 5:
                default:
                    this.endFling();
                    return;
                case 6:
                    scroller = this.mScroller;
                    if (scroller.computeScrollOffset()) {
                        int scrollX = AbsHorizontalListView.this.getScrollX();
                        currX = scroller.getCurrX();
                        deltaX = currX - scrollX;
                        if (AbsHorizontalListView.this.overScrollBy(deltaX, 0, scrollX, 0, 0, 0, AbsHorizontalListView.this.mOverflingDistance, 0, false)) {
                            boolean crossRight = scrollX <= 0 && currX > 0;
                            boolean crossLeft = scrollX >= 0 && currX < 0;
                            if (!crossRight && !crossLeft) {
                                this.startSpringback();
                            } else {
                                int velocity = (int) scroller.getCurrVelocity();
                                if (crossLeft) {
                                    velocity = -velocity;
                                }

                                scroller.abortAnimation();
                                this.start(velocity);
                            }
                        } else {
                            AbsHorizontalListView.this.invalidate();
                            AbsHorizontalListView.this.mViewHelper.postOnAnimation(this);
                        }
                    } else {
                        this.endFling();
                    }
            }

        }
    }

    final class CheckForTap implements Runnable {
        CheckForTap() {
        }

        public void run() {
            if (AbsHorizontalListView.this.mTouchMode == 0) {
                AbsHorizontalListView.this.mTouchMode = 1;
                View child = AbsHorizontalListView.this.getChildAt(AbsHorizontalListView.this.mMotionPosition - AbsHorizontalListView.this.mFirstPosition);
                if (child != null && !child.hasFocusable()) {
                    AbsHorizontalListView.this.mLayoutMode = 0;
                    if (!AbsHorizontalListView.this.mDataChanged) {
                        child.setPressed(true);
                        AbsHorizontalListView.this.setPressed(true);
                        AbsHorizontalListView.this.layoutChildren();
                        AbsHorizontalListView.this.positionSelector(AbsHorizontalListView.this.mMotionPosition, child);
                        AbsHorizontalListView.this.refreshDrawableState();
                        int longPressTimeout = ViewConfiguration.getLongPressTimeout();
                        boolean longClickable = AbsHorizontalListView.this.isLongClickable();
                        if (AbsHorizontalListView.this.mSelector != null) {
                            Drawable d = AbsHorizontalListView.this.mSelector.getCurrent();
                            if (d != null && d instanceof TransitionDrawable) {
                                if (longClickable) {
                                    ((TransitionDrawable) d).startTransition(longPressTimeout);
                                } else {
                                    ((TransitionDrawable) d).resetTransition();
                                }
                            }
                        }

                        if (longClickable) {
                            if (AbsHorizontalListView.this.mPendingCheckForLongPress == null) {
                                AbsHorizontalListView.this.mPendingCheckForLongPress = AbsHorizontalListView.this.new CheckForLongPress();
                            }

                            AbsHorizontalListView.this.mPendingCheckForLongPress.rememberWindowAttachCount();
                            AbsHorizontalListView.this.postDelayed(AbsHorizontalListView.this.mPendingCheckForLongPress, (long) longPressTimeout);
                        } else {
                            AbsHorizontalListView.this.mTouchMode = 2;
                        }
                    } else {
                        AbsHorizontalListView.this.mTouchMode = 2;
                    }
                }
            }

        }
    }

    private class CheckForKeyLongPress extends AbsHorizontalListView.WindowRunnnable implements Runnable {
        private CheckForKeyLongPress() {
            super();
        }

        public void run() {
            if (AbsHorizontalListView.this.isPressed() && AbsHorizontalListView.this.mSelectedPosition >= 0) {
                int index = AbsHorizontalListView.this.mSelectedPosition - AbsHorizontalListView.this.mFirstPosition;
                View v = AbsHorizontalListView.this.getChildAt(index);
                if (!AbsHorizontalListView.this.mDataChanged) {
                    boolean handled = false;
                    if (this.sameWindow()) {
                        handled = AbsHorizontalListView.this.performLongPress(v, AbsHorizontalListView.this.mSelectedPosition, AbsHorizontalListView.this.mSelectedColId);
                    }

                    if (handled) {
                        AbsHorizontalListView.this.setPressed(false);
                        v.setPressed(false);
                    }
                } else {
                    AbsHorizontalListView.this.setPressed(false);
                    if (v != null) {
                        v.setPressed(false);
                    }
                }
            }

        }
    }

    private class CheckForLongPress extends AbsHorizontalListView.WindowRunnnable implements Runnable {
        private CheckForLongPress() {
            super();
        }

        public void run() {
            int motionPosition = AbsHorizontalListView.this.mMotionPosition;
            View child = AbsHorizontalListView.this.getChildAt(motionPosition - AbsHorizontalListView.this.mFirstPosition);
            if (child != null) {
                int longPressPosition = AbsHorizontalListView.this.mMotionPosition;
                long longPressId = AbsHorizontalListView.this.mAdapter.getItemId(AbsHorizontalListView.this.mMotionPosition);
                boolean handled = false;
                if (this.sameWindow() && !AbsHorizontalListView.this.mDataChanged) {
                    handled = AbsHorizontalListView.this.performLongPress(child, longPressPosition, longPressId);
                }

                if (handled) {
                    AbsHorizontalListView.this.mTouchMode = -1;
                    AbsHorizontalListView.this.setPressed(false);
                    child.setPressed(false);
                } else {
                    AbsHorizontalListView.this.mTouchMode = 2;
                }
            }

        }
    }

    private class PerformClick extends AbsHorizontalListView.WindowRunnnable implements Runnable {
        int mClickMotionPosition;

        private PerformClick() {
            super();
        }

        public void run() {
            if (!AbsHorizontalListView.this.mDataChanged) {
                ListAdapter adapter = AbsHorizontalListView.this.mAdapter;
                int motionPosition = this.mClickMotionPosition;
                if (adapter != null && AbsHorizontalListView.this.mItemCount > 0 && motionPosition != -1 && motionPosition < adapter.getCount() && this.sameWindow()) {
                    View view = AbsHorizontalListView.this.getChildAt(motionPosition - AbsHorizontalListView.this.mFirstPosition);
                    if (view != null) {
                        AbsHorizontalListView.this.performItemClick(view, motionPosition, adapter.getItemId(motionPosition));
                    }
                }

            }
        }
    }

    private class WindowRunnnable {
        private int mOriginalAttachCount;

        private WindowRunnnable() {
        }

        public void rememberWindowAttachCount() {
            this.mOriginalAttachCount = AbsHorizontalListView.this.getWindowAttachCount();
        }

        public boolean sameWindow() {
            return AbsHorizontalListView.this.hasWindowFocus() && AbsHorizontalListView.this.getWindowAttachCount() == this.mOriginalAttachCount;
        }
    }

    @TargetApi(14)
    class ListItemAccessibilityDelegate extends AccessibilityDelegateCompat {
        ListItemAccessibilityDelegate() {
        }

        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            int position = AbsHorizontalListView.this.getPositionForView(host);
            ListAdapter adapter = (ListAdapter) AbsHorizontalListView.this.getAdapter();
            if (position != -1 && adapter != null) {
                if (AbsHorizontalListView.this.isEnabled() && adapter.isEnabled(position)) {
                    if (position == AbsHorizontalListView.this.getSelectedItemPosition()) {
                        info.setSelected(true);
                        info.addAction(8);
                    } else {
                        info.addAction(4);
                    }

                    if (AbsHorizontalListView.this.isClickable()) {
                        info.addAction(16);
                        info.setClickable(true);
                    }

                    if (AbsHorizontalListView.this.isLongClickable()) {
                        info.addAction(32);
                        info.setLongClickable(true);
                    }

                }
            }
        }

        public boolean performAccessibilityAction(View host, int action, Bundle arguments) {
            if (super.performAccessibilityAction(host, action, arguments)) {
                return true;
            } else {
                int position = AbsHorizontalListView.this.getPositionForView(host);
                ListAdapter adapter = (ListAdapter) AbsHorizontalListView.this.getAdapter();
                if (position != -1 && adapter != null) {
                    if (AbsHorizontalListView.this.isEnabled() && adapter.isEnabled(position)) {
                        long id = AbsHorizontalListView.this.getItemIdAtPosition(position);
                        switch (action) {
                            case 4:
                                if (AbsHorizontalListView.this.getSelectedItemPosition() != position) {
                                    AbsHorizontalListView.this.setSelection(position);
                                    return true;
                                }

                                return false;
                            case 8:
                                if (AbsHorizontalListView.this.getSelectedItemPosition() == position) {
                                    AbsHorizontalListView.this.setSelection(-1);
                                    return true;
                                }

                                return false;
                            case 16:
                                if (AbsHorizontalListView.this.isClickable()) {
                                    return AbsHorizontalListView.this.performItemClick(host, position, id);
                                }

                                return false;
                            case 32:
                                if (AbsHorizontalListView.this.isLongClickable()) {
                                    return AbsHorizontalListView.this.performLongPress(host, position, id);
                                }

                                return false;
                            default:
                                return false;
                        }
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        }
    }

    static class SavedState extends BaseSavedState {
        long selectedId;
        long firstId;
        int viewLeft;
        int position;
        int width;
        String filter;
        boolean inActionMode;
        int checkedItemCount;
        SparseArrayCompat<Boolean> checkState;
        LongSparseArray<Integer> checkIdState;
        public static final Creator<SavedState> CREATOR = new Creator() {
            public AbsHorizontalListView.SavedState createFromParcel(Parcel in) {
                return new AbsHorizontalListView.SavedState(in);
            }

            public AbsHorizontalListView.SavedState[] newArray(int size) {
                return new AbsHorizontalListView.SavedState[size];
            }
        };

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            this.selectedId = in.readLong();
            this.firstId = in.readLong();
            this.viewLeft = in.readInt();
            this.position = in.readInt();
            this.width = in.readInt();
            this.filter = in.readString();
            this.inActionMode = in.readByte() != 0;
            this.checkedItemCount = in.readInt();
            this.checkState = this.readSparseBooleanArray(in);
            this.checkIdState = this.readSparseLongArray(in);
        }

        private LongSparseArray<Integer> readSparseLongArray(Parcel in) {
            int N = in.readInt();
            if (N <= 0) {
                return null;
            } else {
                LongSparseArray array = new LongSparseArray(N);
                this.readSparseLongArrayInternal(array, in, N);
                return array;
            }
        }

        private SparseArrayCompat<Boolean> readSparseBooleanArray(Parcel in) {
            int N = in.readInt();
            if (N < 0) {
                return null;
            } else {
                SparseArrayCompat sa = new SparseArrayCompat(N);
                this.readSparseBooleanArrayInternal(sa, in, N);
                return sa;
            }
        }

        private void readSparseLongArrayInternal(LongSparseArray<Integer> outVal, Parcel in, int N) {
            while (N > 0) {
                long key = in.readLong();
                int value = in.readInt();
                outVal.put(key, Integer.valueOf(value));
                --N;
            }

        }

        private void readSparseBooleanArrayInternal(SparseArrayCompat<Boolean> outVal, Parcel in, int N) {
            while (N > 0) {
                int key = in.readInt();
                boolean value = in.readByte() == 1;
                outVal.append(key, Boolean.valueOf(value));
                --N;
            }

        }

        private void writeSparseLongArray(LongSparseArray<Integer> array, Parcel out) {
            int N = array != null ? array.size() : 0;
            out.writeInt(N);

            for (int i = 0; i < N; ++i) {
                out.writeLong(array.keyAt(i));
                out.writeInt(((Integer) array.valueAt(i)).intValue());
            }

        }

        private void writeSparseBooleanArray(SparseArrayCompat<Boolean> val, Parcel out) {
            if (val == null) {
                out.writeInt(-1);
            } else {
                int N = val.size();
                out.writeInt(N);

                for (int i = 0; i < N; ++i) {
                    out.writeInt(val.keyAt(i));
                    out.writeByte((byte) (((Boolean) val.valueAt(i)).booleanValue() ? 1 : 0));
                }

            }
        }

        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeLong(this.selectedId);
            out.writeLong(this.firstId);
            out.writeInt(this.viewLeft);
            out.writeInt(this.position);
            out.writeInt(this.width);
            out.writeString(this.filter);
            out.writeByte((byte) (this.inActionMode ? 1 : 0));
            out.writeInt(this.checkedItemCount);
            this.writeSparseBooleanArray(this.checkState, out);
            this.writeSparseLongArray(this.checkIdState, out);
        }

        public String toString() {
            return "AbsListView.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " selectedId=" + this.selectedId + " firstId=" + this.firstId + " viewLeft=" + this.viewLeft + " position=" + this.position + " width=" + this.width + " filter=" + this.filter + " checkState=" + this.checkState + "}";
        }
    }

    public interface SelectionBoundsAdjuster {
        void adjustListItemSelectionBounds(Rect var1);
    }

    public interface OnScrollListener {
        int SCROLL_STATE_IDLE = 0;
        int SCROLL_STATE_TOUCH_SCROLL = 1;
        int SCROLL_STATE_FLING = 2;

        void onScrollStateChanged(AbsHorizontalListView view, int scrollState);

        void onScroll(AbsHorizontalListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount);
    }
}
