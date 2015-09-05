package com.qing.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by zwq on 2015/04/12 21:03.<br/><br/>
 * 操作软键盘工具类
 */
public class KeyboardUtils {

    private static InputMethodManager imm;
    public static InputMethodManager getIMM(Context context){
        if(imm==null){
            imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
        return imm;
    }

    /**
     * 强制隐藏键盘
     * @param context
     * @param v
     */
    public static void hideKeyboard(Context context,View v){
        if(imm==null){
            imm = getIMM(context);
        }
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    /**
     * 强制显示键盘
     * @param context
     * @param v
     */
    public static void showKeyboard(Context context,View v){
        if(imm==null){
            imm = getIMM(context);
        }
        imm.showSoftInput(v, 0);
    }

    /**
     * 关闭或打开键盘
     * @param context
     */
    public static void toggleKeyboard(Context context){
        if(imm==null){
            imm = getIMM(context);
        }
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
