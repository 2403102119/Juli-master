package com.tcckj.juli;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.tcckj.juli.activity.WebShow2Activity;
import com.tcckj.juli.activity.WebShowActivity;
import com.tcckj.juli.base.BaseActivity;
import com.tcckj.juli.entity.Bean;
import com.tcckj.juli.fragment.InformationFragment;
import com.tcckj.juli.fragment.MarketFragment;
import com.tcckj.juli.fragment.MineFragment;
import com.tcckj.juli.fragment.TreasureHuntFragment;
import com.tcckj.juli.thread.MApiResultCallback;
import com.tcckj.juli.thread.ThreadPoolManager;
import com.tcckj.juli.util.MUIToast;
import com.tcckj.juli.util.NetUtil;
import com.tcckj.juli.util.Tools;
import com.tcckj.juli.view.CommonProgressDialog;
import com.yanzhenjie.alertdialog.AlertDialog;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionNo;
import com.yanzhenjie.permission.PermissionYes;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import okhttp3.Call;

public class MainActivity extends BaseActivity {
    private Fragment treasureHuntFragment, marketFragment, infomationFragment, mineFragment;
    RadioGroup radioGroup;
    RadioButton rb_treasure_hunt,rb_market,rb_information,rb_mine;

    private CommonProgressDialog pBar;

    private String newVersion = "1";

    @Override
    protected void initView(Bundle savedInstanceState) {
        setContainer(R.layout.activity_main);
        setTheme(R.style.AppTheme);
        radioGroup = findView(R.id.radioGroup);

        top.setVisibility(View.GONE);

        rb_treasure_hunt = findView(R.id.rb_treasure_hunt);
        rb_market = findView(R.id.rb_market);
        rb_information = findView(R.id.rb_information);
        rb_mine = findView(R.id.rb_mine);
    }

    @Override
    protected void initListener() {

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.rb_treasure_hunt:
                        setSelect(0);
                        break;
                    case R.id.rb_market:
                        setSelect(1);
                        break;
                    case R.id.rb_information:
                        setSelect(2);
                        break;
                    case R.id.rb_mine:
                        setSelect(3);
                        break;
                }
            }
        });

    }


    private int exitFlag;

    @Override
    public void onBackPressed() {
        if (exitFlag==1){
            super.onBackPressed();
            App.finishRealAllActivity();
        }else {
            exitFlag=1;
            handler.sendEmptyMessageDelayed(0x10,2000);
            MUIToast.show(MainActivity.this,"再次按返回键退出");
        }
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            exitFlag=0;
        }
    };


    @Override
    protected void initData() {

        rb_treasure_hunt.setChecked(true);
//        setSelect(0);

        if (null != getIntent()){
            switch (getIntent().getIntExtra("stepTo", 0)){
                case 0:
                    setSelect(0);
                    rb_treasure_hunt.setChecked(true);
                    break;
                case 1:
                    setSelect(1);
                    rb_market.setChecked(true);
                    break;
                case 2:
                    setSelect(2);
                    rb_information.setChecked(true);
                    break;
                case 3:
                    setSelect(3);
                    rb_mine.setChecked(true);
                    break;
            }
        }

        getNewVersion();
    }

    public void setSelect(int i) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        hideFragment(transaction);//先隐藏所有界面
//        treasureHuntFragment = new TreasureHuntFragment(false);
        switch (i) {
            case 0:
//                if (treasureHuntFragment == null) {
                    treasureHuntFragment = new TreasureHuntFragment();
                    transaction.add(R.id.main_content, treasureHuntFragment);
//                } else {
//                    transaction.show(treasureHuntFragment);
//                    treasureHuntFragment.onResume();
//                }
                break;
            case 1:
                if (marketFragment == null) {
                    marketFragment = new MarketFragment();
                    transaction.add(R.id.main_content, marketFragment);
                } else {
                    transaction.show(marketFragment);
                }
                break;
            case 2:
                if (infomationFragment == null) {
                    infomationFragment = new InformationFragment();
                    transaction.add(R.id.main_content, infomationFragment);
                } else {
                    transaction.show(infomationFragment);
                }
                break;
            case 3:
                if (mineFragment == null) {
                    mineFragment = new MineFragment();
                    transaction.add(R.id.main_content, mineFragment);
                } else {
                    transaction.show(mineFragment);
                }
                break;
        }
        transaction.commit();
    }

    private void hideFragment(FragmentTransaction transaction){
        if (treasureHuntFragment != null) {
            transaction.remove(treasureHuntFragment);
        }
        if (marketFragment != null) {
            transaction.hide(marketFragment);
        }
        if (infomationFragment != null) {
            transaction.hide(infomationFragment);
        }
        if (mineFragment != null) {
            transaction.hide(mineFragment);
        }
    }

    //用于隐藏界面
   /* private void hideFragment(FragmentTransaction transaction) {
        if (treasureHuntFragment != null) {
            transaction.hide(treasureHuntFragment);
            treasureHuntFragment.onPause();
        }
        if (marketFragment != null) {
            transaction.hide(marketFragment);
        }
        if (infomationFragment != null) {
            transaction.hide(infomationFragment);
        }
        if (mineFragment != null) {
            transaction.hide(mineFragment);
        }
    }*/


   /*
   获取最新版本号
    */
   private void getNewVersion(){
       if (NetUtil.isNetWorking(MainActivity.this)){
           ThreadPoolManager.getInstance().getNetThreadPool().execute(new Runnable() {
               @Override
               public void run() {
                   httpInterface.updateTypeData("1", new MApiResultCallback() {
                       @Override
                       public void onSuccess(String result) {
                           Log.e("获取.成功", result);

                           Bean.VersionCode data = new Gson().fromJson(result, Bean.VersionCode.class);
                           if (1 == data.status){
                               newVersion = data.type;


                               // 获取本版本号，是否更新
                               int vision = Tools.getVersion(MainActivity.this);
                               getVersion(vision);
                           }
                       }

                       @Override
                       public void onFail(String response) {

                       }

                       @Override
                       public void onError(Call call, Exception exception) {

                       }

                       @Override
                       public void onTokenError(String response) {

                       }
                   });
               }
           });
       }
   }


    // 下载存储的文件名
    private static final String DOWNLOAD_NAME = "channelWe";

    // 获取更新版本号
    private void getVersion(final int vision) {
//         {"data":{"content":"其他bug修复。","id":"2","api_key":"android",
//         // "version":"2.1"},"msg":"获取成功","status":1}
        String data = "";
        //网络请求获取当前版本号和下载链接
        //实际操作是从服务器获取
        //demo写死了

        String newversion = newVersion;//更新新的版本号

        Log.i("11111111", "getVersion: " + newversion);
        Log.i("11111111", "getVersion: " + getApplicationContext().getPackageName() + ".provider");

        String content = "确定去更新吗？";
       /* String content = "\n" +
                "就不告诉你我们更新了什么-。-\n" +
                "\n" +
                "----------万能的分割线-----------\n" +
                "\n" +
                "(ㄒoㄒ) 被老板打了一顿，还是来告诉你吧：\n" +

                "1.下架商品误买了？恩。。。我搞了点小动作就不会出现了\n" +
                "2.侧边栏、弹框优化 —— 这个你自己去探索吧，总得留点悬念嘛-。-\n";//更新内容*/
        String url = "http://openbox.mobilem.360.cn/index/d/sid/3429345";//安装包下载地址

        double newversioncode = Double
                .parseDouble(newversion);
        int cc = (int) (newversioncode);

        System.out.println(newversion + "v" + vision + ",,"
                + cc);
        if (cc != vision) {
            if (vision < cc) {
                System.out.println(newversion + "v"
                        + vision);
                // 版本号不同
                ShowDialog(vision, newversion, content, url);


            }
        }
    }

    /**
     * 升级系统
     *
     * @param content
     * @param url
     *
     */
    private void ShowDialog(int vision, String newversion, String content,
                            final String url) {

        new android.app.AlertDialog.Builder(this)
                .setTitle("版本更新")
                .setMessage(content)
                .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        Intent intent = new Intent();
//Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                        intent.setAction("android.intent.action.VIEW");
                        Uri content_url = Uri.parse("https://fir.im/ugm7");
                        intent.setData(content_url);
                        startActivity(intent);

                        /*Intent intent = new Intent(MainActivity.this, WebShow2Activity.class);
                        intent.putExtra("url", "https://fir.im/ugm7");
                        startActivity(intent);*/

                        /*pBar = new CommonProgressDialog(MainActivity.this);
                        pBar.setCanceledOnTouchOutside(false);
                        pBar.setTitle("正在下载");
                        pBar.setCustomTitle(LayoutInflater.from(
                                MainActivity.this).inflate(
                                R.layout.title_dialog, null));
                        pBar.setMessage("正在下载");
                        pBar.setIndeterminate(true);
                        pBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pBar.setCancelable(true);
                        // downFile(URLData.DOWNLOAD_URL);
                        final DownloadTask downloadTask = new DownloadTask(
                                MainActivity.this);
                        downloadTask.execute(url);
                        pBar.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                downloadTask.cancel(true);
                            }
                        });*/
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }


    /**
     * 下载应用
     *
     * @author Administrator
     */
    class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            File file = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();
                // expect HTTP 200 OK, so we don't mistakenly save error
                // report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP "
                            + connection.getResponseCode() + " "
                            + connection.getResponseMessage();
                }
                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    file = new File(Environment.getExternalStorageDirectory(),
                            DOWNLOAD_NAME);

                    if (!file.exists()) {
                        // 判断父文件夹是否存在
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                    }

                } else {
                    Toast.makeText(MainActivity.this, "sd卡未挂载",
                            Toast.LENGTH_LONG).show();
                }
                input = connection.getInputStream();
                output = new FileOutputStream(file);
                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);

                }
            } catch (Exception e) {
                System.out.println(e.toString());
                return e.toString();

            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }
                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context
                    .getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            pBar.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            pBar.setIndeterminate(false);
            pBar.setMax(100);
            pBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            pBar.dismiss();
            if (result != null) {

//                // 申请多个权限。大神的界面
//                AndPermission.with(MainActivity.this)
//                        .requestCode(REQUEST_CODE_PERMISSION_OTHER)
//                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
//                        // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
//                        .rationale(new RationaleListener() {
//                                       @Override
//                                       public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
//                                           // 这里的对话框可以自定义，只要调用rationale.resume()就可以继续申请。
//                                           AndPermission.rationaleDialog(MainActivity.this, rationale).show();
//                                       }
//                                   }
//                        )
//                        .send();
                // 申请多个权限。
                AndPermission.with(MainActivity.this)
                        .requestCode(REQUEST_CODE_PERMISSION_SD)
                        .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        // rationale作用是：用户拒绝一次权限，再次申请时先征求用户同意，再打开授权对话框，避免用户勾选不再提示。
                        .rationale(rationaleListener
                        )
                        .send();


                Toast.makeText(context, "您未打开SD卡权限" + result, Toast.LENGTH_LONG).show();
            } else {
                // Toast.makeText(context, "File downloaded",
                // Toast.LENGTH_SHORT)
                // .show();
                update();
            }

        }
    }

    private static final int REQUEST_CODE_PERMISSION_SD = 101;

    private static final int REQUEST_CODE_SETTING = 300;
    private RationaleListener rationaleListener = new RationaleListener() {
        @Override
        public void showRequestPermissionRationale(int requestCode, final Rationale rationale) {
            // 这里使用自定义对话框，如果不想自定义，用AndPermission默认对话框：
            // AndPermission.rationaleDialog(Context, Rationale).show();

            // 自定义对话框。
            AlertDialog.build(MainActivity.this)
                    .setTitle(R.string.title_dialog)
                    .setMessage(R.string.message_permission_rationale)
                    .setPositiveButton(R.string.btn_dialog_yes_permission, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.resume();
                        }
                    })

                    .setNegativeButton(R.string.btn_dialog_no_permission, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            rationale.cancel();
                        }
                    })
                    .show();
        }
    };
    //----------------------------------SD权限----------------------------------//


    @PermissionYes(REQUEST_CODE_PERMISSION_SD)
    private void getMultiYes(List<String> grantedPermissions) {
        Toast.makeText(this, R.string.message_post_succeed, Toast.LENGTH_SHORT).show();
    }

    @PermissionNo(REQUEST_CODE_PERMISSION_SD)
    private void getMultiNo(List<String> deniedPermissions) {
        Toast.makeText(this, R.string.message_post_failed, Toast.LENGTH_SHORT).show();

        // 用户否勾选了不再提示并且拒绝了权限，那么提示用户到设置中授权。
        if (AndPermission.hasAlwaysDeniedPermission(this, deniedPermissions)) {
            AndPermission.defaultSettingDialog(this, REQUEST_CODE_SETTING)
                    .setTitle(R.string.title_dialog)
                    .setMessage(R.string.message_permission_failed)
                    .setPositiveButton(R.string.btn_dialog_yes_permission)
                    .setNegativeButton(R.string.btn_dialog_no_permission, null)
                    .show();

            // 更多自定dialog，请看上面。
        }
    }

    //----------------------------------权限回调处理----------------------------------//

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        /**
         * 转给AndPermission分析结果。
         *
         * @param object     要接受结果的Activity、Fragment。
         * @param requestCode  请求码。
         * @param permissions  权限数组，一个或者多个。
         * @param grantResults 请求结果。
         */
        AndPermission.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        switch (requestCode) {
            case REQUEST_CODE_SETTING: {
                Toast.makeText(this, R.string.message_setting_back, Toast.LENGTH_LONG).show();
                //设置成功，再次请求更新
                getVersion(Tools.getVersion(MainActivity.this));
                break;
            }
        }
    }


   /* private void update() {
        //安装应用
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(Environment
                        .getExternalStorageDirectory(), DOWNLOAD_NAME)),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }*/

    private void update() {
        //安装应用
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", new File(Environment
                        .getExternalStorageDirectory(), DOWNLOAD_NAME)),
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }

}
