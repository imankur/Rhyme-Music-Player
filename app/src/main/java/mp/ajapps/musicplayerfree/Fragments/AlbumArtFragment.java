package mp.ajapps.musicplayerfree.Fragments;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nostra13.universalimageloader.core.ImageLoader;

import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.POJOS.AlbumArtistDetails;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Widgets.SquareImageView;

public class AlbumArtFragment extends Fragment {
    public static final long NO_TRACK_ID = -1;
    private static final String ID = AlbumArtFragment.class.getName();
    public SquareImageView mImageView;
    View mRootView;
    private long mAudioId = NO_TRACK_ID;
    private AlbumArtistLoader mTask;

    public AlbumArtFragment() {
    }

    public static AlbumArtFragment newInstance(final long trackId) {
        AlbumArtFragment fragment = new AlbumArtFragment();
        final Bundle args = new Bundle();
        args.putLong(ID, trackId);
        fragment.setArguments(args);
        return fragment;
    }

    private static Cursor openCursorAndGoToFirst(Context con, Uri uri, String[] projection,
                                                 String selection, String[] selectionArgs) {
        Cursor c = con.getContentResolver().query(uri, projection,
                selection, selectionArgs, null, null);
        if (c == null) {
            return null;
        }
        if (!c.moveToFirst()) {
            c.close();
            return null;
        }
        return c;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAudioId = getArguments().getLong(ID, NO_TRACK_ID);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.album_art_fragment, null);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImageView = (SquareImageView) mRootView.findViewById(R.id.audio_player_album_art);
        mTask = new AlbumArtistLoader(this, getActivity());
        mTask.execute(mAudioId);
    }

    public void loadImage(String s) {
        ImageLoader.getInstance().displayImage(s, mImageView);
    }

    public static class AlbumArtistLoader extends AsyncTask<Long, Void, AlbumArtistDetails> {
        private Context mContext;
        private AlbumArtFragment fragments;

        public AlbumArtistLoader(AlbumArtFragment fm, final Context context) {
            mContext = context;
            this.fragments = fm;
        }

        @Override
        protected AlbumArtistDetails doInBackground(final Long... params) {
            long id = params[0];
            return MusicUtils.getAlbumArtDetails(mContext, id);
        }

        @Override
        protected void onPostExecute(final AlbumArtistDetails result) {
            if (result != null) {
                Cursor mAlbumCursor = null;
                String[] ALBUM_PROJECTION = new String[]{
                        MediaStore.Audio.Albums.ALBUM_ART
                };
                if (result.mAlbumId >= 0) {
                    mAlbumCursor = openCursorAndGoToFirst(mContext, MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                            ALBUM_PROJECTION, "_id=" + result.mAlbumId, null);
                    mAlbumCursor.moveToFirst();
                }
                String path = "file:///" + mAlbumCursor.getString(0);
                fragments.loadImage(path);
                mAlbumCursor.close();
            }
        }
    }
}
