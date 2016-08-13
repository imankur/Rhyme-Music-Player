package mp.ajapps.musicplayerfree.Adapters;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 6/17/2015.
 */
public class TrackAdapter extends RecyclerView.Adapter<TrackAdapter.TrackHolder > {
    Cursor dataCursor;
    int rowId;
    private myOnCLickInterface mMyOnClick;

    @Override
    public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(rowId, parent, false);
        return new TrackHolder(v);
    }

    public TrackAdapter(int id, myOnCLickInterface m) {
        this.rowId=id;
        this.mMyOnClick = m;
    }

    @Override
    public void onBindViewHolder(TrackHolder holder, final int position) {
        this.dataCursor.moveToPosition(position);
        holder.mTrackName.setText(this.dataCursor.getString(this.dataCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)));
        holder.mTrackDetail.setText(this.dataCursor.getString(this.dataCursor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.ARTIST)));
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mMyOnClick) {

                    mMyOnClick.myOnClick(position);
                }
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
        TextView mTrackName, mTrackDetail;
        public final View mView;
        public TrackHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTrackName = (TextView) itemView.findViewById(R.id.textView);
            mTrackDetail = (TextView) itemView.findViewById(R.id.textView2);
        }
    }

    public interface myOnCLickInterface {
        public void myOnClick(int pos);
    }
}


