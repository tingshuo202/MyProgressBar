package com.shy.progressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * Created by holyca on 16/12/27.
 */

public class CircleProgressBar extends HorizontalProgressBar {

//    private static final int RADIUS = 30;
    private Paint mPaint;
    private int mPaintWidth;

    private int mRadius ;
    public CircleProgressBar(Context context) {
        this(context,null);
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs,R.styleable.CircleProgressBar);
        mRadius = (int) ta.getDimension(R.styleable.CircleProgressBar_radius,mRadius);
        ta.recycle();

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setTextSize(mTextSize);

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //进度条的宽度
        mPaintWidth = Math.max(mUnReachHeight,mReachHeight);
        int except = getPaddingLeft() + getPaddingRight() + mRadius * 2 + mPaintWidth* 2;
        int width = resolveSize(except,widthMeasureSpec);
        int height = resolveSize(except,widthMeasureSpec);

        int realWidth = Math.min(width,height);

        mRadius = (realWidth - getPaddingRight() -getPaddingLeft() - mPaintWidth) / 2;

        setMeasuredDimension(realWidth,realWidth);

    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
//        super.onDraw(canvas);


        String text = getProgress() + "%";
        float textWidth = mPaint.measureText(text);
        float textHeight = (mPaint.descent() + mPaint.ascent()) / 2;

        canvas.save();
        //不移动的话,进度条的宽度就会只显示一半
        canvas.translate(getPaddingLeft() + mPaintWidth/2,getPaddingTop() + mPaintWidth/2);

        mPaint.setStyle(Paint.Style.STROKE);//空心
        //unreach circle
        mPaint.setColor(mUnReachColor);
        mPaint.setStrokeWidth(mUnReachHeight);
        canvas.drawCircle(mRadius,mRadius,mRadius,mPaint);

        //reach
        mPaint.setColor(mReachColor);
        mPaint.setStrokeWidth(mReachHeight);
        float sweep = getProgress() * 1.0f /getMax() * 360;

        canvas.drawArc(new RectF(0,0,mRadius * 2,mRadius * 2),0,sweep,false,mPaint);

        //text
        mPaint.setStyle(Paint.Style.FILL);//实心
        mPaint.setColor(mTextColor);
        canvas.drawText(text,mRadius - textWidth/2,mRadius-textHeight,mPaint);

        canvas.restore();


    }
}
