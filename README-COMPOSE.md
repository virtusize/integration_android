# Virtusize Android Compose Integration

[![](https://jitpack.io/v/virtusize/integration_android.svg)](https://jitpack.io/#virtusize/integration_android) [![](https://img.shields.io/maven-central/v/com.virtusize.android/virtusize)](https://search.maven.org/search?q=g:%22com.virtusize.android%22%20AND%20a:%22virtusize%22)

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
    - [Enable SNS Authentication](#2-enable-sns-authentication)
    - [Register Virtusize Message Handler (Optional)](#3-register-virtusize-message-handler-optional)
    - [Unregister Virtusize Message Handler (Optional)](#4-unregister-virtusize-message-handler-optional)

- [Virtusize UI Components](#virtusize-ui-components)

    - [Virtusize Button](#1-virtusize-button)
    - [Virtusize InPage](#2-virtusize-inpage)
        - [InPage Standard](#2-inpage-standard)
        - [InPage Mini](#3-inpage-mini)

- [The Order API](#the-order-api)

    - [Initialization](#1-initialization)
    - [Create a *VirtusizeOrder* object for order data](#2-create-a-virtusizeorder-object-for-order-data)
    - [Send an Order](#3-send-an-order)

- [Enable SNS Login in Virtusize for native Webview apps](#enable-sns-login-in-virtusize-for-native-webview-apps)

- [Examples](#examples)

- [License](#license)


## Requirements

- minSdkVersion >= 21
- compileSdkVersion >= 34
- Setup in Jetpack Compose


## Getting Started

### 1. Installation

In your app `build.gradle` file, add the following dependencies:

- Groovy (build.gradle)

  ```groovy
  dependencies {
    implementation 'com.virtusize.android:virtusize:2.6.2'
  }
  ```

- Kotlin (build.gradle.kts)

  ```kotlin
  dependencies {
    implementation("com.virtusize.android:virtusize:2.6.2")
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
  .setAllowedLanguages(mutableListOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
  // By default, Virtusize displays all the possible info categories in the Product Details tab
  .setDetailsPanelCards(mutableListOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
  .build()
}
```

### 2. Enable SNS authentication

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

### 3. Register Virtusize Message Handler (Optional)

Please do not forget to unregister message handler in the activity lifecycle method before
it dies or is removed. See the next section for a how-to.

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

### 4. Unregister Virtusize Message Handler (Optional)

A message handler is tied to an activity lifecycle, but the Virtusize library object
is tied to the application's lifecycle. So if you forget to unregister message handler, then it will
keep listening to events even after activity is dead. In the case of an
activity; depending on where in the lifecycle you registered the message handler, you may need to
unregister it in your `onPause` or `onStop` method before the super method is called.

```kotlin
private val activityMessageHandler: VirtusizeMessageHandler
override fun onPause() {
  // Always unregister message handler in onPause() or depending on implementation onStop().
  Virtusize.getInstance().unregisterMessageHandler(activityMessageHandler)
  super.onPause()
}
```


## Virtusize UI Components

After setting up the SDK, add a Virtusize UI component to allow your customers to find their ideal size.

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

#### (3) Usage

**A. Prepare the product for the VirtusizeButton**

1. Create a `VirtusizeProduct` object with:

    - An `exernalId` that will be used to reference the product in the Virtusize server
    - An `imageURL` for the product image

    ```kotlin
    val product = VirtusizeProduct(
        // Set the product's external ID
        externalId = "vs_dress",
        // Set the product image URL
        imageUrl = "http://www.image.com/goods/12345.jpg"
    )
    ```

**B. Add a VirtusizeButton**

```kotlin
VirtusizeButton(
    // Set the product for the VirtusizeButton
    product = product,
    // Adjust the component layout (optional)
    modifier = Modifier.align(Alignment.CenterHorizontally),
    // Set the Virtusize button colors. (optional)
    // The default colors are VirtusizeColors.teal() and VirtusizeColors.black().
    colors = VirtusizeButtonDefaults.colors(
        containerColor = VirtusizeColors.Teal,
        contentColor = VirtusizeColors.White,
    ),
    // Receive the Virtusize button events (optional)
    onEvent = { event ->
        Log.i(VIRTUSIZE_BUTTON_TAG, event.name)
    },
    // Receive the Virtusize button errors (optional)
    onError = { error ->
        Log.e(VIRTUSIZE_BUTTON_TAG, error.message)
    },
)
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

- **Prepare the product for the VirtusizeInPageStandard**

  1. Create a `VirtusizeProduct` object with:

      - An `exernalId` that will be used to reference the product in the Virtusize server
      - An `imageURL` for the product image

      ```kotlin
      val product = VirtusizeProduct(
          // Set the product's external ID
          externalId = "vs_dress",
          // Set the product image URL
          imageUrl = "http://www.image.com/goods/12345.jpg"
      )
      ```

- **Add a VirtusizeInPageStandard**

    ```kotlin
    VirtusizeInPageStandard(
        // Set the product for the VirtusizeInPageStandard
        product = product,
        // Adjust the component layout (optional)
        modifier = Modifier.padding(horizontal = 16.dp),
        // Adjust the background color of the InPageStandard (optional)
        backgroundColor = VirtusizeColors.Black,
        // Receive the VirtusizeInPageStandard events (optional)
        onEvent = { event ->
            Log.i(VIRTUSIZE_INPAGE_STANDARD_TAG, event.name)
        },
        // Receive the VirtusizeInPageStandard errors (optional)
        onError = { error ->
            Log.e(VIRTUSIZE_INPAGE_STANDARD_TAG, error.message)
        },
    )
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
        - change the background color of the CTA button as long as it passes **[WebAIM contrast test](https://webaim.org/resources/contrastchecker/)**.
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

- **Prepare the product for the VirtusizeInPageMini**

  1. Create a `VirtusizeProduct` object with:

      - An `exernalId` that will be used to reference the product in the Virtusize server
      - An `imageURL` for the product image

      ```kotlin
      val product = VirtusizeProduct(
          // Set the product's external ID
          externalId = "vs_dress",
          // Set the product image URL
          imageUrl = "http://www.image.com/goods/12345.jpg"
      )
      ```

- **Add a VirtusizeInPageMini**

    ```kotlin
    VirtusizeInPageMini(
        // Set the product for the VirtusizeInPageMini
        product = product,
        // Adjust the component layout (optional)
        modifier = Modifier.padding(horizontal = 16.dp),
        // Adjust the background color of the InPageMini (optional)
        backgroundColor = VirtusizeColors.Teal,
        // Receive the VirtusizeInPageMini events (optional)
        onEvent = { event ->
            Log.i(VIRTUSIZE_INPAGE_MINI_TAG, event.name)
        },
        // Receive the VirtusizeInPageMini errors (optional)
        onError = { error ->
            Log.e(VIRTUSIZE_INPAGE_MINI_TAG, error.message)
        },
    )
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
        - Noto Sans CJK JP
        - 12sp (Message)
        - 10sp (Button)
    - Korean
        - Noto Sans CJK KR
        - 12sp (Message)
        - 10sp (Button)
    - English
        - Roboto
        - 14sp (Message)
        - 12sp (Button)

- ##### UI customization

    - **You can:**
        - change the background color of the bar as long as it passes **[WebAIM contrast test](https://webaim.org/resources/contrastchecker/)**.
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

in your activity after the app is launched

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

// In your activity after the app is launched
Virtusize.getInstance().setUserID("user_id")
```

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

#### 3. Send an Order

Call the `Virtusize#sendOrder` method in your activity when the user places an order.

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

## Enable SNS Login in Virtusize for native WebView apps

Use the [Virtusize Auth SDK](https://github.com/virtusize/virtusize_auth_android)

## Examples

https://github.com/virtusize/integration_android/tree/master/sampleAppCompose

## License

Copyright (c) 2018-24 Virtusize CO LTD (https://www.virtusize.jp)