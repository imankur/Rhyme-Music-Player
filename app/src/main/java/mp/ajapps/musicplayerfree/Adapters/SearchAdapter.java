package mp.ajapps.musicplayerfree.Adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import mp.ajapps.musicplayerfree.POJOS.SearchPojo;
import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 12/2/2015.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    //public static final int ITEM_HEADER_TRACK = 1;
    // static final int ITEM_HEADER_ALBUM = 1;
    public static final int ITEM_ALBUM = 4;
    public static final int ITEM_TRACK = 3;

    ArrayList<SearchPojo> mList = new ArrayList<SearchPojo>();
    private myOnCLickInterface mMyOnClick;

    public SearchAdapter(myOnCLickInterface m) {
        this.mMyOnClick = m;
    }

    @Override
    public SearchAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TRACK) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_track_row, parent, false);
            return new ViewHolder(v);
        } else if (viewType == ITEM_ALBUM) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grid_item, parent, false);
            return new ViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.header_row, parent, false);
            return new ViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(SearchAdapter.ViewHolder holder, final int position) {
        SearchPojo temp = mList.get(position);
        final int type = temp.getmType();
        final long id = temp.getmId();

        if (type == ITEM_TRACK) {
            holder.mTrackName.setText(temp.getTitle());
            holder.mTrackDetail.setText(temp.getArtist());
        } else if (type == ITEM_ALBUM) {
            ImageLoader.getInstance().displayImage("file:///" + temp.getAlbum_art(), holder.mImg);
            holder.mArtist.setText(temp.getArtist());
            holder.mAlbum.setText(temp.getTitle());
        } else {
            String s = type == 1 ? "Tracks" : "Album";
            holder.mHeader.setText(s);
        }
       /* holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != mMyOnClick) {
                    mMyOnClick.myOnClick(type, id);
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return (mList == null || mList.size() == 0) ? 0 : mList.size();
    }

    public void changeCursor(ArrayList<SearchPojo> cursor) {
        if (mList != null)
            this.mList.clear();
        this.mList = cursor;
    }

    @Override
    public int getItemViewType(int position) {
        return mList.get(position).getmType();
    }

    public interface myOnCLickInterface {
        public void myOnClick(int type, String id);
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        TextView mTrackName, mTrackDetail, mAlbum , mArtist, mHeader;
        ImageView mImg;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mTrackName = (TextView) itemView.findViewById(R.id.textView);
            mTrackDetail = (TextView) itemView.findViewById(R.id.textView2);

            mImg = (ImageView) itemView.findViewById(R.id.image);
            mAlbum = (TextView) itemView.findViewById(R.id.album_name);
            mArtist = (TextView) itemView.findViewById(R.id.album_artist);

            mHeader = (TextView) itemView.findViewById(R.id.textView9);
        }
    }
}

