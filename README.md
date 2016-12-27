# MyProgressBar包含两个效果：圆形进度条和条形进度条可以自定义进度条的高度和颜色，进度文字的大小和颜色。

一、条形进度条HorizontalProgressBar

    1、自定义属性
          在attrs.xml中定义
            
<resources>
    <declare-styleable name="ProgressView">
        <attr name="unreach_color" format="color"/>
        <attr name="unreach_height" format="dimension"/>
        <attr name="reach_color" format="color"/>
        <attr name="reach_height" format="dimension"/>
        <attr name="text_color" format="color"/>
        <attr name="text_size" format="dimension"/>
        <attr name="text_offset" format="dimension"/>
    </declare-styleable>
</resources>
          在layout中应用
<ScrollView xmlns:android="http://schemas.android.com/apk/res/android"
//添加引用，最后是包名，空间名称shy是自定义的，也可以换成其他的
    xmlns:shy="http://schemas.android.com/apk/res/com.shy.progressview"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        <com.shy.progressview.HorizontalProgressBar
            android:id="@+id/progress"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="30dp"
            shy:text_size="20sp"
            shy:text_color="#0105ef"
            android:padding="10dp"
            android:progress="0"/>
    </LinearLayout>
</ScrollView>
          在view的构造方法中获取
 TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.ProgressView);
        mTextSize = (int) ta.getDimension(R.styleable.ProgressView_text_size,mTextSize);
        mTextColor = ta.getColor(R.styleable.ProgressView_text_color,mTextColor);
        mReachHeight = (int) ta.getDimension(R.styleable.ProgressView_reach_height,mReachHeight);
        mReachColor = ta.getColor(R.styleable.ProgressView_reach_color,mReachColor);
        mUnReachHeight = (int) ta.getDimension(R.styleable.ProgressView_unreach_height,mUnReachHeight);
        mUnReachColor = ta.getColor(R.styleable.ProgressView_unreach_color,mUnReachColor);
        mTextOffset = (int) ta.getDimension(R.styleable.ProgressView_text_offset,mTextOffset);
        ta.recycle();

    2、自定义控件继承ProgressBar
        先定义两个公用的方法
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
    在onMeasure()方法中，对高进行测量。一般是宽高都需要测量的，但是横向进度条的宽度一定是精确值才符合逻辑，所以这里只测量了高度。根据mode确定size。
    
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

    在onDraw()方法中分别绘制到达的进度条，文字，未到达的进度条。
    还需要判断进度条是否还有未到达的，如果都已到达，就不需要绘制未到达的进度条。
    canvas的save和restore方法，是为了保存画布的状态，在两者之间对画布进行特殊的操作，restore方法之后，又会恢复画布之前的状态。
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

    3、在mainActivity中，使用handler更新进度
    
public class MainActivity extends Activity {
    private HorizontalProgressBar progressView;
    private static int MSG_UPDATE = 0x110;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int progress = progressView.getProgress();
            progress++;
            progressView.setProgress(progress);
            if(progress >= 100){
                handler.removeCallbacksAndMessages(null);
            }
            handler.sendEmptyMessageDelayed(MSG_UPDATE,100);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressView = (HorizontalProgressBar)findViewById(R.id.progress);
        handler.sendEmptyMessage(MSG_UPDATE);
    }
}

二、画圆形进度条
    1、继承条形进度条的HorizontalProgressBar，这样可以使用之前定义好的自定义属性。
        再增加一个半径的自定义属性就可以。
      

 方法解析：
View.resolveSize(int size,int measureSpec)
public static int resolveSize(int size, int measureSpec) {
         int result = size;
         int specMode = MeasureSpec.getMode(measureSpec);
         int specSize =  MeasureSpec.getSize(measureSpec);
         switch (specMode) {
         case MeasureSpec.UNSPECIFIED:
             result = size;
             break;
         case MeasureSpec.AT_MOST:
             result = Math.min(size, specSize);
             break;
         case MeasureSpec.EXACTLY:
             result = specSize;
             break;
         }
         return result;
     }

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
        //获取宽高
        int width = resolveSize(except,widthMeasureSpec);
        int height = resolveSize(except,widthMeasureSpec);
        int realWidth = Math.min(width,height);
        mRadius = (realWidth - getPaddingRight() -getPaddingLeft() - mPaintWidth) / 2;
        setMeasuredDimension(realWidth,realWidth);
    }
    @Override
    protected synchronized void onDraw(Canvas canvas) {
//        super.onDraw(canvas);//加上这个就会默认再画一个横向的进度条
        String text = getProgress() + "%";
//文字的宽度
        float textWidth = mPaint.measureText(text);
        //文字高度中间的坐标
        float textHeight = (mPaint.descent() + mPaint.ascent()) / 2;
        canvas.save();
//画布的原点默认是（0，0），translate表示平移。不移动的话,进度条的宽度就会只显示一半       
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




