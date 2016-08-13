package mp.ajapps.musicplayerfree.Widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Sharing Happiness on 12/7/2015.
 */
public class MyView extends View {

    Context c;

    public MyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        c = context;
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        c = context;
    }

    public MyView(Context context) {
        super(context);
        c = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x = getWidth();
        int y = (int) Math.ceil(x * 0.6);
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(x, 0);
        path.lineTo(x, y / 2);
        path.lineTo(0, y);
        path.close();

        Paint p = new Paint();
        p.setColor(Color.parseColor("#1e232e"));
        p.setAntiAlias(true);

        canvas.drawPath(path, p);
    }
}
