package com.example.designtoolproject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class CanvasView extends View {
    private Paint paint;
    private Path path;
    private float startX, startY, currentX, currentY, radius;
    private String drawingMode;

    public CanvasView(Context context) {
        super(context);
        init();
    }

    public CanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void setMode(String mode) {
        this.drawingMode = mode;
    }

    public String getMode() {
        return this.drawingMode;
    }

    public void init() {
        paint = new Paint();
        paint.setColor(0xFF000000); // Blue color
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5f);
        setMode("pencil");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(0xFFFFFFFF); // White background

        switch (drawingMode) {
            case "circle":
                if (radius > 0) {
                    canvas.drawCircle(startX, startY, radius, paint);//Draw a circle
                }
                break;
            case "rectangle":
                canvas.drawRect(startX, startY, currentX, currentY, paint);//Draw a rectangle
                break;
            case "line":
                canvas.drawLine(startX, startY, currentX, currentY, paint);//Draw a line
                break;
            case "pencil":
                if (path != null) {
                    canvas.drawPath(path, paint);
                }
                break;
            case "brush":
                Paint brushPaint = new Paint(paint);
                brushPaint.setStrokeWidth(15f);
                if (path != null) {
                    canvas.drawPath(path, brushPaint);
                }
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                if ("pencil".equals(drawingMode) || "brush".equals(drawingMode)) {
                    path = new Path();
                    path.moveTo(startX, startY);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                currentX = event.getX();
                currentY = event.getY();

                if ("circle".equals(drawingMode)) {
                    float dx = currentX - startX;
                    float dy = currentY - startY;
                    radius = (float) Math.sqrt(dx * dx + dy * dy);
                }
                else if ("rectangle".equals(drawingMode) || "line".equals(drawingMode)) {
                    invalidate();
                }
                else if ("pencil".equals(drawingMode) || "brush".equals(drawingMode)) {
                    if (path != null) {
                        path.lineTo(currentX, currentY);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                if ("pencil".equals(drawingMode) || "brush".equals(drawingMode)) {
                    if (path != null) {
                        path.lineTo(event.getX(), event.getY());
                    }
                }
                break;
        }

        invalidate();
        return true;
    }

}
