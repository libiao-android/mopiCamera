package com.meitu.mopicamera.activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.meitu.mopicamera.R;
/**
 * 从相册选择图片的页面
 * Created by libiao on 2017/6/18 0018.
 */
public class MainActivity extends AppCompatActivity {

    private static final int IMAGE = 1;
    private static final String PHOTO_URI = "photoUri";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN ,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Button button = (Button) findViewById(R.id.btn_select_photo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //打开系统相册
                Intent intent = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == Activity.RESULT_OK && data != null){
            switch (requestCode){
                case IMAGE:
                    String photoUri = data.getData().toString();
                    Intent intent = new Intent(this, MoPiActivity.class);
                    intent.putExtra(PHOTO_URI, photoUri);
                    startActivity(intent);
                    break;
                default:
                    break;
            }
        }
    }
}
