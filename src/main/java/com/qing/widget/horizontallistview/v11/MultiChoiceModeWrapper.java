package com.qing.widget.horizontallistview.v11;

import android.annotation.TargetApi;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.qing.widget.horizontallistview.AbsHorizontalListView;


public class MultiChoiceModeWrapper implements MultiChoiceModeListener {
    private MultiChoiceModeListener mWrapped;
    private AbsHorizontalListView mView;

    public MultiChoiceModeWrapper(AbsHorizontalListView view) {
        this.mView = view;
    }

    public void setWrapped(MultiChoiceModeListener wrapped) {
        this.mWrapped = wrapped;
    }

    public boolean hasWrappedCallback() {
        return this.mWrapped != null;
    }

    @TargetApi(11)
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        if (this.mWrapped.onCreateActionMode(mode, menu)) {
            this.mView.setLongClickable(false);
            return true;
        } else {
            return false;
        }
    }

    @TargetApi(11)
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return this.mWrapped.onPrepareActionMode(mode, menu);
    }

    @TargetApi(11)
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        return this.mWrapped.onActionItemClicked(mode, item);
    }

    @TargetApi(11)
    public void onDestroyActionMode(ActionMode mode) {
        this.mWrapped.onDestroyActionMode(mode);
        this.mView.mChoiceActionMode = null;
        this.mView.clearChoices();
        this.mView.mDataChanged = true;
        this.mView.rememberSyncState();
        this.mView.requestLayout();
        this.mView.setLongClickable(true);
    }

    @TargetApi(11)
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        this.mWrapped.onItemCheckedStateChanged(mode, position, id, checked);
        if (this.mView.getCheckedItemCount() == 0) {
            mode.finish();
        }

    }
}
