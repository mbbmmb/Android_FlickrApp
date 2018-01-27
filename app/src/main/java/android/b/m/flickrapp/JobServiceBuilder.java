package android.b.m.flickrapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.view.View;

/**
 * Created by MadsBorkmann on 26/01/18.
 */

public class JobServiceBuilder {

    private int mId;
    private Context mContext;

    public JobServiceBuilder(Context context, int id) {
            mId = id;
            mContext = context;
    }

    public JobInfo createJobInfo() {
        JobInfo jobInfo = new JobInfo.Builder(
                mId, new ComponentName(mContext, JobSchedulerService.class))
                .setPeriodic(20*60*1000)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .build();
        return jobInfo;
    }

    public boolean isBeenScheduled() {
        JobScheduler scheduler = (JobScheduler) mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;
        for(JobInfo jobInfo : scheduler.getAllPendingJobs()) {
            if(jobInfo.getId() == mId) {
                hasBeenScheduled = true;
            }
        }
        QueryPreferences.setJobScheduled(mContext, hasBeenScheduled);
        return hasBeenScheduled;
    }
}
