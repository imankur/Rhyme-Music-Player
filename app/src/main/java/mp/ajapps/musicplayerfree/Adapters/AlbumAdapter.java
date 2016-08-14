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
import java.util.ArrayList;

import mp.ajapps.musicplayerfree.Activity.Album_Details;
import mp.ajapps.musicplayerfree.POJOS.AlbumModel;
import mp.ajapps.musicplayerfree.POJOS.Song;
import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 1/11/2016.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ViewHolder> {
    Context context;
    DisplayImageOptions options;
    private ArrayList<AlbumModel> mList = new ArrayList<>();

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
        final AlbumModel mModel = mList.get(position);
        ImageLoader.getInstance().displayImage("file:///" + mModel.mArt, holder.image, options);
        holder.malbumArtist.setText(mModel.mArtistName);
        holder.malbumName.setText(mModel.mAlbumName);
        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDetails(mModel.mAlbumId, mModel.mAlbumName, mModel.mArtistName, mModel.mArt);
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
        return (mList == null) ? 0 : mList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        View v;
        TextView malbumName, malbumArtist;
        public ViewHolder(View itemView) {
            super(itemView);
            v = itemView;
            this.image = (ImageView) itemView.findViewById(R.id.image);
            this.malbumArtist = (TextView) itemView.findViewById(R.id.album_artist);
            this.malbumName = (TextView) itemView.findViewById(R.id.album_name);
        }
    }


    public void changeCursor(Cursor cursor) {
        if (cursor != null) {
            int mAlbumIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM);
            int mArtistIdx = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST);
            int mAlbumArtIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART);
            int mID = cursor.getColumnIndexOrThrow(BaseColumns._ID);

            while (cursor.moveToNext()) {
                mList.add(new AlbumModel(cursor.getLong(mID), cursor.getString(mAlbumIdx), cursor.getString(mArtistIdx), cursor.getString(mAlbumArtIndex)));
            }
            this.notifyDataSetChanged();

        }
    }

}
