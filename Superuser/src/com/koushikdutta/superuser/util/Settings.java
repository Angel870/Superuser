package com.koushikdutta.superuser.util;

import java.security.MessageDigest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Base64;

import com.koushikdutta.superuser.R;

public class Settings {
    SQLiteDatabase mDatabase;
    Context mContext;

    private Settings(Context context) {
        mContext = context;
        SQLiteOpenHelper helper = new SQLiteOpenHelper(mContext, "settings.db", null, 1) {
            private final static String mDDL = "CREATE TABLE settings (key TEXT PRIMARY KEY, value TEXT);";

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                onCreate(db);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(mDDL);
            }
        };
        mDatabase = helper.getWritableDatabase();
    }

    private static Settings mInstance;

    public static Settings getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Settings(context.getApplicationContext());
        }
        return mInstance;
    }

    public void setString(String name, String value) {
        ContentValues cv = new ContentValues();
        cv.put("key", name);
        cv.put("value", value);
        mDatabase.replace("settings", null, cv);
    }

    public String getString(String name) {
        return getString(name, null);
    }

    public String getString(String name, String defaultValue) {
        Cursor cursor = mDatabase.query("settings", new String[] { "value" }, "key='" + name + "'", null, null, null, null);
        try {
            if (cursor.moveToNext())
                return cursor.getString(0);
        }
        finally {
            cursor.close();
        }
        return defaultValue;
    }

    public void setInt(String name, int value) {
        setString(name, ((Integer) value).toString());
    }

    public int getInt(String name, int defaultValue) {
        try {
            return Integer.parseInt(getString(name, null));
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }

    public void setLong(String name, long value) {
        setString(name, ((Long) value).toString());
    }

    public long getLong(String name, long defaultValue) {
        try {
            return Long.parseLong(getString(name, null));
        }
        catch (Exception ex) {
            return defaultValue;
        }
    }

    public void setBoolean(String name, boolean value) {
        setString(name, ((Boolean) value).toString());
    }

    public boolean getBoolean(String name, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(getString(name, ((Boolean) defaultValue).toString()));
        }
        catch (Exception ex) {
            ex.printStackTrace();
            return defaultValue;
        }
    }
    
    private static final String KEY_LOGGING = "logging";
    public static boolean getLogging(Context context) {
        return getInstance(context).getBoolean(KEY_LOGGING, true);
    }
    
    public static void setLogging(Context context, boolean logging) {
        getInstance(context).setBoolean(KEY_LOGGING, logging);
    }
    
    private static final String KEY_TIMEOUT = "timeout";
    public static final int REQUEST_TIMEOUT_DEFAULT = 30;
    public static int getRequestTimeout(Context context) {
        return getInstance(context).getInt(KEY_TIMEOUT, REQUEST_TIMEOUT_DEFAULT);
    }
    
    public static void setTimeout(Context context, int timeout) {
        getInstance(context).setInt(KEY_TIMEOUT, timeout);
    }

    private static final String KEY_NOTIFICATION = "notification";
    public static final int NOTIFICATION_TYPE_NONE = 0;
    public static final int NOTIFICATION_TYPE_TOAST = 1;
    public static final int NOTIFICATION_TYPE_NOTIFICATION = 2;
    public static final int NOTIFICATION_TYPE_DEFAULT = NOTIFICATION_TYPE_TOAST;
    public static int getNotificationType(Context context) {
        switch (getInstance(context).getInt(KEY_NOTIFICATION, NOTIFICATION_TYPE_DEFAULT)) {
        case NOTIFICATION_TYPE_NONE:
            return NOTIFICATION_TYPE_NONE;
        case NOTIFICATION_TYPE_NOTIFICATION:
            return NOTIFICATION_TYPE_NOTIFICATION;
        case NOTIFICATION_TYPE_TOAST:
            return NOTIFICATION_TYPE_TOAST;
        default:
            return NOTIFICATION_TYPE_DEFAULT;
        }
    }
    
    public static void setNotificationType(Context context, int notification) {
        getInstance(context).setInt(KEY_NOTIFICATION, notification);
    }
    
    public static final String KEY_PIN = "pin";
    public static final boolean isPinProtected(Context context) {
        return Settings.getInstance(context).getString(KEY_PIN) != null;
    }
    
    private static String digest(String value) {
        // ok, there's honestly no point in digesting the pin.
        // if someone gets a hold of the hash, there's really only like
        // 10^n possible values to brute force, where N is generally
        // 4. Ie, 10000. Yay, security theater. This really ought
        // to be a password.
        if (value == null || value.length() == 0)
            return null;
        try {
            MessageDigest digester = MessageDigest.getInstance("MD5");
            return Base64.encodeToString(digester.digest(value.getBytes()), Base64.DEFAULT);
        }
        catch (Exception e) {
            return value;
        }
    }
    
    public static void setPin(Context context, String pin) {
        Settings.getInstance(context).setString(KEY_PIN, digest(pin));
    }
    
    public static boolean checkPin(Context context, String pin) {
        return digest(pin).equals(Settings.getInstance(context).getString(KEY_PIN));
    }

    private static final String KEY_REQUIRE_PREMISSION = "require_permission";
    public static boolean getRequirePermission(Context context) {
        return getInstance(context).getBoolean(KEY_REQUIRE_PREMISSION, false);
    }
    
    public static void setRequirePermission(Context context, boolean require) {
        getInstance(context).setBoolean(KEY_REQUIRE_PREMISSION, require);
    }
    
    private static final String KEY_AUTOMATIC_RESPONSE = "automatic_response";
    public static final int AUTOMATIC_RESPONSE_PROMPT = 0;
    public static final int AUTOMATIC_RESPONSE_ALLOW = 1;
    public static final int AUTOMATIC_RESPONSE_DENY = 2;
    public static final int AUTOMATIC_RESPONSE_DEFAULT = AUTOMATIC_RESPONSE_PROMPT;
    public static int getAutomaticResponse(Context context) {
        switch (getInstance(context).getInt(KEY_AUTOMATIC_RESPONSE, AUTOMATIC_RESPONSE_DEFAULT)) {
        case AUTOMATIC_RESPONSE_ALLOW:
            return AUTOMATIC_RESPONSE_ALLOW;
        case AUTOMATIC_RESPONSE_PROMPT:
            return AUTOMATIC_RESPONSE_PROMPT;
        case AUTOMATIC_RESPONSE_DENY:
            return AUTOMATIC_RESPONSE_DENY;
        default:
            return AUTOMATIC_RESPONSE_DEFAULT;
        }
    }

    public static String getAutomaticResponseName(Context context) {
        switch (getInstance(context).getInt(KEY_AUTOMATIC_RESPONSE, AUTOMATIC_RESPONSE_DEFAULT)) {
        case AUTOMATIC_RESPONSE_ALLOW:
            return context.getString(R.string.allow);
        case AUTOMATIC_RESPONSE_PROMPT:
            return context.getString(R.string.prompt);
        case AUTOMATIC_RESPONSE_DENY:
            return context.getString(R.string.deny);
        default:
            return context.getString(R.string.prompt);
        }
    }
    
    public static void setAutomaticResponse(Context context, int response) {
        getInstance(context).setInt(KEY_AUTOMATIC_RESPONSE, response);
    }
}