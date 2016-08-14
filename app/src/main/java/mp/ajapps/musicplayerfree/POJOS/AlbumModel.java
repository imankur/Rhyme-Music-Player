package mp.ajapps.musicplayerfree.POJOS;

/**
 * Created by Sharing Happiness on 7/27/2015.
 */
public class AlbumModel {
    public long mAlbumId;
    public String mAlbumName;
    public String mArtistName;
    public String mArt;

    public AlbumModel(long mAlbumId, String mAlbumName, String mArtistName, String mArt) {
        this.mAlbumId = mAlbumId;
        this.mAlbumName = mAlbumName;
        this.mArtistName = mArtistName;
        this.mArt = mArt;
    }
}
