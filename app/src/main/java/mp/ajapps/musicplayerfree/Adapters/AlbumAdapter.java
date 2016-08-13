package mp.ajapps.musicplayerfree.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.lang.ref.WeakReference;

import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 7/28/2015.
 */
public class AlbumAdapter extends BaseAdapter {
    Context context;
    Cursor mCursor;
    private int mAlbumIdx;
    private int mArtistIdx;
    private int mAlbumArtIndex;
    ImageLoader imageLoader;

    DisplayImageOptions options;
    public AlbumAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }

        getColumnIndices(cursor);
        Cursor oldCursor = mCursor;
        this.mCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();
        }
        return oldCursor;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        this.mCursor.moveToPosition(position);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Viewholder viewholder;// = new Viewholder();
        String v = "file:///" + this.mCursor.getString(mAlbumArtIndex);
        if (convertView != null) {
            viewholder = (Viewholder) convertView.getTag();
            viewholder.image.get().setImageResource(R.drawable.default_artwork);
        } else {
            viewholder = new Viewholder();
            convertView = inflater.inflate(R.layout.grid_item,null);
            viewholder.image = new WeakReference<ImageView>((ImageView)convertView.findViewById(R.id.image));
            viewholder.malbumArtist = new WeakReference<TextView>((TextView) convertView.findViewById(R.id.album_artist));
            viewholder.malbumName = new WeakReference<TextView>((TextView) convertView.findViewById(R.id.album_name));
            convertView.setTag(viewholder);
        }
        //String v = "file:///" + this.mCursor.getString(mAlbumArtIndex);
        ImageLoader.getInstance().displayImage(v, viewholder.image.get(), options);
        viewholder.malbumArtist.get().setText(this.mCursor.getString(mArtistIdx));
        viewholder.malbumName.get().setText(this.mCursor.getString(mAlbumIdx));
        return convertView;
    }
    private class Viewholder{
        WeakReference<ImageView> image;
        WeakReference<TextView> malbumName, malbumArtist;
    }

    private void getColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
            mAlbumArtIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
        }
    }

}
