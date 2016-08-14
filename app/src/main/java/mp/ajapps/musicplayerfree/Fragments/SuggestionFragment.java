package mp.ajapps.musicplayerfree.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mp.ajapps.musicplayerfree.Activity.PlaylistDetails;
import mp.ajapps.musicplayerfree.Activity.SuggestionDetails;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.Helpers.PlaylistLoader;
import mp.ajapps.musicplayerfree.POJOS.Playlist;
import mp.ajapps.musicplayerfree.POJOS.Suggest;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Services.IMusicChild;
import mp.ajapps.musicplayerfree.Widgets.SqaureTextView;


public class SuggestionFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Playlist>> {
    ArrayList<Suggest> mData;
    private RecyclerView mList;
    private SuggestAdapter mAdapter;
    MyReceiver myR = null;
    Random generator = new Random();
    private String mColor[] =  {"#2e7d32","#37474f","#607d8b","#36223b","#553d4e","#9c27b0","#f44336","#e91e63","#673ab7","#3f51b5","#009688","#cfd8dc","#78909c"};
    public SuggestionFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_suggestion, container, false);
        mData = new ArrayList<>();
        addInitialData();
        mList = (RecyclerView) v.findViewById(R.id.mList);
        GridLayoutManager mGLM = new GridLayoutManager(getActivity(),3);
        mGLM.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return getSpanSizeOfView(position);
            }
        });
        mAdapter = new SuggestAdapter();
        mAdapter.setData(mData);
        mList.setLayoutManager(mGLM);
        mList.setAdapter(mAdapter);
        getLoaderManager().initLoader(3, null, this).forceLoad();
        return v;
    }

    private int getSpanSizeOfView (int pos) {
        if (pos == 1 || pos == 2) {
            return 1;
        } else  {
            return 3;
        }
    }

    @Override
    public Loader<List<Playlist>> onCreateLoader(int id, Bundle args) {
        return new PlaylistLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Playlist>> loader, List<Playlist> data) {
        addInitialData();
        for (Playlist p: data) {
            mData.add(new Suggest("",p.mPlaylistName,p.mSongCount + " Songs", p.mPlaylistId, null));
        }
        mAdapter.setData(mData);
        mAdapter.notifyDataSetChanged();
    }

    private void addInitialData () {
        mData.clear();
        mData.add(new Suggest("Recents",null,null,0,null));
        mData.add(new Suggest("","Recently\nPlayed",null,0,null));
        mData.add(new Suggest("","Recently\nAdded",null,0,null));
        mData.add(new Suggest("Playlists","",null,0,null));
    }

    @Override
    public void onLoaderReset(Loader<List<Playlist>> loader) {
        mAdapter.clearData();
        mAdapter.notifyDataSetChanged();
    }

    public void RestartLoader() {
        getLoaderManager().restartLoader(1, null, this).forceLoad();
    }
    public String getColor() {
        return mColor[generator.nextInt(mColor.length)];
    }

    private class SuggestAdapter extends RecyclerView.Adapter<SuggestAdapter.ViewHolder> {

        private ArrayList<Suggest> mData = new ArrayList<>();
        public void clearData() {
            mData.clear();
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 1) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recent_item_header, parent, false);
                return new ViewHolder(v);
            } else if (viewType == 2) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.recent_item, parent, false);
                return new ViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.suggest, parent, false);
                return new ViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final Suggest m = mData.get(position);
            if (position == 0 || position == 3) {
                holder.mTitle.setText(m.mHeader);
            } else if (position == 1 || position == 2){
                holder.mTitlesq.setText(m.playlist_name);
                holder.mTitlesq.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), SuggestionDetails.class);
                        i.putExtra("type", position);
                        startActivity(i);
                    }
                });
            } else {
                holder.mCount.setText(m.mCount);
                holder.mTitle.setText(m.playlist_name);
                holder.mTitle1.setText(position-3+"");
                holder.mItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getActivity(), PlaylistDetails.class);
                        i.putExtra("playlist_id", m.mAlbumId);
                        i.putExtra("mName", m.playlist_name);
                        startActivity(i);
                    }
                });
                holder.mSongMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PopupMenu mMenu = new PopupMenu(getActivity(),v);
                        mMenu.inflate(R.menu.suggest_playlisy);
                        mMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                switch (item.getItemId()) {
                                    case R.id.suggest_play :
                                        MusicUtils.PlayPlaylist(m.mAlbumId, getActivity());
                                        return true;
                                    case R.id.suggesyt_delete:
                                        MusicUtils.deletePlaylist(m.mAlbumId, getActivity());
                                        RestartLoader();
                                        return true;
                                }
                                return false;
                            }
                        });
                        mMenu.show();
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public int getItemViewType(int position) {
             if (position == 0 || position == 3) {
                return 1;
             } else if (position == 1 || position == 2){
                 return 2;
             } else {
                 return 3;
             }
        }

        public void setData(ArrayList data) {
            mData = data;
        }
        class ViewHolder extends RecyclerView.ViewHolder{
            View mItemView;
            SqaureTextView mTitlesq;
            TextView mCount, mTitle, mTitle1;
            ImageView mSongMenu;
            public ViewHolder(View itemView) {
                super(itemView);
                mItemView = itemView;
                mCount = (TextView) itemView.findViewById(R.id.imageButton);
                mTitle = (TextView) itemView.findViewById(R.id.title);
                mTitle1 = (TextView) itemView.findViewById(R.id.title1);
                mSongMenu = (ImageView) itemView.findViewById(R.id.song_item_menu);
                mTitlesq = (SqaureTextView) itemView.findViewById(R.id.title_sq);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (myR == null) {
            myR = new MyReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("Playlist_Changed");
            getActivity().registerReceiver(myR, intentFilter);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myR != null) {
            getActivity().unregisterReceiver(myR);
            myR = null;
        }
    }

    public class MyReceiver extends BroadcastReceiver {
        public MyReceiver() {}

        @Override
        public void onReceive(final Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals("Playlist_Changed")) {
                RestartLoader();
            }
        }
    }
}
