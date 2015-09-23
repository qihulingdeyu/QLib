package com.qing.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by zwq on 2015/09/23 16:28.<br/><br/>
 */
public class BaseService extends Service {

    private static final String TAG = BaseService.class.getName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
