package com.kky.wangfang.scanprogress.scanprogress;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.kky.wangfang.scanprogress.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <h1>显示进度条</h1>
 */

public class ScanProgress extends View {
    /**
     * 同时存在的小圆圈的最大数目
     */
    private static final int SUM_CIRCLE_IMAGE = 10;
    /**
     * 圆圈出现在进度左右的范围
     */
    private static final int SUM_CIRCLE_X = 200;
    /**
     * 圆圈出现在进度上下的范围
     */
    private static final int SUM_CIRCLE_Y = 300;
    /**
     * 圆圈的半径
     */
    private static final int CIRCLE_RADIUS= 40;
    /**
     * icon在x方向上移动的最大距离
     */
    private static final int SUM_MOVE_X = 200;
    /**
     * icon在y方向上移动的最大距离
     */
    private static final int SUM_MOVE_Y = 500;
    /**
     * icon翻转的最大角度
     */
    private static final int SUM_ROTATE = 120;
    /**
     * 进度的最大值
     */
    public static final int SUM_PROGRESS = 100;
    /**
     * 同时存在的icon最大数目，用于内存优化
     */
    private static final int SUM_ICON_IMAGE = 10;
    /**
     * 进度动画的时间
     */
    private static final int DURING_PROGRESS = 2000;
    /**
     * 每一个icon的动画时间
     */
    private static final int DURING_ICON = 700;
    /**
     * 每一个圆圈的动画时间
     */
    private static final int DURING_CIRCLE = 300;
    /**
     * 每一个icon显示的间隔时间
     */
    private static final int DURING_ICON_OFFSET = 300;
    /**
     * 进度条的前置颜色
     */
    private final int FORE_COLOR;
    /**
     * 进度条的宽度
     */
    private int mWidth;
    /**
     * 进度条的高度
     */
    private int mHeight;
    /**
     * 进度画笔
     */
    private Paint mProPaint;
    /**
     * 当前的进度值
     */
    private int mProgress;
    /**
     * 当前进度条所在的x值
     */
    private int mXOffset;
    /**
     * 当前进度条所在的y值
     */
    private int mYOffset;
    /**
     * 当前icon的索引
     */
    private int mIconIndex;
    /**
     * 每个icon在x方向上的差值
     */
    private int mIconPerOffset;
    /**
     * 记录上一次icon显示的位置
     */
    private int mLastProgress = SUM_PROGRESS;
    /**
     * 所有icon的集合
     */
    private List<Drawable> mIconsDrawables;
    /**
     * 用于储存复用ImageView，避免oom
     */
    private SparseArray<ImageView> mIconAniIvs;
    /**
     * 当前可用的ImageView id
     */
    private List<Integer> mIconAvailableIds;
    /**
     * 用于储存复用ImageView，避免oom
     */
    private SparseArray<ImageView> mCircleAniIvs;
    /**
     * 当前可用的ImageView id
     */
    private List<Integer> mCircleAvailableIds;
    /**
     * 用于管理所有的动画效果
     */
    private List<Animator> mAnimators;

    public ScanProgress(Context context) {
        this(context, null);
    }

    public ScanProgress(Context context, AttributeSet attrs) {
        super(context, attrs);

        // 获取属性
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScanProgress);
        FORE_COLOR = typedArray.getColor(R.styleable.ScanProgress_foreColor, Color.BLUE);
        typedArray.recycle();

        initPaint();
        mAnimators = new ArrayList<>();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        mProPaint = new Paint();
        mProPaint.setColor(FORE_COLOR);
        mProPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            mYOffset = getTop() + mHeight / 2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawProgress(canvas);
    }

    /**
     * 绘制进度矩形
     */
    private void drawProgress(Canvas canvas) {
        canvas.drawRect(0, 0, mXOffset, mHeight, mProPaint);
    }

    /**
     * 设置当前的进度值
     * @param progress 进度值
     */
    public void setProgress(int progress) {
        if (mProgress != 0 && progress != SUM_PROGRESS && mProgress != progress) {
            startCircleItemAni();
            if (mLastProgress - mProgress >= mIconPerOffset) {
                mLastProgress = mProgress;
                startIconItemAni();
                mIconIndex++;
            }
        }
        mProgress = progress;
        mXOffset = (int) (((float)mProgress / SUM_PROGRESS) * mWidth);
        invalidate();
    }


    /**
     * 开始清理动画
     */
    public void startProgress() {
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "progress", SUM_PROGRESS, 0);
        animator.setDuration(DURING_PROGRESS);
        animator.start();
    }

    /**
     * 绑定一个activity用于在该activity中显示图标和气泡
     */
    public void bindGroup(ViewGroup group, List<Drawable> icons) {
        mIconsDrawables = icons;
        if (mIconsDrawables == null) {
            return;
        }
        mIconPerOffset = Math.max(SUM_PROGRESS / mIconsDrawables.size(), 1);

        mIconAniIvs = new SparseArray<>(SUM_ICON_IMAGE);
        mIconAvailableIds = new ArrayList<>(SUM_ICON_IMAGE);
        mCircleAniIvs = new SparseArray<>(SUM_CIRCLE_IMAGE);
        mCircleAvailableIds = new ArrayList<>(SUM_CIRCLE_IMAGE);

        for (int i = 0; i < SUM_ICON_IMAGE; i++) {
            // 将显示icon的ImageView加入到布局
            // 初始化所有的控制容器
            mIconAvailableIds.add(i);
            ImageView imageView = new ImageView(getContext());
            imageView.setAlpha(0f);
            mIconAniIvs.put(i, imageView);
            group.addView(imageView);
        }

        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(CIRCLE_RADIUS, CIRCLE_RADIUS);
        Drawable circleDrawable = getResources().getDrawable(R.drawable.img_clean_progress_shape);

        for (int i = 0; i < SUM_CIRCLE_IMAGE; i++) {
            // 将显示icon的ImageView加入到布局
            // 初始化所有的控制容器
            mCircleAvailableIds.add(i);
            ImageView imageView = new ImageView(getContext());
            imageView.setLayoutParams(params);
            imageView.setImageDrawable(circleDrawable);
            imageView.setAlpha(0f);
            mCircleAniIvs.put(i, imageView);
            group.addView(imageView);
        }
    }

    /**
     * 开始单个circle的动画效果
     */
    private void startCircleItemAni() {
        if (!mCircleAvailableIds.isEmpty()) {
            final int showId = mCircleAvailableIds.remove(0);
            final ImageView showIv = mCircleAniIvs.get(showId);
            float xFactor = new Random().nextFloat() - 0.5f;
            float yFactor = new Random().nextFloat() - 0.5f;

            int nowX = (int) (mXOffset - CIRCLE_RADIUS / 2 + xFactor * SUM_CIRCLE_X);
            int nowY = (int) (mYOffset + yFactor * SUM_CIRCLE_Y);
            showIv.setX(nowX);
            showIv.setY(nowY);

            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(DURING_CIRCLE);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                final float alphaSeq = 0.5f;
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (value <= alphaSeq) { // scale
                        float scaleFactor = value / alphaSeq;
                        showIv.setScaleX(scaleFactor);
                        showIv.setScaleY(scaleFactor);
                    } else {    // alpha
                        float alphaFactor = (value - alphaSeq) / (1 - alphaSeq);
                        showIv.setAlpha(1 - alphaFactor);
                    }
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    showIv.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    showIv.setAlpha(0f);
                    mAnimators.remove(animation);
                    mCircleAvailableIds.add(showId);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
            mAnimators.add(animator);
        }
    }

    /**
     * 开始单个icon的动画效果
     */
    private void startIconItemAni() {
        if (!mIconAvailableIds.isEmpty()) {
            Drawable drawable = mIconsDrawables.get(mIconIndex);
            final int showId = mIconAvailableIds.remove(0);
            final ImageView showIv = mIconAniIvs.get(showId);

            final int drawableWidth = drawable.getIntrinsicWidth();
            final float yFactor = new Random().nextFloat() - 0.5f;
            final int nowX = mXOffset - drawableWidth / 2;
            final int nowY = mYOffset - drawableWidth / 2;

            showIv.setImageDrawable(drawable);
            ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(DURING_ICON);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                private float alphaSep = 0.8f;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    showIv.setX(nowX + SUM_MOVE_X * value);
                    showIv.setY(nowY + SUM_MOVE_Y * value * yFactor);
                    showIv.setRotation(-SUM_ROTATE * value * yFactor);
                    if (value <= alphaSep) { // 改变scale
                        float scaleFactor = value / alphaSep;
                        showIv.setScaleX(scaleFactor);
                        showIv.setScaleY(scaleFactor);
                    } else {   // 改变alpha
                        float alphaFactor = (value - alphaSep) / (1 - alphaSep);
                        showIv.setAlpha(1 - alphaFactor);
                    }
                }
            });
            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    showIv.setAlpha(1f);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAnimators.remove(animation);
                    mIconAvailableIds.add(showId);
                    showIv.setAlpha(0f);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            animator.start();
            mAnimators.add(animator);
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        mCircleAniIvs.clear();
        mCircleAvailableIds.clear();
        mIconsDrawables.clear();
        mIconAvailableIds.clear();
        mIconsDrawables.clear();

        for (Animator animator : mAnimators) {
            if (animator.isRunning()) {
                animator.cancel();
            }
        }
    }

}
