package mp.ajapps.musicplayerfree.Adapters;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 1/1/2016.
 */
public class Album_Details_Adapter  extends RecyclerView.Adapter<Album_Details_Adapter.TrackHolder>  {
    Cursor dataCursor;
    private myOnCLickInterface mMyOnClick;
    public Album_Details_Adapter(myOnCLickInterface m) {
        this.mMyOnClick = m;
    }

    @Override
    public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
                if (null != mMyOnClick) {
                    mMyOnClick.myOnClick(position);
                }
            }
        });
        holder.mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mMyOnClick.myOnLongClick(position);
                return true;
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

    public interface myOnCLickInterface {
        public void myOnClick(int pos);
        public void myOnLongClick(int pos);
    }

    protected class TrackHolder extends RecyclerView.ViewHolder {
        public final View mView;
        TextView mTrackName, mTrackDetail, mTrachNum;

        public TrackHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mTrackName = (TextView) itemView.findViewById(R.id.textView);
            mTrackDetail = (TextView) itemView.findViewById(R.id.textView1);
            mTrachNum = (TextView) itemView.findViewById(R.id.textView3);
        }
    }

}
