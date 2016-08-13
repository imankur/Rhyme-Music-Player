// IMusicParent.aidl
package mp.ajapps.musicplayerfree;

// Declare any non-default types here with import statements

interface IMusicParent {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

        void setAndPlay(in long [] list, int position); //open()
        long getAudioId();
        void setPlayList(in long[] arr);
        void play();
        int getQueueSize();
        void pagerNextPlay(in int position);
       long[] getSongListForCursor();
       long getAlbumId();
       String getAlbumArt();
       long getQueueItemAtPosition(int position);
       int getQueuePosition();
       String getTrackName();
       String getArtistName();
       long duration();
       void seekSong(long time);
       boolean isPlaying();
       int getPosition();
       void togglePlay();
       void goToNext ();
       void goToPrev ();
       int toggleShuffle();
       int toggleRepeat();
       long[] getQueue();
       void moveQueueItem(int index1, int index2);
       void setAndPlayQueue(int pos);
       int getShuffleMode();
 int getRepeatMode();

       /*// long getIdFromPath(String path);
      //  int getQueuePosition();
        boolean isPlaying();
        void stop();
        void pause();
        void play();
        void prev();
        void next();
        long duration();
        long position();
        long seek(long pos);
        String getTrackName();
        String getAlbumName();
        long getAlbumId();
      //  Bitmap getAlbumBitmap();
        String getArtistName();
        long getArtistId();
       // void enqueue(in long [] list, int action);
      //  long [] getQueue();
        void setQueuePosition(int index);
        String getPath();

        void setShuffleMode(int shufflemode);
        void notifyChange(String what);
        int getShuffleMode();
        int removeTracks(int first, int last);
        void moveQueueItem(int from, int to);
        int removeTrack(long id);
        void setRepeatMode(int repeatmode);
        int getRepeatMode();
        int getMediaMountedCount();
        int getAudioSessionId();
    	void addToFavorites(long id);
    	void removeFromFavorites(long id);
    	boolean isFavorite(long id);
        void toggleFavorite();*/
}
