package com.bypass.frp.common.crash;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class CrashHandler {

    public static final UncaughtExceptionHandler DEFAULT_UNCAUGHT_EXCEPTION_HANDLER = Thread.getDefaultUncaughtExceptionHandler();

    public static void init(Application app) {
        init(app, null);
    }

    public static void init(final Application app, final String crashDir) {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler(){

                @Override
                public void uncaughtException(Thread thread, Throwable throwable) {
                    try {
                        tryUncaughtException(thread, throwable);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null)
                            DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                    }
                }

                private void tryUncaughtException(Thread thread, Throwable throwable) {
                    final String time = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss").format(new Date());
                    File crashFile = new File(TextUtils.isEmpty(crashDir) ? new File(app.getExternalFilesDir(null), "crash")
                                              : new File(crashDir), "crash_" + time + ".txt");

                    String versionName = "unknown";
                    long versionCode = 0;
                    try { 
                        PackageInfo packageInfo = app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
                        versionName = packageInfo.versionName;
                        versionCode = Build.VERSION.SDK_INT >= 28 ? packageInfo.getLongVersionCode()
                            : packageInfo.versionCode;
                    } catch (PackageManager.NameNotFoundException ignored) {}

                    String fullStackTrace; {
                        StringWriter sw = new StringWriter(); 
                        PrintWriter pw = new PrintWriter(sw);
                        throwable.printStackTrace(pw);
                        fullStackTrace = sw.toString();
                        pw.close();
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("************* Crash Head ****************\n");
                    sb.append("Time Of Crash      : ").append(time).append("\n");
                    sb.append("Device Manufacturer: ").append(Build.MANUFACTURER).append("\n");
                    sb.append("Device Model       : ").append(Build.MODEL).append("\n");
                    sb.append("Android Version    : ").append(Build.VERSION.RELEASE).append("\n");
                    sb.append("Android SDK        : ").append(Build.VERSION.SDK_INT).append("\n");
                    sb.append("App VersionName    : ").append(versionName).append("\n");
                    sb.append("App VersionCode    : ").append(versionCode).append("\n");
                    sb.append("************* Crash Head ****************\n");
                    sb.append("\n").append(fullStackTrace);

                    String errorLog = sb.toString();

                    try {
                        writeFile(crashFile, errorLog);
                    } catch (IOException ignored) {}

                    gotoCrashActiviy: {
                        Intent intent = new Intent(app, CrashActiviy.class);
                        intent.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        );
                        intent.putExtra(CrashActiviy.EXTRA_CRASH_INFO, errorLog);
                        try {
                            app.startActivity(intent);
                            android.os.Process.killProcess(android.os.Process.myPid());
                            System.exit(0);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            if (DEFAULT_UNCAUGHT_EXCEPTION_HANDLER != null)
                                DEFAULT_UNCAUGHT_EXCEPTION_HANDLER.uncaughtException(thread, throwable);
                        }
                    }

                }

                private void writeFile(File file, String content) throws IOException {
                    File parentFile = file.getParentFile();
                    if (parentFile != null && !parentFile.exists()) {
                        parentFile.mkdirs();
                    }
                    file.createNewFile();
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(content.getBytes());
                    try {
                        fos.close();
                    } catch (IOException e) {}
                }

            });
    }

    public static final class CrashActiviy extends Activity implements MenuItem.OnMenuItemClickListener {

        private static final String EXTRA_CRASH_INFO = "crashInfo";

        private String mLog;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTheme(android.R.style.Theme_DeviceDefault);
            mLog = getIntent().getStringExtra(EXTRA_CRASH_INFO);
            setContentView: {
                ScrollView contentView = new ScrollView(this);
                contentView.setFillViewport(true);
                HorizontalScrollView hw = new HorizontalScrollView(this);
                TextView message = new TextView(this); {
                    int padding = dp2px(16);
                    message.setPadding(padding, padding, padding, padding);
                    message.setText(mLog);
                    message.setTextIsSelectable(true);
                }
                hw.addView(message);
                contentView.addView(hw, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                setContentView(contentView);
            }
        }

        @Override
        public void onBackPressed() {
            restart();
        }

        private void restart() {
            PackageManager pm = getPackageManager();
            Intent intent = pm.getLaunchIntentForPackage(getPackageName());
            if (intent != null) {
                intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                );
                startActivity(intent);
            }
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

        private int dp2px(final float dpValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.copy: 
                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(getPackageName(), mLog));
                    break;
            }
            return false;
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            menu.add(0, android.R.id.copy, 0, android.R.string.copy).setOnMenuItemClickListener(this)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            return true;
        }

    }

}

