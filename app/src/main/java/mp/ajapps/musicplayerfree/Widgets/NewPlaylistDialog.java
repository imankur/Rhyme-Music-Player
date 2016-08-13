package mp.ajapps.musicplayerfree.Widgets;


import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 12/21/2015.
 */
public class NewPlaylistDialog extends DialogFragment {
    private EditText mEditText;
    private long[] mList;
    private boolean hasData = false;

    public static final long createPlaylist(final Context context, final String name) {
        if (name != null && name.length() > 0) {
            final ContentResolver resolver = context.getContentResolver();
            final String[] projection = new String[]{
                    MediaStore.Audio.PlaylistsColumns.NAME
            };
            final String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
            Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    projection, selection, null, null);
            if (cursor.getCount() <= 0) {
                final ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                final Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                return Long.parseLong(uri.getLastPathSegment());
            } else {
                //Toast.makeText(ge)
            }
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
            return -1;
        }
        return -1;
    }

    public void setData(long[] list) {
        mList = list;
        hasData = true;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Create Playlist");
        mEditText = new EditText(getActivity());

        mEditText.setSingleLine(true);
        mEditText.setHint("Name");
        mEditText.setTextColor(Color.parseColor("#26a69a"));
        mEditText.setInputType(mEditText.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                long n = createPlaylist(getActivity(), mEditText.getText().toString());
                if (n >0) {
                    MusicUtils.addToPlaylist(getActivity(), mList, n);
                    getDialog().dismiss();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setView(mEditText);
        return builder.create();
    }
}
