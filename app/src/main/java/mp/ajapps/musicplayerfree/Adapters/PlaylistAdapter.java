package mp.ajapps.musicplayerfree.Adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import mp.ajapps.musicplayerfree.POJOS.Playlist;
import mp.ajapps.musicplayerfree.R;


public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private List<Playlist> mPlaylistList = new ArrayList<Playlist>();
    private clickManger mManager;

    public PlaylistAdapter(clickManger manage) {
        this.mManager = manage;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Playlist m = mPlaylistList.get(position);
        holder.mContentView.setText(m.getmPlaylistName());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.onCilckEvent(position, m.getmPlaylistId(), m.getmPlaylistName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mPlaylistList.size();
    }

    public void changeData(ArrayList<Playlist> mList) {
        this.mPlaylistList = mList;
        notifyDataSetChanged();
    }

    public interface clickManger {
        public void onCilckEvent(int position, long id, String mName);
    }
    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }


}
