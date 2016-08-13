package mp.ajapps.musicplayerfree.Activity;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.RemoteException;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import mp.ajapps.musicplayerfree.Adapters.TrackAdapter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.Helpers.RecyclerItemClickListener;
import mp.ajapps.musicplayerfree.R;

public class Album_Details extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, TrackAdapter.myOnCLickInterface {
    ImageView mImg;
    TextView mAlbum;
    RecyclerView recyclerView;
    Toolbar toolbar;
    FloatingActionButton fab;
    LinearLayout mLabelLayout;
    Long albumId;CollapsingToolbarLayout ctl;
    Cursor mCursor;
    protected LinearLayoutManager mLayoutManager;
    protected TrackAdapter mAdpt;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_details);
        AppBarLayout abl = (AppBarLayout) findViewById(R.id.appBarLay);

        ((CoordinatorLayout.LayoutParams) abl.getLayoutParams()).setBehavior(new FlingBehavior());
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#9c27b0")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        Bundle mBundle = getIntent().getExtras();
        albumId = mBundle.getLong("id");
        mLabelLayout = (LinearLayout) findViewById(R.id.labelLay);
        ctl = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout);
        String v = "file:///" + mBundle.get("art");
        getLoaderManager().initLoader(0,null,this);
        mImg = (ImageView) findViewById(R.id.image);
        mAlbum = (TextView) findViewById(R.id.albumName);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        recyclerView = (RecyclerView) findViewById(R.id.view3);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        mAdpt = new TrackAdapter(R.layout.track_row_white, this);
        recyclerView.setAdapter(mAdpt);
        ImageLoader.getInstance().displayImage(v, mImg, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {

            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                Palette p = Palette.from(loadedImage).generate();
                mLabelLayout.setBackgroundColor(p.getDarkMutedColor(Color.DKGRAY));
                //  ctl.setContentScrimColor(p.getLightVibrantColor(Color.BLUE));
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {

            }
        });
        mAlbum.setText(mBundle.get("album") + "");


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicUtils.setAndPlay(mCursor,0);
            }
        });
    }

    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.i("oyo", "dfgggg");
        final StringBuilder selection = new StringBuilder();
        selection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''");
        selection.append(" AND " + MediaStore.Audio.AudioColumns.ALBUM_ID + "=" + albumId);
        return new CursorLoader(this,MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,new String[] {
                        /* 0 */
                BaseColumns._ID,
                        /* 1 */
                MediaStore.Audio.AudioColumns.TITLE,
                        /* 2 */
                MediaStore.Audio.AudioColumns.ARTIST,
                        /* 3 */
                MediaStore.Audio.AudioColumns.ALBUM,
                        /* 4 */
                MediaStore.Audio.AudioColumns.DURATION,
                        /* 5 */
                MediaStore.Audio.AudioColumns.YEAR,
        },selection.toString(),null, null);
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }
        if(mCursor !=null) mCursor.close();
        mAdpt.changeCursor(data);
        mAdpt.notifyDataSetChanged();
        mCursor = data;

       /* try {
            MusicUtils.mService.setPlayList(MusicUtils.getSongListForCursor(mCursor));
        } catch (RemoteException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void myOnClick(int pos) {
        MusicUtils.setAndPlay(mCursor,pos);

    }

    public final class FlingBehavior extends AppBarLayout.Behavior {
        private static final int TOP_CHILD_FLING_THRESHOLD = 3;
        private boolean isPositive;

        public FlingBehavior() {
        }

        public FlingBehavior(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onNestedFling(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, float velocityX, float velocityY, boolean consumed) {
            if (velocityY > 0 && !isPositive || velocityY < 0 && isPositive) {
                velocityY = velocityY * -1;
            }
            if (target instanceof RecyclerView && velocityY < 0) {
                final RecyclerView recyclerView = (RecyclerView) target;
                final View firstChild = recyclerView.getChildAt(0);
                final int childAdapterPosition = recyclerView.getChildAdapterPosition(firstChild);
                consumed = childAdapterPosition > TOP_CHILD_FLING_THRESHOLD;
            }
            return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
        }

        @Override
        public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout child, View target, int dx, int dy, int[] consumed) {
            super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
            isPositive = dy > 0;
        }
    }
}
