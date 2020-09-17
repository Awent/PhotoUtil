package com.awen.image.photopick.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

/**
 * 仿微博图片加载进度条
 * Created by Awen <Awentljs@gmail.com>
 */

public class RoundProgressBar extends View {
    private static final String TAG = "RoundProgressBar";
    private boolean DEBUG = false;
    private long maxValue = 100;
    private long progress;
    private Paint circlePaint;
    private float circleWidth;
    private int circleProgressColor;
    private int circleBottomColor;
    private int circleBottomWidth;
    private int circleRadius;
    private int interval;//两个圆之间的间隔

    public RoundProgressBar(Context context) {
        this(context, 70);
    }

    /**
     * @param context
     * @param circleRadius 内部实心圆的半径大小
     */
    public RoundProgressBar(Context context, int circleRadius) {
        super(context);
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(10);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);
        circleProgressColor = 0xdddddddd;
        circleBottomColor = 0xdddddddd;
        circleWidth = 8;
        this.circleRadius = circleRadius;
        interval = 5;
        circleBottomWidth = 2;
        progress = 1;
    }

    /*
     * 画空心圆
     */
    private void drawCircle(Canvas canvas) {
        int xPos = getLeft() + (getWidth() >> 1);
        int yPos = getTop() + (getHeight() >> 1);
        circlePaint.setColor(circleBottomColor);
        circlePaint.setStrokeWidth(circleBottomWidth);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setShader(null);
        if (DEBUG) {
            Log.e(TAG, "xPos = " + xPos + ", yPos = " + yPos);
        }
        canvas.drawCircle(xPos, yPos, circleRadius + interval, circlePaint);
    }

    /*
     * 根据进度条画实心圆
     */
    private void drawArc(Canvas canvas) {
        circlePaint.setStyle(Paint.Style.FILL);
        int xPos = getLeft() + (getWidth() >> 1);
        int yPos = getTop() + (getHeight() >> 1);
        RectF rectF = new RectF(xPos - circleRadius, yPos - circleRadius, xPos + circleRadius, yPos + circleRadius);
        float degree = (float) progress / (float) maxValue * 360;
        if (DEBUG) {
            Log.e(TAG, "degree = " + degree);
        }
        circlePaint.setStrokeWidth(circleWidth);
        circlePaint.setColor(circleProgressColor);
        canvas.drawArc(rectF, -90, degree, true, circlePaint);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);
        if ((int) (((float) progress / (float) maxValue) * 100) == 100) {
            return;
        }
        drawCircle(canvas);
        drawArc(canvas);
    }

    public void setProgress(long mProgress) {
        long origin = this.progress;
        progress = mProgress;
        if (progress != 0 && origin != progress) {
            postInvalidate();
        }
    }

    public RoundProgressBar setMaxValue(long value) {
        this.maxValue = value;
        return this;
    }

    public RoundProgressBar setCircleWidth(float circleWidth) {
        this.circleWidth = circleWidth;
        return this;
    }

    public RoundProgressBar setCircleBottomWidth(int circleBottomWidth) {
        this.circleBottomWidth = circleBottomWidth;
        return this;
    }

    public RoundProgressBar setCircleRadius(int circleRadius) {
        this.circleRadius = circleRadius;
        return this;
    }

    public RoundProgressBar setInterval(int interval) {
        this.interval = interval;
        return this;
    }
}
