package com.example.yu.mybutterknife;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.module_annotation.BindView;
import com.example.module_annotation.OnClick;
import com.example.module_api.ViewInjectHelper;

public class BActivity extends AppCompatActivity {

    public static void startA(Context context) {
        context.startActivity(new Intent(context, BActivity.class));
    }

    @BindView(R.id.textView)
    TextView tv1;
    @BindView(R.id.textView2)
    TextView tv2;
    @BindView(R.id.button)
    Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_b);
        ViewInjectHelper.inject(this);
        tv1.setText("文本1啦");
        tv2.setText("文本2啦");
    }

    @OnClick(R.id.button)
    public void customClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                Toast.makeText(this, "我是button", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
