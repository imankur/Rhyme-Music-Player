package mp.ajapps.musicplayerfree.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;
import mp.ajapps.musicplayerfree.Widgets.ListPlaylistDailog;

/**
 * Created by Sharing Happiness on 1/9/2016.
 */
public class RecentsAdapter extends RecyclerView.Adapter<RecentsAdapter.TrackHolder>{
    Cursor dataCursor;
    Context mContext;

    public RecentsAdapter() {}

    @Override
    public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.track_row_white, parent, false);
        return new TrackHolder(v);
    }

    @Override
    public void onBindViewHolder(TrackHolder holder, final int position) {
        this.dataCursor.moveToPosition(position);
        holder.mTrackName.setText(this.dataCursor.getString(this.dataCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)));
        holder.mTrackDetail.setText(this.dataCursor.getString(this.dataCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)));
        holder.mTrachNum.setText(position+1 + "");
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        holder.mMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu mPop = new PopupMenu(mContext, v);
                mPop.inflate(R.menu.track_menu);
                mPop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.track_delete:
                                MusicUtils.deleteTracks(mContext, MusicUtils.getQueueItemAtPosition(position));
                                return true;
                            case R.id.track_play:
                                try {
                                    MusicUtils.mService.pagerNextPlay(position);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                return true;
                            case R.id.track_ring:
                                MusicUtils.setRingtone(mContext, MusicUtils.getQueueItemAtPosition(position));
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
        return (dataCursor == null) ? 0 : dataCursor.getCount();
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor cursor) {
        if (dataCursor == cursor) {
            return null;
        }
        Cursor oldCursor = dataCursor;
        this.dataCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    protected class TrackHolder extends RecyclerView.ViewHolder {
        public final View mView;
        TextView mTrackName, mTrackDetail, mTrachNum;
        ImageView mMenu;
        public TrackHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTrackName = (TextView) itemView.findViewById(R.id.textView);
            mTrackDetail = (TextView) itemView.findViewById(R.id.textView1);
            mTrachNum = (TextView) itemView.findViewById(R.id.textView3);
            mMenu = (ImageView) itemView.findViewById(R.id.song_item_menu);
        }
    }
}
