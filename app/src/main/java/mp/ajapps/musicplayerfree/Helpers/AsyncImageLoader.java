package mp.ajapps.musicplayerfree.Helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.google.android.gms.ads.formats.NativeAd;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

import mp.ajapps.musicplayerfree.POJOS.Song;
import mp.ajapps.musicplayerfree.R;

/**
 * Created by Sharing Happiness on 2/13/2016.
 */
public class AsyncImageLoader extends AsyncTask<Void, Void, Void> {
    long id;
    ImageView mView;
    String path = "empty";
    Context c;
    Song s;
int pos;
    public AsyncImageLoader(long id, ImageView imageView, Context c, Song s, int pos) {
        this.id = id;
        this.mView = imageView;
        this.c = c;
        this.pos = pos;
        this.s = s;
    }

    @Override
    protected Void doInBackground(Void... params) {
        String s = MusicUtils.getAlbumArt(c, id);
        if (s != null) path = "file:///" + s;
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        this.s.mArt = path;
        Integer ints = new Integer(pos);
        if (ints.equals(mView.getTag()))
            ImageLoader.getInstance().displayImage(path, mView);
    }
}
