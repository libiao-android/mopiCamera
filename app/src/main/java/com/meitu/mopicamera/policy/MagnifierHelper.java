package com.meitu.mopicamera.policy;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.view.MotionEvent;

/**
 * 放大镜管理类
 * 放大镜功能剥离出来，使用者需要
 * 初始化放大镜位置{initPosition(int left, int top, int right, int bottom)}
 * 设置操作图片{setBitmap(Bitmap bitmap)}
 * 设置图片的矩阵信息{setMatrix(Matrix imageMatrix)}
 * 放大镜可以换边{magnifierNeedChangeEdge()}
 * 可以设置放大镜宽高{setMagnifierLength(int magnifierLength)}
 * 可以设置放大镜距离边沿的距离{setMagnifierPadding(int magnifierPadding)}
 * Created by libiao on 2017/6/26.
 */
public class MagnifierHelper {

    //move过程坐标
    private int mDownMoveX;
    private int mDownMoveY;
    private Bitmap mBitmap;

    private Paint mPaint = new Paint();

    //图片经过矩阵变换后最后的值
    private float[] mValues = new float[9];

    //左边放大镜区域位置
    private Rect mLeftMagnifierRect = new Rect();
    //右边放大镜区域位置
    private Rect mRihgtMagnifierRect = new Rect();
    //默认放大镜区域位置
    private Rect mDefaultMagnifierRect = mLeftMagnifierRect;
    //缩放后的图片位置
    private Rect mScaleRect = new Rect();
    //缩放后的图片位置对应原图上的位置
    private Rect mBitmapRect = new Rect();
    //画放大镜框的笔宽
    private int mMagnifierStrokeWidth = 4;

    //放大镜框的宽高
    private int mMagnifierLength = 300;
    //画圆的笔宽
    private int mCircleStrokeWidth = 2;
    //放大镜框的半宽高
    private int mMagnifierLengthHalf = mMagnifierLength / 2;
    //放大镜框距离控件长度
    private  int mMagnifierPadding = 10;
    //放大镜中心位置
    private int mMagnifierCenter = mMagnifierLength / 2 + mMagnifierPadding;

    /**
     * 初始化放大镜位置
     * @param left 控件位置
     * @param top
     * @param right
     * @param bottom
     */
    public void initPosition(int left, int top, int right, int bottom){
        mLeftMagnifierRect.set(left + mMagnifierPadding, top + mMagnifierPadding,
                left + mMagnifierPadding + mMagnifierLength, top + mMagnifierPadding + mMagnifierLength);
        mRihgtMagnifierRect.set(right - mMagnifierPadding - mMagnifierLength,
                top + mMagnifierPadding, right - mMagnifierPadding,
                top + mMagnifierPadding + mMagnifierLength);
    }

    public void onDraw(Canvas canvas, Paint currentPaint, @Nullable Path path) {
        //画放大镜框
        drawFrame(canvas);
        //画放大镜里的图片
        drawBitmap(canvas);
        //画放大镜里的路径
        drawPath(canvas, currentPaint, path);
        //画放大镜里的圆
        drawCircle(canvas, currentPaint);
    }

    public void onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mDownMoveX = (int)event.getX();
                mDownMoveY = (int)event.getY();
                break;
        }

    }
    /**
     * 放大镜是否需要换边
     * 当有点落在放大镜，显示另一个放大镜
     */
    public void magnifierNeedChangeEdge() {
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
     * 设置操作图片
     * @param bitmap
     */
    public void setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
    }

    /**
     * 设置图片的矩阵信息
     * @param imageMatrix
     */
    public void setMatrix(Matrix imageMatrix) {
        imageMatrix.getValues(mValues);
        setScaleRect();
        setBitmapRect();
    }

    /**
     * 设置放大镜宽高
     * @param magnifierLength
     */
    public void setMagnifierLength(int magnifierLength) {
        mMagnifierLength = magnifierLength;
        mMagnifierLengthHalf = mMagnifierLength / 2;
    }

    /**
     * 设置放大镜距离边沿的距离
     * @param magnifierPadding
     */
    public void setMagnifierPadding(int magnifierPadding) {
        mMagnifierPadding = magnifierPadding;
        mMagnifierCenter = mMagnifierLength / 2 + mMagnifierPadding;
    }

    /**
     * 画圆
     * @param canvas
     * @param currentPaint
     */
    private void drawCircle(Canvas canvas, Paint currentPaint) {
        canvas.save();
        canvas.clipRect(mDefaultMagnifierRect);
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(mCircleStrokeWidth);
        canvas.drawCircle(getCircleX(), getCircleY(), currentPaint.getStrokeWidth()/2, mPaint);

        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(currentPaint.getColor());
        mPaint.setAlpha(currentPaint.getAlpha());
        canvas.drawCircle(getCircleX(), getCircleY(), currentPaint.getStrokeWidth()/2, mPaint);
        canvas.restore();
    }

    /**
     * 画路径
     * @param canvas
     * @param currentPaint
     * @param path
     */
    private void drawPath(Canvas canvas, Paint currentPaint, Path path) {
        if(path == null) return;
        canvas.save();
        canvas.clipRect(mDefaultMagnifierRect);
        canvas.translate(getPathTransX(), getPathTransY());
        canvas.drawPath(path, currentPaint);
        canvas.restore();
    }

    /**
     * 画图片
     * @param canvas
     */
    private void drawBitmap(Canvas canvas) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        canvas.save();
        canvas.clipRect(mDefaultMagnifierRect);
        setBitmapRect();
        canvas.scale(mValues[Matrix.MSCALE_X], mValues[Matrix.MSCALE_X]);
        canvas.translate(getBitmapTransX(), getBitmapTransY());
        canvas.drawBitmap(mBitmap, 0, 0, mPaint);
        canvas.restore();
    }

    /**
     * 画框
     * @param canvas
     */
    private void drawFrame(Canvas canvas) {
        mPaint.reset();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(mMagnifierStrokeWidth);
        canvas.drawRect(mDefaultMagnifierRect, mPaint);
    }

    /**
     * 得到放大镜上绘制图片的X坐标
     * 区分左右放大镜
     * @return
     */
    private float getBitmapTransX() {
        if(mDefaultMagnifierRect == mLeftMagnifierRect){
            //左放大镜
            return -mBitmapRect.left + mMagnifierPadding /mValues[Matrix.MSCALE_X];
        }else{
            //右放大镜
            return mDefaultMagnifierRect.left/mValues[Matrix.MSCALE_X] - mBitmapRect.left;
        }
    }

    /**
     * 得到放大镜上绘制图片的Y坐标，不能超出放大镜范围
     * 区分左右放大镜
     * @return
     */
    private float getBitmapTransY() {
        return -mBitmapRect.top + mMagnifierPadding /mValues[Matrix.MSCALE_X];
    }
    /**
     * 得到放大镜上绘制圆的X坐标，不能超出放大镜范围
     * 区分左右放大镜
     * @return
     */
    private float getCircleX() {
        if(mDefaultMagnifierRect == mLeftMagnifierRect){
            //左放大镜
            int disX = mMagnifierCenter;
            if(mDownMoveX + mMagnifierCenter > mScaleRect.right){
                disX = mDownMoveX - (mScaleRect.right - mMagnifierLengthHalf) + disX;
            }
            if(mDownMoveX >= mScaleRect.right){
                disX = mMagnifierCenter + mMagnifierLengthHalf;
            }
            if(mDownMoveX - mMagnifierCenter < mScaleRect.left){
                disX = disX - (mScaleRect.left + mMagnifierLengthHalf - mDownMoveX);
            }
            if(mDownMoveX <= mScaleRect.left){
                disX = mMagnifierCenter - mMagnifierLengthHalf;
            }
            return disX;
        }else{
            //右放大镜
            int disX = mDefaultMagnifierRect.left + mMagnifierLengthHalf;
            if(mDownMoveX + mMagnifierCenter > mScaleRect.right){
                disX = mDownMoveX - (mScaleRect.right - mMagnifierLengthHalf) + disX;
            }
            if(mDownMoveX >= mScaleRect.right){
                disX = mDefaultMagnifierRect.left + mMagnifierLengthHalf + mMagnifierLengthHalf;
            }
            if(mDownMoveX - mMagnifierCenter < mScaleRect.left){
                disX = disX - (mScaleRect.left + mMagnifierLengthHalf - mDownMoveX);
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
    private float getCircleY() {
        int disY = mMagnifierCenter;
        if(mDownMoveY + mMagnifierLengthHalf > mScaleRect.bottom){
            disY = mDownMoveY - (mScaleRect.bottom - mMagnifierLengthHalf) + disY;
        }
        if(mDownMoveY >= mScaleRect.bottom){
            disY = mMagnifierCenter + mMagnifierLengthHalf;
        }
        if(mDownMoveY - mMagnifierLengthHalf < mScaleRect.top){
            disY = disY - (mScaleRect.top + mMagnifierLengthHalf - mDownMoveY);
        }
        if(mDownMoveY <= mScaleRect.top){
            disY = mMagnifierCenter - mMagnifierLengthHalf;
        }
        return disY;
    }
    /**
     * 画放大镜的路径和截图需要平移画布
     * @return X轴平移的距离
     */
    private int getPathTransX() {
        if(mDefaultMagnifierRect == mLeftMagnifierRect){
            int disX = mDownMoveX - mMagnifierCenter;
            if(mDownMoveX + mMagnifierLengthHalf > mScaleRect.right){
                disX = mScaleRect.right - mMagnifierLengthHalf - mMagnifierCenter;
            }
            if(mDownMoveX - mMagnifierLengthHalf < mScaleRect.left){
                disX = mScaleRect.left + mMagnifierLengthHalf - mMagnifierCenter;
            }
            return -disX;
        }else{
            int disX = mRihgtMagnifierRect.left + mMagnifierLengthHalf - mDownMoveX;
            if(mDownMoveX + mMagnifierLengthHalf > mScaleRect.right){
                // 画布需要移动的距离 = 右放大镜 中心 - 右图片 最大限度
                disX = (mRihgtMagnifierRect.left + mMagnifierLengthHalf) - (mScaleRect.right - mMagnifierLengthHalf);
            }
            if(mDownMoveX - mMagnifierLengthHalf < mScaleRect.left){
                disX = (mRihgtMagnifierRect.left + mMagnifierLengthHalf) - (mScaleRect.left + mMagnifierLengthHalf);
            }
            return disX;
        }
    }

    /**
     * 画放大镜的路径和截图需要平移画布
     * @return Y轴平移的距离
     */
    private int getPathTransY() {
        int disY = mDownMoveY - mMagnifierCenter;
        if(mDownMoveY + mMagnifierLengthHalf > mScaleRect.bottom){
            disY = mScaleRect.bottom - mMagnifierLengthHalf - mMagnifierCenter;
        }
        if(mDownMoveY - mMagnifierLengthHalf < mScaleRect.top){
            disY = mScaleRect.top + mMagnifierLengthHalf - mMagnifierCenter;
        }
        return -disY;

    }

    /**
     * 缩放后图片位置
     */
    private void setScaleRect() {
        float scale = mValues[Matrix.MSCALE_X];
        int leftPosition=(int)mValues[Matrix.MTRANS_X];
        int topPosition=(int)mValues[Matrix.MTRANS_Y];
        int rightPosition=(int)(mValues[Matrix.MTRANS_X]
                + mBitmap.getWidth() * scale);
        int bottomPosition=(int)(mValues[Matrix.MTRANS_Y]
                + mBitmap.getHeight() * scale);
        mScaleRect.set(leftPosition, topPosition, rightPosition, bottomPosition);
    }

    /**
     * 需要放大的原图区域，考虑超出边界的情况
     */
    private void setBitmapRect() {
        float scale = mValues[Matrix.MSCALE_X];
        //找到图片上的中心点
        int bitmapX = (int)((mDownMoveX - mScaleRect.left) / scale);
        int bitmapY = (int)((mDownMoveY - mScaleRect.top) / scale);
        int halfLength = (int)(mMagnifierLengthHalf / scale);
        int left = bitmapX - halfLength;
        int top = bitmapY - halfLength;
        int right = bitmapX + halfLength;
        int bottom = bitmapY + halfLength;
        if(left < 0){
            left = 0;
            right = halfLength * 2;
        }
        if(right > mBitmap.getWidth()){
            left = mBitmap.getWidth() - halfLength * 2;
            right = mBitmap.getWidth();
        }
        if(top < 0){
            top = 0;
            bottom = halfLength * 2;
        }
        if(bottom > mBitmap.getHeight()){
            top = mBitmap.getHeight() - halfLength * 2;
            bottom = mBitmap.getHeight();
        }
        mBitmapRect.set(left, top, right, bottom);
    }
}
