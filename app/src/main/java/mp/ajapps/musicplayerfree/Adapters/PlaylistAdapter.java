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
    private SparseBooleanArray selectedItems;

    public PlaylistAdapter() {
        selectedItems = new SparseBooleanArray();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_playlist, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        Playlist m = mPlaylistList.get(position);
        holder.mContentView.setText(m.getmPlaylistName());
        Log.i("ldit", "changeData: -" + mPlaylistList.size());
        /*holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });*/
    }

    @Override
    public int getItemCount() {
        return mPlaylistList.size();
    }

    public void changeData(ArrayList<Playlist> mList) {
        this.mPlaylistList = mList;
        notifyDataSetChanged();
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
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items =
                new ArrayList<Integer>(selectedItems.size());
        for (int i = 0; i < selectedItems.size(); i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
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
