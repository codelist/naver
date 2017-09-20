/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.lcs.ses;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.widget.Toast;
import android.Manifest;

import com.lcs.ses.handler.WebChromeHandler;
import com.lcs.ses.handler.WebViewHandler;
import com.lcs.ses.util.LOG;
import com.lcs.ses.util.Utils;
import com.plugin.btdevice.BTCommand;

import net.khlim.permission.PermissionManager;

import org.apache.cordova.CordovaActivity;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;

public class MainActivity extends CordovaActivity
{

    PermissionManager lim;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
 
        lim = new PermissionManager(this);

        // 1. 필수권한 
        lim.setEssentialPermissions(
                Manifest.permission.READ_PHONE_STATE            // 폰상태
                , Manifest.permission.ACCESS_FINE_LOCATION      // 블루투스 스캔
        ).setEssentialNotice("[필수권한] 사용자 인증 및 블루투스 프린터 자동 탐색 기능을 사용하기 위해 [설정]에서 전화 및 위치 권한을 허용해주세요.");

        // 2. 선택권한
        lim.putOptionalPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE      // 저장공간(카메라 활영 후 사진 저장)
                , "[선택권한] 게시판 사진 업로드 기능을 사용하기 위해서 다음 권한이 필요합니다."
        );


        lim.run();                                              // 권한체크 실행

       // 웹뷰 로딩 ...
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        lim.setResult(requestCode, permissions, grantResults);
    }
}
