package com.virtusize.sampleappjava;

import android.app.Application;
import com.virtusize.libsource.Virtusize;
import com.virtusize.libsource.VirtusizeBuilder;
import com.virtusize.libsource.data.local.VirtusizeEnvironment;
import com.virtusize.libsource.data.local.VirtusizeInfoCategory;
import com.virtusize.libsource.data.local.VirtusizeLanguage;

import java.util.Arrays;

public class App extends Application {

    Virtusize Virtusize;

    @Override
    public void onCreate() {
        super.onCreate();
        Virtusize = new VirtusizeBuilder()
                .init(this)
                .setApiKey("15cc36e1d7dad62b8e11722ce1a245cb6c5e6692")
                .setUserId("123")
                .setEnv(VirtusizeEnvironment.STAGING)
                .setLanguage(VirtusizeLanguage.EN)
                .setShowSGI(false)
                .setAllowedLanguages(Arrays.asList(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
                .setDetailsPanelCards(Arrays.asList(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
                .build();
    }
}