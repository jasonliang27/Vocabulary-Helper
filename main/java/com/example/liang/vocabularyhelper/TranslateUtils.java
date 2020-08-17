package com.example.liang.vocabularyhelper;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

class TranslateUtils {
    /*http://api.fanyi.baidu.com/
    APP ID：20170407000044348
    密钥：x9gxoX9VbrGvqZj8G51H

    demo:http://api.fanyi.baidu.com/api/trans/vip/translate?q=apple&from=en&to=zh&appid=2015063000000001&salt=1435660288&sign=f89f9594663708c1605f3d736d01d2d4
     */
    //private Context mContext;
    private TransApi transApi;
    private Thread networkThread;
    private final int COMPLETED = 0;
    TranslateUtils(/*Context context*/){
        String appid="20170407000044348";
        String securityKey="x9gxoX9VbrGvqZj8G51H";
        transApi=new TransApi(appid,securityKey);
    }

    void translate(final String source, final Handler handler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //tvStatus.setVisibility(View.VISIBLE);
                try {
                    Message message=Message.obtain();
                    Bundle bundle=new Bundle();
                    if(source.equals(""))
                        bundle.putString("result","");
                    else
                        bundle.putString("result",getTranslation(source));//TODO null处理
                    message.setData(bundle);
                    message.what=COMPLETED;
                    handler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
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

        TransApi(String appid, String securityKey) {
            this.appid = appid;
            this.securityKey = securityKey;
        }

        String getTransResult(String query, String from, String to) {
            Map<String, String> params;
            params = buildParams(query, from, to);
            return HttpGet.get(TRANS_API_HOST, params);
        }

        private Map<String, String> buildParams(String query, String from, String to) {
            Map<String, String> params = new HashMap<>();
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
                //Log.d("TranslateDebug Sign", Hash.md5(src).toLowerCase());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
            //Log.d("TranslateDebug Param", params.toString());
            return params;
        }

    }


    public static class Hash {

        static String md5(String input) throws NoSuchAlgorithmException {
            byte[] bytes = MessageDigest.getInstance("MD5").digest(input.getBytes());
            return printHexBinary(bytes);
        }

        static String printHexBinary(byte[] data) {
            StringBuilder r = new StringBuilder(data.length * 2);
            for (byte b : data) {
                r.append(String.format("%02X", b & 0xFF));
            }
            return r.toString();
        }
    }

}
