[![](https://jitpack.io/v/virtusize/integration_android.svg)](https://jitpack.io/#virtusize/integration_android)

# Virtusize Android Integration

Virtusize helps retailers to illustrate the size and fit of clothing, shoes and bags online, by letting customers compare the
measurements of an item they want to buy (on a retailer's product page) with an item that they already own (a reference item).
This is done by comparing the silhouettes of the retailer's product with the silhouette of the customer's reference Item.
Virtusize is a widget which opens when clicking on the Virtusize button, which is located next to the size selection on the product page.

Read more about Virtusize at https://www.virtusize.com

You need a unique API key and an Admin account, only available to Virtusize customers. [Contact our sales team](mailto:sales@virtusize.com) to become a customer.

> This is the integration script for native Android apps only. For web integration, refer to the developer documentation on https://developers.virtusize.com. For iOS integration, refer to https://github.com/virtusize/integration_ios

## Requirements
- minSdkVersion >= 15
- compileSdkVersion >= 29
- Setup in AppCompatActivity

## Installation
- In your root `build.gradle` file, add below dependency:
    ```groovy
    allprojects {
        repositories {
            maven { url 'https://jitpack.io' }
        }
    }
    ```
- In your app `build.gradle` file, add below dependencies:
    ```groovy
    dependencies {
        implementation 'com.github.virtusize:integration_android:1.6'
    }
    ```
## Setup
1. Initialize Virtusize object in your Application class's `onCreate` method using your API key and environment. The environment is the region you are running the integration from, either `VirtusizeEnvironment.STAGING`,  `VirtusizeEnvironment.GLOBAL`, `VirtusizeEnvironment.JAPAN` or `VirtusizeEnvironment.KOREA`
    - Kotlin
        ```kotlin
        lateinit var Virtusize: Virtusize
        override fun onCreate() {
            super.onCreate()
            Virtusize = VirtusizeBuilder().init(this)
                .setApiKey(api_key)
                .setUserId("123")
                .setEnv(VirtusizeEnvironment.STAGING)
                .build()
        }
        ```

    - Java
        ```java
        Virtusize Virtusize;

        @Override
        public void onCreate() {
           super.onCreate();
           Virtusize = new VirtusizeBuilder()
                   .init(this)
                   .setApiKey(api_key)
                   .setUserId("123")
                   .setEnv(VirtusizeEnvironment.STAGING)
                   .build();
        ```
2. Add Virtusize button in your activity's XML layout file. Style of the button can be one of the two default available styles **DefaultStyleBlueJapanese**, **DefaultStyleBlackEnglish** or use any other button styles and/or define attributes like text, height, width, etc on the FitIllustratorButton
    ```xml
    <com.virtusize.libsource.ui.FitIllustratorButton
        android:id="@+id/exampleFitButton"
        style="@style/DefaultStyleBlueJapanese" />
    ```
    
    or
    ```xml
    <com.virtusize.libsource.ui.FitIllustratorButton
        android:id="@+id/exampleFitButton"
        style="@style/Widget.AppCompat.Button.Colored"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="@string/fit_button_text" />
    ```
    
3. Inside your activity, setup the Fit Illustrator button using product details by passing a `imageUrl` of the product in order to populate the comparison view and passing in an `externalId` that will be used to reference that product in our API
    - Kotlin
        ```kotlin
        (application as App)
        .Virtusize
        .setupFitButton(
            fitIllustratorButton = exampleFitButton,
            virtusizeProduct = VirtusizeProduct(externalId = "694", imageUrl = "http://simage-kr.uniqlo.com/goods/31/12/11/71/414571_COL_COL02_570.jpg"))
        ```

    - Java
        ```java
        FitIllustratorButton fitIllustratorButton;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);
            fitIllustratorButton = findViewById(R.id.exampleFitButton);
            App app = (App) getApplication();
            app.Virtusize.setupFitButton(fitIllustratorButton, new VirtusizeProduct("694", "https://www.publicdomainpictures.net/pictures/120000/velka/dress-1950-vintage-style.jpg"));
        }
        ```
        
## The Order API

The order API enables Virtusize to show your customers the items they have recently purchased as part of their `Purchase History`, and use those items to compare with new items they want to buy.

#### 1. Initialization

Make sure to set up the **user ID** in your Application class's `onCreate` method before you start.

* Kotlin

```kotlin
lateinit var Virtusize: Virtusize
override fun onCreate() {
    super.onCreate()
    Virtusize = VirtusizeBuilder().init(this)
            .setApiKey(api_key)
            .setUserId(user_id)
            .setEnv(VirtusizeEnvironment.STAGING)
            .build()
}
```

* Java

~~~~java
Virtusize Virtusize;
@Override
public void onCreate() {
super.onCreate();
Virtusize = new VirtusizeBuilder()
        .init(this)
        .setApiKey(api_key)
        .setUserId(user_id)
        .setEnv(VirtusizeEnvironment.STAGING)
        .build();
~~~~

#### 2. Create a *VirtusizeOrder* object for order data

The ***VirtusizeOrder*** object gets passed to the `Virtusize.sendOrder` method, and has the following attributes:

__**Note:**__ * means the attribute is required

**VirtusizeOrder**

| Attribute        | Data Type                              | Example             | Description                         |
| ---------------- | -------------------------------------- | ------------------- | ----------------------------------- |
| externalOrderId* | String                                 | "20200601586"       | The order ID provided by the client |
| items*           | A list of `VirtusizeOrderItem` objects | See the table below | A list of the order items.          |

**VirtusizeOrderItem**

| Attribute  | Data Type | Example                                  | Description                                                  |
| ---------- | --------- | ---------------------------------------- | ------------------------------------------------------------ |
| productId* | String    | "A001"                                   | The provide ID provided by the client. It must be unique for a product. |
| size*      | String    | "S", "M", etc.                           | The name of the size                                         |
| sizeAlias  | String    | "Small", "Large", etc.                   | The alias of the size is added if the size name is not identical from the product page |
| variantId  | String    | "A001_SIZES_RED"                         | An ID that is set on the product SKU, color, or size if there are several options for the item |
| imageUrl*  | String    | "http[]()://images.example.com/coat.jpg" | The image URL of the item                                    |
| color      | String    | "RED", "R', etc.                         | The color of the item                                        |
| gender     | String    | "W", "Women", etc.                       | An identifier for the gender                                 |
| unitPrice* | Double    | 5100.00                                  | The product price that is a double number with a maximum of 12 digits and 2 decimals (12, 2) |
| currency*  | String    | "JPY", "KRW", "USD", etc.                | Currency code                                                |
| quantity*  | Int       | 1                                        | The number of the item purchased. If it's not passed, It will be set to 1 |
| url        | String    | "http[]()://example.com/products/A001"   | The URL of the product page. Please make sure this is a URL that users can access |

**Samples**

* Kotlin

~~~~kotlin
val order = VirtusizeOrder("20200601586")
order.items = mutableListOf(
    VirtusizeOrderItem(
        "A001",
        "L",
        "Large",
        "A001_SIZEL_RED",
        "http://images.example.com/products/A001/red/image1xl.jpg",
        "Red",
        "W",
        5100.00,
        "JPY",
        1,
        "http://example.com/products/A001"
    )
)
~~~~

* Java

~~~~java
VirtusizeOrder order = new VirtusizeOrder("20200601586");
ArrayList<VirtusizeOrderItem> items = new ArrayList<>();
items.add(new VirtusizeOrderItem(
        "A001",
        "L",
        "Large",
        "A001_SIZEL_RED",
        "http://images.example.com/products/A001/red/image1xl.jpg",
        "Red",
        "W",
        5100.00,
        "JPY",
        1,
        "http://example.com/products/A001"
));
~~~~

#### 3. Send an Order

Call the `Virtusize.sendOrder` method in your activity or fragment when the user places an order.
* Kotlin

The `onSuccess` and `onError` callbacks are optional.
~~~~kotlin
(application as App)
    .Virtusize
    .sendOrder(order,
        // This success callback is optional and gets called when the app successfully sends the order
        onSuccess = {
            Log.i(TAG, "Successfully sent the order")
        },
        // This error callback is optional and gets called when an error occurs when the app is sending the order
        onError = { error ->
            Log.e(TAG, error.message())
        })
~~~~
* Java

The `SuccessResponseHandler` and `ErrorResponseHandler` callbacks are optional.
~~~~java
app.Virtusize.sendOrder(order,
        // This success callback is optional and gets called when the app successfully sends the order
        new SuccessResponseHandler() {
            @Override
            public void onSuccess(@Nullable Object data) {
                Log.i(TAG, "Successfully sent the order");
            }
        },
        // This error callback is optional and gets called when an error occurs when the app is sending the order
        new ErrorResponseHandler() {
            @Override
            public void onError(@NonNull VirtusizeError error) {
                Log.e(TAG, VirtusizeErrorKt.message(error));
            }
});
~~~~

## Proguard rules
Add following rules to your proguard rules file:
```
-keep class com.virtusize.libsource.**
```

## Documentation
https://github.com/virtusize/integration_android/wiki

## Examples
1. Kotlin example https://github.com/virtusize/integration_android/tree/master/sampleAppKotlin
2. Java example https://github.com/virtusize/integration_android/tree/master/sampleappjava

## License

Copyright (c) 2018-20 Virtusize CO LTD (https://www.virtusize.jp)
