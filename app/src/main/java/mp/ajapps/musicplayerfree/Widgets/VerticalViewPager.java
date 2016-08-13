package mp.ajapps.musicplayerfree.Widgets;

import android.content.Context;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewParent;

/**
 * Created by Sharing Happiness on 12/23/2015.
 */
public class VerticalViewPager extends ViewPager {
    // TODO Remove the hack of using a parent view pager
    private ViewPager mParentViewPager;
    private float mLastMotionX;
    private float mLastMotionY;
    private float mTouchSlop;
    private boolean mVerticalDrag;
    private boolean mHorizontalDrag;
    // Vertical transit page transformer
    private final ViewPager.PageTransformer mPageTransformer = new ViewPager.PageTransformer() {
        @Override
        public void transformPage(View view, float position) {
            final int pageWidth = view.getWidth();
            final int pageHeight = view.getHeight();
            if (position < -1) {
                // This page is way off-screen to the left.
                view.setAlpha(0);
            } else if (position <= 1) {
                view.setAlpha(1);
                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // set Y position to swipe in from top
                float yPosition = position * pageHeight;
                view.setTranslationY(yPosition);
            } else {
                // This page is way off-screen to the right.
                view.setAlpha(0);
            }
        }
    };
    public VerticalViewPager(Context context) {
        super(context, null);
    }
    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        init();
    }
    private void init() {
        // Make page transit vertical
        setPageTransformer(true, mPageTransformer);
        // Get rid of the overscroll drawing that happens on the left and right (the ripple)
        setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return false;
    }

    private void initializeParent() {
        if (mParentViewPager == null) {
            // This vertical view pager is nested in the frame layout inside the timer tab
            // (fragment), which is nested inside the horizontal view pager. Therefore,
            // it needs 3 layers to get all the way to the horizontal view pager.
            final ViewParent parent = getParent().getParent().getParent();
            Log.i("wowwww", "onTouchEvent: 88");

            if (parent instanceof ViewPager) {
                Log.i("wowwww", "onTouchEvent: 88" ); ((ViewPager) parent).setCurrentItem(1);
                mParentViewPager = (ViewPager) parent;
            }
        }
    }
    private boolean verticalDrag(MotionEvent ev) {
        final float x = ev.getX();
        final float y = ev.getY();
        ev.setLocation(y, x);
        return super.onTouchEvent(ev);
    }
}