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




import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import android.os.AsyncTask;



class Authentication {
    private static final boolean DBG = false;
    private static final String TAG = "sysmgr";

    private Callback mCallback;
    private Context mContext;
   // private RestClient rest;
    private SSLSocketFactory tsocketFactory = null;

    public interface Callback {
        public void onAuthenticationFinished(boolean status, String message,
                                             int errorCode, int errorElapsed);
    }

    class ResponseStatus {
        boolean status   = false; // true for success
        String message   = null;  // messsage string
        int errorCode    = 0;     // payload error code 
        int errorElapsed = 0;     // error time elapsed
    };

    Authentication(SystemManagerService c) {
        mCallback = (Callback) c;
        mContext = (Context) c;
      
    }

    // issues a rest call for authentication, if the session id is
    // available, use the session id as an id, otherwise use the
    // device id.
    void authenticate(Intent intent) {
 
      
//Snehal : Authenticate using the cerificate.

	loadCertificateData();

	new RetreiveFeedTask().execute();

	
    }

	  class RetreiveFeedTask extends AsyncTask<Void, Void, HttpResponse> {

		private Exception exception;

		protected HttpResponse doInBackground(Void... params) {
		    try {
			HttpResponse response= ConneectUrl();
			return response;

	} catch (Exception e) {
		        this.exception = e;
		        return null;
		    }
		}

		protected void onPostExecute(HttpResponse response) {
		    // TODO: check this.exception 
		    // TODO: do something with the feed
 Resources res = mContext.getResources();
		ResponseStatus status = new ResponseStatus();
		int statusCode = 0; 
		if(response!=null)
			statusCode = response.getStatusLine().getStatusCode();
		Log.v(TAG,"  authentication response status"+statusCode);
		if(statusCode == 200)
		{
			status.status = true;
			status.message =res.getString(R.string.authentication_success_message);
			status.errorCode= 0;
			 

		}
		else
		{
			status.status = false;
			status.message =res.getString(R.string.authentication_fail_message);
			status.errorCode= Global.ECODE_NOT_AUTHENTICATED;
		}
	  mCallback.onAuthenticationFinished(status.status, status.message,
		                                       status.errorCode,
		                                       status.errorElapsed);
		}
	     }


private void loadCertificateData() {
        try {
            
            InputStream certificateStream = null, scertificate = null;
             //certificateStream = new FileInputStream(pfxFiles[0]);
            certificateStream = mContext.openFileInput("client");
                   

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            char[] password = "techmtoss".toCharArray();
            keyStore.load(certificateStream, password);

            Log.v(TAG,"I have loaded [" + keyStore.size() + "] certificates");

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, password);
            
            /* test stub */
            scertificate =  mContext.openFileInput("server");
                   //mContext.getResources().openRawResource(R.raw.myserver);
            KeyStore trustStore = KeyStore.getInstance("BKS");
            Log.e("CERTIFIC", "loading trust store ------------");
            trustStore.load(scertificate,"secret".toCharArray());
            Log.e("CERTIFIC", "loading trust after store ------------");


            Log.v(TAG,"I have loaded trust [" + trustStore.size() + "] certificates");

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

                        /*end of test stub */

            //tsocketFactory = new SSLSocketFactory(keyStore);
            tsocketFactory = new SSLSocketFactory(keyStore, new String(password), trustStore);
            //tsocketFactory = new TrustAllSSLSocketFactory(keyStore);
            } catch (Exception ex) {
        	Log.v(TAG,"Caught exception");
            }
    }
    
    
    private HttpResponse ConneectUrl() {
    	
    	HttpParams params = new BasicHttpParams();
    	//SSLSocketFactory tsocketFactory = new  TrustAllSSLSocketFactory();
		
        HttpConnectionParams .setConnectionTimeout(params, 10000);
        HttpResponse response = null;
 	    SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        try{
            schReg.register(new Scheme("https", tsocketFactory, 443));
          
       // schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
  	    Log.v(TAG,"execute req ");
 	    HttpClient httpclient = new DefaultHttpClient(conMgr, params);
        // httpclient.getConnectionManager().getSchemeRegistry().register(schReg);  
         HttpGet httpget = new HttpGet(techmtossHelper.getAuthenticationServerUrl());  
         Log.v(TAG,"executing request" + httpget.getRequestLine());  
        
         	int status=0;
	
			response = httpclient.execute(httpget);
			 
			status = response.getStatusLine().getStatusCode();
	         Log.v(TAG,"----------------------------------------");  
	         Log.v(TAG,""+response.getStatusLine());
	         parseResponse(response);
	         httpclient.getConnectionManager().shutdown(); 
		} catch (ClientProtocolException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}  
       
       
        	    
       			
				
			
         // When HttpClient instance is no longer needed,  
         // shut down the connection manager to ensure  
         // immediate deallocation of all system resources  
        
    	return response;
    }

    


    

    private ResponseStatus parseResponse(HttpResponse response) {
        ResponseStatus status = new ResponseStatus();
	
            int statusCode = response.getStatusLine().getStatusCode();
            if ( statusCode == 200) {
                try {
                    save(response);
                    status.status = true;
                    return status;
                } catch (Exception e) {
                    // something goes wrong while parsing the response
                    Log.e(TAG, "parseResponse", e);
                }
           
        } else {
         //   String message = response.getErrorMessage();
	//TDB: if authenication fails and all applications need to be disabled	
	    status.errorCode = 0;
           
        }

        status.errorElapsed = saveError(status.errorCode);

        return status;
    }

    // save the error into the database and return the time elapsed
    // from the last error
    private int saveError(int errorCode) {
	Log.v(TAG,"Save error   :  "+ errorCode  );
        // on the error, remove the current session id
        techmtossHelper.removeSessionId(mContext);

        // get the current error count
        int count = techmtossHelper.getAuthenticationErrorCount(mContext);

        // increments the count
        count++;

        // get the error time elapsed
        int elapsed = techmtossHelper.getAuthenticationErrorElapsed(mContext);

        // set the last timezone
        HashMap<String, String> list = new HashMap<String, String>();
        // do not update timestamp if the error time is elapsed
        if (elapsed == 0)
            list.put("error_timestamp", 
                     Long.toString(System.currentTimeMillis()/1000L));
        list.put("error_count", Integer.toString(count));
        list.put("error_code", Integer.toString(errorCode));
        techmtossHelper.setAuthenticationData(mContext, list);

        return elapsed;
    }

    // save the response into the database
    private void save(HttpResponse response) throws Exception {

	HttpEntity entity = response.getEntity();  
	String serverResponse = new String (EntityUtils.toString(entity));  
	
        JSONObject envelope = new JSONObject(serverResponse);
        Log.v(TAG,"Content is : " + serverResponse);  
        // session id must be one of the member
        String session_id = envelope.getString("id");

        // payload
      //  JSONObject payload = new JSONObject(envelope.getString("payload"));
        JSONObject data = envelope.getJSONObject("data");
	Log.v(TAG,"   save   data  :  "+data.toString());
        String timezone = null;
        try {
            timezone = data.getString("timezone");
        } catch (JSONException e) {
        }
        String woe_id = null;
        try {
            woe_id = data.getString("woe_id");
        } catch (JSONException e) {
        }
        String firmware_version = null;
        try {
            firmware_version = data.getString("firmware_version");
        } catch (JSONException e) {
        }
	String homescreenVersion= null;
	int currentVersion =0;
	int newVersion = 0;
	String downloadHomescreen= null;
	
          
		/*currentVersion = techmtossHelper.getHomeScreenVersion(mContext);
		try{
			newVersion = data.getInt("homescreen_version");
		} catch (JSONException e) {
	            }
		
		homescreenVersion=Integer.toString(newVersion);
		if(newVersion!=0 && currentVersion!=newVersion)
			downloadHomescreen = "YES";
		else
			downloadHomescreen = "NO";
*/
Log.v("currentVersion"+ currentVersion,"newVersion : "+newVersion);
	//Log.v("Authentication    :     ",""+data.getInt("homescreen_version"));
	

        JSONObject url = null;
        try {
            url = data.getJSONObject("url");
        } catch (JSONException e) {
        }
        String youtube_url = null;
        String webchannel_url = null;
	String logger_url = null;
	String stats_url = null;
	String imageserver_url=null;
	String addserver_url = null;
	String homescreen_url = null;
	String homescreen_web_url = null;
	String vod_url = null;
	String store_url = null;
	String swupgrade_url = null;
        if (url != null) {
            try {
                youtube_url = url.getString("youtube");
            } catch (JSONException e) {
            }
            try {
                webchannel_url = url.getString("web_channel");
            } catch (JSONException e) {
            }
            try {
                logger_url = url.getString("secure_auth");
            } catch (JSONException e) {
            }
	    try {
                stats_url = url.getString("statsservices");
            } catch (JSONException e) {
            }
	    try {
                imageserver_url = url.getString("image-server");
            } catch (JSONException e) {
            }
	    try {
                addserver_url = url.getString("adv_server");
            } catch (JSONException e) {
            }
	    try {
                homescreen_url = url.getString("homescreen_url");
		Log.v("homescreen_url",""+homescreen_url);
            } catch (JSONException e) {
            }
 	    try {
                homescreen_web_url = url.getString("homescreen_web_url");
		Log.v("homescreen_web_url",""+homescreen_url);
            } catch (JSONException e) {
            }
	     try {
                vod_url = url.getString("vod_url");
		Log.v("vod",""+vod_url);
            } catch (JSONException e) {
            }
	     try {
             store_url = url.getString("store_url");
		Log.v("store_url",""+store_url);
         } catch (JSONException e) {
         }
	     try {
             swupgrade_url = url.getString("swupgrade_url");
		Log.v("swupgrade_url",""+swupgrade_url);
         } catch (JSONException e) {
         }
        }

        if (DBG)
            Log.d(TAG, "session_id=" + session_id);

        HashMap<String, String> list = new HashMap<String, String>();
        list.put("error_code", Integer.toString(0));
        list.put("error_count", Integer.toString(0));
        list.put("error_timestamp", Integer.toString(0));
        list.put("timestamp", Long.toString(System.currentTimeMillis()/1000L));
        if (session_id != null && !TextUtils.isEmpty(session_id))
            list.put("session_id", session_id);
        if (timezone != null && !TextUtils.isEmpty(timezone))
            list.put("timezone", timezone);
        if (woe_id != null && !TextUtils.isEmpty(woe_id))
            list.put("woe_id", woe_id);
        if (firmware_version != null && !TextUtils.isEmpty(firmware_version))
            list.put("firmware_version", firmware_version);
        if (youtube_url != null && !TextUtils.isEmpty(youtube_url))
            list.put("youtube_url", youtube_url);
        if (webchannel_url != null && !TextUtils.isEmpty(webchannel_url))
            list.put("webchannel_url", webchannel_url);
        if (logger_url != null && !TextUtils.isEmpty(logger_url))
            list.put("secure_auth", logger_url);
        if (stats_url != null && !TextUtils.isEmpty(stats_url))
            list.put("stats", stats_url);
	if (imageserver_url != null && !TextUtils.isEmpty(imageserver_url))
            list.put("imageserver_url", imageserver_url);
//	if (homescreenVersion != null && !TextUtils.isEmpty(homescreenVersion))
  //          list.put("homescreen_version", homescreenVersion);
	if (downloadHomescreen != null && !TextUtils.isEmpty(downloadHomescreen))
            list.put("downloadHomescreen",downloadHomescreen);
	if (homescreenVersion != null && !TextUtils.isEmpty(homescreenVersion))
            list.put("Newhomescreen_version", homescreenVersion);

	if (addserver_url != null && !TextUtils.isEmpty(addserver_url))
            list.put("add_server", addserver_url);
	if (homescreen_url != null && !TextUtils.isEmpty(homescreen_url))
            list.put("homescreen_url", homescreen_url);
	if (homescreen_web_url != null && !TextUtils.isEmpty(homescreen_web_url))
            list.put("homescreen_web_url", homescreen_web_url);
	if (vod_url != null && !TextUtils.isEmpty(vod_url))
            list.put("vod_url", vod_url);
	if (store_url != null && !TextUtils.isEmpty(store_url))
        list.put("store_url", store_url);
	if (swupgrade_url != null && !TextUtils.isEmpty(swupgrade_url))
        list.put("swupgrade_url", swupgrade_url);

	//We will use shared preferences
	 
	/* SharedPreferences authData = getSharedPreferences("AuthData", 0);
      SharedPreferences.Editor editor = authData.edit();
     

      
	for (Map.Entry<String, String> entry : list.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
		 editor.putString(key,value);
       	}	
		// Commit the edits!
      editor.commit();			*/
        techmtossHelper.setAuthenticationData(mContext, list);
    }
}
