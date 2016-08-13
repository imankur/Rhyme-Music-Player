package mp.ajapps.musicplayerfree.Activity;

import java.util.ArrayList;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import mp.ajapps.musicplayerfree.Adapters.SearchAdapter;

import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.POJOS.SearchPojo;
import mp.ajapps.musicplayerfree.R;

public class SearchActivity extends AppCompatActivity implements TextWatcher, LoaderManager.LoaderCallbacks<Cursor>, SearchAdapter.myOnCLickInterface {
    protected SearchAdapter mAdpt;
    protected LinearLayoutManager mLayoutManager;
    EditText mEditText;
    ImageView mClear;
    String mString;
    LoaderManager lm;
    StringBuilder where;
    Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    String mWhere;
    Cursor mCursor;
    private RecyclerView mRecycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_navigation_arrow_back);
        where = new StringBuilder();
        where.append(MediaStore.Audio.Media.IS_MUSIC + "=1").append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");
        mWhere = where.toString();

        mEditText = (EditText) findViewById(R.id.search_view);
        mEditText.requestFocus();
        mClear = (ImageView) findViewById(R.id.search_clear);
        mEditText.addTextChangedListener(this);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        mRecycleView = (RecyclerView) findViewById(R.id.view4);
        mAdpt = new SearchAdapter(this);

        GridLayoutManager mGLM = new GridLayoutManager(this,3);
        mGLM.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return getSpanSizeOfView(position);
            }
        });
        mRecycleView.setLayoutManager(mGLM);
        mRecycleView.setAdapter(mAdpt);
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("");
            }
        });
        mString = "------";
        lm = getLoaderManager();
       // lm.initLoader(0, null, this);
    }

    private int getSpanSizeOfView (int pos) {
        int type = mAdpt.getItemViewType(pos);
        if (type == 1 || type == 2 || type == 3) {
            return 3;
        } else  {
            return 1;
        }
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
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mString == s.toString())
            return;
        mString = s.toString();
        if (TextUtils.isEmpty(s)) {
            mAdpt.changeCursor(null);
            mAdpt.notifyDataSetChanged();
            return;
        }

        ArrayList<SearchPojo> data = new ArrayList<>();

        ArrayList<SearchPojo> mALbum = MusicUtils.searchAlbum(this, mString);
        if (mALbum.size() > 0) {
            data.add(new SearchPojo(2));
            data.addAll(mALbum);
        }

        ArrayList<SearchPojo> mTrack = MusicUtils.searchSong(this, mString);
        if (mTrack.size() > 0) {
            data.add(new SearchPojo(1));
            data.addAll(mTrack);
        }


        if (data.size() > 0) {
            mAdpt.changeCursor(data);
        } else {
            mAdpt.changeCursor(null);
        }
        mAdpt.notifyDataSetChanged();


        //lm.restartLoader(0, null, this);
    }

    @Override
    public void afterTextChanged(Editable s) {}

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri uri = Uri.parse("content://media/external/audio/search/fancy/"
                + Uri.encode(mString));
        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.Audio.Media.MIME_TYPE, MediaStore.Audio.Artists.ARTIST,
                MediaStore.Audio.Albums.ALBUM_ID,
                MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Media.TITLE
        };
        return new CursorLoader(this, uri, projection, mWhere, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.getCount() > 1) {
            mAdpt.changeCursor(MusicUtils.getFormatSearchData(data));
        } else {
            mAdpt.changeCursor(null);
        }


        if (mCursor != null) mCursor.close();
        mCursor = data;
        mAdpt.notifyDataSetChanged();
        //MusicUtils.getFormatSearchData(data);
    }

    @Override
    public void myOnClick(int type, String id) {
        Log.i("oyoooo", "myOnClick: + pos" + type + "--" + id);
        if (type == 4) {
            Intent intent = new Intent(this, Album_Details.class);
            final Bundle bundle = new Bundle();
            bundle.putLong("id", Long.valueOf(id).longValue());
          /*  bundle.putString("album", mCursor.getString(mAlbumIdx));
            bundle.putString("artist",mCursor.getString(mArtistIdx));
            bundle.putString("art", mCursor.getString(mAlbumArtIndex));*/
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdpt.changeCursor(null);
    }


}