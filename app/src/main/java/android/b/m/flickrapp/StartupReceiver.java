package android.b.m.flickrapp;

import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {

    private static final String TAG = "StartupReceiver";

    //StartupReceiver will be listening for BOOT_COMPLETED actions, as specified in the manifest.
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received broadcast intent: " + intent.getAction());
        boolean hasBeenScheduled = QueryPreferences.isJobScheduled(context);
        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int JOB_ID = 1;
        JobServiceBuilder builder = new JobServiceBuilder(context, JOB_ID);
        if(builder.isBeenScheduled()) {
            Log.i(TAG, "Cancel JobScheduler on startup");
            scheduler.cancel(JOB_ID);
        } else {
            Log.i(TAG, "Start Jobscheduler on startup");
            scheduler.schedule(builder.createJobInfo());
        }

        }

}
