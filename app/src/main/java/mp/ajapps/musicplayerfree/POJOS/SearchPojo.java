package mp.ajapps.musicplayerfree.POJOS;

/**
 * Created by Sharing Happiness on 12/11/2015.
 */
public class SearchPojo {
    String title, artist,  album_art;
    int mType;
    long mId = 0;

    public SearchPojo() {
    }

    public SearchPojo(String title, String artist, long mId,
                      String album_art, int mType) {
        super();
        this.title = title;
        this.artist = artist;
        this.mId = mId;
        this.album_art = album_art;
        this.mType = mType;
    }

    public String getAlbum_art() {
        return album_art;
    }

    public void setAlbum_art(String album_art) {
        this.album_art = album_art;
    }

    public SearchPojo(int s) {
        this.mType = s;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public long getmId() {
        return mId;
    }

    public void setmId(long mId) {
        this.mId = mId;
    }
}