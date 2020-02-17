package com.example.artapp.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.artapp.Constants;
import com.example.artapp.MyMD5;
import com.example.artapp.MyUtils;
import com.example.artapp.R;
import com.example.artapp.bean.BeanData;
import com.google.gson.Gson;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.modelmsg.ShowMessageFromWX;
import com.tencent.mm.opensdk.modelmsg.WXAppExtendObject;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler, View.OnClickListener {


    private Button btn_pay;

    //获取订单
    private String get_pay = "https://api.mch.weixin.qq.com/pay/unifiedorder";

    private IWXAPI api;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_wxpay);
        btn_pay = findViewById(R.id.btn_pay);
        //初始化调起微信支付页面的工厂类
        api = WXAPIFactory.createWXAPI(this, null);
        api.registerApp(Constants.appid);
        api.handleIntent(getIntent(),this);
        btn_pay.setOnClickListener(this);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
        api.handleIntent(intent,this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_pay:
                getPayRay();
                break;
        }
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BeanData beanData = new Gson().fromJson(msg.obj.toString(),BeanData.class);
            //wxpay(beanData);
            localWxPay(beanData);
        }
    };

    //从服务器获取微信支付的订单数据
    private void getPayRay(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(Constants.pay_url)
                .get()
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("onFailure","err");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Message msg = new Message();
                msg.what = 0;
                msg.obj = result;
                handler.sendMessage(msg);
            }
        });
    };

    private void wxpay(BeanData beanData){
        PayReq req = new PayReq();
        req.appId			= beanData.getAppid();
        req.partnerId		= beanData.getMch_id();
        req.prepayId		= beanData.getPrepay_id();
        req.packageValue    = beanData.getPackageX();
        req.nonceStr		= beanData.getNonce_str();
        req.timeStamp		= String.valueOf(beanData.getTime());
        req.sign = beanData.getSign();
        Log.i("wxpay",String.valueOf(req.checkArgs()));
        api.sendReq(req);
    }


    private void localWxPay(BeanData beanData){
        PayReq req = new PayReq();
        req.appId			= "wx0dc7c1fa36761c64";
        req.partnerId		= "1299369201";
        req.prepayId		= beanData.getPrepay_id();
        req.packageValue    = "Sign=WXPay";
        //生产调用微信支付页面的签名
        Map<String,String> map = new HashMap<>();
        map.put("appid","wx0dc7c1fa36761c64");
        map.put("partnerid","1299369201");
        map.put("prepayid",beanData.getPrepay_id());
        map.put("package","Sign=WXPay");
        map.put("timestamp",String.valueOf((int)(System.currentTimeMillis()/1000)));
        map.put("noncestr", MyUtils.getRandomString(16));
        String sign = getSign(map);
        req.nonceStr = map.get("noncestr");
        req.timeStamp = map.get("timestamp");
        req.sign = sign;
        Log.i("wxpay",String.valueOf(req.checkArgs()));
        api.sendReq(req);
    }


    private String getSign(Map<String,String> map){
        List<Map.Entry<String, String>> infoIds = new ArrayList<Map.Entry<String, String>>(map.entrySet());
        Collections.sort(infoIds, new Comparator<Map.Entry<String, String>>() {

            public int compare(Map.Entry<String, String> o1, Map.Entry<String, String> o2) {
                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });  // 对所有传入参数按照字段名的 ASCII 码从小到大排序（字典序）


        // 构造签名键值对的格式
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> item : infoIds) {
            if (item.getKey() != null || item.getKey() != "") {
                String key = item.getKey();
                String val = item.getValue();
                if (!(val == "" || val == null)) {
                    sb.append(key + "=" + val + "&");
                }
            }
        }
        sb.append("key=");
        sb.append("b2c7da626d2646e5b921830c7e3e7beb");
        return MyMD5.MD5Encode(sb.toString()).toUpperCase();
    }


    @Override
    public void onReq(BaseReq req) {
        Toast.makeText(this, "openid = " + req.openId, Toast.LENGTH_SHORT).show();

        switch (req.getType()) {
            case ConstantsAPI.COMMAND_GETMESSAGE_FROM_WX:

                break;
            case ConstantsAPI.COMMAND_SHOWMESSAGE_FROM_WX:
                goToShowMsg((ShowMessageFromWX.Req) req);
                break;
            case ConstantsAPI.COMMAND_LAUNCH_BY_WX:
                Toast.makeText(this, "by_wx", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onResp(BaseResp resp) {
        Toast.makeText(this, "openid = " + resp.openId, Toast.LENGTH_SHORT).show();

        if (resp.getType() == ConstantsAPI.COMMAND_SENDAUTH) {
            Toast.makeText(this, "code = " + ((SendAuth.Resp) resp).code, Toast.LENGTH_SHORT).show();
        }

        int result = 0;

        switch (resp.errCode) {
            case BaseResp.ErrCode.ERR_OK:
                result = R.string.errcode_success;
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL:
                result = R.string.errcode_cancel;
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED:
                result = R.string.errcode_deny;
                break;
            default:
                result = R.string.errcode_unknown;
                break;
        }

        Toast.makeText(this, result, Toast.LENGTH_LONG).show();
    }

    private void goToShowMsg(ShowMessageFromWX.Req showReq) {
        WXMediaMessage wxMsg = showReq.message;
        WXAppExtendObject obj = (WXAppExtendObject) wxMsg.mediaObject;

        StringBuffer msg = new StringBuffer(); // ��֯һ������ʾ����Ϣ����
        msg.append("description: ");
        msg.append(wxMsg.description);
        msg.append("\n");
        msg.append("extInfo: ");
        msg.append(obj.extInfo);
        msg.append("\n");
        msg.append("filePath: ");
        msg.append(obj.filePath);
    }
}
