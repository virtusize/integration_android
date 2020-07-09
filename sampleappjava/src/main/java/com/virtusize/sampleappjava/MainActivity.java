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
import com.virtusize.libsource.data.local.AoyamaInfoCategory;
import com.virtusize.libsource.data.local.AoyamaLanguage;
import com.virtusize.libsource.data.local.AoyamaParams;
import com.virtusize.libsource.ui.AoyamaButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    AoyamaButton aoyamaButton;
    App app;
    VirtusizeMessageHandler virtusizeMessageHandler;

    private static final String TAG = "MAIN_ACTIVITY_JAVA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        aoyamaButton = findViewById(R.id.exampleAoyamaButton);
        app = (App) getApplication();

        virtusizeMessageHandler = new VirtusizeMessageHandler() {
            @Override
            public void virtusizeControllerShouldClose(@NotNull AoyamaButton aoyamaButton) {
                Log.i(TAG, "Close Aoyama");
            }

            @Override
            public void onEvent(AoyamaButton aoyamaButton, @NonNull VirtusizeEvents event) {
                Log.i(TAG, VirtusizeEventKt.getEventName(event));
            }

            @Override
            public void onError(AoyamaButton aoyamaButton, @NonNull VirtusizeError error) {
                Log.e(TAG, VirtusizeErrorKt.message(error));
            }
        };
        app.Virtusize.registerMessageHandler(virtusizeMessageHandler);

        app.Virtusize.setupAoyamaButton(aoyamaButton, new AoyamaParams.Builder()
                .language(AoyamaLanguage.EN)
                .virtusizeProduct(new VirtusizeProduct("694", "http://simage-kr.uniqlo.com/goods/31/12/11/71/414571_COL_COL02_570.jpg"))
                .showSGI(false)
                .allowedLanguages(Arrays.asList(AoyamaLanguage.EN, AoyamaLanguage.JP))
                .detailsPanelCards(Arrays.asList(AoyamaInfoCategory.BRAND_SIZING, AoyamaInfoCategory.GENERAL_FIT))
                .build());

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
                    public void onError(@Nullable Integer errorCode, @Nullable String errorMessage, @NotNull VirtusizeError error) {
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
