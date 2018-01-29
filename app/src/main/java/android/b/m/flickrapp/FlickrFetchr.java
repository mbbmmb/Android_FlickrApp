package android.b.m.flickrapp;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickrFetchr {

    private static final String TAG = "FlickrFetchr";
    private static final String API_KEY = "YOUR_API_KEY_HERE";
    private static final String FETCH_RECENT_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri ENDPOINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s")
            .appendQueryParameter("page", "10")
            .build();

    public List<GalleryItem> getRecentGallery() {
        String urlString = buildUrl(FETCH_RECENT_METHOD, null);
        return downloadGalleryItems(urlString);
    }

    public List<GalleryItem> getSearchGallery(String query) {
        String urlString = buildUrl(SEARCH_METHOD, query);
        return downloadGalleryItems(urlString);
    }

    private String buildUrl(String method, String query) {
        Uri.Builder uriBuilder = ENDPOINT.buildUpon()
                .appendQueryParameter("method", method);
        if(method.equals(SEARCH_METHOD)) {
            uriBuilder.appendQueryParameter("text", query);
        }
        return uriBuilder.build().toString();
    }

    private List<GalleryItem> downloadGalleryItems(String url) {
        List<GalleryItem> items = new ArrayList<>();
        try {
            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            Log.i(TAG, "JSON data received: " + jsonBody);
            parseItems(items, jsonBody);
        } catch (IOException ioe) {
            Log.i(TAG, "Failed to fetch items.", ioe);
        } catch (JSONException je) {
            Log.i(TAG, "Failed to parse items", je);
        }
        return items;
    }

    public String getUrlString(String urlString) throws IOException {
        return new String(getUrlBytes(urlString));
    }

    public byte[] getUrlBytes(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = connection.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + " with " + urlString);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public void parseItems(List<GalleryItem> items, JSONObject jsonBody) throws IOException, JSONException {
        JSONObject jsonObject = jsonBody.getJSONObject("photos");
        JSONArray jsonArray = jsonObject.getJSONArray("photo");
            for(int i = 0; i<jsonArray.length(); i++) {
                GalleryItem item = new GalleryItem();
                JSONObject JsonPhotoObject = jsonArray.getJSONObject(i);
                item.setCaption(JsonPhotoObject.getString("title"));
                item.setId(JsonPhotoObject.getString("id"));
                if(!JsonPhotoObject.has("url_s")) {
                    continue;
                }
                item.setUrl(JsonPhotoObject.getString("url_s"));
                item.setOwner(JsonPhotoObject.getString("owner"));
                items.add(item);
            }
    }
}
