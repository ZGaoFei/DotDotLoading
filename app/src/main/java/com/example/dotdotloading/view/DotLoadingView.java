package com.example.dotdotloading.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * 小点进度
 * 来源：模仿vivo x6 loading效果
 * <p>
 * 效果1：圆点在转圈
 * 效果2：圆点放大
 * 效果3：圆点叠加
 */
public class DotLoadingView extends View {
    private static final int DEFAULT_DOT_RADIUS = 10; // 默认小点的半径
    private static final int DEFAULT_VIEW_PARAMS = 300; // 默认loadingview的宽高
    private static final int DEFAULT_DOT_COLOR = Color.GRAY; // 默认小点的颜色
    private static final int DEFAULT_VIEW_RADIUS = 100; // 默认小点到圆心的距离
    private static final int DEFAULT_DOT_NUM = 12; // 默认小点的个数，间距为360/12 = 30
    public static final int ANIMATOR_TYPE_ONE = 0;
    public static final int ANIMATOR_TYPE_TWO = 1;
    public static final int ANIMATOR_TYPE_THREE = 2;

    private int mWidth;
    private int mHeight;
    private int mCenterX;
    private int mCenterY;

    private Paint mPaint;

    private int mAnimatorType = ANIMATOR_TYPE_ONE;
    private int mOffset; // 旋转角度偏移量
    private int num; // 动画执行到第几个dot
    private int mAnimatorDuration;
    private int mRadiusOffset; // dot的半径偏移量
    private boolean mIsRevert = false; // dot动画是否走完一遍
    private int mDotOffset; // dot移动偏移量

    private ValueAnimator mAnimator; // 控制旋转角度的动画
    private ValueAnimator mTextAnimator;
    private ValueAnimator mDotAnimator;

    public DotLoadingView(Context context) {
        this(context, null);
    }

    public DotLoadingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DotLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();

        mAnimatorType = ANIMATOR_TYPE_THREE;
        if (mAnimatorType == ANIMATOR_TYPE_ONE) {
            mAnimatorDuration = 3600;
        } else if (mAnimatorType == ANIMATOR_TYPE_TWO) {
            mAnimatorDuration = 3600;
        } else {
            mAnimatorDuration = 1440;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int resultWidth;
        int resultHeight;
        if (widthMode == MeasureSpec.EXACTLY) {
            resultWidth = width;
        } else {
            resultWidth = DEFAULT_VIEW_PARAMS;
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            resultHeight = height;
        } else {
            resultHeight = DEFAULT_VIEW_PARAMS;
        }

        setMeasuredDimension(resultWidth, resultHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;
        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // 交由父布局处理
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBg(canvas);

        if (mAnimatorType == ANIMATOR_TYPE_ONE) {
            drawDots(canvas);
        } else if (mAnimatorType == ANIMATOR_TYPE_TWO) {
            drawDots2(canvas); // 直接放大效果
            // drawDots2Other(canvas); // 渐变放大效果
        } else {
            // drawDots3(canvas);
            // drawDots3Other(canvas);
            drawDots3Other2(canvas);
        }
    }

    /**
     * 在布局的中心位置画一个默认的圆角背景
     */
    private void drawBg(Canvas canvas) {
        resetPaint(Color.parseColor("#44cccccc"));

        RectF rectf = new RectF(
                mCenterX - DEFAULT_VIEW_PARAMS / 2,
                mCenterY - DEFAULT_VIEW_PARAMS / 2,
                mCenterX + DEFAULT_VIEW_PARAMS / 2,
                mCenterY + DEFAULT_VIEW_PARAMS / 2);
        canvas.drawRoundRect(rectf, 20, 20, mPaint);
    }

    /**
     * 画圆点
     */
    private void drawDots(Canvas canvas) {
        resetPaint(Color.RED);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i + mOffset;
            int x = (int) (DEFAULT_VIEW_RADIUS * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (DEFAULT_VIEW_RADIUS * Math.cos(Math.toRadians(currentAngle)));
            canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
        }
    }

    private void drawDots2(Canvas canvas) {
        resetPaint(Color.RED);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i;
            int x = (int) (DEFAULT_VIEW_RADIUS * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (DEFAULT_VIEW_RADIUS * Math.cos(Math.toRadians(currentAngle)));
            if (i == num) {
                canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS * 2, mPaint);
            } else {
                canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
            }
        }
    }

    private void drawDots2Other(Canvas canvas) {
        resetPaint(Color.RED);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i;
            int x = (int) (DEFAULT_VIEW_RADIUS * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (DEFAULT_VIEW_RADIUS * Math.cos(Math.toRadians(currentAngle)));
            if (i == num) {
                canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS + mRadiusOffset, mPaint);
            } else {
                canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
            }
        }
    }

    /**
     * dot一个一个消失然后再一个一个显示的效果
     */
    private void drawDots3(Canvas canvas) {
        resetPaint(Color.RED);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i;
            int x = (int) (DEFAULT_VIEW_RADIUS * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (DEFAULT_VIEW_RADIUS * Math.cos(Math.toRadians(currentAngle)));
            if (!mIsRevert) { // dot一个一个消失的效果
                if (i >= num) {
                    canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
                }
            } else { // dot一个一个显示的效果
                if (i < num) {
                    canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
                }
            }
        }
    }

    /**
     * dot的另一种效果
     * 不忍删除
     */
    private void drawDots3Other(Canvas canvas) {
        resetPaint(Color.RED);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i + mDotOffset;
            int x = (int) (DEFAULT_VIEW_RADIUS * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (DEFAULT_VIEW_RADIUS * Math.cos(Math.toRadians(currentAngle)));
            if (!mIsRevert) {
                if (i >= num) {
                    canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
                }
            } else {
                if (i < num) {
                    canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
                }
            }
        }
    }

    /**
     * dot的显示和消失添加动画
     */
    private void drawDots3Other2(Canvas canvas) {
        resetPaint(Color.RED);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            if (!mIsRevert) {
                if (i == num) {
                    currentAngle = angleNum * i + mDotOffset;
                } else {
                    currentAngle = angleNum * i;
                }
                int x = (int) (DEFAULT_VIEW_RADIUS * Math.sin(Math.toRadians(currentAngle)));
                int y = (int) (DEFAULT_VIEW_RADIUS * Math.cos(Math.toRadians(currentAngle)));
                if (i >= num) {
                    canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
                }
            } else {
                if (i == num) {
                    currentAngle = angleNum * i + mDotOffset;
                } else {
                    currentAngle = angleNum * i;
                }
                int x = (int) (DEFAULT_VIEW_RADIUS * Math.sin(Math.toRadians(currentAngle)));
                int y = (int) (DEFAULT_VIEW_RADIUS * Math.cos(Math.toRadians(currentAngle)));
                if (i <= num) {
                    canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
                }
            }
        }
    }

    private void resetPaint(int color) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeWidth(10);
    }

    private void animatorStart() {
        if (mAnimator == null) {
            mAnimator = ValueAnimator.ofInt(0, 360);
            mAnimator.setDuration(mAnimatorDuration);
            mAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mAnimator.setRepeatMode(ValueAnimator.RESTART);
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mOffset = (int) valueAnimator.getAnimatedValue();

                    if (mAnimatorType == ANIMATOR_TYPE_ONE) {
                        postInvalidate();
                    } else if (mAnimatorType == ANIMATOR_TYPE_TWO) {
                        int currentNum = mOffset / 30;
                        if (num == currentNum) {
                            return;
                        }
                        num = currentNum;

                        postInvalidate();
                    } else {
                        int currentNum = mOffset / 30;
                        if (num == currentNum) {
                            return;
                        }
                        num = currentNum;

                        postInvalidate();
                    }
                }
            });

            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationRepeat(Animator animation) {
                    super.onAnimationRepeat(animation);
                    mIsRevert = !mIsRevert;
                }
            });

        }
        if (mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator.start();
    }

    /**
     * 单个dot放大动画
     */
    private void textAnimatorStart() {
        if (mTextAnimator == null) {
            mTextAnimator = ValueAnimator.ofInt(0, 15);
            mTextAnimator.setDuration(mAnimatorDuration / DEFAULT_DOT_NUM);
            mTextAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mTextAnimator.setRepeatMode(ValueAnimator.RESTART);
            mTextAnimator.setInterpolator(new LinearInterpolator());
            mTextAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mRadiusOffset = (int) valueAnimator.getAnimatedValue();
                    postInvalidate();
                }
            });
        }
        if (mTextAnimator.isRunning()) {
            mTextAnimator.cancel();
        }
        mTextAnimator.start();
    }

    private void dotAnimatorStart() {
        if (mDotAnimator == null) {
            mDotAnimator = ValueAnimator.ofInt(0, 30);
            mDotAnimator.setDuration(mAnimatorDuration / DEFAULT_DOT_NUM);
            mDotAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mDotAnimator.setRepeatMode(ValueAnimator.RESTART);
            mDotAnimator.setInterpolator(new LinearInterpolator());
            mDotAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mDotOffset = (int) valueAnimator.getAnimatedValue();
                    postInvalidate();
                }
            });
        }
        if (mDotAnimator.isRunning()) {
            mDotAnimator.cancel();
        }
        mDotAnimator.start();
    }

    public void allAnimatorStart() {
        if (mAnimatorType == ANIMATOR_TYPE_ONE) {
            animatorStart();
        } else if (mAnimatorType == ANIMATOR_TYPE_TWO) {
            animatorStart();
            textAnimatorStart();
        } else {
            animatorStart();
            dotAnimatorStart();
        }
    }

}
