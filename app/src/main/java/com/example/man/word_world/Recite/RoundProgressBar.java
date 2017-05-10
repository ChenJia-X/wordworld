package com.example.man.word_world.Recite;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import com.example.man.word_world.R;

/**
 * Created by man on 2016/12/29.
 */
public class RoundProgressBar extends View{
    public static final int FILL = 1;
    public static final int STROKE = 0;
    private int max;
    private Paint paint;
    private int progress;
    private int roundColor;
    private int roundProgressColor;
    private float roundWidth;
    private int style;
    private int textColor;
    private boolean textIsDisplayable;
    private float textSize;

    public RoundProgressBar(Context Context)
    {
        this(Context, null);
    }

    public RoundProgressBar(Context Context, AttributeSet AttributeSet) {this(Context, AttributeSet, 0);}

    public RoundProgressBar(Context context, AttributeSet attributeSet, int defstyle) {
        super(context, attributeSet, defstyle);

        paint =new Paint();

        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RoundProgressBar);

        //获取自定义属性和默认值
        roundColor = typedArray.getColor(R.styleable.RoundProgressBar_roundColor, 2131165188);
        roundProgressColor = typedArray.getColor(R.styleable.RoundProgressBar_roundProgressColor, 2131165186);
        textColor = typedArray.getColor(R.styleable.RoundProgressBar_textColor, 2131165186);
        textSize = typedArray.getDimension(R.styleable.RoundProgressBar_textSize, 15.0F);
        roundWidth = typedArray.getDimension(R.styleable.RoundProgressBar_roundWidth, 10.0F);
        max = typedArray.getInteger(R.styleable.RoundProgressBar_max, 100);
        textIsDisplayable = typedArray.getBoolean(R.styleable.RoundProgressBar_textIsDisplayable, true);
        style = typedArray.getInt(R.styleable.RoundProgressBar_style, 0);

        typedArray.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        /**
         * 画最外层的大圆环
         */
        int centre = getWidth() / 2;//获取圆心的x坐标
        int radius = (int)(centre - roundWidth / 2.0F);//圆环的半径
        paint.setColor(roundColor);//设置圆环的颜色
        paint.setStyle(Paint.Style.STROKE);//设置空心
        paint.setStrokeWidth(roundWidth);//设置圆环的宽度
        paint.setAntiAlias(true);//消除锯齿
        canvas.drawCircle(centre, centre, radius, paint);//画出圆环
        /**
         * 画进度百分比
         */
        paint.setStrokeWidth(0.0F);
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(Typeface.DEFAULT_BOLD);//设置字体
        int percent = (int)((progress*100.0F )/ max );//中间的进度百分比，先转换成float在进行除法运算，不然都为0
        float textWidth = paint.measureText(percent + "%");//测量字体宽度，我们需要根据字体的宽度设置在圆环中间
        if ((textIsDisplayable) && (percent >= 0) && (style == 0))
            canvas.drawText(percent + "%", centre - textWidth / 2.0F,
                    centre + textSize * 3.0F / 8.0F, paint);//测量字体宽度，我们需要根据字体的宽度设置在圆环中间
        /**
         * 画圆弧 ，画圆环的进度
         */
        paint.setStrokeWidth(roundWidth);//设置圆环的宽度
        paint.setColor(roundProgressColor);//设置进度的颜色
        RectF localRectF = new RectF(centre - radius, centre - radius, centre + radius, centre + radius);//用于定义的圆弧的形状和大小的界限

        switch (style) {
            case STROKE:{
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawArc(localRectF, -90.0F, progress * 360 / max, false, paint);//根据进度画圆弧
                break;
            }
            case FILL:{
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                if(progress !=0)
                    canvas.drawArc(localRectF, -90.0F, progress * 360 / max, true, paint);//根据进度画圆弧
                break;
            }
        }
    }


    public synchronized int getMax() {return max;}

    /**
     * 设置进度的最大值
     * @param max
     */
    public synchronized void setMax(int max) {
        if(max < 0){
            throw new IllegalArgumentException("max not less than 0");
        }
        this.max = max;
    }

    /**
     * 获取进度.需要同步
     * @return
     */
    public synchronized int getProgress() {return progress;}

    /**
     * 设置进度，此为线程安全控件，由于考虑多线的问题，需要同步
     * 刷新界面调用postInvalidate()能在非UI线程刷新
     * @param progress
     */
    public synchronized void setProgress(int progress) {
        if(progress < 0){
            throw new IllegalArgumentException("progress not less than 0");
        }
        if(progress > max){
            progress = max;
        }
        if(progress <= max){
            this.progress = progress;
            postInvalidate();
        }
    }

    public int getCricleColor()
    {
        return roundColor;
    }

    public void setCricleColor(int cricleColor)
    {
        this.roundColor = cricleColor;
    }

    public int getCricleProgressColor()
    {
        return this.roundProgressColor;
    }

    public void setCricleProgressColor(int cricleProgressColor) {this.roundProgressColor = cricleProgressColor;}

    public int getTextColor()
    {
        return textColor;
    }

    public void setTextColor(int textColor)
    {
        this.textColor = textColor;
    }

    public float getTextSize() {return textSize;}

    public void setTextSize(float textSize)
    {
        this.textSize = textSize;
    }

    public float getRoundWidth()
    {
        return roundWidth;
    }

    public void setRoundWidth(float roundWidth)
    {
        this.roundWidth = roundWidth;
    }


}
