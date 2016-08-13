package mp.ajapps.musicplayerfree.Adapters;

import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 1/28/2016.
 */
public class ArtistTrackAdapter extends RecyclerView.Adapter<ArtistTrackAdapter.ViewHolder> {
    Cursor mData;

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_track_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        mData.moveToPosition(position);
        holder.mTrackName.setText(mData.getString(mData.getColumnIndex
                (android.provider.MediaStore.Audio.Media.TITLE)));
        holder.mTrackDetail.setText(mData.getString(mData.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
    }

    @Override
    public int getItemCount() {
        return mData.getCount();
    }

    public void setData(Cursor c) {
        if (mData != null) mData.close();
        this.mData = c;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        public View mView;
        TextView mTrackName, mTrackDetail;


        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mTrackName = (TextView) itemView.findViewById(R.id.textView);
            mTrackDetail = (TextView) itemView.findViewById(R.id.textView2);
        }
    }
}
