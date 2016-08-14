package mp.ajapps.musicplayerfree.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import mp.ajapps.musicplayerfree.Helpers.AsyncImageLoader;
import mp.ajapps.musicplayerfree.Helpers.ExeTimeCalculator;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.POJOS.Song;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Widgets.ListPlaylistDailog;

public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackHolder> {
    int rowId;
    Context c;
    private myOnCLickInterface mMyOnClick;
    private SparseBooleanArray selectedItems;
    public FragmentManager mFm = null;
    private ArrayList<Song> mList = new ArrayList<>();

    public TrackAdapter(Context c, int id, myOnCLickInterface m, FragmentManager fm) {
        this.c = c;
        this.rowId = id;
        this.mMyOnClick = m;
        selectedItems = new SparseBooleanArray();
        mFm = fm;
    }

    @Override
    public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(rowId, parent, false);
        return new TrackHolder(v);
    }

    @Override
    public void onBindViewHolder(final TrackHolder holder, final int position) {
        holder.mImg.setImageResource(R.drawable.default_artwork);
        final int pos = position;
        final Song m = mList.get(position);
        holder.mBack.setVisibility(selectedItems.get(position) ? View.VISIBLE: View.INVISIBLE);
        holder.mTrackName.setText(m.mSongName);
        holder.mTrackDetail.setText(m.mAlbumName);
        if (m.mArt == null) {
            holder.mImg.setTag(Integer.valueOf(position));
            try {
                AsyncImageLoader asyncImageLoader = new AsyncImageLoader(m.getmAlbumId, holder.mImg, c, m, position);
                asyncImageLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, null);
            } catch (RejectedExecutionException e) {
                // Executor has exhausted queue space
            }
        } else if (m.mArt != "empty") {
            ImageLoader.getInstance().displayImage(m.mArt, holder.mImg);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mMyOnClick) {
                    mMyOnClick.myOnClick(pos, holder.mView);
                }
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mMyOnClick.myOnLongClick(pos, holder.mView);
                return true;
            }
        });
        holder.mMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu mPop = new PopupMenu(c, v);
                mPop.inflate(R.menu.track_menu);
                mPop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.track_delete:
                                MusicUtils.deleteTracks(c, MusicUtils.getQueueItemAtPosition(position));
                                return true;
                            case R.id.track_play:
                                try {
                                    MusicUtils.mService.pagerNextPlay(position);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            case R.id.track_ring:
                                MusicUtils.setRingtone(c, MusicUtils.getQueueItemAtPosition(position));
                                return true;
                            case R.id.track_playlist :
                                long  id[] = new long[1];
                                id[0] = MusicUtils.getQueueItemAtPosition(position);
                                new ListPlaylistDailog().setList(id).show(mFm,"5");
                                return true;
                        }
                        return false;
                    }
                });
                mPop.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return (mList == null) ? 0 : mList.size();
    }

    public void changeCursor(Cursor cursor) {
        if (cursor != null) {
            int colidx = cursor.getColumnIndexOrThrow(BaseColumns._ID);
            int one = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE);
            int two = cursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST);
            int three = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
            while (cursor.moveToNext()) {
                final Song m = new Song(cursor.getLong(colidx), cursor.getString(one), cursor.getString(two), cursor.getLong(three));
                mList.add(m);
            }
        }
            this.notifyDataSetChanged();

    }

    public interface myOnCLickInterface {
        void myOnClick(int pos, View v);
        public void myOnLongClick(int pos, View v);
    }

    protected class TrackHolder extends RecyclerView.ViewHolder {
        public final View mView;
        TextView mTrackName, mTrackDetail;
        ImageView mImg, mMenu;
        View mBack;

        public TrackHolder(View itemView) {
            super(itemView);
            mView = itemView;

            mTrackName = (TextView) itemView.findViewById(R.id.textView);
            mTrackDetail = (TextView) itemView.findViewById(R.id.textView2);
            mImg = (ImageView) itemView.findViewById(R.id.song_item_img);
            mMenu = (ImageView) itemView.findViewById(R.id.song_item_menu);
            mBack = itemView.findViewById(R.id.mBack);
        }
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        List<Integer> selection = getSelectedItems1();
        selectedItems.clear();
        for (Integer i : selection) {
            notifyItemChanged(i);
        }
    }
    public List<Integer> getSelectedItems1() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); ++i) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public long[] getSelectedItems() {
        long[] items = new long[selectedItems.size()];
        for (int i = 0; i < selectedItems.size(); i++) {
            items[i] = mList.get(selectedItems.keyAt(i)).mSongId;
        }
        return items;
    }

}