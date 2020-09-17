package com.simaple;

import android.app.Application;

/**
 * Created by Awen <Awentljs@gmail.com>
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //以下配置可在任何地方设置，或不设置
//        PhotoSetting.init(this,android.R.color.black);
//        PhotoSetting.init(this,android.R.color.holo_blue_light,"/storage/xxxx/xxx");
    }
}
