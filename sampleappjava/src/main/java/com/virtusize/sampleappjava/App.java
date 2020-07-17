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

        // Initialize Virtusize instance for your application
        Virtusize = new VirtusizeBuilder().init(this)
                // Only the API key is required
                .setApiKey("15cc36e1d7dad62b8e11722ce1a245cb6c5e6692")
                // For using the Order API, a user ID is required
                .setUserId("123")
                // By default, the Virtusize environment will be set to GLOBAL
                .setEnv(VirtusizeEnvironment.STAGING)
                // By default, the initial language will be set based on the Virtusize environment
                .setLanguage(VirtusizeLanguage.EN)
                // By default, ShowSGI is false
                .setShowSGI(true)
                // By default, Aoyama allows all the possible languages including English, Japanese and Korean
                .setAllowedLanguages(Arrays.asList(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
                // By default, Aoyama displays all the possible info categories in the Product Details tab,
                // including "modelInfo", "generalFit", "brandSizing" and "material".
                .setDetailsPanelCards(Arrays.asList(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
                .build();
    }
}