# Virtusize Android Integration

[![](https://jitpack.io/v/virtusize/integration_android.svg)](https://jitpack.io/#virtusize/integration_android)

[日本語](README-JP.md)

Virtusize helps retailers to illustrate the size and fit of clothing, shoes and bags online, by letting customers compare the
measurements of an item they want to buy (on a retailer's product page) with an item that they already own (a reference item).
This is done by comparing the silhouettes of the retailer's product with the silhouette of the customer's reference Item.
Virtusize is a widget which opens when clicking on the Virtusize button, which is located next to the size selection on the product page.

Read more about Virtusize at https://www.virtusize.com

You need a unique API key and an Admin account, only available to Virtusize customers. [Contact our sales team](mailto:sales@virtusize.com) to become a customer.

> This is the integration script for native Android apps only. For web integration, refer to the developer documentation on https://developers.virtusize.com. For iOS integration, refer to https://github.com/virtusize/integration_ios



## Requirements

- minSdkVersion >= 15
- compileSdkVersion >= 30
- Setup in AppCompatActivity



## Getting Started

If you'd like to continue using the old Version 1.x.x, refer to the branch [v1](https://github.com/virtusize/integration_android/tree/v1).



### 1. Installation

In your root `build.gradle` file, add the following dependency:

```groovy
allprojects {
  repositories {
      maven { url 'https://jitpack.io' }
  }
}
```

In your app `build.gradle` file, add the following dependencies:

```groovy
dependencies {
  implementation 'com.github.virtusize:integration_android:2.0.5'
}
```



### 2. Proguard rules

If you are using Proguard, add following rules to your proguard rules file:

```
-keep class com.virtusize.libsource.**
```


## Setup

### 1. Initialize Virtusize

Initialize the Virtusize object in your Application class's `onCreate` method using the **VirtusizeBuilder** to set up the configuration. Possible configuration methods are shown in the following table: 

**VirtusizeBuilder**

| Method               | Argument Type                     | Example                                                      | Description                                                  | Requirement                                                  |
| -------------------- | --------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| setApiKey            | String                            | setApiKey("api_key")                                         | A unique API key is provided to each Virtusize client.       | Yes                                                          |
| setUserId            | String                            | setUserId("123")                                             | Passed from the client if the user is logged into the client's app. | Yes, if the Order API is used.                               |
| setEnv               | VirtusizeEnvironment              | setEnv(VirtusizeEnvironment.STAGING)                         | The environment is the region you are running the integration from, either `VirtusizeEnvironment.STAGING`,  `VirtusizeEnvironment.GLOBAL`, `VirtusizeEnvironment.JAPAN` or `VirtusizeEnvironment.KOREA`. | No. By default, the Virtusize environment will be set to `VirtusizeEnvironment.GLOBAL`. |
| setLanguage          | VirtusizeLanguage                 | setLanguage(VirtusizeLanguage.EN)                            | Sets the initial language that the integration will load in. The possible values are `VirtusizeLanguage.EN`, `VirtusizeLanguage.JP` and `VirtusizeLanguage.KR` | No. By default, the initial language will be set based on the Virtusize environment. |
| setShowSGI           | Boolean                           | setShowSGI(true)                                             | Determines whether the integration will fetch SGI and use SGI flow for users to add user generated items to their wardrobe. | No. By default, ShowSGI is set to false                      |
| setAllowedLanguages  | A list of `VirtusizeLanguage`     | In Kotlin, setAllowedLanguages(mutableListOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))<br />In Java, setAllowedLanguages(Arrays.asList(VirtusizeLanguage.EN, VirtusizeLanguage.JP)) | The languages which the user can switch to using the Language Selector | No. By default, the integration allows all possible languages to be displayed, including English, Japanese and Korean. |
| setDetailsPanelCards | A list of `VirtusizeInfoCategory` | In Kotlin, setDetailsPanelCards(mutableListOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))<br />In Java, setDetailsPanelCards(Arrays.asList(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT)) | The info categories which will be display in the Product Details tab. Possible categories are: `VirtusizeInfoCategory.MODEL_INFO`, `VirtusizeInfoCategory.GENERAL_FIT`, `VirtusizeInfoCategory.BRAND_SIZING` and `VirtusizeInfoCategory.MATERIAL` | No. By default, the integration displays all the possible info categories in the Product Details tab. |

- Kotlin

  ```kotlin
  lateinit var Virtusize: Virtusize
  override fun onCreate() {
      super.onCreate()
     Virtusize = VirtusizeBuilder().init(this)
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
              // By default, Virtusize allows all the possible languages
              .setAllowedLanguages(mutableListOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
              // By default, Virtusize displays all the possible info categories in the Product Details tab
              .setDetailsPanelCards(mutableListOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
              .build()
  }
  ```
- Java

    ```java
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
                    // By default, Virtusize allows all the possible languages
                    .setAllowedLanguages(Arrays.asList(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
                    // By default, Virtusize displays all the possible info categories in the Product Details tab
                    .setDetailsPanelCards(Arrays.asList(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
                    .build();
    ```



### 2. Add the Virtusize Button

**A. Add a VirtusizeButton in your activity's XML layout file.**

Add a Virtusize button in your activity's XML layout file. You can use the default button style we provide by setting up `app:virtusizeButtonStyle="default_style"` in XML:

- XML

  ```xml
  <com.virtusize.libsource.ui.VirtusizeButton
      android:id="@+id/exampleVirtusizeButton"
      app:virtusizeButtonStyle="default_style"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content" />
  ```

 or programmatically:

- Kotlin

  ```kotlin
  exampleVirtusizeButton.buttonStyle = VirtusizeButtonStyle.DEFAULT_STYLE
  ```

- Java

  ```java
  virtusizeButton.setButtonStyle(VirtusizeButtonStyle.DEFAULT_STYLE);
  ```

**B. You can also use any other button styles and/or define the button's attributes like text, height, width, etc.**

```xml
<com.virtusize.libsource.ui.VirtusizeButton
    android:id="@+id/exampleVirtusizeButton"
    style="@style/Widget.AppCompat.Button.Colored"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/virtusize_button_text" />
```



### 3. Set Up the Virtusize Button

Inside your activity, set up the Virtusize button using product details by passing an `imageUrl` for the product in order to populate the comparison view, and an `externalId` that will be used to reference that product in our API

- Kotlin

  ```kotlin
  (application as App)
  .Virtusize
  .setupVirtusizeButton(
      virtusizeButton = exampleVirtusizeButton,
      virtusizeProduct = VirtusizeProduct(externalId = "694", imageUrl = "http://simage-kr.uniqlo.com/goods/31/12/11/71/414571_COL_COL02_570.jpg"))
  ```

- Java

  ```java
  VirtusizeButton virtusizeButton;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      virtusizeButton = findViewById(R.id.exampleVirtusizeButton);
      App app = (App) getApplication();
      app.Virtusize.setupVirtusizeButton(virtusizeButton, new VirtusizeProduct("694", "https://www.publicdomainpictures.net/pictures/120000/velka/dress-1950-vintage-style.jpg"));
  }
  ```



### 4. Close the Virtusize Widget Manually

- Kotlin

  ```kotlin
  exampleVirtusizeButton.dismissVirtusizeView()
  ```

- Java

  ```java
  exampleVirtusizeButton.dismissVirtusizeView();
  ```



### 5. Setup Virtusize Message Handler

Please do not forget to unregister message handler in activity or fragment's lifecycle method before it dies or is removed. See next section on how to.

* Kotlin
    ~~~~kotlin
    private val activityMessageHandler = object : VirtusizeMessageHandler {
        override fun virtusizeControllerShouldClose(virtusizeButton: VirtusizeButton) {
            Log.i(TAG, "Close Virtusize View")
            virtusizeButton.dismissVirtusizeView()
        }
    
        override fun onEvent(virtusizeButton: VirtusizeButton?, event: VirtusizeEvent) {
            Log.i(TAG, event.name)
        }
    
        override fun onError(virtusizeButton: VirtusizeButton?, errorType: VirtusizeError) {
            Log.e(TAG, errorType.message)
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        //...
        // Register message handler to listen to events from Virtusize
        (application as App)
            .Virtusize.registerMessageHandler(activityMessageHandler)
        //...
      }
    ~~~~

* Java
    ~~~~java
    VirtusizeMessageHandler virtusizeMessageHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //...
        App app = (App) getApplication();
      app.Virtusize.registerMessageHandler(new VirtusizeMessageHandler() {
            @Override
            public void virtusizeControllerShouldClose(@NotNull VirtusizeButton virtusizeButton) {
                Log.i(TAG, "Close Virtusize View");
          }
    
            @Override
            public void onEvent(@org.jetbrains.annotations.Nullable VirtusizeButton virtusizeButton, @NotNull VirtusizeEvent event) {
                Log.i(TAG, event.getName());
            }
    
            @Override
            public void onError(VirtusizeButton virtusizeButton, @NonNull VirtusizeError error) {
                Log.e(TAG, error.getMessage());
            }
        });
            app.Virtusize.registerMessageHandler(virtusizeMessageHandler);
        //...
     }
    ~~~~
	



### 6. Unregister Virtusize Message Handler

A message handler is tied to an activity or fragment's lifecycle, but the Virtusize library object is tied to the application's lifecycle. So if you forget to unregister message handler, then it will keep listening to events even after activity is dead or fragment has been removed. In the case of an activity; depending on where in the lifecycle you registered the message handler, you may need to unregister it in your `onPause` or `onStop` method before the super method is called. Follow the same guidelines in the case of fragment as well.

* Kotlin
    ~~~~kotlin
    private val activityMessageHandler: VirtusizeMessageHandler
    override fun onPause() {
        // Always un register message handler in onPause() or depending on implementation onStop().
        (application as App)
            .Virtusize.unregisterMessageHandler(activityMessageHandler)
         super.onPause()
    }
    ~~~~

* Java
    ~~~~java
    @Override
    protected void onPause() {
        app.Virtusize.unregisterMessageHandler(virtusizeMessageHandler);
        super.onPause();
    }
    ~~~~



## The Order API

The order API enables Virtusize to show your customers the items they have recently purchased as part of their `Purchase History`, and use those items to compare with new items they want to buy.



#### 1. Initialization

Make sure to set up the **user ID** before sending orders to Virtusize. You can set up the user ID either:

in your Application class's `onCreate` method before the app is launched 

or 

in your activity or fragment after the app is launched

* Kotlin

    ```kotlin
    // In your Application class's `onCreate` method before the app is launched 
    lateinit var Virtusize: Virtusize
    override fun onCreate() {
        super.onCreate()
        Virtusize = VirtusizeBuilder().init(this)
                .setApiKey(api_key)
                .setUserId(user_id)
                .setEnv(VirtusizeEnvironment.STAGING)
                .build()
    }

    // In your activity or fragment after the app is launched
    (application as App).Virtusize.setUserID("user_id")
    ```

* Java

    ~~~~java
    // In your Application class's `onCreate` method before the app is launched 
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
      
    // In your activity or fragment after the app is launched
    app = (App) getApplication();
    app.Virtusize.setUserId("user_id");
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
| productId* | String    | "A001"                                   | The product ID provided by the client. It must be unique for each product. |
| size*      | String    | "S", "M", etc.                           | The name of the size                                         |
| sizeAlias  | String    | "Small", "Large", etc.                   | The alias of the size is added if the size name is not identical from the product page |
| variantId  | String    | "A001_SIZES_RED"                         | An ID that is set on the product SKU, color, or size if there are several options for the item |
| imageUrl*  | String    | "http[]()://images.example.com/coat.jpg" | The image URL of the item                                    |
| color      | String    | "RED", "R', etc.                         | The color of the item                                        |
| gender     | String    | "W", "Women", etc.                       | An identifier for the gender                                 |
| unitPrice* | Double    | 5100.00                                  | The product price that is a double number with a maximum of 12 digits and 2 decimals (12, 2) |
| currency*  | String    | "JPY", "KRW", "USD", etc.                | Currency code                                                |
| quantity*  | Int       | 1                                        | The number of items purchased. If it's not passed, It will be set to 1 |
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
                Log.e(TAG, error.message)
            }
        )
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
                public void onError(@NotNull VirtusizeError error) {
                    Log.e(TAG, error.getMessage());
                }
            }
    );
    ~~~~



## Examples

1. Kotlin example https://github.com/virtusize/integration_android/tree/master/sampleAppKotlin
2. Java example https://github.com/virtusize/integration_android/tree/master/sampleappjava



## License

Copyright (c) 2018-21 Virtusize CO LTD (https://www.virtusize.jp)