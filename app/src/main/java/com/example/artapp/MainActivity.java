package com.example.artapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.support.constraint.Constraints;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import com.example.artapp.wxapi.WXPayEntryActivity;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXTextObject;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Button btn_wxpay;
    private Button btn_wxshare;

    private MyHandler myHandler;

    PopupWindow popupWindow;
    IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_wxpay = findViewById(R.id.btn_wxpay);
        btn_wxshare = findViewById(R.id.btn_wxshare);
        btn_wxpay.setOnClickListener(this);
        btn_wxshare.setOnClickListener(this);
        //初始化handler
        myHandler = new MyHandler(this);
        initData();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_wxpay:

                Intent intent = new Intent(this, WXPayEntryActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_wxshare:
                showSharedWindow();
                break;
            case R.id.btn_1:

                WXWebpageObject textObj = new WXWebpageObject();
                textObj.webpageUrl = "http://www.baidu.com";
                WXMediaMessage msg = new WXMediaMessage();
                msg.mediaObject = textObj;
                msg.description = "分享描述";
                msg.title = "分享标题";
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
                msg.setThumbImage(bitmap);
                SendMessageToWX.Req req = new SendMessageToWX.Req();
                req.message = msg;
                req.scene = SendMessageToWX.Req.WXSceneTimeline;
                api.sendReq(req);
                break;
            case R.id.btn_2:
                WXWebpageObject textObj1 = new WXWebpageObject();
                textObj1.webpageUrl = "http://www.baidu.com";
                WXMediaMessage msg1 = new WXMediaMessage();
                msg1.title = "分享标题";
                msg1.mediaObject = textObj1;
                msg1.description = "分享描述";
                Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);
                msg1.setThumbImage(bitmap1); //设置分享图标
                SendMessageToWX.Req req1 = new SendMessageToWX.Req();
                req1.message = msg1;
                req1.scene = SendMessageToWX.Req.WXSceneSession;
                api.sendReq(req1);
                break;
        }
    }

    private void initData(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = new Message();
                msg.what = 1;
                myHandler.sendMessage(msg);
            }
        }).start();

        api = WXAPIFactory.createWXAPI(this, null);
        api.registerApp(Constants.appid);
    }

    public void updateUI(){

    }


    private void showSharedWindow(){
        if(popupWindow == null){
            View view = LayoutInflater.from(this).inflate(R.layout.layout_share,null);
            popupWindow = new PopupWindow(view, Constraints.LayoutParams.MATCH_PARENT, Constraints.LayoutParams.WRAP_CONTENT,false);
            popupWindow.setFocusable(true);
            popupWindow.getContentView().measure(0,0);
            popupWindow.showAtLocation(btn_wxshare, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL,0,0);
            Button btn_1 = view.findViewById(R.id.btn_1);
            Button btn_2 = view.findViewById(R.id.btn_2);
            btn_1.setOnClickListener(this);
            btn_2.setOnClickListener(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(popupWindow != null && popupWindow.isShowing()){
            popupWindow.dismiss();
            popupWindow = null;
        }
    }

    /**
     * 弱引用化的handler
     */
    static class MyHandler extends Handler{

        private WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity mainActivity){
            weakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    weakReference.get().updateUI();
                    break;
            }
        }
    }

}
