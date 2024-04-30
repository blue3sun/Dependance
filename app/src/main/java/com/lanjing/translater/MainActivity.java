package com.lanjing.translater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PermissionInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.lanjing.translater.Utils.FileUtils;
import com.lanjing.translater.Utils.Md5Utils;
import com.lanjing.translater.Utils.RetrofitManager;
import com.lanjing.translater.Utils.RetrofitService;
import com.lanjing.translater.bean.Constant;
import com.lanjing.translater.bean.TransResult;
import com.lanjing.translater.bean.Translate;
import com.lanjing.translater.bean.TranslateParams;
import com.lanjing.translater.databinding.ActivityMainBinding;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Permission;
import java.util.List;
import java.util.logging.LogManager;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQ_CODE_SELECT_TRANSLATE_FILE = 1;

    private ActivityMainBinding mViewBinding;
    private StringBuilder mStringBuilder = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mViewBinding.getRoot());
        mViewBinding.btnOpenTransDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectTranslateDir();
            }
        });
        mViewBinding.btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //开始翻译
//                String query = "Couldn't get this thing to work to save my life! If  Someone gets this working, pls tell me how??? I read the directions, tried setting up, just won't work, might be defective product, haven't had time to mess with anymore, nor time to send back";
//                String query = "apple";
//                String query = "ｊｃｏｍのルーターが2階で １階が受信できず購入したが、タブレットはまあまあ つながるが、パソコンは弱く、株の取引ソフトが不安定 ｊｃｏｍのメッシュを契約するつもり 返品も開封してしまったので送料考えると<br/>意味なし<br/>お金の損失発生、残念でした 木造住宅でしたが構造によるのかな 私の場合途中２枚のドアがありましたが";
//                String query = "Ersten ein zwei monate gab es kein Problem auch die Konfiguration war schnell gemacht am Anfang. Jetzt hab ich auf einmal keine Internetverbindung sobald ich mich in den wlan des Verstärkers anmelde. Komischerweise beim original signal meinem eigt. Wlan schon";
//                String query = "I bought this device in October 2019, because a prominent tech site rated it as the best value for money among all range extenders tested. The first unit I received from Amazon proved to be defective when I first installed it (a bad sign). I sent it back and got a replacement, which worked just fine until a couple of months ago. It then began routinely dropping the extension networks. I contacted TP-Link Support via chat, and after they tortured me with a long line of questions designed to make the problem MY fault, they recommended updating the firmware. Did this about a month ago, without improvement. Contacted Support again a few days ago and provided the reference number for our earlier chat. They had no record of the chat. Fortunately I had saved a transcript, which I uploaded for them. They again asked a gazillion silly questions, which seemed interminable. I finally lost my patience and told them politely either replace this clearly defective unit under your one-year warranty or I'm discarding the device. I was promised a phone call from a supervisor by later that day - didn't happen. They continued to ask annoying questions via email, when the evidence I've supplied clearly establishes a defective product. Their so-called one-year warranty is a joke. I'll never buy a TP-Link product again.";
//                translate(query);
                showProgressbar(true);
                FileUtils.startTranslateExcelFile(MainActivity.this, mViewBinding.tvTransDirPath.getText().toString());
            }
        });
        myRequetPermission();
    }

    private void myRequetPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }else {
//            Toast.makeText(this,"您已经申请了权限!",Toast.LENGTH_SHORT).show();
        }
    }

    public void showProgressbar(boolean show){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(show){
                    mViewBinding.pb.setVisibility(View.VISIBLE);
                }else{
                    mViewBinding.pb.setVisibility(View.GONE);
                }
            }
        });

    }

    public void translate(String query) {
        RetrofitService retrofitService = RetrofitManager.getRetrofit().create(RetrofitService.class);
        TranslateParams translateParams = new TranslateParams();
        translateParams.setAppid(Constant.APP_ID);
        translateParams.setFrom("auto");
        translateParams.setTo("zh");
        translateParams.setQ(query);
        translateParams.setSalt(Constant.TRANS_SALT);
        mStringBuilder.setLength(0);
        mStringBuilder.append(Constant.APP_ID).append(query).append(Constant.TRANS_SALT).append(Constant.SECRET_KEY);
        String sign = Md5Utils.stringToMD5(mStringBuilder.toString());
        translateParams.setSign(sign);
//        Observable<Translate> observable = retrofitService.translateByPost(translateParams);
        Observable<Translate> observable = retrofitService.translateByGet(query, "auto", "zh", Constant.APP_ID, Constant.TRANS_SALT, sign);

        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Translate>() {

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        mViewBinding.tvTransResult.setText(Log.getStackTraceString(e));
                        Log.i(TAG, Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(Translate translate) {
                        if (translate == null || translate.getTrans_result() == null) {
                            mViewBinding.tvTransResult.setText("null");
                            return;
                        }
                        mViewBinding.tvTransResult.setText("");
                        List<TransResult> transList = translate.getTrans_result();
                        for (int i = 0; i < transList.size(); i++) {
                            TransResult transResult = transList.get(i);
                            String result = transResult == null ? "null" : transResult.getDst();
                            mViewBinding.tvTransResult.append(result);
                            Log.i(TAG, "Translate Result:" + result);
                        }

                    }
                });
    }

    /**
     * 对输入的字符串进行URL编码, 即转换为%20这种形式
     *
     * @param input 原文
     * @return URL编码. 如果编码失败, 则返回原文
     */
    public static String encode(String input) {
        if (input == null) {
            return "";
        }

        try {
            return URLEncoder.encode(input, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return input;
    }


    // 通过工具选择文件
    public void selectTranslateDir() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/plain");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), REQ_CODE_SELECT_TRANSLATE_FILE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
            return;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != REQ_CODE_SELECT_TRANSLATE_FILE) {
            return;
        }
        if (resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = getPath(this, uri);
            if (path == null) {
                path = uri.getPath();
            }
            File file = new File(path);
            String parentPath = file.getParentFile().getPath();
            mViewBinding.tvTransDirPath.setText(parentPath);
        } else {
            mViewBinding.tvTransDirPath.setText("null");
        }
    }

    // 获取file路径
    public String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }
}