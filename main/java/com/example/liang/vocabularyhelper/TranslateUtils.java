package com.example.liang.vocabularyhelper;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class TranslateUtils {
    /*http://api.fanyi.baidu.com/
    APP ID：20170407000044348
    密钥：x9gxoX9VbrGvqZj8G51H

    demo:http://api.fanyi.baidu.com/api/trans/vip/translate?q=apple&from=en&to=zh&appid=2015063000000001&salt=1435660288&sign=f89f9594663708c1605f3d736d01d2d4
     */
    //private Context mContext;
    private TransApi transApi;
    private Thread networkThread;
    TranslateUtils(/*Context context*/){
        String appid="20170407000044348";
        String securityKey="x9gxoX9VbrGvqZj8G51H";
        //mContext=context;
        transApi=new TransApi(appid,securityKey);
    }
    public void translate(final String source, final EditText etTarget, final TextView tvStatus){
        if(source.equals("")) {
            etTarget.setText("");
            tvStatus.setVisibility(View.INVISIBLE);
            return;
        }
        /*String translation;
        while ((translation=getTranslation(source))==null)
            continue;*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                //tvStatus.setVisibility(View.VISIBLE);
                try {
                    etTarget.setText(getTranslation(source));
                    etTarget.selectAll();
                    tvStatus.setVisibility(View.INVISIBLE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
        /*if(networkThread!=null){
            networkThread.interrupt();
            Log.d("TranslateDebug Thread", "interrupted");
        }
        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {

            }
        });*/
//        networkThread.start();
    }

    private String getTranslation(String source){
        final String[] result = new String[1];
        try {
            result[0] =transApi.getTransResult(source,"en","zh");
            JSONObject jsonObject=new JSONObject(result[0]);
            return jsonObject.getJSONArray("trans_result").getJSONObject(0).getString("dst");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        //Log.d("TranslateDebug Result",result[0]);
        //return "";
    }

    public class TransApi {
        private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";

        private String appid;
        private String securityKey;

        public TransApi(String appid, String securityKey) {
            this.appid = appid;
            this.securityKey = securityKey;
        }

        public String getTransResult(String query, String from, String to) {
            Map<String, String> params = null;
            params = buildParams(query, from, to);
            return HttpGet.get(TRANS_API_HOST, params);
        }

        private Map<String, String> buildParams(String query, String from, String to) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("q", query);
            params.put("from", from);
            params.put("to", to);

            params.put("appid", appid);

            // 随机数
            String salt = String.valueOf(System.currentTimeMillis());
            params.put("salt", salt);

            // 签名
            String src = appid + query + salt + securityKey; // 加密前的原文
            try {
                params.put("sign", Hash.md5(src).toLowerCase());
                Log.d("TranslateDebug Sign", Hash.md5(src).toLowerCase());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            Log.d("TranslateDebug Param", params.toString());
            return params;
        }

    }


    public static class Hash {

        public static String md5(String input) throws NoSuchAlgorithmException {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(input.getBytes());
            return printHexBinary(bytes);
        }

        public static String printHexBinary(byte[] data) {
            StringBuilder r = new StringBuilder(data.length * 2);
            for (byte b : data) {
                r.append(String.format("%02X", b & 0xFF));
            }
            return r.toString();
        }
    }

}
