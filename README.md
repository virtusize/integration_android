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
- compileSdkVersion >= 28
- Setup in AppCompatActivity

## Installation
- Download Library AAR file from https://github.com/virtusize/integration_android/blob/master/virtusize.aar
- In your Android Studio add the compiled AAR by 
    1. Click File > New > New Module.
    2. Click Import .JAR/.AAR Package then click Next.
    3. Enter the location of the compiled AAR or JAR file then click Finish
- In your app `build.gradle` file, add below dependencies:
    ```groovy
    implementation project(":virtusize")
    implementation 'com.android.volley:volley:1.1.1'
    implementation 'com.google.code.gson:gson:2.8.5'
    implementation 'javax.annotation:jsr250-api:1.0'
    ```
    Find out more at: https://developer.android.com/studio/projects/android-library#AddDependency
## Setup
1. Initialize Virtusize object in your Application class's `onCreate` method using your API key and environment. The environment is the region you are running the integration from, either `VirtusizeEnvironment.STAGING`,  `VirtusizeEnvironment.GLOBAL`, `VirtusizeEnvironment.JAPAN` or `VirtusizeEnvironment.KOREA`
    - Kotlin
        ```kotlin
        lateinit var Virtusize: Virtusize
        override fun onCreate() {
            super.onCreate()
            Virtusize = VirtusizeBuilder().init(this)
                .setApiKey(api_key)
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
                   .setEnv(VirtusizeEnvironment.STAGING)
                   .build();
        ```
2. Add Virtusize button in your activity's XML layout file
    ```xml
    <com.virtusize.libsource.ui.FitIllustratorButton
        android:id="@+id/exampleFitButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
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

## Documentation
https://github.com/virtusize/integration_android/wiki

## Examples
1. Kotlin example https://github.com/virtusize/integration_android/tree/master/sampleAppKotlin
2. Java example https://github.com/virtusize/integration_android/tree/master/sampleappjava

## License

Copyright (c) 2018-19 Virtusize CO LTD (https://www.virtusize.jp)
