package mp.ajapps.musicplayerfree.Widgets;

import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import java.util.ArrayList;

import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.Helpers.PlaylistLoader;
import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 12/23/2015.
 */
public class ListPlaylistDailog extends DialogFragment {
    private final ArrayList<Long> mPlaylistList = new ArrayList<Long>();
    private long[] list = null;

    public ListPlaylistDailog setList(long[] list) {
        this.list = list;
        return this;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Cursor mCursor = PlaylistLoader.makePlaylistCursor(getActivity());
        CharSequence [] m = new CharSequence[mCursor.getCount()+1];
        m [0] = "New Playlist";
        if (mCursor != null && mCursor.moveToFirst()) {
            for (int i=1; i<= mCursor.getCount(); i++ ) {
                mPlaylistList.add(mCursor.getLong(0));
                m [i] = mCursor.getString(1);
                mCursor.moveToNext();
            }
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Select Playlist");
        builder.setItems(m, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    NewPlaylistDialog npd = new NewPlaylistDialog();
                    npd.setData(list);
                    npd.show(getFragmentManager(), "yo");
                } else {
                    MusicUtils.addToPlaylist(getActivity(),list, mPlaylistList.get(which-1));
                    getDialog().dismiss();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {}
        });
        return builder.create();
    }
}
