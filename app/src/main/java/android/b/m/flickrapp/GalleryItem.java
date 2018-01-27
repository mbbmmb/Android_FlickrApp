package android.b.m.flickrapp;

import android.net.Uri;

public class GalleryItem {

    private String mId;
    private String mCaption;
    private String mUrl;
    private String mOwner;

    public String getString() {
        return mCaption.toString();
    }

    public String getId() {
        return mId;
    }

    public String getCaption() {
        return mCaption;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public Uri getPhotoPageUri() {
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }

    public void setId(String id) {
        mId = id;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public void setUrl(String url) {
        mUrl = url;
    }
}
