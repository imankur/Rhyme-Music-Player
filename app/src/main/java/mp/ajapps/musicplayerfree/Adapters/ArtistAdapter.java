package mp.ajapps.musicplayerfree.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import mp.ajapps.musicplayerfree.POJOS.Artist;
import mp.ajapps.musicplayerfree.R;

public class ArtistAdapter extends RecyclerView.Adapter<ArtistAdapter.ViewHolder> {

    private ArrayList<Artist> mList = new ArrayList<>();
    private ClickManager mManager;

    public ArtistAdapter(ClickManager c) {
        this.mManager = c;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_item, null, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Artist mArtist = mList.get(position);
        holder.mText1.setText(mArtist.mArtistName);
        holder.mText2.setText("Album : " + mArtist.mAlbumNumber);
        holder.mText3.setText("Songs : " + mArtist.mSongNumber);
        holder.mItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.mOnClick(mArtist.mArtistId, mArtist.mArtistName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setdata(ArrayList<Artist> list) {
        if (mList != null) mList = null;
        mList = list;
    }

    public interface ClickManager {
        public void mOnClick(long id, String name);
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView mText1, mText2, mText3;
        public View mItem;
        public ViewHolder(View itemView) {
            super(itemView);
            mText1 = (TextView) itemView.findViewById(R.id.textView12);
            mText2 = (TextView) itemView.findViewById(R.id.textView13);
            mText3 = (TextView) itemView.findViewById(R.id.textView14);
            mItem = itemView;
        }

    }
}
