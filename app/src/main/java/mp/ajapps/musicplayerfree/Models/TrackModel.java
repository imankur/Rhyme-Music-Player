package mp.ajapps.musicplayerfree.Models;

/**
 * Created by Sharing Happiness on 6/17/2015.
 */
public class TrackModel extends Object{
    private long id;
    private String mTrackName, mTrackDetail;

    public TrackModel(long id, String mTrackName, String mTrackDetail) {
        this.id = id;
        this.mTrackName = mTrackName;
        this.mTrackDetail = mTrackDetail;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getmTrackDetail() {
        return mTrackDetail;
    }

    public void setmTrackDetail(String mTrackDetail) {
        this.mTrackDetail = mTrackDetail;
    }

    public String getmTrackName() {
        return mTrackName;
    }

    public void setmTrackName(String mTrackName) {
        this.mTrackName = mTrackName;
    }
}
