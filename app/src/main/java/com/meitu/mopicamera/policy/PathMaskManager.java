package com.meitu.mopicamera.policy;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

import com.meitu.mopicamera.view.MoPiFunctionImageView;

import java.util.LinkedList;

/**
 * 涂抹缓存路径管理
 * Created by libiao on 2017/6/27.
 */
public class PathMaskManager {
    //当前处理对象
    private PathMask mCurrentMask;
    //缓存列表
    private LinkedList<PathMask> mCachePath = new LinkedList<>();
    //记录当前画笔的透明度值
    private int mCurrentPaintAlpha;
    //ImageView控件的位置
    private int mImageViewLeft;
    private int mImageViewTop;
    private int mImageViewRight;
    private int mImageViewBottom;
    //透明度最大值
    private static final int ALPHA_MAX = 255;
    //保存绘制路径的个数
    private static final int CACHE_PATH_COUNT = 5;

    /**
     * 保存绘制的路径，最多保存5个
     * @param currentPaintMode
     * @param currentPaint
     * @param path
     */
    public void addCachePath(int currentPaintMode, Paint currentPaint, Path path) {
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
        mCachePath.add(new PathMask(currentPaintMode, new Paint(currentPaint), new Path(path)));
    }

    public PathMask getCurrentMask() {
        return mCurrentMask;
    }

    public LinkedList<PathMask> getCachePathList() {
        return mCachePath;
    }

    //撤销
    public void pollLast() {
        mCurrentMask = mCachePath.pollLast();
    }

    //将所有路径绘制成bitmap
    public Bitmap getLastMask(Bitmap paintBitmap) {
        Canvas paintCanvas = new Canvas(paintBitmap);
        for(int i = 0; i < mCachePath.size(); i++){
            if(mCachePath.get(i) != null){
                drawPath(mCachePath.get(i), paintCanvas);
            }
        }
        return paintBitmap;
    }
    //将路径绘制成bitmap过程
    private void drawPath(PathMask paintPath, Canvas paintCanvas) {
        if(paintPath.paintMode == MoPiFunctionImageView.ERASER_PAINT){
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

    //把当前路径画出来
    public void drawMask(Bitmap paintBitmap) {
        Canvas paintCanvas = new Canvas(paintBitmap);
        for(int i = 0; i < mCachePath.size(); i++){
            if(mCachePath.get(i) != null){
                drawPath(mCachePath.get(i), paintCanvas);
            }
        }
        drawPath(mCurrentMask, paintCanvas);
    }

    public void setPosition(int left, int top, int right, int bottom) {
        mImageViewLeft = left;
        mImageViewTop = top;
        mImageViewRight = right;
        mImageViewBottom = bottom;
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
     * 路径mask对象
     */
    public class PathMask {
        public int paintMode;
        public Paint paint;
        public Path path;
        public PathMask(int paintMode, Paint paint, Path path){
            this.paintMode = paintMode;
            this.paint = paint;
            this.path = path;
        }
    }
}
