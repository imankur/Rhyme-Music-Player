package mp.ajapps.musicplayerfree.Fragments;

import android.app.Activity;
import android.os.RemoteException;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import javax.security.auth.login.LoginException;

import mp.ajapps.musicplayerfree.Adapters.TrackAdapter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Helpers.RecyclerItemClickListener;


public class TrackFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TrackAdapter.myOnCLickInterface{

    private RecyclerView mRecycleView;
    protected LinearLayoutManager mLayoutManager;
    protected TrackAdapter mAdpt;
    String TAG = "cooool";
    String [] mProjection;
    StringBuilder where;
    Cursor mCursor;
    String mWhere;
    String mSortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;
    Uri mUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    public static TrackFragment newInstance() {
        TrackFragment fragment = new TrackFragment();
        return fragment;
    }

    public TrackFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        where = new StringBuilder();
        where.append(MediaStore.Audio.AudioColumns.IS_MUSIC + "=1").append(" AND " + MediaStore.MediaColumns.TITLE + " != ''");
        mWhere = where.toString();
        mProjection = new String[] {
                BaseColumns._ID, MediaStore.MediaColumns.TITLE, MediaStore.Audio.AudioColumns.ALBUM, MediaStore.Audio.AudioColumns.ARTIST
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getLoaderManager().initLoader(0,null,this);
        View v = inflater.inflate(R.layout.fragment_track, container, false);
        mRecycleView = (RecyclerView) v.findViewById(R.id.mRecycleView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);
        mRecycleView.setLayoutManager(mLayoutManager);
        mAdpt = new TrackAdapter(R.layout.track_row, this);
        mRecycleView.setAdapter(mAdpt);
        return v;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(),mUri,mProjection,mWhere,null, mSortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null) {
            return;
        }
        if(mCursor !=null) mCursor.close();
            mAdpt.changeCursor(data);
            mAdpt.notifyDataSetChanged();
            mCursor = data;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    public void myOnClick(int pos) {
        MusicUtils.setAndPlay(mCursor,pos);
    }

}
