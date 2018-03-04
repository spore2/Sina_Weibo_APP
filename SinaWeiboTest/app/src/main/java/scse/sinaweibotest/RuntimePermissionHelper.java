package scse.sinaweibotest;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import static java.lang.invoke.MethodHandles.Lookup.PACKAGE;

/**
 * Created by HP233 on 2017/10/24.
 */

public class RuntimePermissionHelper {

    private Context mContext;
    public static final String PACKAG = "package:";

    public RuntimePermissionHelper(Context mContext){
        this.mContext = mContext;
    }

    public boolean checkPermissions(String... permissions){
        for(String per : permissions){
            if(!checkPermission(per)){
                return false;
            }
        }
        return true;
    }

    private boolean checkPermission(String permission){
        return ContextCompat.checkSelfPermission(mContext, permission) ==
               PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermission(String permission, int resultCode){
        if(ActivityCompat.shouldShowRequestPermissionRationale((Activity)mContext, permission)){
            showMissingPermissionDialog();
        }else{
            ActivityCompat.requestPermissions((Activity)mContext, new String[] {permission}, resultCode);
        }
    }

    private void showMissingPermissionDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog alertDialog = builder.create();

        builder.setMessage("当前应用缺少必要权限。\n\n请点击\"设置\"-\"权限\"-打开所需权限。\n\n最后点击两次后退按钮，即可返回。");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
            }
        });

        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });

        builder.show();
    }

    public void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse( PACKAGE + mContext.getPackageName()));
        mContext.startActivity(intent);
    }

}
