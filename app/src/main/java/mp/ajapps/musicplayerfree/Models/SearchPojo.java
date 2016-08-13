package mp.ajapps.musicplayerfree.Models;

/**
 * Created by Sharing Happiness on 12/11/2015.
 */
public class SearchPojo {
    private String title, artist, mId = null;
    private int mType;

    public SearchPojo (){
    }

    public SearchPojo (int s){
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

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }
}
