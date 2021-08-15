package com.bypass.frp.util;
import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.content.Intent;
import android.content.Context;

public class UiUtils {

    public static void setStatusBarLightMode(final Activity activity,
                                             final boolean isLightMode) {
        setStatusBarLightMode(activity.getWindow(), isLightMode);
    }

    public static void setStatusBarLightMode(final Window window,
                                             final boolean isLightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = window.getDecorView();
            int vis = decorView.getSystemUiVisibility();
            if (isLightMode) {
                vis |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                vis &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(vis);
        }
    }
	
	
	
	
	

}
