# Virtusize Android Integration

[![](https://jitpack.io/v/virtusize/integration_android.svg)](https://jitpack.io/#virtusize/integration_android) [![](https://img.shields.io/maven-central/v/com.virtusize.android/virtusize)](https://central.sonatype.com/artifact/com.virtusize.android/virtusize)

[日本語](https://github.com/virtusize/integration_android/blob/master/README-JP.md)

**For Jetpack Compose users, refer to [README-COMPOSE.md](https://github.com/virtusize/integration_android/blob/master/README-COMPOSE.md)**

Virtusize helps retailers to illustrate the size and fit of clothing, shoes and bags online, by
letting customers compare the
measurements of an item they want to buy (on a retailer's product page) with an item that they
already own (a reference item).
This is done by comparing the silhouettes of the retailer's product with the silhouette of the
customer's reference Item.
Virtusize is a widget which opens when clicking on the Virtusize button, which is located next to
the size selection on the product page.

Read more about Virtusize at https://www.virtusize.com

You need a unique API key and an Admin account, only available to Virtusize
customers. [Contact our sales team](mailto:sales@virtusize.com) to become a customer.

> This is the integration script for native Android apps only. For web integration, refer to the
> developer documentation on https://developers.virtusize.com. For iOS integration, refer
> to https://github.com/virtusize/integration_ios

## Table of Contents

- [Requirements](#requirements)

- [Getting Started](#getting-started)

    - [Installation](#1-installation)
    - [Proguard Rules](#2-proguard-rules)

- [Setup](#setup)

    - [Initialize Virtusize](#1-initialize-virtusize)
    - [Load Product with Virtusize SDK](#2-load-product-with-virtusize-sdk)
    - [Enable SNS Authentication](#3-enable-sns-authentication)
    - [Register Virtusize Message Handler (Optional)](#4-register-virtusize-message-handler-optional)
    - [Unregister Virtusize Message Handler (Optional)](#5-unregister-virtusize-message-handler-optional)

- [Virtusize Views](#virtusize-views)

    - [Virtusize Button](#1-virtusize-button)
    - [Virtusize InPage](#2-virtusize-inpage)
        - [InPage Standard](#2-inpage-standard)
        - [InPage Mini](#3-inpage-mini)

- [The Order API](#the-order-api)

    - [Initialization](#1-initialization)
    - [Create a
      *VirtusizeOrder* object for order data](#2-create-a-virtusizeorder-object-for-order-data)
    - [Send an Order](#3-send-an-order)

- [Enable SNS Login in Virtusize for native Webview apps](#enable-sns-login-in-virtusize-for-native-webview-apps)

- [Examples](#examples)

- [Contributing](#contributing)

- [License](#license)

## Requirements

- minSdkVersion >= 21
- compileSdkVersion >= 34
- Setup in AppCompatActivity

## Getting Started

If you'd like to continue using the old Version 1.x.x, refer to the
branch [v1](https://github.com/virtusize/integration_android/tree/v1).

### 1. Installation

In your app `build.gradle` file, add the following dependencies:

- Groovy (build.gradle)

  ```groovy
  dependencies {
    implementation 'com.virtusize.android:virtusize:2.12.4'
  }
  ```

- Kotlin (build.gradle.kts)

  ```kotlin
  dependencies {
    implementation("com.virtusize.android:virtusize:2.12.4")
  }
  ```

### 2. Proguard Rules

If you are using Proguard, add following rules to your proguard rules file:

```
-keep class com.virtusize.android.**
```

## Setup

### 1. Initialize Virtusize

Initialize the Virtusize object in your Application class's `onCreate` method using the *
*VirtusizeBuilder** to set up the configuration. Possible configuration methods are shown in the
following table:

**VirtusizeBuilder**

| Method               | Argument Type                     | Example                                                                                                                                                                                                                                        | Description                                                                                                                                                                                                                                       | Requirement                                                                                                            |
|----------------------|-----------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------|
| setApiKey            | String                            | setApiKey("api_key")                                                                                                                                                                                                                           | A unique API key is provided to each Virtusize client.                                                                                                                                                                                            | Yes                                                                                                                    |
| setUserId            | String                            | setUserId("123")                                                                                                                                                                                                                               | Passed from the client if the user is logged into the client's app.                                                                                                                                                                               | Yes, if the Order API is used.                                                                                         |
| setEnv               | VirtusizeEnvironment              | setEnv(VirtusizeEnvironment.STAGING)                                                                                                                                                                                                           | The environment is the region you are running the integration from, either `VirtusizeEnvironment.STAGING`,  `VirtusizeEnvironment.GLOBAL`, `VirtusizeEnvironment.JAPAN` or `VirtusizeEnvironment.KOREA`.                                          | No. By default, the Virtusize environment will be set to `VirtusizeEnvironment.GLOBAL`.                                |
| setLanguage          | VirtusizeLanguage                 | setLanguage(VirtusizeLanguage.EN)                                                                                                                                                                                                              | Sets the initial language that the integration will load in. The possible values are `VirtusizeLanguage.EN`, `VirtusizeLanguage.JP` and `VirtusizeLanguage.KR`                                                                                    | No. By default, the initial language will be set based on the Virtusize environment.                                   |
| setShowSGI           | Boolean                           | setShowSGI(true)                                                                                                                                                                                                                               | Determines whether the integration will fetch SGI and use SGI flow for users to add user generated items to their wardrobe.                                                                                                                       | No. By default, ShowSGI is set to false                                                                                |
| setAllowedLanguages  | A list of `VirtusizeLanguage`     | In Kotlin, setAllowedLanguages(mutableListOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))<br />In Java, setAllowedLanguages(Arrays.asList(VirtusizeLanguage.EN, VirtusizeLanguage.JP))                                                         | The languages which the user can switch to using the Language Selector                                                                                                                                                                            | No. By default, the integration allows all possible languages to be displayed, including English, Japanese and Korean. |
| setDetailsPanelCards | A list of `VirtusizeInfoCategory` | In Kotlin, setDetailsPanelCards(mutableListOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))<br />In Java, setDetailsPanelCards(Arrays.asList(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT)) | The info categories which will be display in the Product Details tab. Possible categories are: `VirtusizeInfoCategory.MODEL_INFO`, `VirtusizeInfoCategory.GENERAL_FIT`, `VirtusizeInfoCategory.BRAND_SIZING` and `VirtusizeInfoCategory.MATERIAL` | No. By default, the integration displays all the possible info categories in the Product Details tab.                  |
| setShowSNSButtons    | Boolean                           | setShowSNSButtons(true)                                                                                                                                                                                                                        | Determines whether the integration will show the SNS buttons to the users                                                                                                                                                                         | No. By default, the integration enables the SNS buttons                                                               |
| setBranch     | String                            | setBranch("branch-name")                                                                                                                                                                                                                | Targets specific environment branch                                                                                                                                                                                         | No. By default, production environment is targeted. `staging` targets staging environment. `<branch-name>` targets a specific branch                                                                     |

- Kotlin

  ```kotlin
  override fun onCreate() {
      super.onCreate()
  
      // Initialize Virtusize instance for your application
      VirtusizeBuilder().init(this)
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
      .setAllowedLanguages(listOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
      // By default, Virtusize displays all the possible info categories in the Product Details tab
      .setDetailsPanelCards(setOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
      // By default, Virtusize enables the SNS buttons
      .setShowSNSButtons(true)
      // Target the specific branch environment by its name
      .setBranch("branch-name")  
      .build()
  }
  ```

- Java

  ```java
  Virtusize virtusize;
  
  @Override
  public void onCreate() {
      super.onCreate();
  
      // Initialize Virtusize instance for your application
      virtusize = new VirtusizeBuilder().init(this)
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
        .setDetailsPanelCards(Set.of(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
        // By default, Virtusize enables the SNS buttons
        .setShowSNSButtons(true)
        // Target the specific environment branch by its name
        .setBranch("branch-name")    
        .build();
  }
  ```

### 2. Load Product with Virtusize SDK

1. Inside your activity,

    - Create a `VirtusizeProduct` object with:

- An `exernalId` that will be used to reference the product in the Virtusize server
    - An `imageURL` for the product image
- Pass the `VirtusizeProduct` object to the `Virtusize#load` function

Kotlin

   ```kotlin
   val product = VirtusizeProduct(
    // Set the product's external ID
    externalId = "vs_dress",
    // Set the product image URL
    imageUrl = "http://www.image.com/goods/12345.jpg"
   )

   Virtusize.getInstance().load(product)
   ```

Java

   ```java
   VirtusizeProduct product = new VirtusizeProduct(
        "vs_dress",
        "http://www.image.com/goods/12345.jpg"
   );
   
   app.virtusize.load(product);
   ```

### 3. Enable SNS authentication

The SNS authentication flow requires opening a Chrome Custom Tab, which will load a web page for the
user to login with their SNS account. A custom URL scheme must be defined to return the login
response to your app from a Chrome Custom Tab.

Edit your `AndroidManifest.xml` file to include an intent filter and a `<data>` tag for the custom
URL scheme.

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.your-company.your-app">

    <activity android:name="com.virtusize.android.auth.views.VitrusizeAuthActivity"
        android:launchMode="singleTask" android:exported="true">
        <intent-filter>
            <action android:name="android.intent.action.VIEW" />

            <category android:name="android.intent.category.DEFAULT" />
            <category android:name="android.intent.category.BROWSABLE" />

            <data android:host="sns-auth" android:scheme="com.your-company.your-app.virtusize" />
        </intent-filter>
    </activity>

</manifest>
```

**❗IMPORTANT**

1. The URL host has to be `sns-auth`
2. The URL scheme must begin with your app's package ID (com.your-company.your-app) and **end with
   .virtusize**, and the scheme which you define must use all **lowercase** letters.
3. The underscores in your app's package ID must be replaced with hyphens. For example `com.your_company.your_app` must be changed to `com.your-company.your-app`.

### Enable Virtusize SNS Login for your WebView app

Use either of the following methods to enable Virtusize SNS login

### Method 1: Use the VirtusizeWebView

##### **Step 1: Replace your `WebView` with `VirtusizeWebView`**

To enable users to sign up or log in with the web version of Virtusize integration in your webview,
please replace your `WebView` with **`VirtusizeWebView`** in your Kotlin or Java file and XML file
to fix and enable SNS login in Virtusize.

- Kotlin/Java

  ```diff
  // Kotlin
  - var webView: WebView
  + var webView: VirtusizeWebView
  
  // Java
  - WebView webView;
  + VirtusizeWebView webView;
  ```

and

- XML

  ```diff
  - <WebView
  + <com.virtusize.libsource.VirtusizeWebView
      android:id="@+id/webView"
      android:layout_width="match_parent"
      android:layout_height="match_parent" />
  ```

##### Step 2: Set the Virtusize SNS auth activity result launcher to `VirtusizeWebView`

- Kotlin

  ```kotlin
  // Register the Virtusize SNS auth activity result launcher
  private val virtusizeSNSAuthLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
      // Handle the SNS auth result of the VirtusizeAuthActivity by passing the webview and the result to the `VirtusizeAuth.handleVirtusizeSNSAuthResult` function                                                                                                
      VirtusizeAuth.handleVirtusizeSNSAuthResult(webView, result.resultCode, result.data)
  }
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      // ... other code
    
      // Set the activity result launcher to the webView
      webView.setVirtusizeSNSAuthLauncher(virtusizeSNSAuthLauncher)
  }
  ```

- Java

  ```java
  // Register the Virtusize SNS auth activity result launcher
  private ActivityResultLauncher<Intent> mLauncher = 
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(), 
          (ActivityResultCallback<ActivityResult>) result ->
              VirtusizeAuth.INSTANCE.handleVirtusizeSNSAuthResult(webView, result.getResultCode(), result.getData())
  );
  
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      // ... other code
    
      // Set the activity result launcher to the webView
      webView.setVirtusizeSNSAuthLauncher(virtusizeSNSAuthLauncher)
  }
  ```

### or

### Method 2: Use WebView

##### Step 1: Make sure your WebView enables the following settings:

```kotlin
webView.settings.javaScriptEnabled = true
webView.settings.domStorageEnabled = true
webView.settings.databaseEnabled = true
webView.settings.setSupportMultipleWindows(true)
```

##### Step 2: Add the following code

```kotlin
// Register the Virtusize SNS auth activity result launcher
private val virtusizeSNSAuthLauncher =
    registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        VirtusizeAuth.handleVirtusizeSNSAuthResult(webView, result.resultCode, result.data)
    }

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            // Enable SNS buttons in Virtusize
            webView.evaluateJavascript("javascript:window.virtusizeSNSEnabled = true;", null)

            // The rest of your code ..... 
        }
    }

    webView.webChromeClient = object : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView,
            dialog: Boolean,
            userGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            // Obtain the popup window link or link title
            val message = view.handler.obtainMessage()
            view.requestFocusNodeHref(message)
            val url = message.data.getString("url")
            val title = message.data.getString("title")
            if (resultMsg.obj != null && resultMsg.obj is WebView.WebViewTransport && VirtusizeURLCheck.isLinkFromVirtusize(url, title)) {
                val popupWebView = WebView(view.context)
                popupWebView.settings.javaScriptEnabled = true
                popupWebView.webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
			if (VirtusizeURLCheck.isExternalLinkFromVirtusize(url)) {
                            runCatching {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(intent)
                                return true
                            }
                        }
                        return VirtusizeAuth.isSNSAuthUrl(context, virtusizeSNSAuthLauncher, url)
                    }
                }
                popupWebView.webChromeClient = object : WebChromeClient() {
                    override fun onCloseWindow(window: WebView) {
                        webView.removeAllViews()
                    }
                }
                val transport = resultMsg.obj as WebView.WebViewTransport
                view.addView(popupWebView)
                transport.webView = popupWebView
                resultMsg.sendToTarget()
                return true
            }

            // The rest of your code ..... 

            return super.onCreateWindow(view, dialog, userGesture, resultMsg)
        }
    }
}
```

### 4. Register Virtusize Message Handler (Optional)

Please do not forget to unregister message handler in activity or fragment's lifecycle method before
it dies or is removed. See the next section for a how-to.

- Kotlin

  ```kotlin
  private val activityMessageHandler = object : VirtusizeMessageHandler {
      override fun onEvent(product: VirtusizeProduct, event: VirtusizeEvent) {
          Log.i(TAG, event.name)
      }

      override fun onError(error: VirtusizeError) {
          Log.e(TAG, error.message)
      }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
      //...
      // Register message handler to listen to events from Virtusize
      Virtusize.getInstance().registerMessageHandler(activityMessageHandler)
      //...
  }
  ```

- Java

  ```java
  VirtusizeMessageHandler virtusizeMessageHandler;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      //...
      App app = (App) getApplication();
  
      virtusizeMessageHandler = new VirtusizeMessageHandler() {
          @Override
          public void onEvent(@NotNull VirtusizeProduct product, @NotNull VirtusizeEvent event) {
              Log.i(TAG, event.getName());
          }
  
          @Override
          public void onError(@NonNull VirtusizeError error) {
              Log.e(TAG, error.getMessage());
          }
      }
      app.virtusize.registerMessageHandler(virtusizeMessageHandler);
      //...
  }
  ```

### 5. Unregister Virtusize Message Handler (Optional)

A message handler is tied to an activity or fragment's lifecycle, but the Virtusize library object
is tied to the application's lifecycle. So if you forget to unregister message handler, then it will
keep listening to events even after activity is dead or fragment has been removed. In the case of an
activity; depending on where in the lifecycle you registered the message handler, you may need to
unregister it in your `onPause` or `onStop` method before the super method is called. Follow the
same guidelines in the case of fragment as well.

- Kotlin

  ```kotlin
  private val activityMessageHandler: VirtusizeMessageHandler
  override fun onPause() {
      // Always unregister message handler in onPause() or depending on implementation onStop().
      Virtusize.getInstance().unregisterMessageHandler(activityMessageHandler)
      super.onPause()
  }
  ```

- Java

  ```java
  VirtusizeMessageHandler virtusizeMessageHandler;
  @Override
  protected void onPause() {
      app.virtusize.unregisterMessageHandler(virtusizeMessageHandler);
      super.onPause();
  }
  ```

### 6. Configure custom branch for WebView-native apps (Optional)

You can test custom environment branch by updating the URL loaded by a WebView:

- Kotlin

  ```kotlin
  override fun onCreate() {
      // ...
      val uri = Uri.parse("https://demo.virtusize.com")
                   .withBranch("staging")
      webView.loadUrl(uri.toString())
  }
  ```

- Java

  ```java
  @Override
  protected void onCreate() {
      // ...
      Uri uri = Uri.parse("https://demo.virtusize.com")
                  .withBranch("staging");
      webView.loadUrl(uri.toString());
  }
  ```

## Virtusize Views

After setting up the SDK, add a `VirtusizeView` to allow your customers to find their ideal size.

Virtusize SDK provides two main UI components for clients to use:

### 1. Virtusize Button

#### (1) Introduction

VirtusizeButton is the simplest UI Button for our SDK. It opens our application in the web view to
support customers finding the right size.

#### (2) Default Styles

There are two default styles of the Virtusize Button in our Virtusize SDK.

|                                                    Teal Theme                                                     |                                                    Black Theme                                                    |
|:-----------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|
| <img src="https://user-images.githubusercontent.com/7802052/92671785-22817a00-f352-11ea-8ce9-6b4f7fcb43c4.png" /> | <img src="https://user-images.githubusercontent.com/7802052/92671771-172e4e80-f352-11ea-8443-dcb8b05f5a07.png" /> |

If you like, you can also customize the button style.

#### (3) Usage

**A. Add a VirtusizeButton in your activity's XML layout file.**

In order to use our default button styles, set `app:virtusizeButtonStyle="virtusize_black"`
or `app:virtusizeButtonStyle="virtusize_teal"` in XML:

- XML

  ```xml
  <com.virtusize.android.ui.VirtusizeButton
      android:id="@+id/exampleVirtusizeButton"
      app:virtusizeButtonStyle="virtusize_black"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content" />
  ```

or programmatically:

- Kotlin

  ```kotlin
  exampleVirtusizeButton.virtusizeViewStyle = VirtusizeViewStyle.BLACK
  ```

- Java

  ```java
  virtusizeButton.setVirtusizeViewStyle(VirtusizeViewStyle.TEAL);
  ```

**B. You can also use any other button styles and/or define the button's attributes like text,
height, width, etc.**

```xml

<com.virtusize.android.ui.VirtusizeButton android:id="@+id/exampleVirtusizeButton"
    style="@style/Widget.AppCompat.Button.Colored" android:layout_width="wrap_content"
    android:layout_height="wrap_content" android:text="@string/virtusize_button_text" />
```

**C. Connect the Virtusize button, along with the** `VirtusizeProduct` **object (which you have
passed to ** `Virtusize#load`) **into the Virtusize API by using the
** `Virtusize#setupVirtusizeView` **function in your activity.**

- Kotlin

  ```kotlin
  Virtusize.getInstance().setupVirtusizeView(exampleVirtusizeButton, product)
  ```

- Java

  ```java
  app.virtusize.setupVirtusizeView(virtusizeButton, product);
  ```

### 2. Virtusize InPage

#### (1) Introduction

Virtusize InPage is a button that behaves like a start button for our service. The button also
behaves as a fitting guide that supports people to find the right size.

##### InPage types

There are two types of InPage in our Virtusize SDK.

|                                                    InPage Standard                                                     |                                                    InPage Mini                                                     |
|:----------------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------:|
| ![InPageStandard](https://user-images.githubusercontent.com/7802052/92671977-9cb1fe80-f352-11ea-803b-5e3cb3469be4.png) | ![InPageMini](https://user-images.githubusercontent.com/7802052/92671979-9e7bc200-f352-11ea-8594-ed441649855c.png) |

⚠️**Caution**⚠️

1. InPage cannot be implemented together with the Virtusize button. Please pick either InPage or
   Virtusize button for your online shop.
2. InPage Mini must always be used in combination with InPage Standard.

#### (2) InPage Standard

##### A. Usage

- **Add a VirtusizeInPageStand in your activity's XML layout file.**

    1. In order to use our default styles, set `app:virtusizeInPageStandardStyle="virtusize_black"`
       or `app:virtusizeInPageStandardStyle="virtusize_teal"`

    2. If you'd like to change the background color of the CTA button, you can
       use `app:inPageStandardButtonBackgroundColor="#123456"`

    3. If you'd like to set the horizontal margins between the edges of the app screen and the
       InPageStandard, you can use `app:inPageStandardHorizontalMargin="16dp"`

    4. If you'd like to change the font sizes of the InPageStandard, you can
       use `app:inPageStandardMessageTextSize="10sp"` and `app:inPageStandardButtonTextSize="10sp"`.

    - XML

      ```xml
      <com.virtusize.android.ui.VirtusizeInPageStandard
          android:id="@+id/exampleVirtusizeInPageStandard"
          app:virtusizeInPageStandardStyle="virtusize_black"
          app:inPageStandardHorizontalMargin="16dp"
          app:inPageStandardMessageTextSize="10sp"
          app:inPageStandardButtonTextSize="10sp"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content" />
      ```

      ```xml
      <com.virtusize.android.ui.VirtusizeInPageStandard
          android:id="@+id/exampleVirtusizeInPageStandard"
          app:inPageStandardButtonBackgroundColor="#123456"
          android:layout_width="300dp"
          android:layout_height="wrap_content" />
      ```

      or programmatically:

    - Kotlin

      ```kotlin
      // Set the Virtusize view style
      exampleVirtusizeInPageStandard.virtusizeViewStyle = VirtusizeViewStyle.TEAL
      // Set the horizontal margins between the edges of the app screen and the InPageStandard
      // Note: Use the helper extension function `dpInPx` if you like
      exampleVirtusizeInPageStandard.horizontalMargin = 16.dpInPx
      // Set the background color of the check size button in InPage Standard
      exampleVirtusizeInPageStandard.setButtonBackgroundColor(ContextCompat.getColor(this, R.color.your_custom_color))
      // Set the text sizes of the InPage message and the check size button
      exampleVirtusizeInPageStandard.messageTextSize = 10f.spToPx
      exampleVirtusizeInPageStandard.buttonTextSize = 10f.spToPx
      ```

    - Java

      ```java
      virtusizeInPageStandard.setVirtusizeViewStyle(VirtusizeViewStyle.BLACK);
      virtusizeInPageStandard.setHorizontalMargin(ExtensionsKt.getDpInPx(16));
      virtusizeInPageStandard.setButtonBackgroundColor(ContextCompat.getColor(this, R.color.your_custom_color));
      virtusizeInPageStandard.setMessageTextSize(ExtensionsKt.getSpToPx(10));
      virtusizeInPageStandard.setButtonTextSize(ExtensionsKt.getSpToPx(10));
      ```

- **Connect the InPage Standard, along with the** `VirtusizeProduct` **object (which you have passed
  to ** `Virtusize#load`) **into the Virtusize API by using the** `Virtusize#setupVirtusizeView` *
  *function in your activity.**

    - Kotlin

      ```kotlin
      Virtusize.getInstance().setupVirtusizeView(exampleVirtusizeInPageStandard, product)
      ```

    - Java

      ```java
      app.virtusize.setupVirtusizeView(virtusizeInPageStandard, product);
      ```

##### B. Design Guidelines

- ##### Default Designs

  There are two default design variations.

  |                          Teal Theme                          |                         Black Theme                          |
  | :----------------------------------------------------------: | :----------------------------------------------------------: |
  | ![InPageStandardTeal](https://user-images.githubusercontent.com/7802052/92672035-b9e6cd00-f352-11ea-9e9e-5385a19e96da.png) | ![InPageStandardBlack](https://user-images.githubusercontent.com/7802052/92672031-b81d0980-f352-11ea-8b7a-564dd6c2a7f1.png) |

- ##### Layout Variations

  Here are some possible layouts

  |               1 thumbnail + 2 lines of message               |              2 thumbnails + 2 lines of message               |
  | :----------------------------------------------------------: | :----------------------------------------------------------: |
  | ![1 thumbnail + 2 lines of message](https://user-images.githubusercontent.com/7802052/97399368-5e879300-1930-11eb-8b77-b49e06813550.png) | ![2 thumbnails + 2 lines of message](https://user-images.githubusercontent.com/7802052/97399370-5f202980-1930-11eb-9a2d-7b71714aa7b4.png) |
  |             **1 thumbnail + 1 line of message**              |        **2 animated thumbnails + 2 lines of message**        |
  | ![1 thumbnail + 1 line of message](https://user-images.githubusercontent.com/7802052/97399373-5f202980-1930-11eb-81fe-9946b656eb4c.png) | ![2 animated thumbnails + 2 lines of message](https://user-images.githubusercontent.com/7802052/97399355-59c2df00-1930-11eb-8a52-292956b8762d.gif) |

- ##### Recommended Placement

    - Near the size table

    - In the size info section

  <img src="https://user-images.githubusercontent.com/7802052/92672185-15b15600-f353-11ea-921d-397f207cf616.png" style="zoom:50%;" />

- ##### UI customization

    - **You can:**
        - change the background color of the CTA button as long as it passes *
          *[WebAIM contrast test](https://webaim.org/resources/contrastchecker/)**.
        - change the width of InPage so it fits your application width.

    - **You cannot:**
        - change interface components such as shapes and spacing.
        - change the font.
        - change the CTA button shape.
        - change messages.
        - change or hide the box shadow.
        - hide the footer that contains VIRTUSIZE logo and Privacy Policy text link.

#### (3) InPage Mini

This is a mini version of InPage you can place in your application. The discreet design is suitable
for layouts where customers are browsing product images and size tables.

##### A. Usage

- **Add a VirtusizeInPageMini in your activity's XML layout file.**

    1. In order to use our default styles, set `app:virtusizeInPageMiniStyle="virtusize_black"`
       or `app:virtusizeInPageMiniStyle="virtusize_teal"`

    2. If you'd like to change the background color of the bar, you can
       use `app:inPageMiniBackgroundColor="#123456"`

    3. If you'd like to change the font sizes, you can use `app:inPageMiniMessageTextSize="12sp"`
       and `app:inPageMiniButtonTextSize="10sp"`.

    - XML

      ```xml
      <com.virtusize.android.ui.VirtusizeInPageMini
          android:id="@+id/exampleVirtusizeInPageMini"
          app:virtusizeInPageMiniStyle="virtusize_teal"                                                         
          app:inPageMiniMessageTextSize="12sp"
          app:inPageMiniButtonTextSize="10sp"
          android:layout_width="wrap_content"
          android:layout_height="wrap_content" />
      ```

      ```xml
      <com.virtusize.android.ui.VirtusizeInPageMini
          android:id="@+id/exampleVirtusizeInPageMini"
          app:inPageMiniBackgroundColor="#123456"
          android:layout_width="300dp"
          android:layout_height="wrap_content" />
      ```

      or programmatically:

    - Kotlin

      ```kotlin
      // Set the Virtusize view style
      exampleVirtusizeInPageMini.virtusizeViewStyle = VirtusizeViewStyle.BLACK
      // Set the background color of the InPageMini view
      exampleVirtusizeInPageMini.setInPageMiniBackgroundColor(ContextCompat.getColor(this, R.color.your_custom_color))
      // Set the text sizes of the InPage message and the check size button
      exampleVirtusizeInPageMini.messageTextSize = 12f.spToPx
      exampleVirtusizeInPageMini.buttonTextSize = 10f.spToPx
      ```

    - Java

      ```java
      virtusizeInPageMini.setVirtusizeViewStyle(VirtusizeViewStyle.TEAL);
      virtusizeInPageMini.setInPageMiniBackgroundColor(ContextCompat.getColor(this, R.color.your_custom_color));
      virtusizeInPageMini.setMessageTextSize(ExtensionsKt.getSpToPx(12));
      virtusizeInPageMini.setButtonTextSize(ExtensionsKt.getSpToPx(10));
      ```

- **Connect the InPage Mini, along with the** `VirtusizeProduct` **object (which you have passed
  to ** `Virtusize#load`) **into the Virtusize API by using the** `Virtusize#setupVirtusizeView` *
  *function in your activity.**

    - Kotlin

      ```kotlin
      Virtusize.getInstance().setupVirtusizeView(exampleVirtusizeInPageMini, product)
      ```

    - Java

      ```java
      app.virtusize.setupVirtusizeView(virtusizeInPageMini, product);
      ```

##### B. Design Guidelines

- ##### Default designs

  There are two default design variations.

  |                          Teal Theme                          |                         Black Theme                          |
  | :----------------------------------------------------------: | :----------------------------------------------------------: |
  | ![InPageMiniTeal](https://user-images.githubusercontent.com/7802052/92672234-2d88da00-f353-11ea-99d9-b9e9b6aa5620.png) | ![InPageMiniBlack](https://user-images.githubusercontent.com/7802052/92672232-2c57ad00-f353-11ea-80f6-55a9c72fb0b5.png) |

- ##### Recommended Placements

  |                 Underneath the product image                 |              Underneath or near the size table               |
  | :----------------------------------------------------------: | :----------------------------------------------------------: |
  | <img src="https://user-images.githubusercontent.com/7802052/92672261-3c6f8c80-f353-11ea-995c-ede56e0aacc3.png" /> | <img src="https://user-images.githubusercontent.com/7802052/92672266-40031380-f353-11ea-8f63-a67c9cf46c68.png" /> |

- ##### Default Fonts

    - Japanese
        - Noto Sans JP
        - 12sp (Message)
        - 10sp (Button)
    - Korean
        - Noto Sans KR
        - 12sp (Message)
        - 10sp (Button)
    - English
        - Roboto
        - 14sp (Message)
        - 12sp (Button)

- ##### UI customization

    - **You can:**
        - change the background color of the bar as long as it passes *
          *[WebAIM contrast test](https://webaim.org/resources/contrastchecker/)**.
    - **You cannot:**
        - change the font.
        - change the CTA button shape.
        - change messages.

## The Order API

The order API enables Virtusize to show your customers the items they have recently purchased as
part of their `Purchase History`, and to use those items to compare with new items they want to buy.

#### 1. Initialization

Make sure to set up the **user ID** before sending orders to Virtusize. You can set up the user ID
either:

in your Application class's `onCreate` method before the app is launched

or

in your activity or fragment after the app is launched

* Kotlin

    ```kotlin
    // In your Application class's `onCreate` method before the app is launched
    override fun onCreate() {
        super.onCreate()
        VirtusizeBuilder().init(this)
            .setApiKey(api_key)
            .setUserId(user_id)
            .setEnv(VirtusizeEnvironment.STAGING)
            .build()
    }
    
    // In your activity or fragment after the app is launched
    Virtusize.getInstance().setUserID("user_id")
    ```
* Java

    ~~~~java
    // In your Application class's `onCreate` method before the app is launched 
    Virtusize virtusize;
    @Override
    public void onCreate() {
    super.onCreate();
    virtusize = new VirtusizeBuilder()
            .init(this)
            .setApiKey(api_key)
            .setUserId(user_id)
            .setEnv(VirtusizeEnvironment.STAGING)
            .build();
      
    // In your activity or fragment after the app is launched
    app = (App) getApplication();
    app.virtusize.setUserId("user_id");
    ~~~~

#### 2. Create a *VirtusizeOrder* object for order data

The ***VirtusizeOrder*** object gets passed to the `Virtusize#sendOrder` method, and has the
following attributes:

__**Note:**__ * means the attribute is required

**VirtusizeOrder**

| Attribute        | Data Type                              | Example             | Description                         |
|------------------|----------------------------------------|---------------------|-------------------------------------|
| externalOrderId* | String                                 | "20200601586"       | The order ID provided by the client |
| items*           | A list of `VirtusizeOrderItem` objects | See the table below | A list of the order items.          |

**VirtusizeOrderItem**

| Attribute  | Data Type | Example                                  | Description                                                                                    |
|------------|-----------|------------------------------------------|------------------------------------------------------------------------------------------------|
| productId* | String    | "A001"                                   | The external product ID provided by the client. It must be unique for each product.            |
| size*      | String    | "S", "M", etc.                           | The name of the size                                                                           |
| sizeAlias  | String    | "Small", "Large", etc.                   | The alias of the size is added if the size name is not identical from the product page         |
| variantId  | String    | "A001_SIZES_RED"                         | An ID that is set on the product SKU, color, or size if there are several options for the item |
| imageUrl*  | String    | "http[]()://images.example.com/coat.jpg" | The image URL of the item                                                                      |
| color      | String    | "RED", "R', etc.                         | The color of the item                                                                          |
| gender     | String    | "W", "Women", etc.                       | An identifier for the gender                                                                   |
| unitPrice* | Double    | 5100.00                                  | The product price that is a double number with a maximum of 12 digits and 2 decimals (12, 2)   |
| currency*  | String    | "JPY", "KRW", "USD", etc.                | Currency code                                                                                  |
| quantity*  | Int       | 1                                        | The number of items purchased. If it's not passed, It will be set to 1                         |
| url        | String    | "http[]()://example.com/products/A001"   | The URL of the product page. Please make sure this is a URL that users can access              |

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
    items.add(
        new VirtusizeOrderItem(
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
    );
    ~~~~

#### 3. Send an Order

Call the `Virtusize#sendOrder` method in your activity or fragment when the user places an order.

* Kotlin

  The `onSuccess` and `onError` callbacks are optional.

    ~~~~kotlin
    Virtusize.getInstance().sendOrder(
        order,
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
    app.virtusize.sendOrder(
        order,
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

## Enable SNS Login in Virtusize for native WebView apps

Use the [Virtusize Auth SDK](https://github.com/virtusize/virtusize_auth_android)

## Examples

1. Kotlin example https://github.com/virtusize/integration_android/tree/master/sampleAppKotlin
2. Java example https://github.com/virtusize/integration_android/tree/master/sampleAppJava

## Contributing

### Linter

We use [ktlint](https://github.com/pinterest/ktlint?tab=readme-ov-file) for formatting. Run linter check or format when needed:
```sh
./gradlew ktlint
./gradlew ktlintFormat
```

### Git Hooks

Ensure to setup the `pre-push` git hooks after cloning the repo.  
Git hook will run `ktlint`, validate fonts and run tests on every push automatically.
```sh
./gradlew installGitHooks
```

### Fonts & Localisation

We use subset fonts to reduce the overall SDK size.  
The subset glyphs are limited to the characters used in the localization files.

Whenever you update the localization files, ensure to regenerate the subset fonts of the SDK.
```sh
# Ensure to install FontTools
pip install --upgrade fonttools

sh ./scripts/build_fonts.sh
```


## License

Copyright (c) 2018-24 Virtusize CO LTD (https://www.virtusize.jp)
