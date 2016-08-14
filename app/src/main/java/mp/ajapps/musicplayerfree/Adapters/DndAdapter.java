package mp.ajapps.musicplayerfree.Adapters;

import android.graphics.Color;
import android.os.RemoteException;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import mp.ajapps.musicplayerfree.Helpers.ItemTouchHelperAdapter;
import mp.ajapps.musicplayerfree.Helpers.ItemTouchHelperViewHolder;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.Helpers.OnStartDragListener;
import mp.ajapps.musicplayerfree.POJOS.Song;
import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 6/17/2015.
 */
public class DndAdapter extends RecyclerView.Adapter<DndAdapter.TrackHolder> implements ItemTouchHelperAdapter{
    private  ArrayList<Song> mSongList = new ArrayList<>();
    private int rowId;
    int Current = 0;
    private final OnStartDragListener mDragStartListener;

    public DndAdapter(int id, myOnCLickInterface m, OnStartDragListener dragStartListener) {
        this.rowId = id;
        myOnCLickInterface mMyOnClick = m;
        mDragStartListener = dragStartListener;
        try {
            this.Current = MusicUtils.mService.getQueuePosition();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(rowId, parent, false);
        return new TrackHolder(v);
    }

    @Override
    public void onBindViewHolder(final TrackHolder holder, final int position) {
        Song mSong = mSongList.get(position);
        if (position == Current) {
            Log.i("fattt", "onBindViewHolder: " + position + "--" + Current);
            holder.mTrackName.setTextColor(Color.parseColor("#f04f40"));
            holder.mTrackDetail.setTextColor(Color.parseColor("#f04f40"));
        } else {
            holder.mTrackName.setTextColor(Color.WHITE);
            holder.mTrackDetail.setTextColor(Color.WHITE);
        }
        holder.mTrackName.setText(mSong.mSongName);
        holder.mTrackDetail.setText(mSong.mAlbumName+"");
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    MusicUtils.mService.setAndPlayQueue(position);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        holder.mImg.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
    }

    public void updateNowPlayingRow() {
        try {
            notifyItemChanged(this.Current);
            this.Current = MusicUtils.mService.getQueuePosition();
            notifyItemChanged(this.Current);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
    @Override
    public int getItemCount() {
        return (mSongList == null) ? 0 : mSongList.size();
    }

    public void changeCursor(ArrayList<Song> list) {
        if (mSongList != null) mSongList =null;
        mSongList = list;
    }

    @Override
    public boolean onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mSongList, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
        try {
            MusicUtils.mService.moveQueueItem(fromPosition, toPosition);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return true;
    }

    public interface myOnCLickInterface {
        void myOnClick(int pos);
    }

    protected class TrackHolder extends RecyclerView.ViewHolder implements ItemTouchHelperViewHolder {
        public final View mView;
        TextView mTrackName, mTrackDetail;
        ImageView mImg;

        public TrackHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTrackName = (TextView) itemView.findViewById(R.id.textView);
            mTrackDetail = (TextView) itemView.findViewById(R.id.textView2);
            mImg = (ImageView) itemView.findViewById(R.id.handle);
        }

        @Override
        public void onItemSelected() {
            itemView.setBackgroundColor(Color.LTGRAY);
        }

        @Override
        public void onItemClear() {
            itemView.setBackgroundColor(0);
        }
    }
}


