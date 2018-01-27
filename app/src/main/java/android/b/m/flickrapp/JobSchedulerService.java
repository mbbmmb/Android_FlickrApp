package android.b.m.flickrapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import java.util.List;

public class JobSchedulerService extends JobService {

    private static final String TAG = "JobSchedulerService";
    public static final String ACTION_SHOW_NOTIFICATION = "android.b.m.flickrapp.SHOW_NOTIFICATION";
    public static final String NOTIFICATION = "NOTIFICATION";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String PERM_PRIVATE = "android.b.m.flickrapp.PRIVATE";
    private PollTask mCurrentTask;

    @Override
    public boolean onStartJob(JobParameters params) {
        mCurrentTask = new PollTask();
        mCurrentTask.execute(params);
        Log.i(TAG, "Service started");
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if(mCurrentTask != null) {
            mCurrentTask.cancel(true);
        }
        return false;
    }

   private class PollTask extends AsyncTask<JobParameters, Void, List<GalleryItem>> {
       @Override
       protected List<GalleryItem> doInBackground(JobParameters... jobParameters) {
           JobParameters params = jobParameters[0];
           List<GalleryItem> galleryItems;
           String query = QueryPreferences.getStoredQuery(JobSchedulerService.this);
           if(query == null) {
               galleryItems = new FlickrFetchr().getRecentGallery();
           } else {
               galleryItems = new FlickrFetchr().getSearchGallery(query);
           }
            jobFinished(params, false);
            return galleryItems;
       }

       @Override
       protected void onPostExecute(List<GalleryItem> galleryItems) {
            if(galleryItems.size() == 0) {
                Log.i(TAG, "Galleryitems size = 0");
                return;
            }
            String lastResultId = QueryPreferences.getLastResultId(JobSchedulerService.this);
            String resultId = galleryItems.get(0).getId();
            if(lastResultId.equals(resultId)) {
                Log.i(TAG, "Got an old result.");
            } else {
                Log.i(TAG, "Got a new result");
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationChannel channel = createNotificationChannel();
                notificationManager.createNotificationChannel(channel);
                Notification notification = createNotification();
                notificationManager.notify(0, notification);
                Log.i(TAG, "Build a notification");
                showBackgroundNotification(0, notification);
            }
            QueryPreferences.setPrefLastResultId(JobSchedulerService.this, resultId);
       }
   }

   private NotificationChannel createNotificationChannel() {
       String channelName = "flickr_channel";
       String channelDescription = "Channel for the JobSchedulerService class";
       int importance = NotificationManager.IMPORTANCE_HIGH;
       NotificationChannel channel = new NotificationChannel(getChannelId(), channelName, importance);
       channel.setDescription(channelDescription);
       return channel;
   }

    private String getChannelId() {
        String channelId = "default";
        return channelId;
    }

   private Notification createNotification() {
        Resources resources = getResources();
       Notification notification = new Notification.Builder(JobSchedulerService.this, getChannelId())
               .setTicker(resources.getString(R.string.new_images_title))
               .setSmallIcon(android.R.drawable.ic_menu_report_image)
               .setContentTitle(resources.getString(R.string.new_images_title))
               .setContentText(resources.getString(R.string.new_images_text))
               .setContentIntent(createPendingIntent())
               .setAutoCancel(true)
               .build();
       return notification;
   }

    private PendingIntent createPendingIntent() {
        Intent i = PhotoGalleryActivity.newIntent(JobSchedulerService.this);
        return PendingIntent.getService(JobSchedulerService.this, 0, i, 0);
    }

    private void showBackgroundNotification(int requestCode, Notification notification) {
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
    }
}
