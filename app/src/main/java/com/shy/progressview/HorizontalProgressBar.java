package com.shy.progressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

/**
 * Created by holyca on 16/12/26.
 */

public class HorizontalProgressBar extends ProgressBar {

    public static final int DEFAULT_TEXT_SIZE = 10;
    public static final int DEFAULT_TEXT_COLOR = 0x44ff0000;
    public static final int DEFAULT_UNREACH_COLOR = 0x4400ff00;
    public static final int DEFAULT_UNREACH_HEIGHT = 2;
    public static final int DEFAULT_REACH_COLOR = DEFAULT_TEXT_COLOR;
    public static final int DEFAULT_REACH_HEIGHT = 2;
    public static final int DEFAULT_TEXT_OFFSET = 10;


    protected int mTextSize = sp2px(DEFAULT_TEXT_SIZE);
    protected int mTextColor = DEFAULT_TEXT_COLOR;
    protected int mReachHeight = dp2px(DEFAULT_REACH_HEIGHT);
    protected int mReachColor = DEFAULT_REACH_COLOR;
    protected int mUnReachHeight = dp2px(DEFAULT_UNREACH_HEIGHT);
    protected int mUnReachColor = DEFAULT_UNREACH_COLOR;
    protected int mTextOffset = dp2px(DEFAULT_TEXT_OFFSET);

    private int mRealWidth;

    private Paint mPaint = new Paint();


    public HorizontalProgressBar(Context context) {
        this(context,null);
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public HorizontalProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obtainStyledAttrs(attrs);
    }

    /**
     * 获取自定义属性值
     * @param attrs
     */
    private void obtainStyledAttrs(AttributeSet attrs) {

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressView);
        mTextSize = (int) ta.getDimension(R.styleable.ProgressView_text_size,mTextSize);
        mTextColor = ta.getColor(R.styleable.ProgressView_text_color,mTextColor);
        mReachHeight = (int) ta.getDimension(R.styleable.ProgressView_reach_height,mReachHeight);
        mReachColor = ta.getColor(R.styleable.ProgressView_reach_color,mReachColor);
        mUnReachHeight = (int) ta.getDimension(R.styleable.ProgressView_unreach_height,mUnReachHeight);
        mUnReachColor = ta.getColor(R.styleable.ProgressView_unreach_color,mUnReachColor);
        mTextOffset = (int) ta.getDimension(R.styleable.ProgressView_text_offset,mTextOffset);

        ta.recycle();

        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setTextSize(mTextSize);

    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        //这里没有测量宽度,因为默认困度一定是精确值。
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = measureHeight(heightMeasureSpec);

        setMeasuredDimension(width,height);

        mRealWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    private int measureHeight(int heightMeasureSpec) {
        int result = 0;

        int mode = MeasureSpec.getMode(heightMeasureSpec);
        int size = MeasureSpec.getSize(heightMeasureSpec);

        if(mode == MeasureSpec.EXACTLY){
            result = size;

        }else{
            //比较到达的高度/未到达的高度/文字的高度,取最大值就是控件的高度
            int textHeight = (int) (mPaint.descent() - mPaint.ascent());
            int maxHeight = Math.max(Math.max(mReachHeight,mUnReachHeight),Math.abs(textHeight));
            result = getPaddingBottom() + getPaddingTop() + maxHeight;

        }

        if(mode == MeasureSpec.AT_MOST){
            result = Math.min(result,size);
        }
        return result;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
//        super.onDraw(canvas);

        canvas.save();
        canvas.translate(getPaddingLeft(),dp2px(10));
        //进度条是否已经到达100%
        boolean reach = false;
        //进度文字
        String text = getProgress()+"%";
        //获取文字的宽度
        int textWidth = (int) mPaint.measureText(text);

        //进度百分比
        float radio = getProgress() * 1.0f /getMax();
        //进度的长度加上和文字之间的间隔
        float progressX = mRealWidth * radio;

        if(progressX + textWidth > mRealWidth){
            //进度已经超过100%
            progressX = mRealWidth - textWidth;

            reach = true;
        }

        //单纯reach进度条的长度
        float endX = progressX - mTextOffset/2;
        if(endX > 0){
            //有进度
            mPaint.setColor(mReachColor);
            mPaint.setStrokeWidth(mReachHeight);
            /**
             * float startX, float startY, float stopX, float stopY, Paint paint
             */
            canvas.drawLine(0,0,endX,0,mPaint);
        }

        //draw text
        mPaint.setColor(mTextColor);
        //y的中心点
        int y = (int) ((mPaint.descent() + mPaint.ascent())/2);
        canvas.drawText(text,progressX,Math.abs(y),mPaint);

        //draw unreach
        if(!reach){
            float start = progressX + textWidth + mTextOffset /2;
            mPaint.setColor(mUnReachColor);
            mPaint.setStrokeWidth(mUnReachHeight);
            canvas.drawLine(start,0,mRealWidth,0,mPaint);
        }

        canvas.restore();

        invalidate();

    }

    /**
     * dp to px
     * @param dpVal
     * @return
     */
    private int dp2px(int dpVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpVal,getResources().getDisplayMetrics());
    }

    /**
     * sp to px
     * @param spVal
     * @return
     */
    private int sp2px(int spVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,spVal,getResources().getDisplayMetrics());
    }
}
