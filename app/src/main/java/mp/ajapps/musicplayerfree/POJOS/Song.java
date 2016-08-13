package mp.ajapps.musicplayerfree.POJOS;

public class Song {
    public long mSongId;
    public String mSongName;
    public String mAlbumName;

    public Song(final long songId, final String songName, final String albumName) {
        mSongId = songId;
        mSongName = songName;
        mAlbumName = albumName;
    }

}
