package com.simaple;

import android.app.Application;

import com.awen.image.PhotoSetting;
import com.awen.image.PhotoUtil;

/**
 * Created by Awen <Awentljs@gmail.com>
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PhotoUtil.setDEBUG(true);
        //以下配置可在任何地方设置，或不设置
//        PhotoUtil.init(this,android.R.color.black);
//        PhotoUtil.init(this,android.R.color.holo_blue_light,"/storage/xxxx/xxx");
    }
}
