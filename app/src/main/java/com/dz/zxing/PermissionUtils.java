package com.dz.zxing;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * FileName com.inspur.icity.tianjin.base.utils .
 * Description ${DESC} .
 * Created by zhang-ning .
 * LoginData 2017/8/22  .
 * Change by zhang-ning .
 * ChangeTime 2017/8/22 .
 */
public class PermissionUtils {

    public static final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    public static final String[] GET_PHONE_LOCATION = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS};
    public static final String[] GET_READ_WRITE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    public static boolean getPermission(Activity context, String premisson, int requestCode) {
        int hasPermission = ActivityCompat.checkSelfPermission(context,
                premisson);
        if (hasPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(context, premisson)) {
                ActivityCompat.requestPermissions(context, new String[]{premisson}, requestCode);
                return false;
            }
            ActivityCompat.requestPermissions(context, new String[]{premisson},
                    requestCode);
            return false;
        }
        return true;
    }


    public static String[] checkPermission(Context context, String... permission) {
        ArrayList<String> neededPermisson = new ArrayList<>();
        for (String premison : permission) {
            int hasPermission = ContextCompat.checkSelfPermission(context,
                    premison);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                neededPermisson.add(premison);
            }
        }

        return (String[]) neededPermisson.toArray(new String[0]);
    }
    //检查腾讯广告所需3个权限是否同时申请
    public static boolean checkAdPermission(Context context){
        ArrayList<String> neededPermisson = new ArrayList<>();
        String[] AdPermission = {PermissionUtils.READ_PHONE_STATE, PermissionUtils.GET_READ_WRITE[1], PermissionUtils.GET_PHONE_LOCATION[1]};
        for (String permission: AdPermission){
            int hasPermission = ContextCompat.checkSelfPermission(context,
                    permission);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                neededPermisson.add(permission);
            }
        }
        return neededPermisson.size() == 0;
    }


    public static boolean getPermission(Activity context, int requestCode, String... premisson) {
        ArrayList<String> neededPermisson = new ArrayList<>();
        for (String premison : premisson) {
            int hasPermission = ContextCompat.checkSelfPermission(context,
                    premison);
            if (hasPermission != PackageManager.PERMISSION_GRANTED) {
                neededPermisson.add(premison);
            }
        }

        if (neededPermisson.size() > 0) {
            ActivityCompat.requestPermissions(context, neededPermisson.toArray(new String[neededPermisson.size()]), requestCode);
            return false;
        }
        return true;
    }

    public static boolean getPermission(final Activity context, String premisson, int requestCode, String permissionName) {
        int hasPermission = ContextCompat.checkSelfPermission(context,
                premisson);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && hasPermission != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, premisson)) {
                startDialog(context, permissionName);
                return false;
            }
            ActivityCompat.requestPermissions(context, new String[]{premisson},
                    requestCode);
            return false;
        }
        return true;
    }

    public static void startDialog(final Activity context, String permissionName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("友情提示");
        builder.setMessage("您没有授权" + permissionName + "权限，请在设置中打开授权");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent intent = new Intent();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= 9) {
                    intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                    intent.setData(Uri.fromParts("package", context.getPackageName(), null));
                } else if (Build.VERSION.SDK_INT <= 8) {
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
                    intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
                }
                context.startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    public static void getOverLayPermission(Activity context) {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(context)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context.getPackageName()));
                context.startActivityForResult(intent, 10);
            }
        }
    }

    /**
     * 判断GPS是否开启，GPS或者AGPS开启一个就认为是开启的
     *
     * @param context
     * @return true 表示开启
     */
    public static final boolean isGpsOPen(final Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 通过WLAN或移动网络(3G/2G)确定的位置（也称作AGPS，辅助GPS定位。主要用于在室内或遮盖物（建筑群或茂密的深林等）密集的地方定位）
        boolean network = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (gps || network) {
            return true;
        }

        return false;
    }

    /**
     * 强制帮用户打开GPS
     *
     * @param context
     */
    public static final void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
