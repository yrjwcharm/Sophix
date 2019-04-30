package com.example.hurong_pc111.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.taobao.sophix.SophixManager;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 0;
    private final String TAG = this.getClass().getSimpleName();
    private Button btnTest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnTest=findViewById(R.id.btTest);
        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                test();
            }
        });
        initPermissions();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (SophixStubApplication.isRelaunch) {
            Log.i(TAG, "如果是冷启动，则杀死App进程，从而加载补丁:");
            SophixStubApplication.isRelaunch = false;
            SophixManager.getInstance().killProcessSafely();
        }

    }

    /**
     * 配置Android 6.0 以上额外的权限
     */
    private void initPermissions() {
        //配置微信登录和6.0权限
        if (Build.VERSION.SDK_INT >= 23) {
            String[] mPermissionList = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,//读取储存权限
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,//写入储存权限
            };
            if (checkPermissionAllGranted(mPermissionList)) {
                /*查询是否有新补丁需要载入*/
                SophixManager.getInstance().queryAndLoadNewPatch();
            } else {

                ActivityCompat.requestPermissions(this, mPermissionList, REQUEST_EXTERNAL_STORAGE_PERMISSION);
            }
        } else {
            /*查询是否有新补丁需要载入*/
            SophixManager.getInstance().queryAndLoadNewPatch();
        }

    }

    private void test() {
        String versionName = null;
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        Toast.makeText(getBaseContext(), "当前版本" + versionName, Toast.LENGTH_SHORT).show();
    }

    /**
     * 检查是否拥有指定的所有权限
     */
    private boolean checkPermissionAllGranted(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // 只要有一个权限没有被授予, 则直接返回 false
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "成功获得权限");
                     /*查询是否有新补丁需要载入*/
                    SophixManager.getInstance().queryAndLoadNewPatch();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage("未获得权限，无法获得补丁升级功能")
                            .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    intent.setData(Uri.parse("package:" + getPackageName()));
                                    startActivity(intent);
                                }
                            }).setNegativeButton("取消", null).show();
                }
            default:
                break;
        }
    }
}
