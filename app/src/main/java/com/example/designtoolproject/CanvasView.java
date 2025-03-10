    package com.example.designtoolproject;

    import android.content.Context;
    import android.graphics.*;
    import android.util.AttributeSet;
    import android.util.Base64;
    import android.util.Log;
    import android.view.MotionEvent;
    import android.view.View;

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
            return Math.abs(distance-radius) <= 50; //check if the given point is on the circle
        }

        public float getRadius() {
            return this.radius;
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

            // Handle case where line length is zero (start and end points are the same)
            if (lineLength == 0) {
                return Math.abs(x - startX) < 10 && Math.abs(y - startY) < 10; // 10 is a threshold for selection
            }

            // Calculate perpendicular distance from point (x, y) to the line
            float distance = Math.abs(dy * x - dx * y + endX * startY - endY * startX) / lineLength;

            return distance < 20; // 10 is the threshold distance for selecting the line
        }

        public float getStartX() { return startX; }
        public float getStartY() { return startY; }
        public float getEndX() { return endX; }
        public float getEndY() { return endY; }
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
            // Get the bounding box of the path (for simplicity, use path's bounds)
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);

            // Check if the point is within the bounding box
            return bounds.contains(x, y);
        }

        public void start(float x, float y) {
            path.moveTo(x, y);
        }

        public Path getPath() {
            return this.path;
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
        private Stack<Shape> undoStack = new Stack<>();
        private Stack<Shape> redoStack = new Stack<>();

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
                    currentPaint.setStrokeWidth(8f);
                    break;
                case 3:
                    currentPaint.setStyle(Paint.Style.STROKE);
                    currentPaint.setStrokeWidth(12f);
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
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            //draw bitmap
            canvas.drawBitmap(bitmap, 0, 0, null);

            //draw each shapes
            for (Shape shape : shapes) {
                shape.draw(canvas);

                // Highlight if it's the selected shape
                if (shape == selectedShape) {
                    //selected shape logic (delete, change, etc...)
                }
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

            if (drawingMode.equals("edit")) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    selectedShape = null;
                    for (Shape shape : shapes) {
                        if (shape.contains(x, y)) {
                            selectedShape = shape;
                            break; // Stop checking after finding the first matching shape
                        }
                    }
                }
                //debugging
                if (selectedShape != null) {
                    Log.d("CanvasView", "Shape selected: " + selectedShape.getClass().getSimpleName());
                } else {
                    Log.d("CanvasView", "No shape selected");
                }

                invalidate();//redraw canvas (refresh) to show selected shapes
                return true;
            } else {
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
            }
            invalidate();
            return true;
        }

        private Shape createShape(float x, float y) {
            Paint shapePaint = new Paint(currentPaint); //create a new paint for the current shape
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

        public void deleteSelectedShape() {
            if (selectedShape != null) {
                shapes.remove(selectedShape);  // Remove from list
                selectedShape = null;          // Clear selection
                invalidate();                   // Redraw the canvas
            }
        }

        public void undo() {
            if (!shapes.isEmpty()) {
                Shape lastShape = shapes.remove(shapes.size() - 1); // Remove last shape
                undoStack.push(lastShape); // Store in undo stack
                redoStack.clear(); // Clear redo stack (standard behavior)
                invalidate(); // Redraw canvas
            }
        }

        public void redo() {
            if (!undoStack.isEmpty()) {
                Shape shape = undoStack.pop(); // Get last undone shape
                shapes.add(shape); // Re-add it to canvas
                invalidate(); // Redraw canvas
            }
        }

        public String getDrawingAsBase64() {
            Bitmap bitmap = getDrawingBitmap(); // A method to get the drawing as a bitmap
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        private Bitmap getDrawingBitmap() {
            return this.bitmap;
        }
    }
