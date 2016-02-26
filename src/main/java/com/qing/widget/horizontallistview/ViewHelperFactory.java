package com.qing.widget.horizontallistview;

import android.os.Build.VERSION;
import android.util.Log;
import android.view.View;

import com.qing.widget.horizontallistview.v14.ViewHelper14;
import com.qing.widget.horizontallistview.v16.ViewHelper16;

public class ViewHelperFactory {
    private static final String LOG_TAG = "ViewHelper";

    public ViewHelperFactory() {
    }

    public static final ViewHelperFactory.ViewHelper create(View view) {
        int version = VERSION.SDK_INT;
        return (ViewHelperFactory.ViewHelper) (version >= 16 ? new ViewHelper16(view) : (version >= 14 ? new ViewHelper14(view) : new ViewHelperFactory.ViewHelperDefault(view)));
    }

    public static class ViewHelperDefault extends ViewHelperFactory.ViewHelper {
        public ViewHelperDefault(View view) {
            super(view);
        }

        public void postOnAnimation(Runnable action) {
            this.view.post(action);
        }

        public void setScrollX(int value) {
            Log.d("ViewHelper", "setScrollX: " + value);
            this.view.scrollTo(value, this.view.getScrollY());
        }

        public boolean isHardwareAccelerated() {
            return false;
        }
    }

    public abstract static class ViewHelper {
        protected View view;

        protected ViewHelper(View view) {
            this.view = view;
        }

        public abstract void postOnAnimation(Runnable var1);

        public abstract void setScrollX(int var1);

        public abstract boolean isHardwareAccelerated();
    }
}
