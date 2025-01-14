    package com.example.designtoolproject;

    import android.content.Context;
    import android.graphics.*;
    import android.util.AttributeSet;
    import android.view.MotionEvent;
    import android.view.View;
    import java.util.ArrayList;
    import java.util.List;

    //abstract shape (monitor all shapes)
    abstract class Shape {
        protected Paint paint;

        public Shape(Paint paint) {
            this.paint = paint;
        }

        public abstract void draw(Canvas canvas);

        public abstract void update(float x, float y);
    }

    //circle shape
    class Circle extends Shape {

        private float centerX, centerY, radius;

        public Circle(float startX, float startY, Paint paint) {
            super(paint);
            this.centerX = startX;
            this.centerY = startY;
            this.radius = 0;
        }

        @Override
        public void draw(Canvas canvas) {
            if (radius > 0) {
                canvas.drawCircle(centerX, centerY, radius, paint);
            }
        }

        @Override
        public void update(float x, float y) {
            float dx = x - centerX;
            float dy = y - centerY;
            this.radius = (float) Math.sqrt(dx * dx + dy * dy);//calc radius (distance formula :)
        }
    }

    //rectangle shape
    class Rectangle extends Shape {

        private float startX, startY, endX, endY;

        public Rectangle(float startX, float startY, Paint paint) {
            super(paint);
            this.startX = startX;
            this.startY = startY;
            this.endX = startX;
            this.endY = startY;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRect(startX, startY, endX, endY, paint);
        }

        @Override
        public void update(float x, float y) {
            this.endX = x;
            this.endY = y;
        }
    }

    //line shape
    class Line extends Shape {

        private float startX, startY, endX, endY;

        public Line(float startX, float startY, Paint paint) {
            super(paint);
            this.startX = startX;
            this.startY = startY;
            this.endX = startX;
            this.endY = startY;
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawLine(startX, startY, endX, endY, paint);
        }

        @Override
        public void update(float x, float y) {
            this.endX = x;
            this.endY = y;
        }
    }

    //free draw (pencil)
    class PathShape extends Shape {

        private Path path;

        public PathShape(Paint paint) {
            super(paint);
            this.path = new Path();
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public void update(float x, float y) {
            path.lineTo(x, y);
        }

        public void start(float x, float y) {
            path.moveTo(x, y);
        }
    }

    //canvas view class (the canvas)
    public class CanvasView extends View {
        private Paint paint;
        private Bitmap bitmap;
        private Canvas bitmapCanvas;
        private String drawingMode;
        private Shape currentShape;
        private List<Shape> shapes;

        public CanvasView(Context context) {
            super(context);
            init();
        }

        public CanvasView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            paint = new Paint();
            paint.setColor(0xFF000000);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5f);
            drawingMode = "pencil";
            shapes = new ArrayList<>();
        }

        public void setMode(String mode) {
            this.drawingMode = mode;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //draw bitmap
            canvas.drawBitmap(bitmap, 0, 0, null);

            //draw each shapes
            for (Shape shape : shapes) {
                shape.draw(canvas);
            }

            //draw current shape
            if (currentShape != null) {
                currentShape.draw(canvas);
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    currentShape = createShape(x, y);
                    if (currentShape instanceof PathShape) {
                        ((PathShape) currentShape).start(x, y);
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    if (currentShape != null) {
                        currentShape.update(x, y);
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    if (currentShape != null) {
                        currentShape.draw(bitmapCanvas);
                        shapes.add(currentShape);
                        currentShape = null;
                    }
                    break;
            }

            invalidate();
            return true;
        }

        private Shape createShape(float x, float y) {
            switch (drawingMode) {
                case "circle":
                    return new Circle(x, y, paint);
                case "rectangle":
                    return new Rectangle(x, y, paint);
                case "line":
                    return new Line(x, y, paint);
                case "pencil":
                    return new PathShape(paint);
                default:
                    throw new IllegalArgumentException("Unknown drawing mode: " + drawingMode);
            }
        }
    }
