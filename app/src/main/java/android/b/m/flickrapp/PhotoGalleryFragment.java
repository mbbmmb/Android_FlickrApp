package android.b.m.flickrapp;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends VisibleFragment {

    private RecyclerView mRecyclerView;
    private List<GalleryItem> mGalleryItems = new ArrayList<>();
    private ThumbNailDownloader<PhotoHolder> mThumbNailDownloader;
    private static final String TAG = "PhotoGalleryFragment";
    private int mLastPosition;

    public static PhotoGalleryFragment newInstance() {
        return new PhotoGalleryFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        updateItems();
        //Handler attached to the main thread's Looper.
        Handler responseHandler = new Handler();
        mThumbNailDownloader = new ThumbNailDownloader<>(responseHandler);
        mThumbNailDownloader.setThumbNailDownloadListener(new ThumbNailDownloader.ThumbNailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbNailDownloaded(PhotoHolder holder, Bitmap bitmap) {
                Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                holder.bindDrawable(drawable);
            }
        });
        mThumbNailDownloader.start();
        mThumbNailDownloader.getLooper();
        Log.i(TAG, "Background thread started.");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.photo_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        mRecyclerView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            final GridLayoutManager manager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                mLastPosition = manager.findLastVisibleItemPosition();
                if(mLastPosition == mGalleryItems.size() - 1) {
                    updateItems();
                }
            }
        });
        setupAdapter();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mThumbNailDownloader.clearQueue();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mThumbNailDownloader.quit();
        Log.i(TAG, "Background thread destroyed.");
    }

    private void updateItems() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemsTask(query).execute();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.photo_gallery_fragment, menu);

        MenuItem toggle = menu.findItem(R.id.menu_item_toggle_polling);
        final int JOB_ID = 1;
        JobServiceBuilder builder = new JobServiceBuilder(getActivity(), JOB_ID);
        if(builder.isBeenScheduled()) {
            toggle.setTitle(R.string.stop_polling);
        } else {
            toggle.setTitle(R.string.start_polling);
        }

        MenuItem menuItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();
        
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Query text submitted" + query);
                QueryPreferences.setQuery(getActivity(), query);
                searchView.onActionViewCollapsed();
                updateItems();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "Query text changed" + newText);
                return false;
            }
        });
        
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear_search:
                QueryPreferences.setQuery(getActivity(), null);
                updateItems();
                return  true;
            case R.id.menu_item_toggle_polling:
                       JobScheduler scheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);
                       final int JOB_ID = 1;
                       JobServiceBuilder builder = new JobServiceBuilder(getActivity(), JOB_ID);
                       if(builder.isBeenScheduled()) {
                           Log.i(TAG, "Cancel JobScheduler");
                           scheduler.cancel(JOB_ID);
                       } else {
                           Log.i(TAG, "Start Jobscheduler");
                           scheduler.schedule(builder.createJobInfo());
                       }
                       getActivity().invalidateOptionsMenu();
                       return true;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void setupAdapter() {
        if(isAdded()) {
            mRecyclerView.setAdapter(new PhotoAdapter(mGalleryItems));
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder
    implements View.OnClickListener{
        private ImageView mImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            mImageView.setOnClickListener(this);
        }

        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(GalleryItem galleryItem) {
            mGalleryItem = galleryItem;
        }
        
        @Override
        public void onClick(View v) {
            Intent i = PhotoPageActivity.newIntent(getActivity(), mGalleryItem.getPhotoPageUri());
            startActivity(i);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {
        private List<GalleryItem> mItems;

        public PhotoAdapter(List<GalleryItem> items) {
            mItems= items;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem item = mItems.get(position);
            holder.bindGalleryItem(item);
            mThumbNailDownloader.queueThumbNail(holder, item.getUrl());
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }
    }

    private class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {
        private String mQuery;

        public FetchItemsTask(String query) {
            mQuery = query;
        }

        @Override
        protected List<GalleryItem> doInBackground(Void... params) {
            if(mQuery == null) {
                return new FlickrFetchr().getRecentGallery();
            } else {
                return new FlickrFetchr().getSearchGallery(mQuery);
            }
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mGalleryItems = galleryItems;
            setupAdapter();
        }
    }
}
