package com.meitu.mopicamera.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.meitu.mopicamera.R;

/**
 * 给画笔和橡皮设置宽度、颜色和颜色的view
 * Created by libiao on 2017/6/20.
 */
public class MoPiSetttingView extends LinearLayout{
    //保存按钮
    private Button mSaveBtn;
    //恢复默认值按钮
    private Button mRestoreBtn;
    //画笔宽度值
    private TextView mPaintWidthValueTv;
    //画笔透明度值
    private TextView mPaintAlphaValueTv;
    //画笔颜色R值
    private TextView mPaintColorRValueTv;
    //画笔颜色G值
    private TextView mPaintColorGValueTv;
    //画笔颜色B值
    private TextView mPaintColorBValueTv;
    //画笔颜色
    private ImageView mPaintColorIv;
    //画笔宽度拖动条
    private SeekBar mPaintWidthValueSb;
    //画笔透明度拖动条
    private SeekBar mPaintAlphaValueSb;
    //画笔颜色R拖动条
    private SeekBar mPaintColorRValueSb;
    //画笔颜色G拖动条
    private SeekBar mPaintColorGValueSb;
    //画笔颜色B拖动条
    private SeekBar mPaintColorBValueSb;

    //橡皮宽度值
    private TextView mEraserWidthValueTv;
    //橡皮透明度值
    private TextView mEraserAlphaValueTv;
    //橡皮颜色R值
    private TextView mEraserColorRValueTv;
    //橡皮颜色G值
    private TextView mEraserColorGValueTv;
    //橡皮颜色B值
    private TextView mEraserColorBValueTv;
    //橡皮颜色
    private ImageView mEraserColorIv;
    //橡皮宽度拖动条
    private SeekBar mEraserWidthValueSb;
    //橡皮透明度拖动条
    private SeekBar mEraserAlphaValueSb;
    //橡皮颜色R拖动条
    private SeekBar mEraserColorRValueSb;
    //橡皮颜色G拖动条
    private SeekBar mEraserColorGValueSb;
    //橡皮颜色B拖动条
    private SeekBar mEraserColorBValueSb;
    //拖动条监听
    private SettingSeekBarChangeListener mSeekBarChangeListener;

    private int mPaintWidthValue;
    private int mPaintAlphaValue;
    private int mPaintColorRValue;
    private int mPaintColorGValue;
    private int mPaintColorBValue;

    private int mEraserWidthValue;
    private int mEraserAlphaValue;
    private int mEraserColorRValue;
    private int mEraserColorGValue;
    private int mEraserColorBValue;

    public static final String SETTING_VALUE = "SettingValue";
    public static final String PAINT_WIDTH_KEY = "PaintWidth";
    public static final String PAINT_ALPHA_KEY = "PaintAlpha";
    public static final String PAINT_COLOR_KEY = "PaintColor";

    public static final String ERASER_WIDTH_KEY = "EraserWidth";
    public static final String ERASER_ALPHA_KEY = "EraserAlpha";
    public static final String ERASER_COLOR_KEY = "EraserColor";

    public MoPiSetttingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initUI();
        initValue();
    }

    /**
     * 从SharedPreferences读取值，给各控件赋值
     */
    private void initValue() {
        SharedPreferences sp = getContext().getSharedPreferences(SETTING_VALUE, Context.MODE_PRIVATE);

        mPaintWidthValue = sp.getInt(PAINT_WIDTH_KEY, MoPiFunctionImageView.DEFAULT_PAINT_WIDTH);
        mPaintAlphaValue = sp.getInt(PAINT_ALPHA_KEY, MoPiFunctionImageView.DEFAULT_PAINT_ALPHA);
        int paintColor = sp.getInt(PAINT_COLOR_KEY, MoPiFunctionImageView.DEFAULT_PAINT_COLOR);
        mPaintColorRValue = Color.red(paintColor);
        mPaintColorGValue = Color.green(paintColor);
        mPaintColorBValue = Color.blue(paintColor);

        mEraserWidthValue = sp.getInt(ERASER_WIDTH_KEY, MoPiFunctionImageView.DEFAULT_ERASER_WIDTH);
        mEraserAlphaValue = sp.getInt(ERASER_ALPHA_KEY, MoPiFunctionImageView.DEFAULT_ERASER_ALPHA);
        int eraserColor = sp.getInt(ERASER_COLOR_KEY, MoPiFunctionImageView.DEFAULT_ERASER_COLOR);
        mEraserColorRValue = Color.red(eraserColor);
        mEraserColorGValue = Color.green(eraserColor);
        mEraserColorBValue = Color.blue(eraserColor);
        mPaintWidthValueTv.setText(String.valueOf(mPaintWidthValue));
        mPaintWidthValueSb.setProgress(mPaintWidthValue);

        mPaintAlphaValueTv.setText(String.valueOf(mPaintAlphaValue));
        mPaintAlphaValueSb.setProgress(mPaintAlphaValue);

        mPaintColorRValueTv.setText(String.valueOf(mPaintColorRValue));
        mPaintColorRValueSb.setProgress(mPaintColorRValue);

        mPaintColorGValueTv.setText(String.valueOf(mPaintColorGValue));
        mPaintColorGValueSb.setProgress(mPaintColorGValue);

        mPaintColorBValueTv.setText(String.valueOf(mPaintColorBValue));
        mPaintColorBValueSb.setProgress(mPaintColorBValue);

        mEraserWidthValueTv.setText(String.valueOf(mEraserWidthValue));
        mEraserWidthValueSb.setProgress(mEraserWidthValue);

        mEraserAlphaValueTv.setText(String.valueOf(mEraserAlphaValue));
        mEraserAlphaValueSb.setProgress(mEraserAlphaValue);

        mEraserColorRValueTv.setText(String.valueOf(mEraserColorRValue));
        mEraserColorRValueSb.setProgress(mEraserColorRValue);

        mEraserColorGValueTv.setText(String.valueOf(mEraserColorGValue));
        mEraserColorGValueSb.setProgress(mEraserColorGValue);

        mEraserColorBValueTv.setText(String.valueOf(mEraserColorBValue));
        mEraserColorBValueSb.setProgress(mEraserColorBValue);

        mPaintColorIv.setBackgroundColor(paintColor);
        mEraserColorIv.setBackgroundColor(eraserColor);
    }

    /**
     * 初始化各控件
     */
    private void initUI() {
        mSeekBarChangeListener = new SettingSeekBarChangeListener();
        mPaintWidthValueTv = (TextView) findViewById(R.id.tv_paint_width_value);
        mPaintAlphaValueTv = (TextView) findViewById(R.id.tv_paint_alpha_value);
        mPaintColorRValueTv = (TextView) findViewById(R.id.tv_paint_color_R_value);
        mPaintColorGValueTv = (TextView) findViewById(R.id.tv_paint_color_G_value);
        mPaintColorBValueTv = (TextView) findViewById(R.id.tv_paint_color_B_value);
        mPaintColorIv = (ImageView) findViewById(R.id.iv_paint);

        mPaintWidthValueSb = (SeekBar) findViewById(R.id.sb_paint_width_value);
        mPaintAlphaValueSb = (SeekBar) findViewById(R.id.sb_paint_alpha_value);
        mPaintColorRValueSb = (SeekBar) findViewById(R.id.sb_paint_color_R);
        mPaintColorGValueSb = (SeekBar) findViewById(R.id.sb_paint_color_G);
        mPaintColorBValueSb = (SeekBar) findViewById(R.id.sb_paint_color_B);

        mPaintWidthValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mPaintAlphaValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mPaintColorRValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mPaintColorGValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mPaintColorBValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);

        mEraserWidthValueTv = (TextView) findViewById(R.id.tv_eraser_width_value);
        mEraserAlphaValueTv = (TextView) findViewById(R.id.tv_eraser_alpha_value);
        mEraserColorRValueTv = (TextView) findViewById(R.id.tv_eraser_color_R_value);
        mEraserColorGValueTv = (TextView) findViewById(R.id.tv_eraser_color_G_value);
        mEraserColorBValueTv = (TextView) findViewById(R.id.tv_eraser_color_B_value);
        mEraserColorIv = (ImageView) findViewById(R.id.iv_eraser);

        mEraserWidthValueSb = (SeekBar) findViewById(R.id.sb_eraser_width_value);
        mEraserAlphaValueSb = (SeekBar) findViewById(R.id.sb_eraser_alpha_value);
        mEraserColorRValueSb = (SeekBar) findViewById(R.id.sb_eraser_color_R);
        mEraserColorGValueSb = (SeekBar) findViewById(R.id.sb_eraser_color_G);
        mEraserColorBValueSb = (SeekBar) findViewById(R.id.sb_eraser_color_B);

        mEraserWidthValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mEraserAlphaValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mEraserColorRValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mEraserColorGValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);
        mEraserColorBValueSb.setOnSeekBarChangeListener(mSeekBarChangeListener);

        mSaveBtn = (Button) findViewById(R.id.btn_setting_save);
        mRestoreBtn = (Button) findViewById(R.id.btn_setting_restore);
        mSaveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                Toast.makeText(getContext(), R.string.setting_save_success, Toast.LENGTH_SHORT).show();
            }
        });
        mRestoreBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                restore();
            }
        });
    }

    /**
     * 还原成默认值
     */
    private void restore() {
        mPaintWidthValue = MoPiFunctionImageView.DEFAULT_PAINT_WIDTH;
        mPaintAlphaValue = MoPiFunctionImageView.DEFAULT_PAINT_ALPHA;
        int paintColor = MoPiFunctionImageView.DEFAULT_PAINT_COLOR;
        mPaintColorRValue = Color.red(paintColor);
        mPaintColorGValue = Color.green(paintColor);
        mPaintColorBValue = Color.blue(paintColor);

        mEraserWidthValue = MoPiFunctionImageView.DEFAULT_ERASER_WIDTH;
        mEraserAlphaValue = MoPiFunctionImageView.DEFAULT_ERASER_ALPHA;
        int eraserColor = MoPiFunctionImageView.DEFAULT_ERASER_COLOR;
        mEraserColorRValue = Color.red(eraserColor);
        mEraserColorGValue = Color.green(eraserColor);
        mEraserColorBValue = Color.blue(eraserColor);
        mPaintWidthValueTv.setText(String.valueOf(mPaintWidthValue));
        mPaintWidthValueSb.setProgress(mPaintWidthValue);

        mPaintAlphaValueTv.setText(String.valueOf(mPaintAlphaValue));
        mPaintAlphaValueSb.setProgress(mPaintAlphaValue);

        mPaintColorRValueTv.setText(String.valueOf(mPaintColorRValue));
        mPaintColorRValueSb.setProgress(mPaintColorRValue);

        mPaintColorGValueTv.setText(String.valueOf(mPaintColorGValue));
        mPaintColorGValueSb.setProgress(mPaintColorGValue);

        mPaintColorBValueTv.setText(String.valueOf(mPaintColorBValue));
        mPaintColorBValueSb.setProgress(mPaintColorBValue);

        mEraserWidthValueTv.setText(String.valueOf(mEraserWidthValue));
        mEraserWidthValueSb.setProgress(mEraserWidthValue);

        mEraserAlphaValueTv.setText(String.valueOf(mEraserAlphaValue));
        mEraserAlphaValueSb.setProgress(mEraserAlphaValue);

        mEraserColorRValueTv.setText(String.valueOf(mEraserColorRValue));
        mEraserColorRValueSb.setProgress(mEraserColorRValue);

        mEraserColorGValueTv.setText(String.valueOf(mEraserColorGValue));
        mEraserColorGValueSb.setProgress(mEraserColorGValue);

        mEraserColorBValueTv.setText(String.valueOf(mEraserColorBValue));
        mEraserColorBValueSb.setProgress(mEraserColorBValue);

        mPaintColorIv.setBackgroundColor(paintColor);
        mEraserColorIv.setBackgroundColor(eraserColor);
    }

    /**
     * 保存各个值
     */
    public void save() {
        SharedPreferences sp = getContext().getSharedPreferences(SETTING_VALUE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PAINT_WIDTH_KEY, mPaintWidthValue);
        editor.putInt(PAINT_ALPHA_KEY, mPaintAlphaValue);
        editor.putInt(PAINT_COLOR_KEY, Color.rgb(mPaintColorRValue, mPaintColorGValue, mPaintColorBValue));

        editor.putInt(ERASER_WIDTH_KEY, mEraserWidthValue);
        editor.putInt(ERASER_ALPHA_KEY, mEraserAlphaValue);
        editor.putInt(ERASER_COLOR_KEY, Color.rgb(mEraserColorRValue, mEraserColorGValue, mEraserColorBValue));
        editor.commit();
    }

    /**
     * 拖动条的监听事件
     */
    class SettingSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()){
                case R.id.sb_paint_width_value:
                    mPaintWidthValue = progress;
                    mPaintWidthValueTv.setText(String.valueOf(progress));
                    break;
                case R.id.sb_paint_alpha_value:
                    mPaintAlphaValue = progress;
                    mPaintAlphaValueTv.setText(String.valueOf(progress));
                    break;
                case R.id.sb_paint_color_R:
                    mPaintColorRValue = progress;
                    mPaintColorRValueTv.setText(String.valueOf(progress));
                    mPaintColorIv.setBackgroundColor(Color.rgb(mPaintColorRValue,
                            mPaintColorGValue, mPaintColorBValue));
                    break;
                case R.id.sb_paint_color_G:
                    mPaintColorGValue = progress;
                    mPaintColorGValueTv.setText(String.valueOf(progress));
                    mPaintColorIv.setBackgroundColor(Color.rgb(mPaintColorRValue,
                            mPaintColorGValue, mPaintColorBValue));
                    break;
                case R.id.sb_paint_color_B:
                    mPaintColorBValue = progress;
                    mPaintColorBValueTv.setText(String.valueOf(progress));
                    mPaintColorIv.setBackgroundColor(Color.rgb(mPaintColorRValue,
                            mPaintColorGValue, mPaintColorBValue));
                    break;
                case R.id.sb_eraser_width_value:
                    mEraserWidthValue = progress;
                    mEraserWidthValueTv.setText(String.valueOf(progress));
                    break;
                case R.id.sb_eraser_alpha_value:
                    mEraserAlphaValue = progress;
                    mEraserAlphaValueTv.setText(String.valueOf(progress));
                    break;
                case R.id.sb_eraser_color_R:
                    mEraserColorRValue = progress;
                    mEraserColorRValueTv.setText(String.valueOf(progress));
                    mEraserColorIv.setBackgroundColor(Color.rgb(mEraserColorRValue,
                            mEraserColorGValue, mEraserColorBValue));
                    break;
                case R.id.sb_eraser_color_G:
                    mEraserColorGValue = progress;
                    mEraserColorGValueTv.setText(String.valueOf(progress));
                    mEraserColorIv.setBackgroundColor(Color.rgb(mEraserColorRValue,
                            mEraserColorGValue, mEraserColorBValue));
                    break;
                case R.id.sb_eraser_color_B:
                    mEraserColorBValue = progress;
                    mEraserColorBValueTv.setText(String.valueOf(progress));
                    mEraserColorIv.setBackgroundColor(Color.rgb(mEraserColorRValue,
                            mEraserColorGValue, mEraserColorBValue));
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
