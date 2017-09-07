package com.queatz.sparkleme.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by jacob on 9/1/17.
 *
 * Adopted from https://android.googlesource.com/.../apis/graphics/ColorPickerDialog.java
 */

public class ColorChooser extends RelativeLayout {

    public interface OnColorChangedListener {
        void colorChanged(int color);
    }

    private OnColorChangedListener listener;
    private OnColorChangedListener listenerWrapper;
    private ColorPickerView colorPicker;

    private static class ColorPickerView extends View {

        private Paint mPaint;
        private Paint mPaintRadial;
        private float brightness = .5f;
        private int mColor;
        private final int[] mColors;
        private OnColorChangedListener mListener;

        ColorPickerView(Context c, OnColorChangedListener l) {
            super(c);
            mListener = l;
            mColors = new int[] {
                    0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
                    0xFFFFFF00, 0xFFFF0000
            };
            Shader s = new SweepGradient(0, 0, mColors, null);
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(s);
            mPaint.setStyle(Paint.Style.FILL);

            mPaintRadial = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaintRadial.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float r = (getMeasuredWidth() / 2);
            canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
            mPaintRadial.setShader(new RadialGradient(0, 0, r, Color.WHITE, Color.TRANSPARENT, Shader.TileMode.CLAMP));
            canvas.drawOval(new RectF(-r, -r, r, r), mPaint);
            canvas.drawOval(new RectF(-r, -r, r, r), mPaintRadial);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        }

        private int floatToByte(float x) {
            int n = java.lang.Math.round(x);
            return n;
        }

        private int pinToByte(int n) {
            if (n < 0) {
                n = 0;
            } else if (n > 255) {
                n = 255;
            }
            return n;
        }

        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }

        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }
            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;
            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);
            return Color.argb(a, r, g, b);
        }

        private int rotateColor(int color, float rad) {
            float deg = rad * 180 / 3.1415927f;
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            ColorMatrix cm = new ColorMatrix();
            ColorMatrix tmp = new ColorMatrix();
            cm.setRGB2YUV();
            tmp.setRotate(0, deg);
            cm.postConcat(tmp);
            tmp.setYUV2RGB();
            cm.postConcat(tmp);
            final float[] a = cm.getArray();
            int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
            int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
            return Color.argb(Color.alpha(color), pinToByte(ir),
                    pinToByte(ig), pinToByte(ib));
        }

        private static final float PI = 3.1415926f;

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - getMeasuredWidth() / 2;
            float y = event.getY() - getMeasuredHeight() / 2;
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                case MotionEvent.ACTION_MOVE:
                    float angle = (float)Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle/(2*PI);
                    if (unit < 0) {
                        unit += 1;
                    }

                    mColor = mix(Color.WHITE, interpColor(mColors, unit), Math.hypot(x, y) / (getMeasuredWidth() / 2));
                    mListener.colorChanged(getColor());
                    break;
            }
            return true;
        }

        private int mix(int a, int b, double amount) {
            amount = Math.min(1, Math.max(0, amount));

            return Color.argb(
                    (int) Math.round(Color.alpha(a) * (1 - amount) + Color.alpha(b) * amount),
                    (int) Math.round(Color.red(a) * (1 - amount) + Color.red(b) * amount),
                    (int) Math.round(Color.green(a) * (1 - amount) + Color.green(b) * amount),
                    (int) Math.round(Color.blue(a) * (1 - amount) + Color.blue(b) * amount)
            );
        }

        public void setBrightness(float brightness) {
            this.brightness = brightness;
            mListener.colorChanged(getColor());
        }

        private int getColor() {
            return Color.argb(
                    Color.alpha(mColor),
                    Math.round(Color.red(mColor) * brightness),
                    Math.round(Color.green(mColor) * brightness),
                    Math.round(Color.blue(mColor) * brightness)
            );
        }
    }

    public ColorChooser(Context context) {
        super(context);
        init();
    }

    public ColorChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorChooser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(OnColorChangedListener listener) {
        this.listener = listener;
    }

    public void setBrightness(float brightness) {
        colorPicker.setBrightness(brightness);
    }

    private void init() {
        listenerWrapper = new OnColorChangedListener() {
            public void colorChanged(int color) {
                if (listener != null) {
                    Log.e("COLOR", Color.red(color) + " " + Color.green(color) + " " + Color.blue(color));
                    listener.colorChanged(color);
                }
            }
        };

        colorPicker = new ColorPickerView(getContext(), listenerWrapper);
        addView(colorPicker);
    }
}