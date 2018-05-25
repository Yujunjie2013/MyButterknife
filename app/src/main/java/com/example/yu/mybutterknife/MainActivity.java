package com.example.yu.mybutterknife;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.example.module_annotation.BindLayout;
import com.example.module_annotation.BindView;
import com.example.module_annotation.OnClick;
import com.example.module_api.ViewInject;
import com.example.module_api.ViewInjectHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

@BindLayout(R.layout.activity_main)
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewInjectHelper.inject(this);
        if (tv != null) {
            tv.setText("哈哈、我是注解");
        }
    }

    @OnClick({R.id.tv})
    public void myClick(View view) {
        switch (view.getId()) {
            case R.id.tv:
                Toast.makeText(this, "通过注解实现点击", Toast.LENGTH_SHORT).show();
                BActivity.startA(this);
                break;
        }
    }
}
