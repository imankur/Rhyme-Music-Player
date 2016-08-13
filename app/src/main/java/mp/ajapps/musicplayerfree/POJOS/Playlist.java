package mp.ajapps.musicplayerfree.POJOS;

/**
 * Created by Sharing Happiness on 12/20/2015.
 */
public class Playlist {


    public long mPlaylistId;
    public String mPlaylistName;
    public int mSongCount;

    public Playlist(final long playlistId, final String playlistName, final int songCount) {
        mPlaylistId = playlistId;
        mPlaylistName = playlistName;
        mSongCount = songCount;
    }

    public long getmPlaylistId() {
        return mPlaylistId;
    }

    public String getmPlaylistName() {
        return mPlaylistName;
    }

    public int getmSongCount() {
        return mSongCount;
    }

    @Override
    public String toString() {
        return mPlaylistName;
    }
}
