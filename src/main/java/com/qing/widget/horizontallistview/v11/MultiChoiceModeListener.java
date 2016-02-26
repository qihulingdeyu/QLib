package com.qing.widget.horizontallistview.v11;

import android.annotation.TargetApi;
import android.view.ActionMode;
import android.view.ActionMode.Callback;

@TargetApi(11)
public interface MultiChoiceModeListener extends Callback {
    void onItemCheckedStateChanged(ActionMode var1, int var2, long var3, boolean var5);
}
