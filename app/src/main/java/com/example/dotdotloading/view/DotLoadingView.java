package com.example.dotdotloading.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.example.dotdotloading.R;

/**
 * 小点进度
 * 来源：模仿vivo x6 loading效果
 * <p>
 * 支持6种效果
 */
public class DotLoadingView extends View {
    private static final int DEFAULT_DOT_RADIUS = 10; // 默认小点的半径
    private static final int DEFAULT_VIEW_PARAMS = 250; // 默认loadingview的宽高
    private static final int DEFAULT_VIEW_RADIUS = 75; // 默认小点到圆心的距离
    private static final int DEFAULT_DOT_NUM = 12; // 默认小点的个数，间距为360/12 = 30
    public static final int ANIMATOR_TYPE_ONE = 0;
    public static final int ANIMATOR_TYPE_TWO = 1;
    public static final int ANIMATOR_TYPE_THREE = 2;
    public static final int ANIMATOR_TYPE_FOUR = 3;
    public static final int ANIMATOR_TYPE_FIVE = 4;
    public static final int ANIMATOR_TYPE_SIX = 5;

    private int mCenterX;
    private int mCenterY;

    private Paint mPaint;

    private int mBgColor;
    private int mDotColor;
    private int mAnimatorType;
    private int mOffset; // 旋转角度偏移量
    private int num; // 动画执行到第几个dot
    private int mAnimatorDuration;
    private int mRadiusOffset; // dot的半径偏移量
    private boolean mIsRevert = false; // dot动画是否走完一遍
    private int mDotOffset; // dot移动偏移量

    private int mDotParams; // dot到中心点的距离
    private int mViewParams; // view的边长，总是比mDotParams大100,（左右上下留50的间距）
    private int mPaddingInTop; // 允许loadingview向上平移的距离

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

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DotLoadingView);
        mBgColor = a.getColor(R.styleable.DotLoadingView_bgColor, Color.parseColor("#44cccccc"));
        mDotColor = a.getColor(R.styleable.DotLoadingView_dotColor, Color.RED);
        mAnimatorType = a.getInt(R.styleable.DotLoadingView_animatorType, 0);
        mPaddingInTop = a.getDimensionPixelSize(R.styleable.DotLoadingView_paddingInTop, 0);
        a.recycle();
        init();
    }

    private void init() {
        mPaint = new Paint();

        switch (mAnimatorType) {
            case ANIMATOR_TYPE_ONE:
            case ANIMATOR_TYPE_TWO:
            case ANIMATOR_TYPE_THREE:
                mAnimatorDuration = 3600;
                break;
            case ANIMATOR_TYPE_FOUR:
            case ANIMATOR_TYPE_FIVE:
            case ANIMATOR_TYPE_SIX:
                mAnimatorDuration = 1440;
                break;
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
        int paddingLeft = getPaddingLeft();
        int paddingRight = getPaddingRight();
        int paddingTop = getPaddingTop();
        int paddingBottom = getPaddingBottom();

        int width = w - paddingLeft - paddingRight;
        int height = h - paddingTop - paddingBottom - mPaddingInTop;

        mCenterX = width / 2 + paddingLeft;
        mCenterY = height / 2 + paddingTop;

        int dotParamsWidth = DEFAULT_VIEW_RADIUS;
        int dotParamsHeight = DEFAULT_VIEW_RADIUS;
        if (width < DEFAULT_VIEW_PARAMS) {
            dotParamsWidth = width / 2 - 100;
        }
        if (height < DEFAULT_VIEW_PARAMS) {
            dotParamsHeight = height / 2 - 100;
        }
        int dotParams = Math.min(dotParamsWidth, dotParamsHeight);
        if (dotParams <= 0) {
            dotParams = 0;
        }

        mDotParams = dotParams;
        mViewParams = mDotParams * 2 + 100;
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

        switch (mAnimatorType) {
            case ANIMATOR_TYPE_ONE:
                drawDots(canvas);
                break;
            case ANIMATOR_TYPE_TWO:
                drawDots2(canvas);
                break;
            case ANIMATOR_TYPE_THREE:
                drawDots2Other(canvas); // 渐变放大效果
                break;
            case ANIMATOR_TYPE_FOUR:
                drawDots3(canvas);
                break;
            case ANIMATOR_TYPE_FIVE:
                drawDots3Other(canvas);
                break;
            case ANIMATOR_TYPE_SIX:
                drawDots3Other2(canvas);
                break;
        }
    }

    /**
     * 在布局的中心位置画一个默认的圆角背景
     */
    private void drawBg(Canvas canvas) {
        resetPaint(mBgColor);

        RectF rectf = new RectF(
                mCenterX - mViewParams / 2,
                mCenterY - mViewParams / 2,
                mCenterX + mViewParams / 2,
                mCenterY + mViewParams / 2);
        canvas.drawRoundRect(rectf, 20, 20, mPaint);
    }

    /**
     * 画圆点
     */
    private void drawDots(Canvas canvas) {
        resetPaint(mDotColor);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i + mOffset;
            int x = (int) (mDotParams * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (mDotParams * Math.cos(Math.toRadians(currentAngle)));
            canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
        }
    }

    private void drawDots2(Canvas canvas) {
        resetPaint(mDotColor);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i;
            int x = (int) (mDotParams * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (mDotParams * Math.cos(Math.toRadians(currentAngle)));
            if (i == num) {
                canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS * 2, mPaint);
            } else {
                canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
            }
        }
    }

    private void drawDots2Other(Canvas canvas) {
        resetPaint(mDotColor);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i;
            int x = (int) (mDotParams * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (mDotParams * Math.cos(Math.toRadians(currentAngle)));
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
        resetPaint(mDotColor);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i;
            int x = (int) (mDotParams * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (mDotParams * Math.cos(Math.toRadians(currentAngle)));
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
        resetPaint(mDotColor);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            currentAngle = angleNum * i + mDotOffset;
            int x = (int) (mDotParams * Math.sin(Math.toRadians(currentAngle)));
            int y = (int) (mDotParams * Math.cos(Math.toRadians(currentAngle)));
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
        resetPaint(mDotColor);
        int angleNum = 360 / DEFAULT_DOT_NUM;
        int currentAngle;
        for (int i = 0; i < DEFAULT_DOT_NUM; i++) {
            if (!mIsRevert) {
                if (i == num) {
                    currentAngle = angleNum * i + mDotOffset;
                } else {
                    currentAngle = angleNum * i;
                }
                int x = (int) (mDotParams * Math.sin(Math.toRadians(currentAngle)));
                int y = (int) (mDotParams * Math.cos(Math.toRadians(currentAngle)));
                if (i >= num) {
                    canvas.drawCircle(mCenterX + x, mCenterY - y, DEFAULT_DOT_RADIUS, mPaint);
                }
            } else {
                if (i == num) {
                    currentAngle = angleNum * i + mDotOffset;
                } else {
                    currentAngle = angleNum * i;
                }
                int x = (int) (mDotParams * Math.sin(Math.toRadians(currentAngle)));
                int y = (int) (mDotParams * Math.cos(Math.toRadians(currentAngle)));
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

    private void allAnimatorStart() {
        animatorStart();
        switch (mAnimatorType) {
            case ANIMATOR_TYPE_ONE:
                break;
            case ANIMATOR_TYPE_TWO:
                break;
            case ANIMATOR_TYPE_THREE:
                textAnimatorStart();
                break;
            case ANIMATOR_TYPE_FOUR:
                break;
            case ANIMATOR_TYPE_FIVE:
            case ANIMATOR_TYPE_SIX:
                dotAnimatorStart();
                break;
        }
    }

    private void allStopAnimator() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
            mAnimator.removeAllUpdateListeners();
        }
        if (mTextAnimator != null && mTextAnimator.isRunning()) {
            mTextAnimator.cancel();
            mTextAnimator.removeAllUpdateListeners();
        }
        if (mDotAnimator != null && mDotAnimator.isRunning()) {
            mDotAnimator.cancel();
            mDotAnimator.removeAllUpdateListeners();
        }
        mAnimator = null;
        mTextAnimator = null;
        mDotAnimator = null;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        allAnimatorStart();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        allStopAnimator();
    }
}
