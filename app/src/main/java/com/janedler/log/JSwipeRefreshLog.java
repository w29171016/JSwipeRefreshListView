package com.janedler.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;

/**
 * Created byjanedler on 2016/4/2.
 * <p/>
 * A wrapper for android.util.Log
 */
public class JSwipeRefreshLog {


    private static final String TAG = "JSwipeRefreshTAG";

    /**
     * Turn on/off for Log
     */
    private static final boolean IS_LOG_ON;
    static {
        IS_LOG_ON = (boolean) getBuildConfigValue("DEBUG");
    }

    /**
     * Priority constant for the println method; use EMLog.v.
     */
    public static final int VERBOSE = 2;

    /**
     * Priority constant for the println method; use EMLog.d.
     */
    public static final int DEBUG = 3;

    /**
     * Priority constant for the println method; use EMLog.i.
     */
    public static final int INFO = 4;

    /**
     * Priority constant for the println method; use EMLog.w.
     */
    public static final int WARN = 5;

    /**
     * Priority constant for the println method; use EMLog.e.
     */
    public static final int ERROR = 6;

    /**
     * Priority constant for the println method.
     */
    public static final int ASSERT = 7;

    private JSwipeRefreshLog() {
    }

    /**
     * Send a {@link #VERBOSE} log message.
     *
     * @param msg The message you would like logged.
     */
    public static int v(String msg) {
        if (IS_LOG_ON) {
            return android.util.Log.v(TAG, msg);
        } else {
            return 0;
        }
    }

    /**
     * Send a {@link #VERBOSE} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int v(String msg, Throwable tr) {
        if (IS_LOG_ON) {
            return android.util.Log.v(TAG, msg, tr);
        } else {
            return 0;
        }
    }

    /**
     * Send a {@link #DEBUG} log message.
     *
     * @param msg The message you would like logged.
     */
    public static int d(String msg) {
        if (IS_LOG_ON) {
            return android.util.Log.d(TAG, msg);
        } else {
            return 0;
        }
    }

    /**
     * Send a {@link #DEBUG} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int d(String msg, Throwable tr) {
        if (IS_LOG_ON) {
            return android.util.Log.v(TAG, msg, tr);
        } else {
            return 0;
        }
    }

    /**
     * Send an {@link #INFO} log message.
     *
     * @param msg The message you would like logged.
     */
    public static int i(String msg) {
        if (IS_LOG_ON) {
            return android.util.Log.i(TAG, msg);
        } else {
            return 0;
        }
    }

    /**
     * Send a {@link #INFO} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int i(String msg, Throwable tr) {
        if (IS_LOG_ON) {
            return android.util.Log.i(TAG, msg, tr);
        } else {
            return 0;
        }
    }

    /**
     * Send a {@link #WARN} log message.
     *
     * @param msg The message you would like logged.
     */
    public static int w(String msg) {
        if (IS_LOG_ON) {
            return android.util.Log.w(TAG, msg);
        } else {
            return 0;
        }
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int w(String msg, Throwable tr) {
        if (IS_LOG_ON) {
            return android.util.Log.i(TAG, msg, tr);
        } else {
            return 0;
        }
    }

    /**
     * Send a {@link #WARN} log message and log the exception.
     *
     * @param tr  An exception to log
     */
    public static int w(Throwable tr) {
        if (IS_LOG_ON) {
            return android.util.Log.w(TAG, tr);
        } else {
            return 0;
        }
    }

    /**
     * Send an {@link #ERROR} log message.
     *
     * @param msg The message you would like logged.
     */
    public static int e(String msg) {
        if (IS_LOG_ON) {
            return android.util.Log.e(TAG, msg);
        } else {
            return 0;
        }
    }

    /**
     * Send a {@link #ERROR} log message and log the exception.
     *
     * @param msg The message you would like logged.
     * @param tr  An exception to log
     */
    public static int e(String msg, Throwable tr) {
        if (IS_LOG_ON) {
            return android.util.Log.i(TAG, msg, tr);
        } else {
            return 0;
        }
    }

    /**
     * Handy function to get a loggable stack trace from a Throwable
     *
     * @param tr An exception to log
     */
    @SuppressWarnings("unused")
    public static String getStackTraceString(Throwable tr) {
        if (IS_LOG_ON) {
            if (tr == null) {
                return "";
            }

            // This is to reduce the amount of log spew that apps do in the non-error
            // condition of the network being unavailable.
            Throwable t = tr;
            while (t != null) {
                if (t instanceof UnknownHostException) {
                    return "";
                }
                t = t.getCause();
            }

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            tr.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        } else {
            return null;
        }
    }

    /**
     * Gets a field from the project's BuildConfig. This is useful when, for example, flavors
     * are used at the project level to set custom fields.
     *
     * @param fieldName The name of the field-to-access
     * @return The value of the field, or {@code null} if the field is not found.
     */
    public static Object getBuildConfigValue(String fieldName) {
        try {
            Class<?> clazz = Class.forName("com.eastmoney.android.berlin.BuildConfig");
            java.lang.reflect.Field field = clazz.getField(fieldName);
            return field.get(null);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return false;
    }
}
