package com.android.launcher3;

/**
 * Global static variables referenced by different processes.
 */
public interface Global {

    /*************************************************************************/
    /** ACTION ****************************************************************/
    /*************************************************************************/

    public static final String ACTION_ACTIVATION = "com.android.launcher3.ACTIVATED";
    public static final String ACTION_AUTHENTICATION = "com.android.launcher3.AUTHENTICATED";
    public static final String ACTION_CONFIG_PROVIDER = "tv.techm.content.provider.ConfigReceiver";
    public static final String ACTION_SYSTEM_MANAGER = "com.android.launcher3.SystemManagerService";
    public static final String ACTION_STATSCOLLECTOR = "tv.techm.stats.StatsService";
   
   
   
    /*************************************************************************/
    /** PERMISSION ***********************************************************/
    /*************************************************************************/

    public static final String PERMISSION_SYSTEM_MANAGER = "com.android.launcher3.permission.RECEIVE";

    /*************************************************************************/
    /** EVENT ****************************************************************/
    /*************************************************************************/

    public static final String EVENT_START_ACTIVITY = "tv.techm.sm.START_ACTIVITY";
    public static final String EVENT_ACTIVATE = "tv.techm.sm.ACTIVATE";
    public static final String EVENT_DISABLED = "tv.techm.sm.DISABLED";
    public static final String EVENT_DEACTIVATED = "tv.techm.sm.DEACTIVATED";
    public static final String EVENT_AUTHENTICATE = "tv.techm.sm.AUTHENTICATE";
    public static final String EVENT_SETUP_LAUNCHER = "tv.techm.sm.SETUP_LAUNCHER";
    public static final String EVENT_CONFIG_PROVIDER = "tv.techm.sm.CONFIG_PROVIDER";
    public static final String EVENT_AUTHENTICATE_ON_BOOT = "tv.techm.sm.AUTHENTICATE_ON_BOOT";
    public static final String EVENT_AUTHENTICATION_EXPIRED = "tv.techm.sm.AUTHENTICATION_EXPIRED";
    public static final String EVENT_SAVE_SESSIONID = "tv.techm.sm.SAVE_SESSIONID";
     public static final String EVENT_AUTH_FAIL = "tv.techm.sm.AUTH_FAIL";
    /*************************************************************************/
    /** REST CLIENT ERRORS ****************************************************/
    /*************************************************************************/

    public static final int RC_ERR_NONE                  	= 0; // success
    public static final int RC_ERR_UNKNOWN               	= 1;
    public static final int RC_ERR_PROTOCOL_CONSTRUCT    	= 2;
    public static final int RC_ERR_PROTOCOL_DECONSTRUCT  	= 3;
    public static final int RC_ERR_NETWORK               	= 4;
    public static final int RC_ERR_RESTCLIENT            	= 5;
    public static final int RC_ERR_CONCURRENT            	= 6;
    public static final int RC_ERR_RESTCALL              	= 7;

    /*************************************************************************/
    /** REST ERROR CODES *****************************************************/
    /*************************************************************************/

    public static final int ECODE_DEVICE_DEACTIVATED 		= 5006;
    public static final int ECODE_DEVICE_DISABLED 		= 5007;
    public static final int ECODE_INVALID_DEVICEID              = 5008;
     public static final int ECODE_NOT_AUTHENTICATED              = 5009;

    public static final String EVENT_LOG_STATS = "tv.techm.stats.logstats";
    

 }
