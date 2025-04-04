    package com.example.designtoolproject;

    import android.content.Context;
    import android.graphics.*;
    import android.util.AttributeSet;
    import android.util.Base64;
    import android.util.Log;
    import android.view.MotionEvent;
    import android.view.View;
    import android.widget.LinearLayout;

    import java.io.ByteArrayOutputStream;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.Stack;

    //abstract shape (monitor all shapes)
    abstract class Shape {
        protected Paint paint;
        protected int id;

        public Shape(Paint paint) {
            this.paint = paint;
        }

        public abstract void draw(Canvas canvas);

        public abstract void update(float x, float y);

        public abstract boolean contains(float x, float y);

        public abstract RectF getBounds();
    }

    //circle shape
    class Circle extends Shape {

        private float centerX, centerY, radius;


        public float getCenterX() {
            return this.centerX;
        }

        public float getCenterY() {
            return this.centerY;
        }

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

        @Override
        public boolean contains(float x, float y) {
            float distance = (float) Math.sqrt((x-centerX)*(x-centerX) + (y-centerY)*(y-centerY));
            return Math.abs(distance-radius) <= 50;//check if the given point is on the circle
        }

        @Override
        public RectF getBounds() {
            return new RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
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

        @Override
        public boolean contains(float x, float y) {
            //margin for user inaccuracy (30 pixels)
            float margin = 30;

            boolean nearLeftEdge = (x >= startX - margin) && (x <= startX + margin) && (y >= startY) && (y <= endY);
            boolean nearRightEdge = (x >= endX - margin) && (x <= endX + margin) && (y >= startY) && (y <= endY);
            boolean nearTopEdge = (y >= startY - margin) && (y <= startY + margin) && (x >= startX) && (x <= endX);
            boolean nearBottomEdge = (y >= endY - margin) && (y <= endY + margin) && (x >= startX) && (x <= endX);

            return nearLeftEdge || nearRightEdge || nearTopEdge || nearBottomEdge;
        }

        public float getStartX() { return startX; }
        public float getStartY() { return startY; }
        public float getEndX() { return endX; }
        public float getEndY() { return endY; }

        @Override
        public RectF getBounds() {
            return new RectF(Math.min(startX, endX), Math.min(startY, endY), Math.max(startX, endX), Math.max(startY, endY));
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

        @Override
        public boolean contains(float x, float y) {
            float dx = endX - startX;
            float dy = endY - startY;
            float lineLength = (float) Math.sqrt(dx * dx + dy * dy);

            //handle case where line length is zero (start and end points are the same)
            if (lineLength == 0) {
                return Math.abs(x - startX) < 10 && Math.abs(y - startY) < 10; // 10 is a threshold for selection
            }

            //calculate perpendicular distance from point (x, y) to the line
            float distance = Math.abs(dy * x - dx * y + endX * startY - endY * startX) / lineLength;

            return distance < 20;//10 is the threshold distance for selecting the line
        }

        public float getStartX() { return startX; }
        public float getStartY() { return startY; }
        public float getEndX() { return endX; }
        public float getEndY() { return endY; }

        @Override
        public RectF getBounds() {
            return new RectF(Math.min(startX, endX), Math.min(startY, endY), Math.max(startX, endX), Math.max(startY, endY));
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
        @Override
        public boolean contains(float x, float y) {
            //get the bounding box of the path
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);

            //check if the point is within the bounding box
            return bounds.contains(x, y);
        }
        public void start(float x, float y) {
            path.moveTo(x, y);
        }
        @Override
        public RectF getBounds() {
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);
            return bounds;
        }
    }

    //canvas view class (the canvas)
    public class CanvasView extends View {
        private Paint currentPaint;
        private Bitmap bitmap;
        private Canvas bitmapCanvas;
        private String drawingMode;
        private Shape currentShape, selectedShape;
        private List<Shape> shapes;
        private Stack<Shape> redoStack = new Stack<>();
        private RectF toolbarRect;
        private boolean isToolbarVisible = false;
        private final int TOOLBAR_WIDTH = 150;
        private final int TOOLBAR_HEIGHT = 60;
        private Bitmap loadedBitmap = null;//external bitmap to load

        public CanvasView(Context context) {
            super(context);
            init();
        }

        public CanvasView(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }

        private void init() {
            currentPaint = new Paint();
            setColor(0xff000000);
            setCurrentPaint(1);
            drawingMode = "pencil";
            shapes = new ArrayList<>();
        }

        public void setColor(int color) {
            currentPaint.setColor(color);
            invalidate();
        }

        public void setCurrentPaint(int m) {
            switch (m) {
                case 1:
                    currentPaint.setStyle(Paint.Style.STROKE);
                    currentPaint.setStrokeWidth(5f);
                    break;
                case 2:
                    currentPaint.setStyle(Paint.Style.STROKE);
                    currentPaint.setStrokeWidth(10f);
                    break;
                case 3:
                    currentPaint.setStyle(Paint.Style.FILL);
                    currentPaint.setStrokeWidth(15f);
                    break;
            }
        }

        public void setMode(String mode) {
            this.drawingMode = mode;
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(bitmap);
            redrawBitmap();//redraw shapes into the new bitmap
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawBitmap(bitmap, 0, 0, null);

            //draw all shapes
            for (Shape shape : shapes) {
                shape.draw(canvas);
            }

            //draw the shape while it’s being drawn
            if (currentShape != null) {
                currentShape.draw(canvas);
            }

            //draw selection border and toolbar
            if (selectedShape != null) {
                Paint highlightPaint = new Paint();
                highlightPaint.setColor(Color.RED);
                highlightPaint.setStyle(Paint.Style.STROKE);
                highlightPaint.setStrokeWidth(5f);
                RectF bounds = selectedShape.getBounds();
                canvas.drawRect(bounds, highlightPaint);

                //draw toolbar
                float toolbarX = bounds.right + 20;
                float toolbarY = bounds.top;
                toolbarRect = new RectF(toolbarX, toolbarY, toolbarX + TOOLBAR_WIDTH, toolbarY + TOOLBAR_HEIGHT);
                Paint toolbarPaint = new Paint();
                toolbarPaint.setColor(Color.LTGRAY);
                canvas.drawRoundRect(toolbarRect, 10, 10, toolbarPaint);

                //draw delete icon
                Paint textPaint = new Paint();
                textPaint.setColor(Color.BLACK);
                textPaint.setTextSize(40);
                canvas.drawText("🗑", toolbarX + 50, toolbarY + 45, textPaint);

                isToolbarVisible = true;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            if (drawingMode.equals("edit")) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (isToolbarVisible && toolbarRect.contains(x, y)) {
                        //delete shape
                        shapes.remove(selectedShape);
                        selectedShape = null;
                        isToolbarVisible = false;
                        redrawBitmap();//ensure the shape is erased from the bitmap
                        return true;
                    }
                    selectedShape = null;
                    for (Shape shape : shapes) {
                        if (shape.contains(x, y)) {
                            selectedShape = shape;
                            break;//stop checking after finding a shape
                        }
                    }
                    invalidate();
                    return true;
                }
            } else {
                selectedShape = null;
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
                            invalidate();//update in real-time
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (currentShape != null) {
                            currentShape.draw(bitmapCanvas);//draw the final shape onto the bitmap
                            shapes.add(currentShape);
                            currentShape = null;
                            redrawBitmap();//ensure the shape is properly saved
                        }
                        break;
                }
            }
            invalidate();
            return true;
        }

        private Shape createShape(float x, float y) {
            Paint shapePaint = new Paint(currentPaint);//create a new paint for the current shape
            switch (drawingMode) {
                case "circle":
                    return new Circle(x, y, shapePaint);
                case "rectangle":
                    return new Rectangle(x, y, shapePaint);
                case "line":
                    return new Line(x, y, shapePaint);
                case "pencil":
                    return new PathShape(shapePaint);
                default:
                    throw new IllegalArgumentException("Unknown drawing mode: " + drawingMode);
            }
        }

        public void setBitmap(Bitmap newBitmap) {
            this.loadedBitmap = newBitmap.copy(Bitmap.Config.ARGB_8888, true);
            this.bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            this.bitmapCanvas = new Canvas(this.bitmap);
            redrawBitmap();
        }


        public void undo() {
            if (!shapes.isEmpty()) {
                Shape lastShape = shapes.get(shapes.size() - 1);//get last shape
                redoStack.push(lastShape);//push to redo stack
                shapes.remove(shapes.size() - 1);//remove from list
                redrawBitmap();//draw list after changes
            }
        }

        public void redo() {
            if (!redoStack.isEmpty()) {
                Shape shape = redoStack.pop();
                shapes.add(shape);
                redrawBitmap();//redraw shapes after pop
            }
        }

        private void redrawBitmap() {
            if (bitmapCanvas != null) {
                bitmap.eraseColor(Color.TRANSPARENT);//clear bitmap
                bitmapCanvas.drawColor(Color.WHITE);//draw background

                if (loadedBitmap != null) {//make sure to load the external drawing if there is
                    bitmapCanvas.drawBitmap(loadedBitmap, 0, 0, null); // ✅ draw loaded image as base
                }

                //redraw all shapes onto the bitmap
                for (Shape shape : shapes) {
                    shape.draw(bitmapCanvas);
                }
                invalidate();//refresh view
            }
        }
    }
