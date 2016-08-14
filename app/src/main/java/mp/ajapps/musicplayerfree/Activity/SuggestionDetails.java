package mp.ajapps.musicplayerfree.Activity;

import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import mp.ajapps.musicplayerfree.Adapters.RecentsAdapter;
import mp.ajapps.musicplayerfree.Adapters.TrackAdapter;
import mp.ajapps.musicplayerfree.Helpers.CursorSorter;
import mp.ajapps.musicplayerfree.Helpers.MusicUtils;
import mp.ajapps.musicplayerfree.Helpers.RecentStore;
import mp.ajapps.musicplayerfree.R;

public class SuggestionDetails extends AppCompatActivity implements TrackAdapter.myOnCLickInterface {
    Cursor c;
    private LinearLayoutManager mLayoutManager;
    private RecentsAdapter mAdpt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestion_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        RecyclerView mRecycleView = (RecyclerView) findViewById(R.id.list);
        mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mLayoutManager.setSmoothScrollbarEnabled(true);

        mRecycleView.setLayoutManager(mLayoutManager);
        mAdpt = new RecentsAdapter();
        if (getIntent().getIntExtra("type", 1) == 1) {
            c = makeRecentTracksCursor();
            getSupportActionBar().setTitle("Recent Tracks");
        } else {
            getSupportActionBar().setTitle("Recent Played");
            c = MusicUtils.makeLastAddedCursor(this);
        }
        mAdpt.changeCursor(c);
        mRecycleView.setAdapter(mAdpt);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @Override
    public void myOnClick(int pos, View v) {}

    @Override
    public void myOnLongClick(int pos,  View v) {}

    public Cursor getCursor() {
        CursorSorter cs = makeRecentTracksCursor();
        if (cs != null) {
            ArrayList<Long> missingIds = cs.getMissingIds();
            if (missingIds != null && missingIds.size() > 0) {
                for (long id : missingIds) {
                    RecentStore.getInstance(this).removeItem(id);
                }
            }
        }
        return cs;
    }

    private CursorSorter makeRecentTracksCursor() {
        Cursor songs = RecentStore.getInstance(this).queryRecentIds();
        try {
            return MusicUtils.makeSortedCursor(this, songs,
                    songs.getColumnIndex("songid"));
        } finally {
            if (songs != null) {
                songs.close();
                songs = null;
            }
        }
    }
}
