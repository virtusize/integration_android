package com.virtusize.sampleappjava;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.virtusize.android.ErrorResponseHandler;
import com.virtusize.android.SuccessResponseHandler;
import com.virtusize.android.data.local.VirtusizeError;
import com.virtusize.android.data.local.VirtusizeEvent;
import com.virtusize.android.data.local.VirtusizeMessageHandler;
import com.virtusize.android.data.local.VirtusizeOrder;
import com.virtusize.android.data.local.VirtusizeOrderItem;
import com.virtusize.android.data.local.VirtusizeProduct;
import com.virtusize.android.data.local.VirtusizeViewStyle;
import com.virtusize.android.ui.VirtusizeButton;
import com.virtusize.android.ui.VirtusizeInPageMini;
import com.virtusize.android.ui.VirtusizeInPageStandard;
import com.virtusize.android.util.ExtensionsKt;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    VirtusizeButton virtusizeButton;
    VirtusizeInPageStandard virtusizeInPageStandard;
    VirtusizeInPageMini virtusizeInPageMini;
    App app;
    VirtusizeMessageHandler virtusizeMessageHandler;

    private static final String TAG = "MAIN_ACTIVITY_JAVA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        virtusizeButton = findViewById(R.id.exampleVirtusizeButton);
        virtusizeInPageStandard = findViewById(R.id.exampleVirtusizeInPageStandard);
        virtusizeInPageMini = findViewById(R.id.exampleVirtusizeInPageMini);
        app = (App) getApplication();

        virtusizeMessageHandler = new VirtusizeMessageHandler() {
            @Override
            public void onEvent(@NotNull VirtusizeProduct product, @NotNull VirtusizeEvent event) {
                Log.i(TAG, event.getName());
            }

            @Override
            public void onError(@NonNull VirtusizeError error) {
                Log.e(TAG, error.getMessage());
            }
        };
        app.Virtusize.registerMessageHandler(virtusizeMessageHandler);

        VirtusizeProduct product = new VirtusizeProduct(
                "694",
                "http://www.image.com/goods/12345.jpg"
        );
        app.Virtusize.load(product);

        app.Virtusize.setupVirtusizeView(virtusizeButton, product);
        /*
         * To set up the button style programmatically
         * virtusizeButton.setVirtusizeViewStyle(VirtusizeViewStyle.BLACK);
         */

        app.Virtusize.setupVirtusizeView(virtusizeInPageStandard, product);
        // Set up the InPage Standard style programmatically
        virtusizeInPageStandard.setVirtusizeViewStyle(VirtusizeViewStyle.TEAL);
        /*
         * If you like, you can set up the horizontal margins between the edges of the app screen and the InPage Standard view
         * Note: Use the helper extension function `ExtensionsKt.getDpInPx` if you like
         * virtusizeInPageStandard.setHorizontalMargin(ExtensionsKt.getDpInPx(16));
         */

        /*
         * If you like, you can set up the background color of InPage Standard view as long as it passes WebAIM contrast test.
         *
         * virtusizeInPageStandard.setButtonBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue));
         */

        // If you like, you can change the text sizes of the InPage message and the Check Size button
        virtusizeInPageStandard.setMessageTextSize(ExtensionsKt.getSpToPx(10));
        virtusizeInPageStandard.setButtonTextSize(ExtensionsKt.getSpToPx(10));


        app.Virtusize.setupVirtusizeView(virtusizeInPageMini, product);
        virtusizeInPageMini.setVirtusizeViewStyle(VirtusizeViewStyle.TEAL);
        /*
         * If you like, you can set up the background of InPage Mini view as long as it passes WebAIM contrast test.
         *
         * virtusizeInPageMini.setInPageMiniBackgroundColor(ContextCompat.getColor(this, R.color.ocean_blue));
         */

        // If you like, you can change the text sizes of the InPage message and the Check Size button
        virtusizeInPageMini.setMessageTextSize(ExtensionsKt.getSpToPx(12));
        virtusizeInPageMini.setButtonTextSize(ExtensionsKt.getSpToPx(10));

        /*
         * To close the Virtusize page
         *
         * virtusizeButton.dismissVirtusizeView();
         * virtusizeInPageStandard.dismissVirtusizeView();
         * virtusizeInPageMini.dismissVirtusizeView();
         */

        // The sample function to send an order to the Virtusize server
        sendOrderSample();
    }

    /**
     * Demonstrates how to send an order to the Virtusize server
     * <p>
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
                    public void onError(@NotNull VirtusizeError error) {
                        Log.e(TAG, error.getMessage());
                    }
                });
    }

    @Override
    protected void onPause() {
        app.Virtusize.unregisterMessageHandler(virtusizeMessageHandler);
        super.onPause();
    }
}
