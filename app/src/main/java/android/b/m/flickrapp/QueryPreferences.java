package android.b.m.flickrapp;

import android.content.Context;
import android.preference.PreferenceManager;

public class QueryPreferences {
    private static final String  PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_RESULT_QUERY = "lastResultId";
    private static final String PREF_LAST_RESULT_ID = "lastResultId";
    private static final String PREF_IS_JOB_SCHEDULED = "isAlarmOn";

    public static String getStoredQuery(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_SEARCH_QUERY, null);
    }

    public static void setQuery(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static String getLastResultId(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_LAST_RESULT_QUERY, null);
    }

    public static void setLastResultId(Context context, String query) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public static void setPrefLastResultId(Context context, String lastResultId) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastResultId)
                .apply();
    }

    public static boolean isJobScheduled(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getBoolean(PREF_IS_JOB_SCHEDULED, false);
    }

    public static void setJobScheduled(Context context, boolean isScheduled) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_JOB_SCHEDULED, isScheduled)
                .apply();
    }
}