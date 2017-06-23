package com.meitu.mopicamera.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.util.LinkedList;

/**
 * 磨皮涂抹控件的实现
 * Created by libiao on 2017/6/16.
 */
public class MoPiFunctionImageView extends ImageView{

    private float mStartDis;//两指开始距离
    private PointF mMidPoint;//两指中间点

    private Paint mPaint;
    //橡皮画笔
    private Paint mEraserPaint;
    //路径画笔
    private Paint mPathPaint;
    //表示当前是哪只画笔
    private Paint mCurrentPaint;
    //记录当前画笔的透明度值
    private int mCurrentPaintAlpha;
    private int mCurrentPaintMode;
    private Path mPath;
    //ImageView显示的bitmap
    private Bitmap mBitmap;
    //move过程坐标
    private int mDownMoveX;
    private int mDownMoveY;
    //down坐标
    private int mFirstDownX;
    private int mFirstDownY;
    private PaintMask mCurrentMask;
    private LinkedList<PaintMask> mCachePath = new LinkedList<>();
    //缩放、平移实时矩阵
    private Matrix mMatrix = new Matrix();
    //缩放、平移前的矩阵
    private Matrix mCurrentMatrix = new Matrix();
    //图片经过矩阵变换后最后的值
    private float[] mFinallyValues = new float[9];
    //图片经过矩阵变换前的值
    private float[] mCurrentValues = new float[9];
    //缩放后的图片位置
    private RectF mScaleRect = new RectF();
    //左边放大镜区域位置
    private Rect mLeftMagnifierRect = new Rect();
    //右边放大镜区域位置
    private Rect mRihgtMagnifierRect = new Rect();
    //默认放大镜区域位置
    private Rect mDefaultMagnifierRect = mLeftMagnifierRect;
    //是否开始了缩放
    private boolean mBeginZoom = false;
    //初始
    private boolean mFirst = true;
    //初始图片的缩放、移动值
    private float mInitscale = 0;
    private float mInitTranslateX = 0;
    private float mInitTranslateY = 0;
    //ImageView控件的位置
    private int mImageViewLeft = -1;
    private int mImageViewTop;
    private int mImageViewRight;
    private int mImageViewBottom;

    //涂抹完成后的回调器
    private PaintFinishCallback mPaintFinishCallback;

    //操作模式
    private int mOperateMode = DEFAULT;
    private static final int DEFAULT = 0;//默认
    private static final int DRAW = 1;//绘制
    private static final int DRAG = 2;//拖拉
    private static final int DRAG_OR_ZOOM = 3;//拖拉or缩放
    private static final int WITHDRAW = 4;//撤销

    // 最大限制缩放级别
    private float mMaxLimitScale = 6.0f;
    // 最大缩放级别
    private float mMaxScale = 3.0f;
    //最小限制缩放级别
    private float mMinLimitScale = 0.5f;
    //最小缩放级别
    private float mMinScale = (float) Math.sqrt(mMinLimitScale);

    //画圆的笔宽
    private int mCircleStrokeWidth = 2;
    //画放大镜框的笔宽
    private int mMagnifierStrokeWidth = 4;
    //移动阻尼，控制移动速率
    private static final float DRAG_DAMPING = 0.4f;
    //缩放阻尼，控制缩放速率
    private static final float ZOOM_DAMPING = 0.25f;
    //开始缩放的阈值
    private static final float ZOOM_THRESHOLD = 0.3f;
    //放大镜框的宽高
    private static final int MAGNIFIER_LENGTH = 300;
    //放大镜框的半宽高
    private static final int MAGNIFIER_LENGTH_HALF = MAGNIFIER_LENGTH / 2;
    //放大镜框距离控件长度
    private static final int MAGNIFIER_PADDING = 10;
    //放大镜中心位置
    private static final int MAGNIFIER_CENTER = MAGNIFIER_LENGTH / 2 + MAGNIFIER_PADDING;
    //保存绘制路径的个数
    private static final int CACHE_PATH_COUNT = 5;
    //绘制的路径超过一定距离才保存
    private static final int PATH_MOVE_THRESHOLD = 20;
    //默认画笔宽度
    public static final int DEFAULT_PAINT_WIDTH = 60;
    //默认画笔透明度
    public static final int DEFAULT_PAINT_ALPHA = 75;
    //默认画笔颜色
    public static final int DEFAULT_PAINT_COLOR = Color.rgb(209, 222, 238);
    //默认橡皮宽度
    public static final int DEFAULT_ERASER_WIDTH = 60;
    //默认橡皮透明度
    public static final int DEFAULT_ERASER_ALPHA = 75;
    //默认橡皮颜色
    public static final int DEFAULT_ERASER_COLOR = Color.rgb(186, 150, 125);
    //画笔模式
    public static final int PATH_PAINT = 1;
    public static final int ERASER_PAINT = 2;

    private static final int ALPHA_MAX = 255;

    private static final int SELF_FIT_TIME = 300;

    public MoPiFunctionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //图片变换为矩阵类型
        setScaleType(ScaleType.MATRIX);
        mPaint = new Paint();
        //初始化橡皮画笔
        mEraserPaint = new Paint();
        mEraserPaint.setAntiAlias(true);
        mEraserPaint.setStrokeCap(Paint.Cap.ROUND);
        mEraserPaint.setStrokeJoin(Paint.Join.ROUND);
        mEraserPaint.setStyle(Paint.Style.STROKE);
        mEraserPaint.setStrokeWidth(DEFAULT_ERASER_WIDTH);
        mEraserPaint.setColor(DEFAULT_ERASER_COLOR);
        mEraserPaint.setAlpha(DEFAULT_ERASER_ALPHA);
        //初始化路径画笔
        mPathPaint = new Paint();
        mPathPaint.setAntiAlias(true);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeWidth(DEFAULT_PAINT_WIDTH);
        mPathPaint.setColor(DEFAULT_PAINT_COLOR);
        mPathPaint.setAlpha(DEFAULT_PAINT_ALPHA);
        mCurrentPaint = mPathPaint;
        mCurrentPaintMode = PATH_PAINT;
        mPath = new Path();
    }

    /**
     * 记录控件的位置，初始化左右放大镜框的位置
     * 初始化图片的矩阵变换值
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(mImageViewLeft == -1){
            mImageViewLeft = left;
            mImageViewTop = top;
            mImageViewRight = right;
            mImageViewBottom = bottom;
        }
        mLeftMagnifierRect.set(left + MAGNIFIER_PADDING, top + MAGNIFIER_PADDING,
                left + MAGNIFIER_PADDING + MAGNIFIER_LENGTH, top + MAGNIFIER_PADDING + MAGNIFIER_LENGTH);
        mRihgtMagnifierRect.set(right - MAGNIFIER_PADDING - MAGNIFIER_LENGTH,
                top + MAGNIFIER_PADDING, right - MAGNIFIER_PADDING,
                top + MAGNIFIER_PADDING + MAGNIFIER_LENGTH);
        if(getDrawable() != null){
            mBitmap = ((BitmapDrawable)getDrawable()).getBitmap();
            mInitscale = Math.min(((float) (right - left))/mBitmap.getWidth(),((float) (bottom - top))/mBitmap.getHeight());
            mInitTranslateY = (bottom - top - mBitmap.getHeight() * mInitscale)/2;
            mInitTranslateX = (right - left - mBitmap.getWidth() * mInitscale)/2;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mBitmap != null){
            if(mFirst){
                //第一次绘制图片之前，计算好图片的位置
                float left = mInitTranslateX < 0 ? 0 : mInitTranslateX;
                float top = mInitTranslateY < 0 ? 0 : mInitTranslateY;
                float right = left + mBitmap.getWidth() * mInitscale;
                float bottom = top + mBitmap.getHeight() * mInitscale;
                //记录图片位置信息
                mScaleRect.set(left, top, right, bottom );
                //达到自适应位置，进行矩阵变换
                mMatrix.set(getImageMatrix());//在之前的位置基础上进行变换
                mMatrix.postScale(mInitscale,mInitscale);
                mMatrix.postTranslate(left, top);
                mFirst = false;
                setImageMatrix(mMatrix);
            }
        }
        super.onDraw(canvas);

        //单手指模式
        if(mOperateMode == DRAW){
            canvas.save();
            //画布大小为图片大小，不能在图片外绘制
            canvas.clipRect(mScaleRect.left, mScaleRect.top, mScaleRect.right, mScaleRect.bottom);
            //画路径
            canvas.drawPath(mPath, mCurrentPaint);
            //画手指映射的指针圆
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(mCircleStrokeWidth);
            canvas.drawCircle(getBitmapCircleX(), getBitmapCircleY(), mCurrentPaint.getStrokeWidth()/2, mPaint);
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mCurrentPaint.getColor());
            mPaint.setAlpha(mCurrentPaint.getAlpha());
            canvas.drawCircle(getBitmapCircleX(), getBitmapCircleY(), mCurrentPaint.getStrokeWidth()/2, mPaint);
            //画放大镜框
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(mMagnifierStrokeWidth);
            canvas.drawRect(mDefaultMagnifierRect, mPaint);
            canvas.restore();
            //画放大镜的路径和截图，原理是平移画布
            canvas.save();
            canvas.clipRect(mDefaultMagnifierRect);
            canvas.translate(getCanvasTransX(), getCanvasTransY());
            super.onDraw(canvas);
            canvas.drawPath(mPath, mCurrentPaint);
            canvas.restore();

            //画放大镜里的圆
            canvas.save();
            canvas.clipRect(mDefaultMagnifierRect);
            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(Color.WHITE);
            mPaint.setStrokeWidth(mCircleStrokeWidth);
            canvas.drawCircle(getMagnifierCircleX(), getMagnifierCircleY(), mCurrentPaint.getStrokeWidth()/2, mPaint);

            mPaint.reset();
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(mCurrentPaint.getColor());
            mPaint.setAlpha(mCurrentPaint.getAlpha());
            canvas.drawCircle(getMagnifierCircleX(), getMagnifierCircleY(), mCurrentPaint.getStrokeWidth()/2, mPaint);
            canvas.restore();

        }
        //绘制撤销的mask
        if(mOperateMode == WITHDRAW && mCurrentMask != null){
            mPaint.reset();
            mPaint.setAntiAlias(true);
            Bitmap paintBitmap = Bitmap.createBitmap(mImageViewRight-mImageViewLeft,
                    mImageViewBottom-mImageViewTop, Bitmap.Config.ARGB_8888);
            Canvas paintCanvas = new Canvas(paintBitmap);
            for(int i = 0; i < mCachePath.size(); i++){
                if(mCachePath.get(i) != null){
                    drawPath(mCachePath.get(i), paintCanvas);
                }
            }
            drawPath(mCurrentMask, paintCanvas);
            canvas.drawBitmap(paintBitmap, 0,0, mPaint);
        }
    }

    private void drawPath(PaintMask paintPath, Canvas paintCanvas) {
        if(paintPath.paintMode == ERASER_PAINT){
            //橡皮擦擦除磨皮路径
            paintPath.paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
            mCurrentPaintAlpha = paintPath.paint.getAlpha();
            paintPath.paint.setAlpha(ALPHA_MAX);
            paintCanvas.drawPath(paintPath.path, paintPath.paint);
            paintPath.paint.setAlpha(mCurrentPaintAlpha);
            paintPath.paint.setXfermode(null);
        }else{
            paintCanvas.drawPath(paintPath.path, paintPath.paint);
        }
    }

    /**
     * 得到图片上绘制圆的X坐标，不能超出图片范围
     * @return
     */
    private float getBitmapCircleX() {
        int x = mDownMoveX;
        if(mDownMoveX >= mScaleRect.right){
            x = (int)mScaleRect.right;
        }
        if(mDownMoveX <= mScaleRect.left){
            x = (int)mScaleRect.left;
        }
        return x;
    }

    /**
     * 得到图片上绘制圆的Y坐标，不能超出图片范围
     * @return
     */
    private float getBitmapCircleY() {
        int y = mDownMoveY;
        if(mDownMoveY >= mScaleRect.bottom){
            y = (int)mScaleRect.bottom;
        }
        if(mDownMoveY <= mScaleRect.top){
            y = (int)mScaleRect.top;
        }
        return y;
    }

    /**
     * 得到放大镜上绘制圆的X坐标，不能超出放大镜范围
     * 区分左右放大镜
     * @return
     */
    private float getMagnifierCircleX() {
        if(mDefaultMagnifierRect == mLeftMagnifierRect){
            //左放大镜
            int disX = MAGNIFIER_CENTER;
            if(mDownMoveX + MAGNIFIER_CENTER > mScaleRect.right){
                disX = mDownMoveX - ((int)mScaleRect.right - MAGNIFIER_LENGTH_HALF) + disX;
            }
            if(mDownMoveX >= mScaleRect.right){
                disX = MAGNIFIER_CENTER + MAGNIFIER_LENGTH_HALF;
            }
            if(mDownMoveX - MAGNIFIER_CENTER < mScaleRect.left){
                disX = disX - ((int)mScaleRect.left + MAGNIFIER_LENGTH_HALF - mDownMoveX);
            }
            if(mDownMoveX <= mScaleRect.left){
                disX = MAGNIFIER_CENTER - MAGNIFIER_LENGTH_HALF;
            }
            return disX;
        }else{
            //右放大镜
            int disX = mDefaultMagnifierRect.left + MAGNIFIER_LENGTH_HALF;
            if(mDownMoveX + MAGNIFIER_CENTER > mScaleRect.right){
                disX = mDownMoveX - ((int)mScaleRect.right - MAGNIFIER_LENGTH_HALF) + disX;
            }
            if(mDownMoveX >= mScaleRect.right){
                disX = mDefaultMagnifierRect.left + MAGNIFIER_LENGTH_HALF + MAGNIFIER_LENGTH_HALF;
            }
            if(mDownMoveX - MAGNIFIER_CENTER < mScaleRect.left){
                disX = disX - ((int)mScaleRect.left + MAGNIFIER_LENGTH_HALF - mDownMoveX);
            }
            if(mDownMoveX <= mScaleRect.left){
                disX = mDefaultMagnifierRect.left;
            }
            return disX;
        }

    }

    /**
     * 得到放大镜上绘制圆的Y坐标，不能超出放大镜范围
     * 区分左右放大镜
     * @return
     */
    private float getMagnifierCircleY() {
        int disY = MAGNIFIER_CENTER;
        if(mDownMoveY + MAGNIFIER_LENGTH_HALF > mScaleRect.bottom){
            disY = mDownMoveY - ((int)mScaleRect.bottom - MAGNIFIER_LENGTH_HALF) + disY;
        }
        if(mDownMoveY >= mScaleRect.bottom){
            disY = MAGNIFIER_CENTER + MAGNIFIER_LENGTH_HALF;
        }
        if(mDownMoveY - MAGNIFIER_LENGTH_HALF < mScaleRect.top){
            disY = disY - ((int)mScaleRect.top + MAGNIFIER_LENGTH_HALF - mDownMoveY);
        }
        if(mDownMoveY <= mScaleRect.top){
            disY = MAGNIFIER_CENTER - MAGNIFIER_LENGTH_HALF;
        }
        return disY;
    }

    /**
     * 画放大镜的路径和截图需要平移画布
     * @return X轴平移的距离
     */
    private int getCanvasTransX() {
        if(mDefaultMagnifierRect == mLeftMagnifierRect){
            int disX = mDownMoveX - MAGNIFIER_CENTER;
            if(mDownMoveX + MAGNIFIER_LENGTH_HALF > mScaleRect.right){
                disX = (int)mScaleRect.right - MAGNIFIER_LENGTH_HALF - MAGNIFIER_CENTER;
            }
            if(mDownMoveX - MAGNIFIER_LENGTH_HALF < mScaleRect.left){
                disX = (int)mScaleRect.left + MAGNIFIER_LENGTH_HALF - MAGNIFIER_CENTER;
            }
            return -disX;
        }else{
            int disX = mRihgtMagnifierRect.left + MAGNIFIER_LENGTH_HALF - mDownMoveX;
            if(mDownMoveX + MAGNIFIER_LENGTH_HALF > mScaleRect.right){
                // 画布需要移动的距离 = 右放大镜 中心 - 右图片 最大限度
                disX = (mRihgtMagnifierRect.left + MAGNIFIER_LENGTH_HALF) - ((int)mScaleRect.right - MAGNIFIER_LENGTH_HALF);
            }
            if(mDownMoveX - MAGNIFIER_LENGTH_HALF < mScaleRect.left){
                disX = (mRihgtMagnifierRect.left + MAGNIFIER_LENGTH_HALF) - ((int)mScaleRect.left + MAGNIFIER_LENGTH_HALF);
            }
            return disX;
        }
    }

    /**
     * 画放大镜的路径和截图需要平移画布
     * @return Y轴平移的距离
     */
    public int getCanvasTransY() {
        int disY = mDownMoveY - MAGNIFIER_CENTER;
        if(mDownMoveY + MAGNIFIER_LENGTH_HALF > mScaleRect.bottom){
            disY = (int)mScaleRect.bottom - MAGNIFIER_LENGTH_HALF - MAGNIFIER_CENTER;
        }
        if(mDownMoveY - MAGNIFIER_LENGTH_HALF < mScaleRect.top){
            disY = (int)mScaleRect.top + MAGNIFIER_LENGTH_HALF - MAGNIFIER_CENTER;
        }
        return -disY;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointCount = event.getPointerCount();
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                mDownMoveX = (int)event.getX();
                mDownMoveY = (int)event.getY();
                mFirstDownX = mDownMoveX;
                mFirstDownY = mDownMoveY;
                //图片外的事件不处理
                if(isInBitmap(mDownMoveX, mDownMoveY)){
                    mOperateMode = DRAW;
                    mPath.moveTo(mDownMoveX, mDownMoveY);
                    //判断放大镜是否需要换边
                    magnifierNeedChangeEdge();
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                if(pointCount == 2){
                    //两个手指可拖动，可放大
                    mOperateMode = DRAG_OR_ZOOM;

                }
                if(pointCount > 2){
                    //大于2个手指只可拖动
                    mOperateMode = DRAG;
                }
                mStartDis = distance(event);//两点间的初始距离
                mCurrentMatrix.set(getImageMatrix());//记录ImageView当前的变化参数
                mMatrix.getValues(mCurrentValues);
                mMidPoint = mid(event, pointCount);//中心点
                mBeginZoom = false;
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mOperateMode){
                    case DRAG:
                    case DRAG_OR_ZOOM:
                        if(mStartDis == 0){
                            mStartDis = distance(event);//两点间的初始距离
                        }
                        if(mMidPoint == null){
                            mMidPoint = mid(event, pointCount);//中心点
                        }
                        float distance = distance(event);//得到两点之间的距离
                        PointF midPoint = mid(event, pointCount);//得到中间点
                        mMatrix.set(mCurrentMatrix);//在之前的位置基础上进行变换
                        //移动的距离
                        float transX = midPoint.x - mMidPoint.x;
                        float transY = midPoint.y - mMidPoint.y;
                        //控制移动距离
                        transX = transX * DRAG_DAMPING;
                        transY = transY * DRAG_DAMPING;
                        mMatrix.postTranslate(transX, transY);
                        if(mOperateMode == DRAG_OR_ZOOM){
                            float scale = distance / mStartDis;//得到缩放倍数
                            //达到一定值才开始缩放
                            if(Math.abs(1 - scale) > ZOOM_THRESHOLD && !mBeginZoom){
                                mStartDis = distance;
                                mBeginZoom = true;
                                scale = 1;
                            }
                            if(mBeginZoom){
                                scale = scale + (1 - scale) * ZOOM_DAMPING;
                                //缩放倍数不能超过最大\最小值
                                scale = checkFitScale(scale, mCurrentValues);
                                mMatrix.postScale(scale, scale, mMidPoint.x, mMidPoint.y);
                            }
                        }
                        break;
                    case DRAW:
                        mDownMoveX = (int)event.getX();
                        mDownMoveY = (int)event.getY();
                        mPath.lineTo(getBitmapCircleX(), getBitmapCircleY()); // 画线
                        magnifierNeedChangeEdge();
                        break;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                //重新计算图片框大小
                resetScaleRect();
                //图片自适应ImageView位置
                selfFitPosition();
                mStartDis = 0;
                mMidPoint = null;
                if(pointCount == 3){
                    mOperateMode = DRAG_OR_ZOOM;
                }
                if(pointCount == 2){
                    mOperateMode = DEFAULT;
                }
                break;
            case MotionEvent.ACTION_UP:
                //重新计算图片框大小
                resetScaleRect();
                //滑动一定距离才保存绘制路径
                float dx = Math.abs(event.getX() - mFirstDownX);
                float dy = Math.abs(event.getY() - mFirstDownY);
                if(Math.sqrt(dx * dx + dy * dy) > PATH_MOVE_THRESHOLD && mOperateMode == DRAW){
                    addCachePath();
                }
                mOperateMode = DEFAULT;
                mPath.reset();
                break;
        }
        setImageMatrix(mMatrix);
        invalidate();
        return true;
    }

    /**
     * 重新计算图片框大小
     */
    private void resetScaleRect() {
        //重新获取矩阵参数
        mMatrix.getValues(mFinallyValues);
        int leftPosition=(int)mFinallyValues[Matrix.MTRANS_X];
        int topPosition=(int)mFinallyValues[Matrix.MTRANS_Y];
        int rightPosition=(int)(mFinallyValues[Matrix.MTRANS_X]
                + getDrawable().getBounds().width() * mFinallyValues[Matrix.MSCALE_X]);
        int bottomPosition=(int)(mFinallyValues[Matrix.MTRANS_Y]
                + getDrawable().getBounds().height() * mFinallyValues[Matrix.MSCALE_X]);
        mScaleRect.set(leftPosition, topPosition, rightPosition, bottomPosition);
    }

    /**
     * 缩放和平移后自适应ImageView的位置
     */
    private void selfFitPosition() {
        //平移
        float deviateX = ((mImageViewRight - mImageViewLeft) - (mScaleRect.right - mScaleRect.left))/2;
        float deviateY = ((mImageViewBottom - mImageViewTop) - (mScaleRect.bottom - mScaleRect.top))/2;
        float transX = 0;
        float transY = 0;
        if(deviateX >= 0){
            transX = mImageViewLeft + deviateX - mScaleRect.left;
        }
        if(deviateY >= 0){
            transY = mImageViewTop + deviateY - mScaleRect.top;
        }
        if(deviateX < 0 && mScaleRect.left >= mImageViewLeft){
            transX = mImageViewLeft - mScaleRect.left;
        }
        if(deviateX < 0 && mScaleRect.right <= mImageViewRight){
            transX = mImageViewRight - mScaleRect.right;
        }
        if(deviateY < 0 && mScaleRect.top >= mImageViewTop){
            transY = mImageViewTop - mScaleRect.top;
        }
        if(deviateY < 0 && mScaleRect.bottom <= mImageViewBottom){
            transY = mImageViewBottom - mScaleRect.bottom;
        }
        final float animatorX = transX;
        final float animatorY = transY;
        final float scale = mFinallyValues[Matrix.MSCALE_X];
        final PointF midPointF = new PointF(mMidPoint.x, mMidPoint.y);
        mCurrentMatrix.set(getImageMatrix());
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(SELF_FIT_TIME);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mMatrix.set(mCurrentMatrix);
                mMatrix.postTranslate(animatorX * value, animatorY * value);
                if(scale < mInitscale * mMinScale){
                    //回到最小的缩放比例，在ImageView中心点缩放
                    mMatrix.postScale(1 + value * ((mInitscale * mMinScale / scale) - 1),
                            1 + value * ((mInitscale * mMinScale / scale) - 1),
                            (mImageViewRight - mImageViewLeft)/2, (mImageViewBottom - mImageViewTop)/2);
                }
                //回到最大的缩放比例
                if(scale > mInitscale * mMaxScale){
                    mMatrix.postScale(1 + value * ((mInitscale * mMaxScale / scale) - 1),
                            1 + value * ((mInitscale * mMaxScale / scale) - 1),
                            midPointF.x, midPointF.y);
                }
                setImageMatrix(mMatrix);
                invalidate();
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentMatrix.set(getImageMatrix());
            }
        });
        anim.start();
    }

    /**
     * 放大镜是否需要换边
     * 当有点落在放大镜，显示另一个放大镜
     */
    private void magnifierNeedChangeEdge() {
        if(mDownMoveX >= mLeftMagnifierRect.left && mDownMoveX <= mLeftMagnifierRect.right
                && mDownMoveY >= mLeftMagnifierRect.top && mDownMoveY <= mLeftMagnifierRect.bottom){
            mDefaultMagnifierRect = mRihgtMagnifierRect;
        }
        if(mDownMoveX >= mRihgtMagnifierRect.left && mDownMoveX <= mRihgtMagnifierRect.right
                && mDownMoveY >= mRihgtMagnifierRect.top && mDownMoveY <= mRihgtMagnifierRect.bottom){
            mDefaultMagnifierRect = mLeftMagnifierRect;
        }
    }

    /**
     * 保存绘制的路径，最多保存5个
     */
    private void addCachePath() {
        if(mCachePath.size() == 0){
            //回到初始状态
            mCachePath.add(null);
        }
        if(mCachePath.size() > CACHE_PATH_COUNT){
            mCachePath.pollFirst();
            mCachePath.pollFirst();
            mCachePath.addFirst(null);
        }
        if(mCurrentMask != null){
            mCachePath.add(mCurrentMask);
        }
        mCachePath.add(new PaintMask(mCurrentPaintMode, new Paint(mCurrentPaint), new Path(mPath)));

        if(mPaintFinishCallback != null){
            //通知外界完成了一个绘制路径
            mPaintFinishCallback.paintFinish();
        }
    }

    /**
     * 屏幕上按下的点是否在图片上
     * @param downMoveX
     * @param downMoveY
     * @return
     */
    private boolean isInBitmap(int downMoveX, int downMoveY) {
        if(downMoveX < mScaleRect.left
                || downMoveX > mScaleRect.right
                || downMoveY < mScaleRect.top
                || downMoveY > mScaleRect.bottom){
            return false;
        }
        return true;
    }

    /**
     * 控制缩放倍数不能超过最大/最小值
     * @param scale
     * @param values 矩阵参数
     * @return
     */
    private float checkFitScale(float scale, float[] values) {
        if (scale * values[Matrix.MSCALE_X] > mMaxLimitScale * mInitscale)
            scale = (mMaxLimitScale*mInitscale / values[Matrix.MSCALE_X]);
        if (scale * values[Matrix.MSCALE_X] < mMinLimitScale * mInitscale )
            scale = (mMinLimitScale*mInitscale / values[Matrix.MSCALE_X]);
        return scale;
    }

    /**
     * 计算两点之间的距离
     * @param event
     * @return
     */
    private float distance(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float)Math.sqrt(dx * dx + dy * dy);
    }

    //计算多点之间的中间点
    public PointF mid(MotionEvent event, int pointCount){
        float sumX = 0;
        float sumY = 0;
        for(int i = 0;i < pointCount; i++){
            sumX += event.getX(i);
            sumY += event.getY(i);
        }
        return new PointF(sumX / pointCount, sumY / pointCount);
    }

    /**
     * 撤销操作
     */
    public void withdraw() {
        mOperateMode = WITHDRAW;
        mCurrentMask = mCachePath.pollLast();
        invalidate();
    }

    /**
     * 是否有缓存
     * @return
     */
    public boolean hasWithdrawCache() {
        return mCachePath.size() != 0;
    }

    /**
     * 设置完成一次路径绘制的回调
     * @param callback
     */
    public void setPaintFinishCallback(PaintFinishCallback callback) {
        mPaintFinishCallback = callback;
    }

    public void setPaintWidth(int paintWidth) {
        mPathPaint.setStrokeWidth(paintWidth);
    }

    public void setPaintAlpha(int paintAlpha) {
        mPathPaint.setAlpha(paintAlpha);
    }

    public void setPaintColor(int paintColor) {
        mPathPaint.setColor(paintColor);
    }

    public void setEraserWidth(int eraserWidth) {
        mEraserPaint.setStrokeWidth(eraserWidth);
    }

    public void setEraserAlpha(int eraserAlpha) {
        mEraserPaint.setAlpha(eraserAlpha);
    }

    public void setEraserColor(int eraserColor) {
        mEraserPaint.setColor(eraserColor);
    }

    /**
     * 设置画笔模式，有两种选择，PATH_PAINT or ERASER_PAINT
     * @param paint
     */
    public void setPaintMode(int paint) {
        switch (paint){
            case PATH_PAINT:
                mCurrentPaint = mPathPaint;
                mCurrentPaintMode = PATH_PAINT;
                break;
            case ERASER_PAINT:
                mCurrentPaint = mEraserPaint;
                mCurrentPaintMode = ERASER_PAINT;
                break;
        }
    }

    /**
     * 将最后的Mask图层生成bitmap，该图层记录了用户之前所有的操作
     * @return
     */
    public Bitmap getLastMask() {
        Bitmap paintBitmap = Bitmap.createBitmap(mImageViewRight-mImageViewLeft,
                mImageViewBottom-mImageViewTop, Bitmap.Config.ARGB_8888);
        Canvas paintCanvas = new Canvas(paintBitmap);
        for(int i = 0; i < mCachePath.size(); i++){
            if(mCachePath.get(i) != null){
                drawPath(mCachePath.get(i), paintCanvas);
            }
        }
        return paintBitmap;
    }


    /**
     * 路径绘制完成的回调接口
     */
    public interface PaintFinishCallback{
        void paintFinish();
    }

    class PaintMask {
        int paintMode;
        Paint paint;
        Path path;
        public PaintMask(int paintMode, Paint paint, Path path){
            this.paintMode = paintMode;
            this.paint = paint;
            this.path = path;
        }
    }
}

