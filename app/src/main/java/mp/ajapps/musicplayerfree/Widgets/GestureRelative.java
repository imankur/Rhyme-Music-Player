package mp.ajapps.musicplayerfree.Widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

/**
 * Created by Sharing Happiness on 1/20/2016.
 */
public class GestureRelative extends RelativeLayout {
    public GestureRelative(Context context) {
        super(context);
    }

    public GestureRelative(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GestureRelative(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public GestureRelative(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
