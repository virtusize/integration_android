package com.virtusize.sampleappjava;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.virtusize.libsource.ErrorResponseHandler;
import com.virtusize.libsource.SuccessResponseHandler;
import com.virtusize.libsource.data.local.VirtusizeError;
import com.virtusize.libsource.data.local.VirtusizeErrorKt;
import com.virtusize.libsource.data.local.VirtusizeEventKt;
import com.virtusize.libsource.data.local.VirtusizeEvents;
import com.virtusize.libsource.data.local.VirtusizeMessageHandler;
import com.virtusize.libsource.data.local.VirtusizeOrder;
import com.virtusize.libsource.data.local.VirtusizeOrderItem;
import com.virtusize.libsource.data.local.VirtusizeProduct;
import com.virtusize.libsource.ui.FitIllustratorButton;

import java.util.ArrayList;

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

        sendOrderSample();
    }

    /**
     * Demonstrates how to send an order to the Virtusize server
     *
     * Notes:
     * 1. The parameters sizeAlias, variantId, color, gender, and url for [VirtusizeOrderItem] are optional
     * 2. If quantity is not provided, it will be set to 1 on its own
     */
    private void sendOrderSample() {
        VirtusizeOrder order = new VirtusizeOrder("888400111032");
        ArrayList<VirtusizeOrderItem> items = new ArrayList<>();
        items.add(new VirtusizeOrderItem(
                "P001",
                "L",
                "Large",
                "P001_SIZEL_RED",
                "http://images.example.com/products/P001/red/image1xl.jpg",
                "Red",
                "W",
                5100.00,
                "JPY",
                1,
                "http://example.com/products/P001"
        ));
        order.setItems(items);

        app.Virtusize.sendOrder(order,
                // this optional success callback is called when the app successfully sends the order
                new SuccessResponseHandler() {
                    @Override
                    public void onSuccess(@Nullable Object data) {
                        Log.i(TAG, "Successfully sent the order");
                    }
                },
                // this optional error callback is called when an error occurs when the app is sending the order
                new ErrorResponseHandler() {
                    @Override
                    public void onError(@NonNull VirtusizeError error) {
                        Log.e(TAG, VirtusizeErrorKt.message(error));
                    }
        });
    }

    @Override
    protected void onPause() {
        app.Virtusize.unregisterMessageHandler(virtusizeMessageHandler);
        super.onPause();
    }
}
