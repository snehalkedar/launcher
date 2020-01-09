package com.android.launcher3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
//import tv.techm.Global;

public class SystemManagerReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            startService(context);
        }
	
    }

    private void startService(Context context) {
        context.startService(new Intent(context, SystemManagerService.class));
    }
    
    
}
