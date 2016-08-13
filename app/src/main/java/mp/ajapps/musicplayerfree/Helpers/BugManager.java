package mp.ajapps.musicplayerfree.Helpers;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;


public class BugManager {
    private static final String DEFAULT_URL = "https://bbr.16mb.com/public/";
    private static String mToken;
    private static Context mContext;
    private static String mAppVersionCode = "";
    private static String mDevice;
    private static BugManager mInstance;
    private static String mOs;

    protected BugManager(Context context, String token) {
        this.mContext = context.getApplicationContext();
        this.mToken = token;

        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            mAppVersionCode = info.versionCode + "(" + info.versionName + ")";
        } catch (PackageManager.NameNotFoundException se) {
        }

        mOs = Build.VERSION.RELEASE;
        mDevice = Build.MANUFACTURER + " " + Build.MODEL + "(" + Build.DEVICE + ")";
    }

    public static BugManager getInstance(Context context, String token) {
        if (mInstance == null) {
            mInstance = new BugManager(context, token);
        }
        return mInstance;
    }

    public static void reportLog(String log) {
        final String logs = log;
     /*   JsonObjectRequest mRequest = new JsonObjectRequest(Request.Method.POST, DEFAULT_URL + "/logs", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> mData = new HashMap<String, String>();
                mData.put("access_token", mToken);
                mData.put("device", mDevice);
                mData.put("android_version", mOs);
                mData.put("log_details", logs);
                mData.put("app_ver", mAppVersionCode);
                return mData;
            }
        };
        Volley.newRequestQueue(mContext).add(mRequest);*/
    }

    public static void reportBug(String bug, String class_name, String tag) {
        final String bugs = bug;
        final String className = class_name == null ? "" : class_name;
        final String tags = tag == null ? "" : tag;

      /*  JsonObjectRequest mRequest = new JsonObjectRequest(Request.Method.POST, DEFAULT_URL + "/bugs", null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> mData = new HashMap<String, String>();
                mData.put("access_token", mToken);
                mData.put("device", mDevice);
                mData.put("android_version", mOs);
                mData.put("bug_details", bugs);
                mData.put("app_ver", mAppVersionCode);
                mData.put("class_name", className);
                mData.put("tag", tags);
                return mData;
            }
        };

        Volley.newRequestQueue(mContext).add(mRequest);*/
    }


}
