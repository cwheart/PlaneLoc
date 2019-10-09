package com.xinshuaifeng.work.planeloc;

import android.app.Activity;
import android.support.annotation.NonNull;

public interface IRequestPermissionsResult {
    boolean doRequestPermissionsResult(Activity activity, @NonNull String[] permissions, @NonNull int[] grantResults);
}
