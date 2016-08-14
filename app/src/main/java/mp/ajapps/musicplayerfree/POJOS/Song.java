package mp.ajapps.musicplayerfree.POJOS;

import android.content.Context;

import mp.ajapps.musicplayerfree.Helpers.MusicUtils;

public class Song {
    public long mSongId, getmAlbumId;
    public String mSongName;
    public String mAlbumName;
    public String mArt = null;

    public Song(final long songId, final String songName, final String albumName, final long getmAlbumId) {
        mSongId = songId;
        mSongName = songName;
        mAlbumName = albumName;
        this.getmAlbumId = getmAlbumId;
    }

    public void setAlbumArt(Context c) {
        this.mArt = MusicUtils.getAlbumArt(c, getmAlbumId);
    }

}
