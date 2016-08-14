package mp.ajapps.musicplayerfree.Activity;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.provider.MediaStore;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import mp.ajapps.musicplayerfree.Adapters.Album_Details_Adapter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;


public class PlaylistDetails extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>, Album_Details_Adapter.myOnCLickInterface  {
long mId;
    protected Album_Details_Adapter mAdpt;
    private RecyclerView mList;
    Cursor mCursor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist_details);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.view6);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getIntent().getExtras().getString("mName"));

        mId = getIntent().getExtras().getLong("playlist_id");
        getLoaderManager().initLoader(5, null, this);

        mList = (RecyclerView) findViewById(R.id.view7);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mAdpt = new Album_Details_Adapter(this);
        mList.setLayoutManager(mLayoutManager);
        mList.setAdapter(mAdpt);
    }
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            final StringBuilder mSelection = new StringBuilder();
            mSelection.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1");
            mSelection.append(" AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''");
        return  new CursorLoader(this, MediaStore.Audio.Playlists.Members.getContentUri("external", mId),
                new String[] {
                        MediaStore.Audio.Playlists.Members._ID,
                        MediaStore.Audio.AudioColumns.TITLE,
                        MediaStore.Audio.AudioColumns.ARTIST,
                }, mSelection.toString(), null, MediaStore.Audio.Playlists.Members.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(mCursor != null) {
            mCursor.close();
            mCursor = null;
        }
        mCursor = data;
        mAdpt.changeCursor(data);
        mAdpt.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void myOnClick(int pos) {
        MusicUtils.setAndPlay(mCursor, pos);
    }

    @Override
    public void myOnLongClick(int pos) {

    }

    public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;
        private int size;

        public SimpleDividerItemDecoration(Context context, int paddingLeft) {
            this.size = paddingLeft;
            mDivider = ContextCompat.getDrawable(context, R.drawable.line_divider_black);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft() + (size * 2);
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }
}
