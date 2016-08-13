package mp.ajapps.musicplayerfree.Widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

/**
 * Created by Sharing Happiness on 8/1/2015.
 */
public class SquareView extends LinearLayout {

    /**
     * @param context The {@link Context} to use
     * @param attrs   The attributes of the XML tag that is inflating the view.
     */
    public SquareView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(final boolean changed, final int l, final int u, final int r,
                            final int d) {
        getChildAt(0).layout(0, 0, r - l, d - u);
    }

}
