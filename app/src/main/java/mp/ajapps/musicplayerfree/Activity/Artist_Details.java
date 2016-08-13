package mp.ajapps.musicplayerfree.Activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import mp.ajapps.musicplayerfree.Adapters.AlbumAdapter;
import mp.ajapps.musicplayerfree.Adapters.ArtistTrackAdapter;
import mp.ajapps.musicplayerfree.Adapters.SearchAdapter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.POJOS.SearchPojo;
import mp.ajapps.musicplayerfree.R;

public class Artist_Details extends AppCompatActivity implements SearchAdapter.myOnCLickInterface {
    private RecyclerView mAlbumList;
    private SearchAdapter mAlbumAdapter;
    private ArrayList<SearchPojo> mData = new ArrayList<>();
    long id; String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist__details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        name = getIntent().getStringExtra("mName");
        id = getIntent().getLongExtra("mId",0);
        getSupportActionBar().setTitle(name);

        mAlbumList = (RecyclerView) findViewById(R.id.albumlist);
        mAlbumAdapter = new SearchAdapter(this);
        GridLayoutManager mGLM = new GridLayoutManager(this,3);
        mGLM.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return getSpanSizeOfView(position);
            }
        });
        mAlbumList.setLayoutManager(mGLM);

        mAlbumList.setAdapter(mAlbumAdapter);
        getData();


    }
    private int getSpanSizeOfView (int pos) {
        int type = mAlbumAdapter.getItemViewType(pos);
        if (type == 1 || type == 2 || type == 3) {
            return 3;
        } else  {
            return 1;
        }
    }
    private void getData () {
        ArrayList<SearchPojo> mALbum = MusicUtils.getAlbumListOfArtist(this, id);
        if (mALbum.size() > 0) {
            mData.add(new SearchPojo(2));
            mData.addAll(mALbum);
        }
        ArrayList<SearchPojo> mTrack = MusicUtils.getSongsListOfArtist(this, name);
        if (mTrack.size() > 0) {
            mData.add(new SearchPojo(1));
            mData.addAll(mTrack);
        }
        mAlbumAdapter.changeCursor(mData);
        mAlbumAdapter.notifyDataSetChanged();
    }

    @Override
    public void myOnClick(int type, String id) {

    }
}
