# MyProgressBar包含两个效果：圆形进度条和条形进度条可以自定义进度条的高度和颜色，进度文字的大小和颜色。

   ![image](https://github.com/tingshuo202/MyProgressBar/blob/master/app/gif/progress.gif) 
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
         
    在view的构造方法中获取即可使用自定义属性。
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



