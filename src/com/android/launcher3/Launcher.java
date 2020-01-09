/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.launcher3;

import com.android.common.Search;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.StatusBarManager;
import android.app.WallpaperManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.LiveFolders;
import android.text.Selection;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.view.Display;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.LinearLayout;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.DataInputStream;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.JsResult;
import java.util.Collections;
import java.util.StringTokenizer;

//added by clei for mangage ethernet
import android.net.ethernet.EthernetManager;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.os.ServiceManager;

// techm:
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.app.ProgressDialog;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManager;

// techm:
//import tv.techm.Global;
import tv.techm.dbproviderhelper.techmtossHelper;
import tv.techm.dbproviderhelper.techmtossUtils;
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import static android.net.ethernet.EthernetManager.ETH_STATE_DISABLED;
import static android.net.ethernet.EthernetManager.ETH_STATE_ENABLED;
import static android.net.ethernet.EthernetManager.ETH_STATE_UNKNOWN;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
//import tv.techm.datastore.DataProviderApi;
/**
 * Default launcher application.
 */
public final class Launcher extends Activity
        {
    static final String TAG = "Launcher";
    static final boolean LOGD = true;

    static final boolean PROFILE_STARTUP = false;
    static final boolean DEBUG_WIDGETS = false;
    static final boolean DEBUG_USER_INTERFACE = false;

    private static final int WALLPAPER_SCREENS_SPAN = 2;

    private static final int MENU_GROUP_ADD = 1;
    private static final int MENU_GROUP_WALLPAPER = MENU_GROUP_ADD + 1;

    private static final int MENU_ADD = Menu.FIRST + 1;
    private static final int MENU_WALLPAPER_SETTINGS = MENU_ADD + 1;
    private static final int MENU_SEARCH = MENU_WALLPAPER_SETTINGS + 1;
    private static final int MENU_NOTIFICATIONS = MENU_SEARCH + 1;
    private static final int MENU_SETTINGS = MENU_NOTIFICATIONS + 1;

    private static final int REQUEST_CREATE_SHORTCUT = 1;
    private static final int REQUEST_CREATE_LIVE_FOLDER = 4;
    private static final int REQUEST_CREATE_APPWIDGET = 5;
    private static final int REQUEST_PICK_APPLICATION = 6;
    private static final int REQUEST_PICK_SHORTCUT = 7;
    private static final int REQUEST_PICK_LIVE_FOLDER = 8;
    private static final int REQUEST_PICK_APPWIDGET = 9;
    private static final int REQUEST_PICK_WALLPAPER = 10;

    static final String EXTRA_SHORTCUT_DUPLICATE = "duplicate";

    // techm: from 5 to 10
    static final int SCREEN_COUNT = 9;
    static final int DEFAULT_SCREEN = 0;
    //added by clei for sometimes icon disappeared when reboot
    // techm: from 6 to 3
    static final int NUMBER_CELLS_X = 3;//4
    static final int NUMBER_CELLS_Y = 3;//4

    static final int DIALOG_CREATE_SHORTCUT = 1;
    static final int DIALOG_RENAME_FOLDER = 2;

    private static final String PREFERENCES = "launcher.preferences";

    // Type: int
    private static final String RUNTIME_STATE_CURRENT_SCREEN = "launcher.current_screen";
    // Type: boolean
    private static final String RUNTIME_STATE_ALL_APPS_FOLDER = "launcher.all_apps_folder";
    // Type: long
    private static final String RUNTIME_STATE_USER_FOLDERS = "launcher.user_folder";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SCREEN = "launcher.add_screen";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_X = "launcher.add_cellX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_CELL_Y = "launcher.add_cellY";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_X = "launcher.add_spanX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_SPAN_Y = "launcher.add_spanY";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_X = "launcher.add_countX";
    // Type: int
    private static final String RUNTIME_STATE_PENDING_ADD_COUNT_Y = "launcher.add_countY";
    // Type: int[]
    private static final String RUNTIME_STATE_PENDING_ADD_OCCUPIED_CELLS = "launcher.add_occupied_cells";
    // Type: boolean
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME = "launcher.rename_folder";
    // Type: long
    private static final String RUNTIME_STATE_PENDING_FOLDER_RENAME_ID = "launcher.rename_folder_id";

    static final int APPWIDGET_HOST_ID = 1024;

    private static final Object sLock = new Object();
    private static int sScreen = DEFAULT_SCREEN;
	private WebView webView; 
	private WebView webViewFordb;
   String url = null;
	private HashMap<String,String> mNativeAppNames= new HashMap<String,String>();

    
  //  private EthernetManager myEthManager;
		int flag=1;


    // Hotseats (quick-launch icons next to AllApps)
    /*private static final int NUM_HOTSEATS = 2;
    private String[] mHotseatConfig = null;
    private Intent[] mHotseats = null;
    private Drawable[] mHotseatIcons = null;
    private CharSequence[] mHotseatLabels = null;
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
  	webViewFordb = new mywebview(this);
         webViewFordb.requestFocus(View.FOCUS_DOWN);
        webViewFordb.getSettings().setJavaScriptEnabled(true);
        webViewFordb.getSettings().setDomStorageEnabled(true);
//	webViewFordb.getSettings().setAppCacheEnabled(true);
        webViewFordb.getSettings().setDatabaseEnabled(true);
        webViewFordb.getSettings().setDatabasePath("/data/data/"+this.getPackageName()+"/databases/");
        //webViewFordb.loadUrl("file:///android_asset/store.html");
      
    //    webViewFordb.loadUrl("http://swaroop.com/store");
//addded by clei for get ETH_FLAG
//		ContentResolver cr = getContentResolver();
	/*  myEthManager =(EthernetManager)getSystemService(ETH_SERVICE);	
			
       	 if(myEthManager.getEthState() == ETH_STATE_ENABLED)
		{   
		        Log.i(TAG,"*************flag setEthdisabled*******************"); 		
		
 			//myEthManager =(EthernetManager)getSystemService(ETH_SERVICE);
 			myEthManager.setEthEnabled(true);
 			
		}*/        
	
    
    }
 
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d(TAG, "onStart");
	IntentFilter filter = new IntentFilter(Global.ACTION_AUTHENTICATION);
        filter.addAction(Global.ACTION_ACTIVATION);
	filter.addAction(Global.EVENT_SAVE_SESSIONID);
        registerReceiver(mSystemManagerReceiver, filter);
	}

	protected void onStop() {
		// TODO Auto-generated method stub
		unregisterReceiver(mSystemManagerReceiver);
		super.onStop();

	}

void setupWorkspace(){
	        Log.v(TAG,"setupWorkspace");
//Collect all the main activities of all installed packages.
		PackageManager manager = this.getPackageManager();

		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		List<ResolveInfo> apps = 
				manager.queryIntentActivities(mainIntent, 0);

		if (apps == null) {
			return;
		}

		Collections.sort(apps, 
				new ResolveInfo.DisplayNameComparator(manager));

		int count = apps.size();

		for (int i = 0; i < count; i++) {
			ResolveInfo info = apps.get(i);

			//CharSequence title = info.loadLabel(manager);
			String pn = info.activityInfo.applicationInfo.packageName;
			String n = info.activityInfo.name;
			mNativeAppNames.put(pn,n);
		}

		
		//this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.tlauncher);

		//Instance of webview
		webView = new mywebview(this);//(WebView)findViewById(R.id.webView1);
                webView.requestFocus(View.FOCUS_DOWN);
		LinearLayout layout= (LinearLayout)findViewById(R.id.linearlayout1);
		layout.addView(webView, new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
                webView.setScrollBarStyle(webView.SCROLLBARS_INSIDE_OVERLAY);
		//String url = "http://www.google.com/tv/spotlight-gallery.html";//"http://192.168.1.100/test.html";

		webView.setWebChromeClient(new WebChromeClient() {

			@Override
			public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
				Log.v(TAG,"SDK version --->  "+android.os.Build.VERSION.SDK_INT); 
				/*if(android.os.Build.VERSION.SDK_INT !=10)
					 return super.onJsAlert(view, url, message, result);
				 else*/
					 super.onJsAlert(view, url, message, result);
				String packageName=null;
				String appType=null;
				
				if(message!=null){

					//Tokenize the alert message to separate out the application type and the package/URL string
					StringTokenizer st = new StringTokenizer(message);
					appType=st.nextToken();
					if(st.hasMoreTokens())
						packageName = st.nextToken();
					startApplication(appType,packageName);
					
				}
				
				result.confirm();
				
				return true;

			}
		});

				webView.getSettings().setJavaScriptEnabled(true);
				webView.getSettings().setDomStorageEnabled(true);
                webView.getSettings().setDatabaseEnabled(true);
                webView.getSettings().setAppCacheEnabled(true);
                webView.getSettings().setDatabasePath("/data/data/"+this.getPackageName()+"/databases/");
                webView.setWebViewClient(new WebViewClient(){
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url != null && !(url.startsWith("http://ec2-54-243-53-181.compute-1.amazonaws.com"))) {
            startActivity(
                new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        } else {
            return false;
        }
    }
});
/*		
               webView.setWebViewClient(new WebViewClient()) {
			public void onPageFinished(WebView view, String url)  
			{  
				webview.loadUrl("javascript:(function() { " +  
						"document.getElementsByTagName('body')[0].style.color = 'red'; " +  
						"})()");  
			}  
                }

*/
		webView.addJavascriptInterface(new JavascriptBridge(), "jb");
		url=techmtossHelper.getWebHomescreenUrl(this);;
		Log.v(TAG, "URL  :  "+url);
		if(url==null)
			url= "http://techmtoss.in:8080/homescreen/web/index";
//		HashMap<String,String> map = new HashMap<String,String>();
//		map.put("session_id",techmtossHelper.getSessionId());
               Log.v(TAG,"before webview load=============== session id push "+ techmtossHelper.getSessionId(this));
           //     webViewFordb.loadUrl("file:///android_asset/store.html");
	     //   webViewFordb.loadUrl("javascript:setSesssionId('"+ techmtossHelper.getSessionId(this) +"')");
			webView.loadUrl(url);
	//	webView.loadUrl("file:///android_asset/store.html");
		Log.v(TAG,"load url: "+url);

                     


}

     public void startApplication(String appType ,String packageName){
    	 Log.v(TAG,"appType --->  "+appType);
    	 ComponentName cn =null;
			if(appType.equalsIgnoreCase("native"))
			{
			//	packageName=st.nextToken();
				Log.v(TAG,"message --->  "+packageName);
				if(packageName!=null){
					String activityName = mNativeAppNames.get(packageName);
					if(activityName!=null)
					{
						cn =new ComponentName(packageName,activityName);

						Intent app = new Intent();	
						app.setComponent(cn);
						app.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						app.setAction(Intent.ACTION_MAIN);
						app.addCategory(Intent.CATEGORY_LAUNCHER);

						startActivity(app);

						Log.v(TAG,"Application started");
					}
				}
			}
			else if (appType.equalsIgnoreCase("publicweb"))
			{

		//		packageName=st.nextToken();
				Log.v(TAG,"public url  --->  "+packageName);
				if(packageName!=null){
					cn =new ComponentName("com.android.browser", "com.android.browser.BrowserActivity");
					Intent app = new Intent();	
					app.setComponent(cn);
					app.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					app.setAction(Intent.ACTION_VIEW);
					
					//app.setData(Uri.parse("http://"+packageName));
					app.setData(Uri.parse(packageName));
					startActivity(app);

					Log.v(TAG,"Application started");
				}
				

			}
			else if(appType.equalsIgnoreCase("techmweb")){
			//	packageName=st.nextToken();
				Log.v(TAG,"techmweb url  --->  "+packageName);
				if(packageName!=null){
				/*cn = new ComponentName("tv.techm.techmbrowser","tv.techm.techmbrowser.TechMBrowserActivity");
				Bundle urlBundle= new Bundle();
				urlBundle.putString("url", packageName);
				Intent app = new Intent();	
				app.putExtra("url", packageName);
				app.setComponent(cn);
				app.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				app.setAction(Intent.ACTION_MAIN);
				app.addCategory(Intent.CATEGORY_LAUNCHER);

				startActivity(app);*/
				webView.loadUrl(packageName);
				}
			}
     }

 final class JavascriptBridge
	{
		public void callme()
		{
			//Generate the returnValue from the bridge
			String toastValue = "time pass";

			//Setup the Toast
			Toast toast = Toast.makeText(Launcher.this, toastValue, Toast.LENGTH_LONG);
			Log.v(TAG,"display toast");
			//Show the Toast
			toast.show();


		}
		public void invoke(String param1, String param2){
			startApplication(param1,param2);
		}
		
	}
   
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // techm:
        handleLauncherStart();

     
    }

protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent");
		 handleSystemManagerEvent(intent);
	}

    @Override
    protected void onPause() {
        super.onPause();
		//added by clei for show screen arrow
      }

  
 void startActivityForResultSafely(Intent intent, int requestCode) {
        try {
            startActivityForResult(intent, requestCode);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            Toast.makeText(this, R.string.activity_not_found, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Launcher does not have the permission to launch " + intent +
                    ". Make sure to create a MAIN intent-filter for the corresponding activity " +
                    "or use the exported attribute for this activity.", e);
        }
    }
   
 @Override
    public void startActivityForResult(Intent intent, int requestCode) {
    //    if (requestCode >= 0) mWaitingForResult = true;
        super.startActivityForResult(intent, requestCode);
    }

   


    /*************************************************************************/ 
    /* TechM Addition ********************************************************/
    /*************************************************************************/

    private static final int REQUEST_NETWORK_SETTING = REQUEST_PICK_WALLPAPER + 1;

    // FIXME: should not have these flags
    private boolean isRequestedByLauncher = false;
    private boolean isSystemManagerEventHandled = false;
    private boolean hasDialog = false;

    private Dialog mProgressBar;
    private Dialog mDialog;

    
    // Launcher is the first few application started by the system.
    // For the first time, if the device is not activated, it displays
    // the network setting.
    
    private void handleLauncherStart() {
        if (hasDialog) return;

        if (isSystemManagerEventHandled) {
            isSystemManagerEventHandled = false;
            return;
        }

        if (LOGD) {
            Log.d(TAG, "activation url=" + techmtossHelper.getActivationServerUrl());
            Log.d(TAG, "auth url=" + techmtossHelper.getAuthenticationServerUrl());
         //   Log.d(TAG, "mac addr=" + techmtossHelper.getMacAddress(this));
            Log.d(TAG, "activated=" + techmtossHelper.hasActivated(this));
            Log.d(TAG, "authenticated=" + techmtossHelper.hasAuthenticated(this));
            Log.d(TAG, "device_id=" + techmtossHelper.getDeviceId(this));       
            Log.d(TAG, "session_id=" + techmtossHelper.getSessionId(this));
        }

        // 1. Device is not activated
        if (!techmtossHelper.hasActivated(this)) {
            if (LOGD) Log.d(TAG, "activation process starts");
            // wait for 60 seconds for network connection, if the
            // network is being connected, no progress bar is shown.
            checkNetworkConnectivity(new StartActivation(), 10, true);
        } 
        // 2. Device is not authenticated
        else if (!/*techmtossHelper.isAuthenticationOfflineMode(this)*/techmtossHelper.hasAuthenticated(this)) {
            String id = techmtossHelper.getSessionId(this);
            if (TextUtils.isEmpty(id)) {
                if (LOGD) Log.d(TAG, "authentication process starts");
                checkNetworkConnectivity(new StartAuthentication(), 10, true);
            }
        }
    }

   
     // SysMgr sends events to Launcher.  Those events are captured
     // in either onCreate or onNewIntent before handleLauncherStart().
     
    private void handleSystemManagerEvent(Intent intent) {
        String event = intent.getStringExtra("event");
        Log.d(TAG, "handleSystemManagerEvent"+event);
        if (TextUtils.isEmpty(event))
            return;

	  
	 if (event.equals(Global.EVENT_AUTHENTICATE_ON_BOOT)) {
            if (LOGD) Log.d(TAG, "EVENT_AUTHENTICATE_ON_BOOT");
            if (!techmtossHelper.hasAuthenticated(this)) {
                isSystemManagerEventHandled = true;
                checkNetworkConnectivity(new StartAuthentication(), 10, true);
            }
        } else if(event.equals(Global.EVENT_AUTH_FAIL)){
        	 if (LOGD) Log.d(TAG, "faile to authenticate");
		  isSystemManagerEventHandled = true;
		  mDialog = new AuthenticationFailDialog().createDialog("Authentiation fail . PLease check for certificate files in external storage and restart the box");
            mDialog.show();


	}else if (event.equals(Global.EVENT_AUTHENTICATION_EXPIRED)) {
            if (LOGD) Log.d(TAG, "EVENT_AUTHENTICATION_EXPIRED");
            isSystemManagerEventHandled = true;
            String message = intent.getStringExtra("message");
            mDialog = new AuthenticationFailDialog().createDialog(message);
            mDialog.show();
        } else if (event.equals(Global.EVENT_DEACTIVATED)) {
            if (LOGD) Log.d(TAG, "EVENT_DEACTIVATED");
            isSystemManagerEventHandled = true;
            String message = intent.getStringExtra("message");
            mDialog = new DeactivatedDialog().createDialog(message);
            mDialog.show();
        }/*else if(event.equals(Global.EVENT_SAVE_SESSIONID)){

	    String sId = intent.getStringExtra("message");
            if (LOGD) Log.d(TAG, "Global.EVENT_SAVE_SESSIONID" + sId);
	    webViewFordb.loadUrl("javascript:setSesssionId('"+ sId +"')");
	}*/
    }

    private boolean isNetworkConnectingConnected() {
        final ConnectivityManager connMgr = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        final NetworkInfo[] infos = connMgr.getAllNetworkInfo();
        if (infos == null)
            return false;

        for (NetworkInfo info : infos) {
            NetworkInfo.State state = info.getState();
            if (state == NetworkInfo.State.CONNECTED ||
                state == NetworkInfo.State.CONNECTING) {
                return true;
            }
        }

        return false;
    }

    
     //* check the network connecitivity, if the repeat count is
     //* specified, check every 15 seconds until network is available or
     //* to the repeat count.  callback will be called in any case,
     //* callback is executed in the mail thread.
     
    private void checkNetworkConnectivity(final Runnable callback,
                                          final int repeats, 
                                          boolean hasProgressBar) {
        if (repeats <= 0) {
            if (callback != null)
                callback.run();
            return;
        }

        final ConnectivityManager connMgr = 
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        // calling callback without network should generate an error
        final NetworkInfo[] infos = connMgr.getAllNetworkInfo();
        if (infos == null) {
            if (callback != null)
                callback.run();
            return;
        }

        if (hasProgressBar)
            startProgressBar(R.string.network_progress);

        final Handler mWorker = new Handler();

        new Thread(new Runnable() {
            public void run() {
                int isRepeat = repeats;
                while(isRepeat > 0) {
                    if (LOGD)
                        Log.d(TAG, "checkNetworkConnectivity: repeats=" + isRepeat);
                    try {
                        Thread.sleep(3*1000);
                    } catch (InterruptedException e) {}
                    for (NetworkInfo info : infos) {
                        if (info.getState() == NetworkInfo.State.CONNECTED)
                            isRepeat = 0;
                    }
                    isRepeat--;
                }
                if (callback != null)
                    mWorker.post(callback);
            }
        }).start();
    }

    class StartActivation implements Runnable {
        public void run() {
            
            if (isNetworkConnectingConnected()) {
         
		requestActivation();
		//stopProgressBar();
            } else {
                startNetworkSettingActivity();
            }
        }
    }

    class StartAuthentication implements Runnable {
        public void run() {
            if (LOGD) Log.d(TAG, "startAuthentication");
            stopProgressBar();
            if (isNetworkConnectingConnected()) {
                requestAuthentication();
            } else {
                startNetworkSettingActivity();
            }
        }
    }

    private void startNetworkSettingActivity() {
        Intent setting = new Intent();
        setting.setClassName("com.android.settings", 
                             "com.android.settings.Settings");
        setting.setAction("android.settings.SETTINGS");
        startActivityForResultSafely(setting, REQUEST_NETWORK_SETTING);
    }

    private void requestStartActivityToSystemManager(Intent intent) {
        Intent app = new Intent(Global.ACTION_SYSTEM_MANAGER);
        ComponentName cn = intent.getComponent();
        app.setSourceBounds(intent.getSourceBounds());
        app.putExtra("pkg", cn.getPackageName());
        app.putExtra("cls", cn.getClassName());
	if(intent.getData()!=null){
		Log.v(TAG,"requestStartActivityToSystemManager"+intent.getData());
		app.putExtra("url",intent.getData().toString());
	}
        app.putExtra("event", Global.EVENT_START_ACTIVITY);
        startService(app);
    }

    private void requestActivation() {
  startProgressBar("System is activating....");
       Intent app = new Intent(Global.ACTION_SYSTEM_MANAGER);
        app.putExtra("event", Global.EVENT_ACTIVATE);
   
        startService(app);
  
//	handleActivate
    }


/* private void handleActivate(final Intent intent) {
        // run this in the main thread
        Handler mWorker = new Handler(Looper.getMainLooper());
        mWorker.post(new Runnable() {
                public void run() {
                    Activation activation = 
                        new Activation(SystemManagerService.this);
                    activation.activate(intent);
                }
            });
    }*/

    private void requestAuthentication() {
        if (LOGD) Log.d(TAG, "requestAuthentication==========");
       // if (LOGD) Log.d(TAG, "=========loading store file");
         webViewFordb.loadUrl(techmtossHelper.getStoreURL(this));
	stopProgressBar();
        startProgressBar(R.string.authentication_progress);
        Intent app = new Intent(Global.ACTION_SYSTEM_MANAGER);
        app.putExtra("event", Global.EVENT_AUTHENTICATE);
        startService(app);
        isRequestedByLauncher = true;
    }

    private void completeNetworkSetting() {
        if (LOGD) Log.d(TAG, "Comes back from Setting.");
        if (!techmtossHelper.hasActivated(this)) {
            checkNetworkConnectivity(new StartActivation(), 10,
                                     !isNetworkConnectingConnected());
        } else {
            checkNetworkConnectivity(new StartAuthentication(), 10, 
                                     !isNetworkConnectingConnected());
        }
    }

    private final BroadcastReceiver mSystemManagerReceiver = 
        new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
		   Log.v(TAG,"We have received some event "+action);

              if (action.equals(Global.EVENT_SAVE_SESSIONID)){
			   String sId = intent.getStringExtra("sessionId");
		    Log.v(TAG,"EVENT_SAVE_SESSIONID --- storing value to seession" + sId);
	    	    webViewFordb.loadUrl("javascript:setSesssionId('"+ sId +"')");
	    	    //webView.loadUrl("javascript:setSesssionId('"+ sId +"')");
		   }
		   	else if (action.equals(Global.ACTION_AUTHENTICATION)) {
                    isSystemManagerEventHandled = false;
                    stopProgressBar();
                    
		    setupWorkspace();
                /*    if (isRequestedByLauncher) {
                        isRequestedByLauncher = false;
                        if (techmtossHelper.hasAuthenticated(Launcher.this)) {
                            //finishBindingItems();
				setupWorkspace();
                        } else {
                            if (techmtossHelper.getAuthenticationOfflineDurationOnBoot() == 0) {
                                String msg = intent.getStringExtra("message");
                                dismissDialog();
                                mDialog = new AuthenticationFailDialog().createDialog(msg);
                                mDialog.show();
                            } else {
                                mDialog = new AuthenticationFailOfflineDialog().createDialog();
                                mDialog.show();
                                final Handler mWorker = new Handler();
                                new Thread(new Runnable() {
                                    public void run() {
                                        try {
                                            Thread.sleep(10*1000);
                                        } catch (InterruptedException e) {}
                                        mWorker.post(new DismissDialog());
                                    }
                                }).start();
                            }
                        }
                    }*/
                } else if (action.equals(Global.ACTION_ACTIVATION)) {
                    isSystemManagerEventHandled = false;
                    stopProgressBar();

             //       dismissDialog();
                    
                    if (techmtossHelper.hasActivated(Launcher.this)) {
                       
		Log.v(TAG,"File is suucessfully copied and system is activated");
		 new StartAuthentication().run();
                    } else {
                        String msg = intent.getStringExtra("message");
                        mDialog = new ActivationFailDialog().createDialog(msg);
                        mDialog.show();
                    }
                }
            }
        };

    private class TechMProgress {
        Dialog createDialog(String message) {
            ProgressDialog dialog = new ProgressDialog(Launcher.this);
            dialog.setMessage(message);
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            return dialog;
        }
    }

    private void startProgressBar(int res) {
        startProgressBar(getResources().getString(res));
    }

    private void startProgressBar(String msg) {
        dismissDialog();
        stopProgressBar();
        mProgressBar = new TechMProgress().createDialog(msg);
        mProgressBar.show();
    }

    private void stopProgressBar() {
        if (mProgressBar != null) {
            try {
                    mProgressBar.dismiss();
                } catch (Exception e) {}
            mProgressBar = null;
        }
    }

    private void dismissDialog() {
        new DismissDialog().run();
    }

    class DismissDialog implements Runnable {
        public void run() {
            if (mDialog != null) {
                try {
                    mDialog.dismiss();
                } catch (Exception e) {}
                mDialog = null;
            }
        }
    }

  
    private class AuthenticationFailDialog {
        Dialog createDialog(String message) {
            TextDialog d = new TextDialog();
            DialogInterface.OnClickListener okListener = 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new StartAuthentication().run();
                    }
                };
            DialogInterface.OnClickListener cancelListener = 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                      //  startNetworkSettingActivity();
                    }
                };
            return d.create(false, 
                            android.R.drawable.ic_dialog_alert,
                            R.string.authentication_fail_title, null,
                            0, message,
                            true, R.string.authentication_fail_ok, null, okListener,
                            false, 0, null, null,
                            true, R.string.authentication_fail_no, null, cancelListener);
        }
    }

    private class ActivationFailDialog {
        Dialog createDialog(String message) {
            TextDialog d = new TextDialog();
            DialogInterface.OnClickListener okListener = 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        checkNetworkConnectivity(new StartActivation(), 
                                                 10, true);
                    }
                };
            DialogInterface.OnClickListener cancelListener = 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                     //   startNetworkSettingActivity();
                    }
                };
            return d.create(false, 
                            android.R.drawable.ic_dialog_alert,
                            R.string.activation_title, null,
                            0, message,
                            true, R.string.activation_fail_ok, null, okListener,
                            false, 0, null, null,
                            true, R.string.activation_fail_no, null, cancelListener);
        }
    }

    private class AuthenticationFailOfflineDialog {
        Dialog createDialog() {
            TextDialog d = new TextDialog();
            DialogInterface.OnClickListener okListener = 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                };
            return d.create(false, 
                            android.R.drawable.ic_dialog_alert,
                            R.string.authentication_title, null,
                            R.string.authentication_offline_string, null,
                            true, R.string.authentication_offline_ok, null, okListener,
                            false, 0, null, null,
                            false, 0, null, null);
        }
    }

    private class ActivationSuccessDialog {
        Dialog createDialog(String message) {
            TextDialog d = new TextDialog();
            DialogInterface.OnClickListener okListener = 
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new StartAuthentication().run();
                    }
                };
            return d.create(false, 
                            android.R.drawable.ic_menu_more,
                            0, message,
                            0, null,
                            true, R.string.activation_success_ok, null, okListener,
                            false, 0, null, null,
                            false, 0, null, null);
        }
    }

    private class DeactivatedDialog {
        Dialog createDialog(String message) {
            TextDialog d = new TextDialog();
            return d.create(false, 
                            android.R.drawable.ic_dialog_alert,
                            R.string.deactivation_title, null,
                            0, message,
                            false, 0, null, null,
                            false, 0, null, null,
                            false, 0, null, null);
        }
    }

    private class TextDialog {
        Dialog create(boolean cancelable,
                      int icon, 
                      int titleResource,
                      String titleString, 
                      int messageResource, 
                      String messageString, 
                      boolean hasPositiveButton,
                      int positiveButtonResource, 
                      String positiveButtonLabel, 
                      DialogInterface.OnClickListener positveListener,
                      boolean hasNeutralButton,
                      int neutralButtonResource, 
                      String neutralButtonLabel, 
                      DialogInterface.OnClickListener neutralListener,
                      boolean hasNegativeButton,
                      int negativeButtonResource, 
                      String negativeButtonLabel, 
                      DialogInterface.OnClickListener negativelistener) {

            final View layout = 
                View.inflate(Launcher.this, R.layout.text_dialog, null);
            final TextView text = 
                (TextView) layout.findViewById(R.id.dialog_textview);

            if (messageResource > 0)
                messageString = getResources().getString(messageResource);
            text.setText(messageString);

            AlertDialog.Builder builder = new AlertDialog.Builder(Launcher.this);
            builder.setView(layout);
            builder.setCancelable(cancelable);

            if (icon > 0)
                builder.setIcon(icon);
            if (titleResource > 0)
                builder.setTitle(titleResource);
            else if (!TextUtils.isEmpty(titleString))
                builder.setTitle(titleString);

            if (hasPositiveButton) {
                if (positiveButtonResource > 0)
                    builder.setPositiveButton(positiveButtonResource, 
                                              positveListener);
                else
                    builder.setPositiveButton(positiveButtonLabel, 
                                              positveListener);
            }
            if (hasNeutralButton) {
                if (neutralButtonResource > 0)
                    builder.setNeutralButton(neutralButtonResource, 
                                             neutralListener);
                else
                    builder.setNeutralButton(neutralButtonLabel, 
                                             neutralListener);
            }
            if (hasNegativeButton) {
                if (negativeButtonResource > 0)
                    builder.setNegativeButton(negativeButtonResource, 
                                              negativelistener);
                else
                    builder.setNegativeButton(negativeButtonLabel, 
                                              negativelistener);
            }

            Dialog dialog = builder.create();

            dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                public void onShow(DialogInterface dialog) {
                    stopProgressBar();
                    hasDialog = true;
                }
            });
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    hasDialog = false;
                }
            });

            return dialog;
        }
    }
}
