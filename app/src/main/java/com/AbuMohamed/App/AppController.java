package com.AbuMohamed.App;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.CookieHandler;
import java.net.CookieManager;

public class AppController extends Application {
    public static final String TAG = "DataParsing";
    private RequestQueue requestQueue;
    private static AppController mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        CookieHandler.setDefault(new CookieManager());

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                File crashFile = new File(getExternalFilesDir(null), "crash_log.txt");
                PrintWriter pw = new PrintWriter(new FileWriter(crashFile, false));
                pw.println("CRASH: " + throwable.toString());
                pw.println("Cause: " + (throwable.getCause() != null ? throwable.getCause().toString() : "none"));
                for (StackTraceElement el : throwable.getStackTrace()) {
                    pw.println("  at " + el.toString());
                }
                pw.flush();
                pw.close();

                new android.os.Handler(android.os.Looper.getMainLooper()).post(() ->
                    Toast.makeText(getApplicationContext(),
                        "CRASH: " + throwable.getMessage(), Toast.LENGTH_LONG).show()
                );
                Thread.sleep(3000);
            } catch (Exception e) {
                e.printStackTrace();
            }
            android.os.Process.killProcess(android.os.Process.myPid());
        });
    }

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequest(Request<T> request, String tag) {
        request.setTag(TextUtils.isEmpty(tag) ? TAG : tag);
        getRequestQueue().add(request);
    }

    public <T> void addToRequest(Request<T> request) {
        getRequestQueue().add(request);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public void cancellPendingRequesat(Object tag) {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag);
        }
    }

    public void clearApplicationData() {
        File cache = getCacheDir();
        File appDir = new File(cache.getParent());
        if (appDir.exists()) {
            String[] children = appDir.list();
            for (String s : children) {
                if (!s.equals("lib")) {
                    deleteDir(new File(appDir, s));
                    Log.i("TAG", "File /data/data/APP_PACKAGE/" + s + " DELETED");
                }
            }
        }
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) return false;
            }
        }
        return dir.delete();
    }
}
