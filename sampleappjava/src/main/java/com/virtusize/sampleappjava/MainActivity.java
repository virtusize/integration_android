package com.virtusize.sampleappjava;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import com.virtusize.libsource.model.*;
import com.virtusize.libsource.ui.FitIllustratorButton;

public class MainActivity extends AppCompatActivity {

    FitIllustratorButton fitIllustratorButton;
    App app;
    VirtusizeMessageHandler virtusizeMessageHandler;

    private static final String TAG = "MAIN_ACTIVITY_JAVA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fitIllustratorButton = findViewById(R.id.exampleFitButton);
        app = (App) getApplication();

        virtusizeMessageHandler = new VirtusizeMessageHandler() {
            @Override
            public void virtusizeControllerShouldClose(@NonNull FitIllustratorButton fitIllustratorButton) {
                Log.i(TAG, "Close fit illustrator");
            }

            @Override
            public void onEvent(FitIllustratorButton fitIllustratorButton, @NonNull VirtusizeEvents event) {
                Log.i(TAG, VirtusizeEventKt.getEventName(event));
            }

            @Override
            public void onError(FitIllustratorButton fitIllustratorButton, @NonNull VirtusizeError error) {
                Log.e(TAG, VirtusizeErrorKt.message(error));
            }
        };
        app.Virtusize.registerMessageHandler(virtusizeMessageHandler);

        app.Virtusize.setupFitButton(fitIllustratorButton, new VirtusizeProduct("694", "https://www.publicdomainpictures.net/pictures/120000/velka/dress-1950-vintage-style.jpg"));
    }

    @Override
    protected void onPause() {
        app.Virtusize.unregisterMessageHandler(virtusizeMessageHandler);
        super.onPause();
    }
}
