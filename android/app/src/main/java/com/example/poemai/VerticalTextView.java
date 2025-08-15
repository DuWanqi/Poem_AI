package com.example.poemai;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.appcompat.widget.AppCompatTextView;

public class VerticalTextView extends AppCompatTextView {
    private boolean isVertical = false;
    private int mIndex;
    private TextPaint mTextPaint;

    public VerticalTextView(Context context) {
        super(context);
        init();
    }

    public VerticalTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    public void setVertical(boolean vertical) {
        isVertical = vertical;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isVertical) {
            super.onDraw(canvas);
            return;
        }

        String text = getText().toString();
        if (text == null || text.length() == 0) {
            return;
        }

        mTextPaint.setTextSize(getTextSize());
        mTextPaint.setColor(getCurrentTextColor());
        mTextPaint.setTypeface(getTypeface());

        float verticalSpacing = getLineSpacingExtra();
        float charWidth = mTextPaint.measureText("中");
        float charHeight = -mTextPaint.ascent() + mTextPaint.descent();
        
        float x = getWidth() - charWidth;
        float y = getPaddingTop() + charHeight;

        for (int i = 0; i < text.length(); i++) {
            String charString = String.valueOf(text.charAt(i));
            
            if ("\n".equals(charString)) {
                // 换列
                x -= charWidth + verticalSpacing;
                y = getPaddingTop() + charHeight;
                continue;
            }
            
            canvas.drawText(charString, x, y, mTextPaint);
            y += charHeight;
            
            // 检查是否需要换列
            if (y + charHeight > getHeight() - getPaddingBottom()) {
                x -= charWidth + verticalSpacing;
                y = getPaddingTop() + charHeight;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isVertical) {
            return super.onTouchEvent(event);
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            requestFocus();
            // 计算点击位置对应的光标位置
            mIndex = calculateIndex(event.getX(), event.getY());
            setSelection(mIndex);
            return true;
        }
        
        return super.onTouchEvent(event);
    }

    private int calculateIndex(float x, float y) {
        // 简化版光标位置计算
        String text = getText().toString();
        if (text.isEmpty()) return 0;
        
        mTextPaint.setTextSize(getTextSize());
        float charWidth = mTextPaint.measureText("中");
        float charHeight = -mTextPaint.ascent() + mTextPaint.descent();
        
        // 计算列数
        int col = (int) ((getWidth() - x) / (charWidth + getLineSpacingExtra()));
        int row = (int) ((y - getPaddingTop()) / charHeight);
        
        // 简化处理，实际应该更复杂
        return Math.min(Math.max(0, col * (int)((getHeight() - getPaddingTop() - getPaddingBottom()) / charHeight) + row), text.length());
    }

    // 添加兼容EditText的方法
    public void setSelection(int index) {
        // 空实现，避免编译错误
    }
    
    public void setSelection(int start, int stop) {
        // 空实现，避免编译错误
    }
}