package com.android.launcher3;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

import tv.techm.dbproviderhelper.techmtossHelper;
import tv.techm.dbproviderhelper.techmtossUtils;
/*import tv.techm.crypto.CryptoManager;
import tv.techm.rest.service.RestClient;
import tv.techm.rest.service.RestClientListener;
import tv.techm.rest.service.RestService;
import tv.techm.rest.obj.RestRequest;
import tv.techm.rest.obj.RestResponse;
import tv.techm.rest.util.Base64;
import tv.techm.rest.util.Base64DecoderException;*/
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import android.os.Environment;

class Activation {
    private static final boolean DBG = true;
    private static final String TAG = "sysmgr";

    private Callback mCallback;
    private Context mContext;
  //  private RestClient rest;
    private final static String clientBaseFile = "client.p12";
    private final static String serverBaseFile = "myserver";

    public interface Callback {
        public void onActivationFinished(boolean status, String message, 
                                         int errorCode);
    }

    class ResponseStatus {
        boolean status   = false; // true for success
        String message   = null;  // messsage string
        int errorCode    = 0;     // payload error code 
    };


    Activation(SystemManagerService c) {
        mCallback = (Callback) c;
        mContext = (Context) c;
     
    }

    // FIXME: exception handling
    void activate(Intent intent) {
       


  final Resources res = mContext.getResources();
//  TODO : Find the Certificate file in the sd card.... store it in the TechmLaunchers local store.

	

       

	  copyCerts();

       
	
	
	final ResponseStatus status = new ResponseStatus();
	//if( new File("/data/data/tv.techm.systemmanager/files/client").exists())
	if( new File("/data/data/com.android.launcher3/files/client").length()>0 || new File("/data/data/com.android.launcher3/files/server").length()>0)
	{
		Log.v(TAG, "certificate files are copied");
		status.status = true;
		status.message =res.getString(R.string.activation_success_message);
		status.errorCode= 0;
		HashMap<String, String> list = new HashMap<String, String>();
		list.put("shared_key", "true");
		techmtossHelper.setActivationData(mContext, list); 
			Log.v(TAG, "activation status : "+status.status +" message "+status.message +" error code "+ status.errorCode);
	
			 mCallback.onActivationFinished(status.status, status.message, status.errorCode);

	}
	else
	{
	Log.v(TAG, "certificate files are failed to get copied");

	 new Thread(new Runnable() {
            public void run() {
                int isRepeat = 10;
		boolean discovered = false;
                while(isRepeat > 0) {
                   
                        Log.d(TAG, "check for usb connection : repeats=" + isRepeat);
                    try {
                        Thread.sleep(3*1000);
                    } catch (InterruptedException e) {}
                  
                        if (new File("/mnt/sda/sda1"+ "/" + serverBaseFile).length()>0 && new File("/mnt/sda/sda1"+ "/" + clientBaseFile).length()>0){
                            isRepeat = 0;
			    discovered = true;
			}
                   
                    isRepeat--;
                }
                if (discovered){
			Log.v(TAG,"discovred");
			status.status = true;
			status.message =res.getString(R.string.activation_success_message);
			status.errorCode= 0;
			HashMap<String, String> list = new HashMap<String, String>();
			list.put("shared_key", "true");
			techmtossHelper.setActivationData(mContext, list); 
			
			copyCerts();
			


		}                   
		else{
			Log.v(TAG,"could not discovred");
			status.status = false;
			status.message =res.getString(R.string.activation_fail_message);
			status.errorCode= 1;
		}
			Log.v(TAG, "activation status : "+status.status +" message "+status.message +" error code "+ status.errorCode);
	
			 mCallback.onActivationFinished(status.status, status.message, status.errorCode);
            }
        }).start();

		
	}
	
	 
	  
    }
private void copyCerts() {
 InputStream in = null;
	 OutputStream out = null;

	 InputStream inserver = null;
	 OutputStream outserver = null;
 try {
	 File f = null;
	   if(new File(Environment.getExternalStorageDirectory()+ "/" + clientBaseFile).length()>0)
	   	f = new File(Environment.getExternalStorageDirectory()+ 
                              "/" + clientBaseFile);

	    else if(new File("/mnt/sda/sda1"+ "/" + clientBaseFile).length()>0)
            	 f = new File("/mnt/sda/sda1"+ 
	                              "/" + clientBaseFile);
	 else
		f= new  File(Environment.getExternalStorageDirectory()+ "/Download" + 
	                              "/" + clientBaseFile);
	
 	  if(f.exists()){
Log.d(TAG,"clientbasefile not null");
            in = new FileInputStream(f);
	    out =  mContext.openFileOutput("client",0);
            copyFile(in, out);
             in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
	   }

	     File server = null;
	     if( new File(Environment.getExternalStorageDirectory()+ "/" + serverBaseFile).length()>0)

		server  = new File(Environment.getExternalStorageDirectory()+ 
                              "/" + serverBaseFile);
	     else  if(new File("/mnt/sda/sda1"+ "/" + serverBaseFile).length()>0)
		server= new File("/mnt/sda/sda1"+ "/" + serverBaseFile);
	     else
		server= new  File(Environment.getExternalStorageDirectory()+ "/Download" + 
	                              "/" + serverBaseFile);
		
		if(server.exists()){
Log.d(TAG,"serverbasefile not null");
            inserver = new FileInputStream(server);
	    outserver =  mContext.openFileOutput("server",0);
            copyFile(inserver, outserver);
             inserver.close();
            inserver = null;
            outserver.flush();
            outserver.close();
            outserver = null;
	}
 } catch (FileNotFoundException e) {
		e.printStackTrace();
        } catch (IOException e){
		Log.v(TAG,"IOException");
		e.printStackTrace();
        }
}
private void copyFile(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[1024];
    int read;
    while((read = in.read(buffer)) != -1){
      out.write(buffer, 0, read);
    }
}


 


    static void disable(Context c) {
       // techmtossHelper.removeSessionId(c);
	//TODO : Something needs to be figured out
    }

    static void deactivate(Context c) {
        //techmtossUtils.deleteTable(c, "device");
	//TODO : Something needs to be figured out
    }
}
