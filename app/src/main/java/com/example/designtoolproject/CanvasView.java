package com.example.designtoolproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
    private Paint paint;
    private float startX, startY, currentX, currentY, radius;

    public CanvasView(Context context) {
        super(context);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        paint = new Paint();
        paint.setColor(0xFF0000FF); // Blue color
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFFFFFFFF);
        if (radius > 0) {
            canvas.drawCircle(startX, startY, radius, paint); // Draw a circle for testing
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        currentX = event.getX();
        currentY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = currentX;
                startY = currentY;
                break;
            case MotionEvent.ACTION_MOVE:
                float dx = currentX - startX;
                float dy = currentY - startY;
                radius = (float) Math.sqrt(dx * dx + dy * dy);
                break;
        }
        invalidate(); // Redraw the view
        return true;
    }
}
