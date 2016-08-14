package mp.ajapps.musicplayerfree.Fragments;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import mp.ajapps.musicplayerfree.Adapters.AlbumAdapter;
import mp.ajapps.musicplayerfree.R;


public class AlbumFragment extends Fragment implements LoaderCallbacks<Cursor> {

    private final int PLAY_SELECTION = 0;
    private final int ADD_TO_PLAYLIST = 1;
    private final int SEARCH = 2;
    protected AlbumAdapter mAdapter;
    protected Cursor mCursor;
    protected int mFragmentGroupId = 0;
    protected String mCurrentId, mSortOrder = null, mType = null;
    protected String[] mProjection = null;
    protected Uri mUri = null;
    String TAG = "ankurr";
    private RecyclerView mGridview;
    private int mAlbumIdx;
    private int mArtistIdx;
    private int mAlbumArtIndex, mID;

    public AlbumFragment() {}

    public static AlbumFragment newInstance() {
        AlbumFragment fragment = new AlbumFragment();
        return fragment;
    }

    private void getColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
            mAlbumArtIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
            mID = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupFragmentData();
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_album, container, false);
        mGridview = (RecyclerView) v.findViewById(R.id.album_frag);
        mGridview.setLayoutManager(new GridLayoutManager(getActivity(),3));
        mAdapter = new AlbumAdapter(getActivity());
        setupFragmentData();
        mGridview.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //mGridview.setNestedScrollingEnabled(true);
        }

       /* mGridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCursor.moveToPosition(position);
                Intent intent = new Intent(getActivity(), Album_Details.class);
                final Bundle bundle = new Bundle();
                bundle.putLong("id", mCursor.getLong(mID));
                bundle.putString("album", mCursor.getString(mAlbumIdx));
                bundle.putString("artist", mCursor.getString(mArtistIdx));
                bundle.putString("art", mCursor.getString(mAlbumArtIndex));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });*/

        mGridview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                switch (newState) {
                    case RecyclerView.SCROLL_STATE_IDLE:
                        ImageLoader.getInstance().resume();
                        break;
                    case RecyclerView.SCROLL_STATE_DRAGGING:
                        ImageLoader.getInstance().pause();
                        break;
                    case RecyclerView.SCROLL_STATE_SETTLING:
                        ImageLoader.getInstance().resume();
                        break;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        return v;
    }

    public void setupFragmentData() {
        mProjection = new String[]{
                BaseColumns._ID, MediaStore.Audio.AlbumColumns.ALBUM, MediaStore.Audio.AlbumColumns.ARTIST,
                MediaStore.Audio.AlbumColumns.ALBUM_ART};
        mUri = MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI;
        mSortOrder = MediaStore.Audio.Albums.DEFAULT_SORT_ORDER;
        mFragmentGroupId = 2;
        mType = "album";
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCursor.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), mUri, mProjection, null, null, mSortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }
        if (mCursor != null)
            mCursor.close();
        mAdapter.changeCursor(data);
        getColumnIndices(data);
        mAdapter.notifyDataSetChanged();
        mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (mAdapter != null)
            mAdapter.changeCursor(null);
    }
}
