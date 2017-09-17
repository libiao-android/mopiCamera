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
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.meitu.mopicamera.policy.MagnifierHelper;
import com.meitu.mopicamera.policy.PathMaskManager;

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
    //图片自适应回弹动画
    private ValueAnimator mSelfFitAnim;
    //涂抹完成后的回调器
    private PaintFinishCallback mPaintFinishCallback;
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

    //是否开始了缩放
    private boolean mBeginZoom = false;

    //初始图片的缩放、移动值
    private float mInitscale = 0;
    private float mInitTranslateX = 0;
    private float mInitTranslateY = 0;
    //ImageView控件的位置
    private int mImageViewLeft = -1;
    private int mImageViewTop;
    private int mImageViewRight;
    private int mImageViewBottom;

    //放大镜对象
    private MagnifierHelper mMagnifierHelper;
    //路径缓存对象
    private PathMaskManager mPathMaskManager;

    //操作模式
    private int mOperateMode = DEFAULT;
    private static final int DEFAULT = 0;//默认
    private static final int DRAW = 1;//绘制
    private static final int DRAG = 2;//拖拉
    private static final int DRAG_OR_ZOOM = 3;//拖拉or缩放
    private static final int REVOKE = 4;//撤销

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

    //移动阻尼，控制移动速率
    private static final float DRAG_DAMPING = 0.4f;
    //缩放阻尼，控制缩放速率
    private static final float ZOOM_DAMPING = 0.25f;
    //开始缩放的阈值
    private static final float ZOOM_THRESHOLD = 0.3f;

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

    //图片缩放回弹动画时间
    private static final int SELF_FIT_TIME = 300;

    public MoPiFunctionImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //图片变换为矩阵类型
        setScaleType(ScaleType.MATRIX);
        initPaint();
        mPath = new Path();
        mMagnifierHelper = new MagnifierHelper();
    }

    private void initPaint() {
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
    }

    /**
     * 记录控件的位置
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
            mMagnifierHelper.initPosition(left, top, right, bottom);
            mPathMaskManager.setPosition(left, top, right, bottom);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mBitmap == null) return;
        super.onDraw(canvas);

        //单手指模式
        if(mOperateMode == DRAW){
            drawMode(canvas);
        }
        //绘制撤销的mask
        if(mOperateMode == REVOKE && mPathMaskManager.getCurrentMask() != null){
            revokeMode(canvas);
        }
    }

    /**
     * 撤销模式下绘制mask
     * @param canvas
     */
    private void revokeMode(Canvas canvas) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        Bitmap paintBitmap = Bitmap.createBitmap(mImageViewRight-mImageViewLeft,
                mImageViewBottom-mImageViewTop, Bitmap.Config.ARGB_8888);
        mPathMaskManager.drawMask(paintBitmap);
        canvas.drawBitmap(paintBitmap, 0,0, mPaint);
    }

    /**
     * 绘制模式下绘制path，放大镜
     * @param canvas
     */
    private void drawMode(Canvas canvas) {
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
        canvas.restore();
        //画放大镜
        mMagnifierHelper.onDraw(canvas, mCurrentPaint, mPath);
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mMagnifierHelper.onTouchEvent(event);
        int pointCount = event.getPointerCount();
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                eventDown(event);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                eventPointerDown(event, pointCount);
                break;
            case MotionEvent.ACTION_MOVE:
                switch (mOperateMode){
                    case DRAG:
                    case DRAG_OR_ZOOM:
                        //如果正在进行图片自适应动画，直接返回
                        if(mSelfFitAnim != null && mSelfFitAnim.isStarted()){
                            return true;
                        }
                        eventMoveForDragOrZoom(event, pointCount);
                        break;
                    case DRAW:
                        eventMoveForDraw(event);
                        break;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                eventPointerUp(event, pointCount);
                break;
            case MotionEvent.ACTION_UP:
                eventUp(event);
                break;
        }
        setImageMatrix(mMatrix);
        invalidate();
        return true;
    }

    /**
     * up事件的处理
     * @param event
     */
    private void eventUp(MotionEvent event) {
        //重新计算图片框大小
        resetScaleRect();
        //滑动一定距离才保存绘制路径
        float dx = Math.abs(event.getX() - mFirstDownX);
        float dy = Math.abs(event.getY() - mFirstDownY);
        if(Math.sqrt(dx * dx + dy * dy) > PATH_MOVE_THRESHOLD && mOperateMode == DRAW){
            mPathMaskManager.addCachePath(mCurrentPaintMode, mCurrentPaint, mPath);
            if(mPaintFinishCallback != null){
                //通知外界完成了一个绘制路径
                mPaintFinishCallback.paintFinish();
            }
        }
        mOperateMode = DEFAULT;
        mPath.reset();
    }

    /**
     * 多指up事件的处理
     * @param event
     * @param pointCount
     */
    private void eventPointerUp(MotionEvent event, int pointCount) {
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
    }

    /**
     * move事件下绘制模式的处理
     * @param event
     */
    private void eventMoveForDraw(MotionEvent event) {
        mDownMoveX = (int)event.getX();
        mDownMoveY = (int)event.getY();
        mPath.lineTo(getBitmapCircleX(), getBitmapCircleY()); // 画线
        mMagnifierHelper.magnifierNeedChangeEdge();
    }

    /**
     * move事件下拖拽模式的处理
     * @param event
     * @param pointCount
     */
    private void eventMoveForDragOrZoom(MotionEvent event, int pointCount) {
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
    }

    /**
     * 多指down事件的处理
     * @param event
     * @param pointCount
     */
    private void eventPointerDown(MotionEvent event, int pointCount) {
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
    }

    /**
     * down事件的处理
     * @param event
     */
    private void eventDown(MotionEvent event) {
        mDownMoveX = (int)event.getX();
        mDownMoveY = (int)event.getY();
        mFirstDownX = mDownMoveX;
        mFirstDownY = mDownMoveY;
        //图片外的事件不处理
        if(isInBitmap(mDownMoveX, mDownMoveY)){
            mOperateMode = DRAW;
            mPath.moveTo(mDownMoveX, mDownMoveY);
            //把当前的矩阵信息给到放大镜
            mMagnifierHelper.setMatrix(getImageMatrix());
            //判断放大镜是否需要换边
            mMagnifierHelper.magnifierNeedChangeEdge();
        }
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
        if(mMidPoint == null) return;//连续两个up的场景
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
        //缩放
        float scaleX = 0;
        float scaleY = 0;
        float midX = 0;
        float midY = 0;
        final float scale = mFinallyValues[Matrix.MSCALE_X];
        final PointF midPointF = new PointF(mMidPoint.x, mMidPoint.y);
        if(scale < mInitscale * mMinScale){
            //回到最小的缩放比例，在ImageView中心点缩放
            scaleX = mInitscale * mMinScale / scale;
            scaleY = mInitscale * mMinScale / scale;
            midX = (mImageViewRight - mImageViewLeft)/2;
            midY = (mImageViewBottom - mImageViewTop)/2;
        }
        if(scale > mInitscale * mMaxScale){
            //回到最大的缩放比例
            scaleX = mInitscale * mMaxScale / scale;
            scaleY = mInitscale * mMaxScale / scale;
            midX = midPointF.x;
            midY = midPointF.y;
        }
        //开始动画
        startFitAnimator(transX, transY, scale, scaleX, scaleY, midX, midY);

    }

    /**
     * 图片自适应回弹动画
     * @param transX 需要平移X
     * @param transY 需要平移Y
     * @param scale 图片当前缩放
     * @param scaleX X轴需要缩放
     * @param scaleY Y轴需要缩放
     * @param midX 缩放中心点X
     * @param midY 缩放中心点Y
     */
    private void startFitAnimator(final float transX, final float transY, final float scale, final float scaleX,
                                  final float scaleY, final float midX, final float midY) {
        mCurrentMatrix.set(getImageMatrix());
        if(mSelfFitAnim != null){
            mSelfFitAnim.cancel();
        }
        mSelfFitAnim = ValueAnimator.ofFloat(0f, 1f);
        mSelfFitAnim.setDuration(SELF_FIT_TIME);
        mSelfFitAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mMatrix.set(mCurrentMatrix);
                mMatrix.postTranslate(transX * value, transY * value);
                if(scale < mInitscale * mMinScale || scale > mInitscale * mMaxScale){
                    mMatrix.postScale(1 + value * (scaleX - 1),
                            1 + value * (scaleY - 1),
                            midX, midY);
                }
                setImageMatrix(mMatrix);
                invalidate();
            }
        });
        mSelfFitAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCurrentMatrix.set(getImageMatrix());
                //重新计算图片框大小
                resetScaleRect();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mCurrentMatrix.set(getImageMatrix());
                //重新计算图片框大小
                resetScaleRect();
            }
        });
        mSelfFitAnim.start();
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
            scale = (mMaxLimitScale * mInitscale / values[Matrix.MSCALE_X]);
        if (scale * values[Matrix.MSCALE_X] < mMinLimitScale * mInitscale )
            scale = (mMinLimitScale * mInitscale / values[Matrix.MSCALE_X]);
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
    public void revokeOperation() {
        mOperateMode = REVOKE;
        mPathMaskManager.pollLast();
        invalidate();
    }

    /**
     * 是否有缓存
     * @return
     */
    public boolean hasCachePath() {
        return mPathMaskManager.getCachePathList().size() != 0;
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
     * 设置完成一次路径绘制的回调
     * @param callback
     */
    public void setPaintFinishCallback(PaintFinishCallback callback) {
        mPaintFinishCallback = callback;
    }

    /**
     * 由外部把图片传进来，初始化图片信息，矩阵信息
     */
    public void initImageBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        mMagnifierHelper.setBitmap(mBitmap);
        mInitscale = Math.min(((float) (mImageViewRight - mImageViewLeft))/mBitmap.getWidth(),
                ((float) (mImageViewBottom - mImageViewTop))/mBitmap.getHeight());
        mInitTranslateY = (mImageViewBottom - mImageViewTop - mBitmap.getHeight() * mInitscale)/2;
        mInitTranslateX = (mImageViewRight - mImageViewLeft - mBitmap.getWidth() * mInitscale)/2;
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
        setImageMatrix(mMatrix);
        setImageBitmap(bitmap);
    }

    public void setPathMaskManager(PathMaskManager maskManager) {
        mPathMaskManager = maskManager;
    }

    /**
     * 路径绘制完成的回调接口
     */
    public interface PaintFinishCallback{
        void paintFinish();
    }
}

