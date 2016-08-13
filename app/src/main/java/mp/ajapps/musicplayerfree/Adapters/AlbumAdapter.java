package mp.ajapps.musicplayerfree.Adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import java.lang.ref.WeakReference;

import mp.ajapps.musicplayerfree.Activity.Album_Details;
import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 1/11/2016.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    Context context;
    Cursor mCursor;
    DisplayImageOptions options;
    private int mAlbumIdx;
    private int mArtistIdx;
    private int mAlbumArtIndex;
    private int mID;

    public AlbumAdapter (Context context) {
        this.context = context;
        options = new DisplayImageOptions.Builder().displayer(new FadeInBitmapDisplayer(800)).showImageForEmptyUri(R.drawable.default_artwork).showImageOnFail(R.drawable.default_artwork).build();
    }

    @Override
    public AlbumAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.grid_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AlbumAdapter.ViewHolder holder, int position) {
        holder.image.setImageResource(R.drawable.default_artwork);
        this.mCursor.moveToPosition(position);
        final String mArt = this.mCursor.getString(mAlbumArtIndex);
        final long id = mCursor.getLong(mID);
        final String mAlbum = mCursor.getString(mAlbumIdx);
        final String mArtist = mCursor.getString(mArtistIdx);
        ImageLoader.getInstance().displayImage("file:///" + mArt, holder.image, options);
        holder.malbumArtist.setText(this.mCursor.getString(mArtistIdx));
        holder.malbumName.setText(mAlbum);
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDetails(id, mAlbum, mArtist, mArt);
            }
        });
    }

    private void callDetails (long id, String mAlbum, String mArtist, String mArt) {
        Intent intent = new Intent(context, Album_Details.class);
        final Bundle bundle = new Bundle();
        bundle.putLong("id", id);
        bundle.putString("album", mAlbum);
        bundle.putString("artist", mArtist);
        bundle.putString("art", mArt);
        intent.putExtras(bundle);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        View v;
        TextView malbumName, malbumArtist;
        public ViewHolder(View itemView) {
            super(itemView);
            v =itemView;
            this.image = (ImageView) itemView.findViewById(R.id.image);
            this.malbumArtist = (TextView) itemView.findViewById(R.id.album_artist);
            this.malbumName = (TextView) itemView.findViewById(R.id.album_name);
        }
    }

    private void getColumnIndices(Cursor cursor) {
        if (cursor != null) {
            mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
            mAlbumArtIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
            mID = cursor.getColumnIndexOrThrow(BaseColumns._ID);
        }
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
}
