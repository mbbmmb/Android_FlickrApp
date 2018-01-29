package android.b.m.flickrapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ThumbNailDownloader<T> extends HandlerThread {

    private Handler mResponseHandler;
    private Handler mRequestHandler;
    private ConcurrentMap<T, String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbNailDownloadListener<T> mThumbNailDownloadListener;
    private boolean mHasQuit = false;

    private static final String TAG = "ThumbNailDownLoader";
    private static final int MESSAGE_DOWNLOAD = 0;

    public ThumbNailDownloader(Handler responseHandler) {
        super(TAG);
        mResponseHandler = responseHandler;
    }

    public interface ThumbNailDownloadListener<T> {
        void onThumbNailDownloaded(T target, Bitmap thumbnail);
    }

    public void setThumbNailDownloadListener(ThumbNailDownloadListener<T> listener) {
        mThumbNailDownloadListener = listener;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    T target = (T) msg.obj;
                    Log.i(TAG, "Got a request for URL: " + mRequestMap.get(target));
                    handleRequest(target);
                }
            }
        };
    }

    @Override
    public boolean quit() {
        mHasQuit = true;
        return super.quit();
    }

    public void queueThumbNail(T target, String url) {
        Log.i(TAG, "Got a url: " + url);
        if (url == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, url);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, target)
                    .sendToTarget();
        }
    }

    public void clearQueue() {
        mResponseHandler.removeMessages(MESSAGE_DOWNLOAD);
        mRequestMap.clear();
    }

    private void handleRequest(final T target) {
        final String url = mRequestMap.get(target);
        if (url == null) {
            return;
        }
         final Bitmap bitmap = createBitmap(url);
         postDownloadedBitmapToMainThread(target, url, bitmap);
    }

    private Bitmap createBitmap(String url) {
        try {
            byte[] bitmapBytes = new FlickrFetchr().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
            Log.i(TAG, "Bitmap created");
            return bitmap;
        } catch (IOException ioe) {
            Log.e(TAG, "Error downloading image", ioe);
            return null;
        }
    }

    private void postDownloadedBitmapToMainThread(final T target, final String url, final Bitmap bitmap) {
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mRequestMap.get(target) != url || mHasQuit) {
                    return;
                }
                mRequestMap.remove(target);
                mThumbNailDownloadListener.onThumbNailDownloaded(target, bitmap);
            }
        });
    }
}
