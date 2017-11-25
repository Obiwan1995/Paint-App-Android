package com.wordpress.priyankvex.paintapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Stack;

/**
 * Created by Priyank(@priyankvex) on 5/9/15.
 *
 * Class which provides the view on which drawing takes place.
 */
public class DrawingView extends View{

    // To hold the path that will be drawn.
    private Path drawPath;
    ArrayList<Path> drawPaths;
    // Paint object to draw drawPath and drawCanvas.
    private Paint drawPaint, canvasPaint;
    ArrayList<Paint> drawPaints;
    // initial color
    private int paintColor = 0xff000000;
    private int previousColor = paintColor;
    // canvas on which drawing takes place.
    private Canvas drawCanvas;
    // canvas bitmap
    private Bitmap canvasBitmap;
    // Brush stroke width
    private float brushSize, lastBrushSize;
    // To enable and disable erasing mode.
    private boolean erase = false;
    private Bitmap lastLoadedImage = null;
    private Stack<Bitmap> previousImages = new Stack<Bitmap>();
    private Bitmap currentImage = null;

    public Bitmap getCurrentImage() {
    	return this.currentImage;
	}

    public DrawingView(Context context, AttributeSet attrs){
        super(context, attrs);
        setUpDrawing();
    }

    /**
     * Initialize all objects required for drawing here.
     * One time initialization reduces resource consumption.
     */
    private void setUpDrawing(){
        drawPath = new Path();
		initPaint();
        canvasPaint = new Paint(Paint.DITHER_FLAG);

        // Initial brush size is medium.
        brushSize = getResources().getInteger(R.integer.medium_size);
        lastBrushSize = brushSize;
        drawPaint.setStrokeWidth(brushSize);
        drawPaths = new ArrayList<>();
        drawPaints = new ArrayList<>();
    }

    public void updateStack(){
        if(currentImage != null)
            previousImages.push(currentImage);
    }

    private void initPaint()
	{
		drawPaint = new Paint();
		drawPaint.setColor(paintColor);
		// Making drawing smooth.
		drawPaint.setAntiAlias(true);
		drawPaint.setStyle(Paint.Style.STROKE);
		drawPaint.setStrokeJoin(Paint.Join.ROUND);
		drawPaint.setStrokeCap(Paint.Cap.ROUND);
		drawPaint.setStrokeWidth(brushSize);
	}

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (lastLoadedImage != null)
		{
			canvasBitmap = Bitmap.createBitmap(lastLoadedImage);
		}
		else
		{
			canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		}
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        updateStack();
        // X and Y position of user touch.
        float touchX = event.getX();
        float touchY = event.getY();
        // Draw the path according to the touch event taking place.
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                if (erase){
                    drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
                }
                drawCanvas.drawPath(drawPath, drawPaint);
                drawPaths.add(drawPath);
                drawPaints.add(drawPaint);
                drawPath = new Path();
               	initPaint();
                break;
            default:
                return false;
        }

        // invalidate the view so that canvas is redrawn.
        invalidate();
        return true;
    }

    public void setColor(String newColor){
        // invalidate the view
        invalidate();
        paintColor = Color.parseColor(newColor);
        drawPaint.setColor(paintColor);
        previousColor = paintColor;
    }

    public void setBrushSize(float newSize){
        float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                newSize, getResources().getDisplayMetrics());
        brushSize=pixelAmount;
        drawPaint.setStrokeWidth(brushSize);
    }

    public void setLastBrushSize(float lastSize){
        lastBrushSize=lastSize;
    }
    public float getLastBrushSize(){
        return lastBrushSize;
    }

    public void setErase(boolean isErase){
        //set erase true or false
        erase = isErase;
        if(erase) {
            drawPaint.setColor(Color.WHITE);
            //drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        }
        else {
            drawPaint.setColor(previousColor);
            drawPaint.setXfermode(null);
        }
    }

    public void eraseAllAnnotations(){
        updateStack();
    	if (lastLoadedImage != null)
		{
			drawPaths.clear();
			drawPaints.clear();
			loadImage(lastLoadedImage);
		}
		else
		{
			startNewDrawing();
		}
    }

    public void startNewDrawing() {
		drawPaths.clear();
		drawPaints.clear();
		drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		lastLoadedImage = null;
		invalidate();
	}

    public void loadImage(Bitmap bmp){
        lastLoadedImage = bmp;
        drawBitmap(bmp);
    }

    public void removeAllFilters() {
        updateStack();
    	drawBitmap(lastLoadedImage);
    }

    public void applyNegateFilter(){
        updateStack();
        drawBitmap(Negate.invert(currentImage));
    }

    public void applyGrayScaleFilter() {
        updateStack();
		drawBitmap(Grayscale.toGrayscale(currentImage));
    }

    public void drawBitmap(Bitmap bitmap) {
    	currentImage = bitmap;
		drawCanvas.drawBitmap(bitmap, null, new RectF(0, 0, drawCanvas.getWidth(), drawCanvas.getHeight()), null);
		restorePaths();
		invalidate();
    }

    public void restorePaths() {
        for (int i = 0; i < drawPaths.size(); i++)
        {
            drawCanvas.drawPath(drawPaths.get(i), drawPaints.get(i));
        }
        invalidate();
    }

    public void undo(){
        if(previousImages.size() > 0)
        {
            drawBitmap(previousImages.pop());
        }
    }
}
