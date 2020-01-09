package com.android.launcher3;

import android.app.Service;
import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.text.TextUtils;
import android.util.Log;
 
import tv.techm.dbproviderhelper.techmtossHelper;
import tv.techm.dbproviderhelper.techmtossUtils;
//import tv.techm.stats.Statistics;

public class SystemManagerService extends Service 
    implements Activation.Callback, 
               Authentication.Callback 
                {

    private static final boolean DBG = true;
    private static final String TAG = "sysmgr";
    private boolean authOnBoot = false;
    private boolean homescreenStore = false;
    private int mAuthInterval;
    private int mAuthOffTime;

    private ServiceHandler mServiceHandler;
    private Looper mServiceLooper;

    public void onCreate() {
        super.onCreate();

        HandlerThread thread = 
            new HandlerThread(TAG, Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);

        mAuthInterval = techmtossHelper.getAuthenticationInterval();
        mAuthOffTime = techmtossHelper.getAuthenticationOfflineDurationOnBoot();

        // sysmgr initiates authentication via launcher if activated
        Intent intent = new Intent();
        intent.putExtra("event", Global.EVENT_AUTHENTICATE_ON_BOOT);
        sendMessage(intent);
    }

    public void onDestroy() {
        super.onDestroy();
        mServiceLooper.quit();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.e(TAG, "null intent at onStartCommand");
            return Service.START_STICKY;
        }

        if (DBG)
            Log.d(TAG, "onStartCommand: " + "intent=" + intent.getAction() + 
                  "startId=" + startId);

        sendMessage(intent);

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void sendMessage(Intent intent) {
        Message msg = mServiceHandler.obtainMessage();
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
    }

    private void sendMessage(Intent intent, int delay) {
        String event = intent.getStringExtra("event");
        int what = event.hashCode();
        if (mServiceHandler.hasMessages(what)) {
            if (DBG)
                Log.d(TAG, "sendMessage: previous event removed:" + event);
            mServiceHandler.removeMessages(what);
        }
        Message msg = mServiceHandler.obtainMessage();
        msg.obj = intent;
        msg.what = what;
        mServiceHandler.sendMessageDelayed(msg, delay);
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            try {
                Intent intent = (Intent)msg.obj;
		 Log.v(TAG, "**intent.getData() : "+intent.getData());
                if (intent != null) {
                    String event = intent.getStringExtra("event");

                    if (DBG)
                        Log.d(TAG, "handleMessage: " + 
                              " event=" + event + 
                              " action=" + intent.getAction());

                    if (Global.EVENT_START_ACTIVITY.equals(event)) {
                        handleStartActivity(intent);
                    } else if (Global.EVENT_ACTIVATE.equals(event)) {
                        handleActivate(intent);
                    } else if (Global.EVENT_AUTHENTICATE.equals(event)) {
                        handleAuthenticate(intent);
                    } else if (Global.EVENT_AUTHENTICATE_ON_BOOT.equals(event)) {
                        handleAuthenticateBoot();
                    }
                }
            } catch (Throwable t) {
                Log.e(TAG, "handleMessage fatal error", t);
            }
        }
    }

    private void handleStartActivity(Intent intent) {
        ComponentName cn = new ComponentName(intent.getStringExtra("pkg"),
                                             intent.getStringExtra("cls"));
       //Snehal
	 Log.v(TAG, "SystemManahegr   handleStartActivity ");
//TDB: if authenication fails and all applications need to be disabled	
 /*Log.v("Error code ","Error   code :  "+  techmtossHelper.getAuthenticationErrorCode(this));
	if(techmtossHelper.getAuthenticationErrorCode(this)!=0){
	Log.v(TAG,"cant start app as auth error occured");
		return;
}*/
        Intent app = new Intent();	
	if(intent.getStringExtra("url")!=null){
		app.setAction(Intent.ACTION_VIEW);		
		Log.v(TAG, "1.intent.getData() : "+intent.getStringExtra("url"));
		app.setData(Uri.parse(intent.getStringExtra("url"))); 
	}
	else{
		app.setAction(Intent.ACTION_MAIN);
		app.addCategory(Intent.CATEGORY_LAUNCHER);

	}

		
	 Log.v(TAG, "Action"+intent.getAction());
//      Intent app = new Intent(Intent.ACTION_MAIN);
	
	      
        app.setComponent(cn);
        app.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 
                     Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        app.setSourceBounds(intent.getSourceBounds());
        startActivity(app);
	
        Log.v(TAG, "Show some add first ");
	Intent collectAdd=new Intent();
	collectAdd.setComponent(new ComponentName("tv.techm","tv.techm.AddClientService"));
   	collectAdd.putExtra("tv.techm.ShowAdd",true);
        startService(collectAdd);



    }

    public void onActivationFinished(boolean status, String message, 
                                     int errorCode) {
        if (DBG) Log.d(TAG, "onActivationFinished");
	//Statistics.logStatistics(this,"Activation","Activation passed");
	Intent intent = new Intent(Global.ACTION_ACTIVATION);
        intent.putExtra("status", status);
        intent.putExtra("message", message);
        intent.putExtra("error_code", errorCode);
	homescreenStore = false;
	sendBroadcast(intent, Global.PERMISSION_SYSTEM_MANAGER);
	
    }

    public void onAuthenticationFinished(boolean status, String message,
                                         int errorCode, int errorElapsed) {
        if (DBG) Log.d(TAG, "onAuthenticationFinished: " + status +"    error code   : "+ errorCode);
 

       
  if (status) {
	String homescreenVersion = "yes";//techmtossHelper.shouldUpdateHomeScreen(this);
	Log.d(TAG, "homescreenVersion :" +homescreenVersion);
	
	 Intent intentforsessionId = new Intent(Global.EVENT_SAVE_SESSIONID);
	 intentforsessionId.putExtra("sessionId", techmtossHelper.getSessionId(this));
	 sendBroadcast(intentforsessionId, Global.PERMISSION_SYSTEM_MANAGER);
	
	 if (authOnBoot){
		homescreenStore = true;
		authOnBoot=false;
	}
      
            mAuthInterval = techmtossHelper.getAuthenticationInterval();
            mAuthOffTime = techmtossHelper.getAuthenticationOfflineDuration();
        } else {

            if (DBG) Log.d(TAG, "auth expired:" + mAuthOffTime + 
                           " elapsed=" + errorElapsed);

            // activation has been disabled
            if (errorCode == Global.ECODE_DEVICE_DISABLED) {
		Log.d(TAG, "auth failed with code ECODE_DEVICE_DISABLED");
                Activation.disable(this);
            }

            // activation has been deactivated
            if (errorCode == Global.ECODE_DEVICE_DEACTIVATED) {
		Log.d(TAG, "auth failed with code ECODE_DEVICE_DEACTIVATED");
                Activation.deactivate(this);
                startLauncher(Global.EVENT_DEACTIVATED, message);
                return;
            }
		if(errorCode == Global.ECODE_NOT_AUTHENTICATED){
			Log.d(TAG, "auth failed with code ECODE_NOT_AUTHENTICATED");
			  startLauncher(Global.EVENT_AUTH_FAIL, message);
				return;
		}

            if (errorElapsed >= mAuthOffTime) {
                // reset the counter
                techmtossUtils.deleteKeyValueRow(this, "auth", "error_count");
                String m = null;
                if (errorCode == Global.ECODE_DEVICE_DISABLED)
                    m = getResources().getString(R.string.disabled_message);
                else
                    m = getResources().getString(R.string.offline_message);
                startLauncher(Global.EVENT_AUTHENTICATION_EXPIRED, m);
                return;
            }
	

            // on error, call it again with shorter interval :Snehal disable this for a while
/*            mAuthInterval = techmtossHelper.getAuthenticationIntervalError();
            Intent intent = new Intent();
            intent.putExtra("event", Global.EVENT_AUTHENTICATE);
            sendMessage(intent, mAuthInterval);*/
        }


        // broadcast an intent every authentication
        Intent intent = new Intent(Global.ACTION_AUTHENTICATION);
        if (!status) {
            intent.putExtra("message", message);
            intent.putExtra("error_code", errorCode);
        }
	
        sendBroadcast(intent, Global.PERMISSION_SYSTEM_MANAGER);

	//String sessiodId = techmtossHelper.getSessionId(this);
	//startLauncher(Global.EVENT_SAVE_SESSIONID,sessiodId);
	
    }

    public void onLauncherFinished() {
        if (DBG) Log.d(TAG, "onLauncherFinished");

  

        Intent intent = new Intent(Global.ACTION_AUTHENTICATION);
        sendBroadcast(intent, Global.PERMISSION_SYSTEM_MANAGER);
    }

    private void handleActivate(final Intent intent) {
        // run this in the main thread
        Handler mWorker = new Handler(Looper.getMainLooper());
        mWorker.post(new Runnable() {
                public void run() {
                    Activation activation = 
                        new Activation(SystemManagerService.this);
                    activation.activate(intent);
                }
            });
    }

    private void handleAuthenticate(final Intent intent) {
        // run this in the main thread
        Handler mWorker = new Handler(Looper.getMainLooper());
        mWorker.post(new Runnable() {
                public void run() {
                    Authentication auth = 
                        new Authentication(SystemManagerService.this);
                    auth.authenticate(intent);
                }
            });

        // call itself again
        sendMessage(intent, mAuthInterval);
    }

   

    private void handleCreateConfigProvider() {
        Intent intent = new Intent(Global.ACTION_CONFIG_PROVIDER);
        sendBroadcast(intent);
    }

    private void handleAuthenticateBoot() {
	Log.v(TAG,"handleAuthenticateBoot only");
        boolean hasActivated = false;
        try {
            hasActivated = techmtossHelper.hasActivated(this);
        } catch(Exception e) {}

        // send authentication request to launcher if the device has
        // been activated, if not, return.
        if (!hasActivated) 
            return;
	authOnBoot = true;
        // delete the previsous authentication and rest cache
        try {
            techmtossHelper.removeSessionId(this);
            techmtossUtils.deleteKeyValueRow(this, "auth", "error_code");
            techmtossUtils.deleteKeyValueRow(this, "auth", "error_count");
            techmtossUtils.deleteKeyValueRow(this, "auth", "error_timestamp");
            // FIXME: allow to show the app after reboot
            techmtossUtils.deleteKeyValueRow(this, "auth", "menu_updated");
          //  techmtossUtils.deleteTable(this, "rest");
        } catch(Exception e) {}
	
	//snehal
	//handleSetupLauncher(new Intent(Intent.ACTION_MAIN, null));
	Log.v(TAG,"handleAuthenticateBoot + handleSetupLauncher");
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("event", Global.EVENT_AUTHENTICATE_ON_BOOT);
        startActivity(intent);
    }

    private void startLauncher(String event, String message) {
        // remove the current message from the queue
        int what = Global.EVENT_AUTHENTICATE.hashCode();
        mServiceHandler.removeMessages(what);

        // start launcher with the error dialog
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("event", event);
	Log.v(TAG,"=========== pushing session id startLauncher "+ message);
        intent.putExtra("message", message);
        startActivity(intent);
    }
}
