package mp.ajapps.musicplayerfree.POJOS;

/**
 * Created by Sharing Happiness on 1/5/2016.
 */
public class Suggest {
    public String mHeader = "", playlist_name = "", mAlbum_src = null;
    public String mCount = "";
    public long mAlbumId ;

    public Suggest(String mHeader, String playlist_name, String mCount, long mAlbumId, String mAlbum_src) {
        this.mHeader = mHeader;
        this.playlist_name = playlist_name;
        this.mCount = mCount;
        this.mAlbumId = mAlbumId;
        this.mAlbum_src = mAlbum_src;
    }
}
