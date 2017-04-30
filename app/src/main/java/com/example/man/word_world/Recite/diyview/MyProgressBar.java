package com.example.man.word_world.Recite.diyview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

/**
 * Created by man on 2016/12/29.
 */
public class MyProgressBar extends ProgressBar {
    private Paint mPaint;

    public MyProgressBar(Context context) {
        super(context);
        initPaint();
    }

    public MyProgressBar(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        initPaint();
    }

    public MyProgressBar(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
        initPaint();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(-1);
    }

    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
    }
}
