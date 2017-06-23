package com.meitu.mopicamera.activity;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.meitu.mopicamera.R;
import com.meitu.mopicamera.util.BitmapUtil;
import com.meitu.mopicamera.view.MoPiFunctionImageView;
import com.meitu.mopicamera.view.MoPiSetttingView;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 磨皮主页
 * Created by libiao on 2017/6/18 0018.
 */
public class MoPiActivity extends AppCompatActivity implements MoPiFunctionImageView.PaintFinishCallback{
    //加载图片的进度条
    private ProgressBar mProgressBar;
    //加载图片任务对象
    private LoadPhotoTask mLoadPhotoTask;
    private SavePhotoTask mSavePhotoTask;
    //磨皮ImageView
    private MoPiFunctionImageView mMoPiIv;
    //撤销按钮
    private Button mWithdrawBtn;
    //设置按钮
    private Button mSettingBtn;
    //磨皮模式选择
    private RadioGroup mPaintModeRg;
    //橡皮模式
    private RadioButton mEraserRb;
    //橡皮模式
    private RadioButton mCleanRb;
    //橡皮模式
    private RadioButton mDeepCleanRb;
    //
    private Button mSaveBtn;
    private Button mCancelBtn;
    //button的点击监听
    private MoPiClickListener mMoPiClickListener;

    private static final String PHOTO_URI = "photoUri";
    //RadioButton图片大小
    private int mDrawableWidth;
    //ImageView控件的宽
    private int mMoPiIvWidth;
    //ImageView控件的高
    private int mMoPiIvHeight;
    //表示根目录
    private String mRootPath = "/storage/emulated/0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_mopi);
        initUI();
        initBounds();
        String photoUri = getIntent().getStringExtra(PHOTO_URI);
        loadPhoto(photoUri);
    }

    /**
     * 初始化UI
     */
    private void initUI() {
        mMoPiClickListener = new MoPiClickListener();
        mProgressBar = (ProgressBar)findViewById(R.id.pb_mopi_activity);
        mMoPiIv = (MoPiFunctionImageView) findViewById(R.id.iv_mo_pi_view);
        mMoPiIv.setPaintFinishCallback(this);
        mWithdrawBtn = (Button) findViewById(R.id.btn_withdraw);
        mWithdrawBtn.setOnClickListener(mMoPiClickListener);
        mSettingBtn = (Button) findViewById(R.id.btn_setting);
        mSettingBtn.setOnClickListener(mMoPiClickListener);
        mSaveBtn = (Button) findViewById(R.id.activity_save);
        mSaveBtn.setOnClickListener(mMoPiClickListener);
        mCancelBtn = (Button) findViewById(R.id.activity_cancel);
        mCancelBtn.setOnClickListener(mMoPiClickListener);
        mPaintModeRg = (RadioGroup) findViewById(R.id.rg_paint_mode);
        mEraserRb = (RadioButton) findViewById(R.id.rb_eraser);
        mCleanRb = (RadioButton) findViewById(R.id.rb_clean);
        mDeepCleanRb = (RadioButton) findViewById(R.id.rb_deep_clean);
        mPaintModeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_clean:
                    case R.id.rb_deep_clean:
                        mMoPiIv.setPaintMode(MoPiFunctionImageView.PATH_PAINT);
                        break;
                    case R.id.rb_eraser:
                        mMoPiIv.setPaintMode(MoPiFunctionImageView.ERASER_PAINT);
                        break;
                }
            }
        });
        refreshBtnState();
    }

    /**
     * 设置RadioButton的图片大小和其它值
     */
    private void initBounds() {
        mDrawableWidth = (int)getResources().getDimension(R.dimen.mopi_radio_button_drawable_width);
        Drawable clean = getResources().getDrawable(R.drawable.radio_paint_clean);
        clean.setBounds(0, 0, mDrawableWidth, mDrawableWidth);
        mCleanRb.setCompoundDrawables(clean, null,null,null);

        Drawable deepClean = getResources().getDrawable(R.drawable.radio_paint_deep_clean);
        deepClean.setBounds(0, 0, mDrawableWidth, mDrawableWidth);
        mDeepCleanRb.setCompoundDrawables(deepClean, null,null,null);

        Drawable eraser = getResources().getDrawable(R.drawable.radio_paint_eraser);
        eraser.setBounds(0, 0, mDrawableWidth, mDrawableWidth);
        mEraserRb.setCompoundDrawables(eraser, null,null,null);

        //通过屏幕宽高大概计算ImageView的宽高，以便采样加载大图
        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        wm.getDefaultDisplay().getSize(point);
        mMoPiIvWidth = point.x;
        mMoPiIvHeight = point.y - (int)getResources().getDimension(R.dimen.mopi_activity_paint_menu_height)
                - (int)getResources().getDimension(R.dimen.mopi_activity_save_menu_height);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initPaintAndEraserValue();
    }

    /**
     * 给画笔和橡皮设置宽度、颜色、透明度
     */
    private void initPaintAndEraserValue() {
        SharedPreferences sp = getSharedPreferences(MoPiSetttingView.SETTING_VALUE, Context.MODE_PRIVATE);
        mMoPiIv.setPaintWidth(sp.getInt(MoPiSetttingView.PAINT_WIDTH_KEY, MoPiFunctionImageView.DEFAULT_PAINT_WIDTH));
        mMoPiIv.setPaintColor(sp.getInt(MoPiSetttingView.PAINT_COLOR_KEY, MoPiFunctionImageView.DEFAULT_PAINT_COLOR));
        mMoPiIv.setPaintAlpha(sp.getInt(MoPiSetttingView.PAINT_ALPHA_KEY, MoPiFunctionImageView.DEFAULT_PAINT_ALPHA));
        mMoPiIv.setEraserWidth(sp.getInt(MoPiSetttingView.ERASER_WIDTH_KEY, MoPiFunctionImageView.DEFAULT_ERASER_WIDTH));
        mMoPiIv.setEraserColor(sp.getInt(MoPiSetttingView.ERASER_COLOR_KEY, MoPiFunctionImageView.DEFAULT_ERASER_COLOR));
        mMoPiIv.setEraserAlpha(sp.getInt(MoPiSetttingView.ERASER_ALPHA_KEY, MoPiFunctionImageView.DEFAULT_ERASER_ALPHA));
    }

    /**
     * 刷新按钮不可用状态
     */
    private void refreshBtnState() {
        if(mMoPiIv.hasWithdrawCache()){
            mWithdrawBtn.setEnabled(true);
            mEraserRb.setEnabled(true);
        }else{
            mWithdrawBtn.setEnabled(false);
            mEraserRb.setEnabled(false);
            mMoPiIv.setPaintMode(MoPiFunctionImageView.PATH_PAINT);
            if(mEraserRb.isChecked()){
                mEraserRb.setChecked(false);
                mCleanRb.setChecked(true);
            }
        }
    }

    /**
     * 启动加载图片任务
     * @param photoUri 通过URI加载
     */
    private void loadPhoto(String photoUri) {
        if(mLoadPhotoTask != null){
            mLoadPhotoTask.cancel(false);
            mLoadPhotoTask = null;
        }
        mLoadPhotoTask = new LoadPhotoTask();
        mLoadPhotoTask.execute(photoUri);
    }

    @Override
    public void paintFinish() {
        refreshBtnState();
    }

    /**
     * 通过phototUri加载一张图片
     */
    private class LoadPhotoTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Uri photoUri = Uri.parse(params[0]);
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(photoUri, filePathColumns, null, null, null);
            if(c != null && c.moveToFirst()){
                int columnIndex = c.getColumnIndex(filePathColumns[0]);
                String imagePath = c.getString(columnIndex);
                c.close();
                Bitmap bitmap = BitmapUtil.scaleFromSdcard(imagePath, mMoPiIvWidth, mMoPiIvHeight);
                return bitmap;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null){
                mMoPiIv.setImageBitmap(bitmap);
                mProgressBar.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 保存图片
     */
    private class SavePhotoTask extends AsyncTask<Bitmap, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Bitmap... params) {
            return save(params[0]);
        }

        @Override
        protected void onPreExecute() {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Boolean success) {
            mProgressBar.setVisibility(View.GONE);
            if(success){
                Toast.makeText(MoPiActivity.this, R.string.mopi_activity_save_success, Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(MoPiActivity.this, R.string.mopi_activity_save_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 按钮的事件监听
     */
    private class MoPiClickListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_withdraw:
                    mMoPiIv.withdraw();
                    refreshBtnState();
                    break;
                case R.id.btn_setting:
                    Intent intent = new Intent(MoPiActivity.this, SettingActivity.class);
                    startActivity(intent);
                    break;
                case R.id.activity_cancel:
                    finish();
                    break;
                case R.id.activity_save:
                    if(mSavePhotoTask != null){
                        mSavePhotoTask.cancel(false);
                        mSavePhotoTask = null;
                    }
                    mSavePhotoTask = new SavePhotoTask();
                    mSavePhotoTask.execute(mMoPiIv.getLastMask());
                    break;
            }
        }
    }

    /**
     * 保存图片到本地
     * @param lastMask
     * @return
     */
    private boolean save(Bitmap lastMask) {
        File file = new File(mRootPath, System.currentTimeMillis()+".png");
        try {
            FileOutputStream out = new FileOutputStream(file);
            lastMask.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
