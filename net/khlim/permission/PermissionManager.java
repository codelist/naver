package net.khlim.permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class PermissionManager {

    private static final boolean IS_DEV = false;
    private static final int ESSENTIAL_PERMISSIONS_REQUEST = 1;
    private static final int OPTIONAL_PERMISSIONS_REQUEST = 2;

    enum PermissionState{
        EMPTY
        , DIALOG_REQUEST
        , SETTING_DIALOG_REQUEST
    }


    private Activity context;

    // 필수조건
    private String[] essentialPermissions;
    private ArrayList<String> filtratedEssentialPermissions;

    // 선택조건
    private Map<String, PmsEntry> optionalPermission;
    private Queue<PmsEntry> filtratedOptionalPermissions;


    private String essentialNotice;

    public PermissionManager(Activity context){
        this.context = context;
    }

    public PermissionManager setEssentialPermissions(String... permissions){
        this.essentialPermissions = permissions;

        return this;
    }

    public void setEssentialNotice(String msg){
        this.essentialNotice = new String(msg);
    }

    public PermissionManager putOptionalPermission(String permission){

        if(null == permission) return null; // 퍼미션이 맞는지 예외 처리 필요

        if(null == optionalPermission){
            optionalPermission = new HashMap<String, PmsEntry>();
        }

        // 중복 제거
        optionalPermission.put(permission, new PmsEntry(permission));

        return this;
    }

    public PermissionManager putOptionalPermission(String permission, String notice){

        if(null == permission) return null; // 퍼미션이 맞는지 예외 처리 필요
        if(null == notice) return null;

        if(null == optionalPermission){
            optionalPermission = new HashMap<String, PmsEntry>();
        }

        // 중복 제거filtratedOptionalPermissions
        optionalPermission.put(permission, new PmsEntry(permission, notice));

        return this;
    }


    public void run(){

        if(PermissionState.EMPTY == checkEssentialPermissions()){
            checkOptionalPermission();
        }

    }

    private PermissionState checkEssentialPermissions(){

        if(essentialPermissions == null) return PermissionState.EMPTY;

        filtratedEssentialPermissions = new ArrayList<String>();

        int result = -1;
        for(String permission : essentialPermissions){
            if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                filtratedEssentialPermissions.add(permission);
            }
        }

        log("filtratedEssentialPermissions:" + filtratedEssentialPermissions);
        if(null != filtratedEssentialPermissions && !filtratedEssentialPermissions.isEmpty()){

            // 필수 권한 중 사용자가 한번이라도 거부한 경험이 있는지 체크
            for(String permission : filtratedEssentialPermissions){
                log("shouldShowRequest : :" + ActivityCompat.shouldShowRequestPermissionRationale(context, permission));
                if(ActivityCompat.shouldShowRequestPermissionRationale(context, permission)){

                    // 고지
                    log("notice!");
                    AlertDialog.Builder alert_confirm = new AlertDialog.Builder(context);
                    alert_confirm.setMessage(essentialNotice).setCancelable(false).setNegativeButton("취소",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ActivityCompat.finishAffinity(context);
                                }
                            }).setPositiveButton("권한설정",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
                                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                    ActivityCompat.finishAffinity(context);
                                }
                            });

                    AlertDialog alert = alert_confirm.create();
                    alert.show();
                    return PermissionState.SETTING_DIALOG_REQUEST;
                }


                // 권한 요청
                log("requestPermissions1:" + filtratedEssentialPermissions);
                log("requestPermissions2:" + filtratedEssentialPermissions.toArray(new String[0]));

                ActivityCompat.requestPermissions(context, filtratedEssentialPermissions.toArray(new String[0]),
                        ESSENTIAL_PERMISSIONS_REQUEST);

                return PermissionState.DIALOG_REQUEST;
            }
        }
        // App 정상 실행
        log("App 정상 실행");

        return PermissionState.EMPTY;
    }

    // 1. 선택 권한은 개별적으로 받아야 한다.

    private void checkOptionalPermission(){

        if(optionalPermission == null) return;

        filtratedOptionalPermissions = new LinkedList<PmsEntry>();

        for(String permission : optionalPermission.keySet()){
            if(ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                filtratedOptionalPermissions.offer(optionalPermission.get(permission));
            }
        }

        requestPermissions();
    }

    private void requestPermissions(){

        log("filtratedOptionalPermissions:" + filtratedOptionalPermissions);

        if(null != filtratedOptionalPermissions && !filtratedOptionalPermissions.isEmpty()) {

            final PmsEntry entry =  filtratedOptionalPermissions.poll();

            if(entry.getNotice() != null){
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(context, new String[]{entry.getPermission()}, OPTIONAL_PERMISSIONS_REQUEST);
                        dialog.dismiss();     //닫기
                    }
                });
                alert.setMessage(entry.getNotice());
                alert.show();
            }else{
                ActivityCompat.requestPermissions(context, new String[]{entry.getPermission()}, OPTIONAL_PERMISSIONS_REQUEST);
            }

        }
    }



    public void setResult(int requestCode, String permissions[], int[] grantResults){
        switch (requestCode) {
            case ESSENTIAL_PERMISSIONS_REQUEST:

                if(IS_DEV)
                    for(int i = 0 ; i < grantResults.length ; i++){
                        log("Result" + "[" +i + "]: "+ permissions[i] + " - " + grantResults[i]);
                    }

                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                        && (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    log("[ESSENTIAL_PERMISSIONS_REQUEST] 권한허가");
                    checkOptionalPermission();
                } else {
                    log("[ESSENTIAL_PERMISSIONS_REQUEST] 권한거부");
                    ActivityCompat.finishAffinity(context);
                }

                break;

            case OPTIONAL_PERMISSIONS_REQUEST:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    log("[OPTIONAL_PERMISSIONS_REQUEST] 권한허가");
                } else {
                    log("[OPTIONAL_PERMISSIONS_REQUEST] 권한거부");
                }

                requestPermissions();
                break;
        }
    }

    public void msg(String msg){
        if(IS_DEV) Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public void log(String msg){
        if(IS_DEV) Log.d("PERMISSION", "[PERMISSION] " + msg);
    }
}