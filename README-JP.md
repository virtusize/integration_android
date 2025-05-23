# Android SDK 実装ガイド

[![](https://jitpack.io/v/virtusize/integration_android.svg)](https://jitpack.io/#virtusize/integration_android) [![](https://img.shields.io/maven-central/v/com.virtusize.android/virtusize)](https://search.maven.org/search?q=g:%22com.virtusize.android%22%20AND%20a:%22virtusize%22)

[English](README.md)

Virtusize helps retailers to illustrate the size and fit of clothing, shoes and bags online, by
letting customers compare the
measurements of an item they want to buy (on a retailer's product page) with an item that they
already own (a reference item).
This is done by comparing the silhouettes of the retailer's product with the silhouette of the
customer's reference Item.
Virtusize is a widget which opens when clicking on the Virtusize button, which is located next to
the size selection on the product page.

Read more about Virtusize at [https://www.virtusize.jp](https://www.virtusize.jp/)

You need a unique API key and an Admin account, only available to Virtusize
customers. [Contact our sales team](mailto:sales@virtusize.com) to become a customer.

> This is the integration script for native Android apps only. For web integration, refer to the
> developer documentation on https://developers.virtusize.com. For iOS integration, refer
> to https://github.com/virtusize/integration_ios

## Table of Contents

- [対応バージョン](#対応バージョン)

- [はじめに](#はじめに)

    - [Virtusize SDKを実装する](#1-virtusize-sdkを実装する)
    - [Proguardの設定](#2-proguardの設定)

- [セットアップ](#セットアップ)

    - [はじめに](#1-はじめに)
    - [Load Product with Virtusize SDK](#2-load-product-with-virtusize-sdk)
    - [Enable SNS Authentication](#3-enable-sns-authentication)
    - [Virtusize Message Handlerの登録（オプション）](#4-virtusize-message-handlerの登録オプション)
    - [Virtusize Message Handler登録解除（オプション）](#5-virtusize-message-handler登録解除オプション)

- [Virtusize Views](#virtusize-views)

    - [バーチャサイズボタン（Virtusize Button）](#1-バーチャサイズボタンvirtusize-button)
    - [バーチャサイズ・インページ（Virtusize InPage）](#2-バーチャサイズインページvirtusize-inpage)
        - [InPage Standard](#2-inpage-standard)
        - [InPage Mini](#3-inpage-mini)

- [Order APIについて](#order-apiについて)

    - [初期化](#1-初期化)
    - [注文データ向けに
      *VirtusizeOrder* オブジェクトを作成](#2-注文データ向けにvirtusizeorder-オブジェクトを作成)
    - [注文情報の送信](#3-注文情報の送信)

- [Enable SNS Login in Virtusize for native Webview apps](#enable-sns-login-in-virtusize-for-native-webview-apps)

- [Examples](#examples)

- [License](#license)

## 対応バージョン

- minSdkVersion >= 21
- compileSdkVersion >= 34
- Setup in AppCompatActivity

## はじめに

もし、1.x.xの古いバージョンを引き続きご利用いただく場合は、[v1](https://github.com/virtusize/integration_android/tree/v1)
を参照くださいませ。

### 1. Virtusize SDKを実装する

In your appの`build.gradle`ファイルに下記のdependencyを追加

- Groovy (build.gradle)

  ```groovy
  dependencies {
    implementation 'com.virtusize.android:virtusize:2.11.1'
  }
  ```

- Kotlin (build.gradle.kts)

  ```kotlin
  dependencies {
    implementation("com.virtusize.android:virtusize:2.11.1")
  }
  ```

### 2. Proguardの設定

Proguardをお使いの場合、Proguardのルールファイルに下記のルールを追加

```
-keep class com.virtusize.android.**
```

## セットアップ

### 1. はじめに

アプリケーションクラスの`onCreate`メソッドでVirtusizeオブジェクトを**VirtusizeBuilder**
を使って初期化し、設定を行います。可能な設定方法を以下の表に示します。

**VirtusizeBuilder**

| 項目                   | データ形式                         | 例                                                                                                                                                                                                                                              | 説明                                                                                                                                                                             | 要件                                                    |
|----------------------|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------------------------|
| setApiKey            | String                        | setApiKey("api_key")                                                                                                                                                                                                                           | 固有のAPIキーは各Virtusizeクライアントに提供されます。                                                                                                                                              | あり。                                                   |
| setUserId            | String                        | setUserId("123")                                                                                                                                                                                                                               | ユーザーがクライアントのアプリにログインしている場合に、クライアントから渡されます。                                                                                                                                     | あり。Order APIを使用する場合。                                  |
| setEnv               | VirtusizeEnvironment          | setEnv(VirtusizeEnvironment.STAGING)                                                                                                                                                                                                           | 環境は実装をしている環境を選択してください、`VirtusizeEnvironment.STAGING`,  `VirtusizeEnvironment.GLOBAL`, `VirtusizeEnvironment.JAPAN` or `VirtusizeEnvironment.KOREA`のいずれかです。                     | 特になし。デフォルトでは、`VirtusizeEnvironment.GLOBAL`に設定されます。    |
| setLanguage          | VirtusizeLanguage             | setLanguage(VirtusizeLanguage.EN)                                                                                                                                                                                                              | インテグレーションをロードする際の初期言語を設定します。設定可能な値は以下：`VirtusizeLanguage.EN`, `VirtusizeLanguage.JP` および`VirtusizeLanguage.KR`                                                                 | 特になし。デフォルトでは、初期言語はVirtusizeの環境に基づいて設定されます。            |
| setShowSGI           | Boolean                       | setShowSGI(true)                                                                                                                                                                                                                               | ユーザーが生成したアイテムをワードローブに追加するために、SGIを取得してSGIフローを使用するかどうかを決定します。                                                                                                                    | 特になし。デフォルトではShowSGIはfalseに設定されています。                   |
| setAllowedLanguages  | `VirtusizeLanguage`列挙のリスト     | In Kotlin, setAllowedLanguages(mutableListOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))<br />In Java, setAllowedLanguages(Arrays.asList(VirtusizeLanguage.EN, VirtusizeLanguage.JP))                                                         | ユーザーが言語選択ボタンより選択できる言語                                                                                                                                                          | 特になし。デフォルトでは、英語、日本語、韓国語など、表示可能なすべての言語が表示されるようになっています。 |
| setDetailsPanelCards | `VirtusizeInfoCategory`列挙のリスト | In Kotlin, setDetailsPanelCards(mutableListOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))<br />In Java, setDetailsPanelCards(Arrays.asList(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT)) | 商品詳細タブに表示する情報のカテゴリ。表示可能カテゴリは以下：`VirtusizeInfoCategory.MODELINFO`, `VirtusizeInfoCategory.GENERALFIT`, `VirtusizeInfoCategory.BRANDSIZING` および `VirtusizeInfoCategory.MATERIAL` | 特になし。デフォルトでは、商品詳細タブに表示可能なすべての情報カテゴリが表示されます。           |
| setShowSNSButtons | Boolean | setShowSNSButtons(true) | Determines whether the integration will show the SNS buttons to the users | No. By default, the integration disables the SNS buttons |

- Kotlin

  ```kotlin
  lateinit var virtusize: Virtusize
  override fun onCreate() {
      super.onCreate()
  
      // Initialize Virtusize instance for your application
      virtusize = VirtusizeBuilder().init(this)
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
      // By default, Virtusize disables the SNS buttons
      .setShowSNSButtons(false)
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
        .setDetailsPanelCards(Arrays.asList(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
        // By default, Virtusize disables the SNS buttons
        .setShowSNSButtons(false)
        .build();
  }
  ```

### 2. Load Product with Virtusize SDK

1. アクティビティ内では、

    - Create a `VirtusizeProduct` object with:

- An `exernalId` that will be used to reference the product in the Virtusize server
    - An `imageURL` for the product image

    - Pass the `VirtusizeProduct` object to the `Virtusize#load` function

Kotlin

   ```kotlin
val product = VirtusizeProduct(
    externalId = "vs_dress",
    imageUrl = "http://www.image.com/goods/12345.jpg"
)

(application as App)
    .virtusize
    .load(product)
   ```

Java

   ```java
VirtusizeProduct product = new VirtusizeProduct(
        "vs_dress",
        "http://www.image.com/goods/12345.jpg"
);
   
app.virtusize.

load(product);
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

### 4. Virtusize Message Handlerの登録（オプション）

アクティビティやフラグメントが終了したり削除されたりする前に、アクティビティ（activity）やフラグメント（fragment）のライフサイクル（lifecycle）・メソッドでメッセージ・ハンドラの登録を解除することを忘れないでください。方法については次のセクションを参照してください。

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
      (application as App).virtusize.registerMessageHandler(activityMessageHandler)
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

### 5. Virtusize Message Handler登録解除（オプション）

Message
Handlerはアクティビティ（activity）やフラグメント（fragment）のライフサイクル（lifecycle）に結びついていますが、Virtusizeライブラリオブジェクトはアプリケーションのライフサイクルに結びついています。そのため、Message
Handlerの登録解除を忘れると、アクティビティが終了したりフラグメントが削除されたりしても、イベントを聞き続けることになります。アクティビティの場合、ライフサイクルのどこでMessage
Handlerを登録したかによって、superメソッドが呼ばれる前に`onPause`または`onStop`
メソッドで登録を解除する必要があります。フラグメントの場合も、同様のガイドラインに従ってください。

- Kotlin

  ```kotlin
  private val activityMessageHandler: VirtusizeMessageHandler
  override fun onPause() {
          // Always unregister message handler in onPause() or depending on implementation onStop().
          (application as App).virtusize.unregisterMessageHandler(activityMessageHandler)
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

## Virtusize Views

SDKをセットアップした後、`VirtusizeView`を追加して、顧客が理想的なサイズを見つけられるようにします。Virtusize
SDKはユーザーが使用するために2つの主要なUIコンポーネントを提供します。:

### 1. バーチャサイズボタン（Virtusize Button）

#### (1) はじめに

VirtusizeButtonはこのSDKの中でもっとシンプルなUIのボタンです。ユーザーが正しいサイズを見つけられるように、ウェブビューでアプリケーションを開きます。

#### (2) デフォルトスタイル

SDKのVirtusizeボタンには2つのデフォルトスタイルがあります。

|                                                    Teal Theme                                                     |                                                    Black Theme                                                    |
|:-----------------------------------------------------------------------------------------------------------------:|:-----------------------------------------------------------------------------------------------------------------:|
| <img src="https://user-images.githubusercontent.com/7802052/92671785-22817a00-f352-11ea-8ce9-6b4f7fcb43c4.png" /> | <img src="https://user-images.githubusercontent.com/7802052/92671771-172e4e80-f352-11ea-8443-dcb8b05f5a07.png" /> |

もしご希望であれば、ボタンのスタイルもカスタマイズすることができます。

#### (3) 使用方法

**A. アクティビティのXMLレイアウトファイルにVirtusizeButtonを追加してください。**

私たちのデフォルトのボタンスタイルを使用するために、XMLで`app:virtusizeButtonStyle="virtusize_black "`
または`app:virtusizeButtonStyle="virtusize_teal "`を設定します。

- XML

  ```xml
  <com.virtusize.android.ui.VirtusizeButton
      android:id="@+id/exampleVirtusizeButton"
      app:virtusizeButtonStyle="virtusize_black"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content" />
  ```

もしくは、プログラムとして設定します。

- Kotlin

  ```kotlin
  exampleVirtusizeButton.virtusizeViewStyle = VirtusizeViewStyle.BLACK
  ```

- Java

  ```java
  virtusizeButton.setVirtusizeViewStyle(VirtusizeViewStyle.TEAL);
  ```

**B. また、他のボタンスタイルを使用したり、ボタンの属性（テキスト、高さ、幅など）を定義することもできます。
**

```xml

<com.virtusize.android.ui.VirtusizeButton android:id="@+id/exampleVirtusizeButton"
    style="@style/Widget.AppCompat.Button.Colored" android:layout_width="wrap_content"
    android:layout_height="wrap_content" android:text="@string/virtusize_button_text" />
```

**C. Connect the Virtusize button, along with the** `VirtusizeProduct` **object (which you have
passed to** `Virtusize#load`) **into the Virtusize API by using the** `Virtusize#setupVirtusizeView`
**function in your activity.**

- Kotlin

  ```kotlin
  (application as App).virtusize.setupVirtusizeView(exampleVirtusizeButton, product)
  ```

- Java

  ```java
  app.virtusize.setupVirtusizeView(virtusizeButton, product);
  ```

### 2. バーチャサイズ・インページ（Virtusize InPage）

#### (1) はじめに

Virtusize InPageは、私たちのサービスのスタートボタンのような役割を果たすボタンです。また、このボタンは、お客様が正しいサイズを見つけるためのフィッティングガイドとしても機能します。

##### **InPageの種類**

Virtusize SDKには2種類のInPageがあります。

|                                                    InPage Standard                                                     |                                                    InPage Mini                                                     |
|:----------------------------------------------------------------------------------------------------------------------:|:------------------------------------------------------------------------------------------------------------------:|
| ![InPageStandard](https://user-images.githubusercontent.com/7802052/92671977-9cb1fe80-f352-11ea-803b-5e3cb3469be4.png) | ![InPageMini](https://user-images.githubusercontent.com/7802052/92671979-9e7bc200-f352-11ea-8594-ed441649855c.png) |

⚠️**注意事項**⚠️

1. InPageはVirtusizeボタンと一緒に導入することはできません。オンラインショップでは、InPageかVirtusizeボタンのどちらかをお選びください。

2. InPage Miniは、必ずInPage Standardと組み合わせてご利用ください。

#### (2) InPage Standard

##### A. 使用方法

- **アクティビティのXMLレイアウトファイルにVirtusizeInPageStandを追加します。**

    1.
    私たちのデフォルトスタイルを使用するために、 `app:virtusizeInPageStandardStyle="virtusize_black"`
    または `app:virtusizeInPageStandardStyle="virtusize_teal"` を設定してください。

    2. CTAボタンの背景色を変更したい場合は、`app:inPageStandardButtonBackgroundColor="#123456 "`
       を使用できます。

    3.
    アプリ画面の端とInPageStandardの間の水平方向の余白を設定したい場合は、`app:inPageStandardHorizontalMargin="16dp"`
    とします。

    4. InPageStandardのフォントサイズを変更したい場合は、 `app:inPageStandardMessageTextSize="10sp"`
       と`app:inPageStandardButtonTextSize="10sp"`を利用できます。

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

      または、プログラムとして設定します。

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
  to** `Virtusize#load`) **into the Virtusize API by using the** `Virtusize#setupVirtusizeView` *
  *function in your activity.**

    - Kotlin

      ```kotlin
      (application as App)
          .virtusize
          .setupVirtusizeView(exampleVirtusizeInPageStandard, product)
      ```

    - Java

      ```java
      app.virtusize.setupVirtusizeView(virtusizeInPageStandard, product);
      ```

##### B. デザインガイドライン

- ##### デフォルトデザイン

  デフォルトデザインは2種類あります。

  |                          Teal Theme                          |                         Black Theme                          |
  | :----------------------------------------------------------: | :----------------------------------------------------------: |
  | ![InPageStandardTeal](https://user-images.githubusercontent.com/7802052/92672035-b9e6cd00-f352-11ea-9e9e-5385a19e96da.png) | ![InPageStandardBlack](https://user-images.githubusercontent.com/7802052/92672031-b81d0980-f352-11ea-8b7a-564dd6c2a7f1.png) |

- ##### レイアウトのバリエーション

  設定可能なレイアウト例

  |               1 thumbnail + 2 lines of message               |              2 thumbnails + 2 lines of message               |
  | :----------------------------------------------------------: | :----------------------------------------------------------: |
  | ![1 thumbnail + 2 lines of message](https://user-images.githubusercontent.com/7802052/97399368-5e879300-1930-11eb-8b77-b49e06813550.png) | ![2 thumbnails + 2 lines of message](https://user-images.githubusercontent.com/7802052/97399370-5f202980-1930-11eb-9a2d-7b71714aa7b4.png) |
  |             **1 thumbnail + 1 line of message**              |        **2 animated thumbnails + 2 lines of message**        |
  | ![1 thumbnail + 1 line of message](https://user-images.githubusercontent.com/7802052/97399373-5f202980-1930-11eb-81fe-9946b656eb4c.png) | ![2 animated thumbnails + 2 lines of message](https://user-images.githubusercontent.com/7802052/97399355-59c2df00-1930-11eb-8a52-292956b8762d.gif) |

- ##### 推奨設定箇所

    - サイズテーブルの近く

    - サイズ情報掲載箇所

  <img src="https://user-images.githubusercontent.com/7802052/92672185-15b15600-f353-11ea-921d-397f207cf616.png" style="zoom:50%;" />

- ##### UI カスタマイゼーション

    - **変更可:**
        - CTAボタンの背景色（[WebAIM contrast test](https://webaim.org/resources/contrastchecker/)
          で問題がなければ）
        - Inpageの横幅（アプリの横幅に合わせて変更可）

    - **変更不可**:
        - 形状やスペースなどのインターフェイスコンポーネント
        - フォント
        - CTA ボタンの形状
        - テキスト文言
        - ボタンシャドウ（削除も不可）
        - VIRTUSIZE ロゴと プライバシーポリシーのテキストが入ったフッター（削除も不可）

#####  

#### (3) InPage Mini

こちらは、InPageのミニバージョンで、アプリに配置することができます。目立たないデザインなので、お客様が商品画像やサイズ表を閲覧するようなレイアウトに適しています。

##### A. 使用方法

- **アクティビティのXMLレイアウトファイルにVirtusizeInPageMiniを追加します。**

    1. 私たちのデフォルトスタイルを使用するには、`app:virtusizeInPageMiniStyle="virtusize_black"`
       または `app:virtusizeInPageMiniStyle="virtusize_teal"` を設定してください。

    2. バーの背景色を変更したい場合は、`app:inPageMiniBackgroundColor="#123456"` とします。

    3. InPage Miniのフォントサイズを変更したい場合は、 `app:inPageMiniMessageTextSize="12sp"`
       と`app:inPageMiniButtonTextSize="10sp"`を利用できます。

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

      もしくは、プログラムとして設定します。

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

- **Connect the InPage Mini, along with the** `VirtusizeProduct` **object (which you have passed to
  ** `Virtusize#load`) **into the Virtusize API by using the** `Virtusize#setupVirtusizeView` *
  *function in your activity.**

    - Kotlin

      ```kotlin
      (application as App)
          .Virtusize
          .setupVirtusizeView(exampleVirtusizeInPageMini, product)
      ```

    - Java

      ```java
      app.vituriszesetupVirtusizeView(virtusizeInPageMini, product);
      ```

##### B. デザインガイドライン

- ##### デフォルト デザイン

  ２種類のでフォルトデザインを用意しています。

  |                          Teal Theme                          |                         Black Theme                          |
  | :----------------------------------------------------------: | :----------------------------------------------------------: |
  | ![InPageMiniTeal](https://user-images.githubusercontent.com/7802052/92672234-2d88da00-f353-11ea-99d9-b9e9b6aa5620.png) | ![InPageMiniBlack](https://user-images.githubusercontent.com/7802052/92672232-2c57ad00-f353-11ea-80f6-55a9c72fb0b5.png) |

- ##### 推奨設置箇所

  |                 Underneath the product image                 |              Underneath or near the size table               |
  | :----------------------------------------------------------: | :----------------------------------------------------------: |
  | <img src="https://user-images.githubusercontent.com/7802052/92672261-3c6f8c80-f353-11ea-995c-ede56e0aacc3.png" /> | <img src="https://user-images.githubusercontent.com/7802052/92672266-40031380-f353-11ea-8f63-a67c9cf46c68.png" /> |

- ##### デフォルトのフォント

    - **Japanese**
        - Noto Sans CJK JP
        - 12sp (メッセージ文言)
        - 10sp (ボタン内テキスト)
    - **Noto Sans CJK JP**
        - Noto Sans CJK KR
        - 12sp (メッセージ文言)
        - 10sp (ボタン内テキスト)
    - **Noto Sans CJK JP**
        - Roboto
        - 14sp (メッセージ文言)
        - 12sp (ボタン内テキスト)

- ##### UI カスタマイゼーション

    - **変更可**
        - CTAボタンの背景色（[WebAIM contrast test](https://webaim.org/resources/contrastchecker/)
          で問題がなければ）
    - **変更不可:**
        - フォント
        - CTA ボタンの形状
        - テキスト文言

## Order APIについて

The order APIはバーチャサイズがユーザーが購入した商品を`Purchase History`
（購入履歴）の一部として表示するために必要で、これらの商品がユーザーが購入検討している商品と比較可能になります。

#### 1. 初期化

Virtusizeにリクエストを送信する前に、**user ID**が設定されていることを確認してください。以下、どちらの方法でも
**user ID** を設定することが可能です。

​ - アプリローンチ前に、アプリ内クラスのonCreateにて設定

​ - アプリローンチ後に、アクティビティやフラグメントで設定

* Kotlin

```kotlin
// アプリローンチ前に、アプリ内クラスのonCreateにて設定する場合 
lateinit var Virtusize: Virtusize
override fun onCreate() {
    super.onCreate()
    Virtusize = VirtusizeBuilder().init(this)
        .setApiKey(api_key)
        .setUserId(user_id)
        .setEnv(VirtusizeEnvironment.STAGING)
        .build()
}

// アプリローンチ後に、アクティビティやフラグメントで設定
(application as App).vituriszesetUserID("user_id")
```

* Java

~~~~java
// アプリローンチ前に、アプリ内クラスのonCreateにて設定する場合 
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

// アプリローンチ後に、アクティビティやフラグメントで設定
    app = (App) getApplication();
    app.vituriszesetUserId("user_id");
~~~~

#### 2. 注文データ向けに*VirtusizeOrder* オブジェクトを作成

*VirtusizeOrder*オブジェクトはViturisze#sendOrderに情報を送るもので、下記の項目が必要です。

注意: * 表記のある場合項目は必須項目です

**VirtusizeOrder**

| **項目**           | **データ形式**                     | **例**         | **詳細**                |
|------------------|-------------------------------|---------------|-----------------------|
| externalOrderId* | String                        | "20200601586" | クライアント様でご使用している注文IDです |
| items*           | VirtusizeOrderItem オブジェクトのリスト | 次項の表参照        | 注文商品の詳細リストです          |

**VirtusizeOrderItem**

| **項目**     | **データ形式** | **例**                                    | **詳細**                                        |
|------------|-----------|------------------------------------------|-----------------------------------------------|
| productId* | String    | "A001"                                   | 商品詳細ページで設定いただいているproductIDと同じもの               |
| size*      | String    | "S", "M"など.                              | サイズ名称                                         |
| sizeAlias  | String    | "Small", "Large"など.                      | 前述のサイズ名称が商品詳細ページと異なる場合のAlias                  |
| variantId  | String    | "A001_SIZES_RED"                         | 商品の SKU、色、サイズなどの情報を設定してください。                  |
| imageUrl*  | String    | "http[]()://images.example.com/coat.jpg" | 商品画像の URL です。この画像がバーチャサイズのサービスに登録されます。        |
| color      | String    | "RED", "R'など.                            | 商品の色を設定してください。                                |
| gender     | String    | "W", "Women"など.                          | 性別を設定してください。                                  |
| unitPrice* | Double    | 5100.00                                  | 最大12桁の商品単価を設定してください。                          |
| currency*  | String    | "JPY", "KRW", "USD"など.                   | 通貨コードを設定してください。                               |
| quantity*  | Int       | 1                                        | 購入数量を設定してください。                                |
| url        | String    | "http[]()://example.com/products/A001"   | 商品ページのURLを設定してください。一般ユーザーがアクセス可能なURLで設定が必要です。 |

**例**

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
items.

add(new VirtusizeOrderItem(
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

#### 3. 注文情報の送信

ユーザーが注文完了時、ActivityあるいはFragment内で `Viturisze#sendOrder`を呼び出してください。

* Kotlin

`onSuccess`と`onError`はオプションです。

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
        })
~~~~

* Java

`SuccessResponseHandler`と`ErrorResponseHandler`のコールバックはオプションです。

~~~~java
app.vituriszesendOrder(order,
                       // This success callback is optional and gets called when the app successfully sends the order
        new SuccessResponseHandler() {
    @Override
    public void onSuccess (@Nullable Object data){
        Log.i(TAG, "Successfully sent the order");
    }
},
        // This error callback is optional and gets called when an error occurs when the app is sending the order
        new

ErrorResponseHandler() {
    @Override
    public void onError (@NotNull VirtusizeError error){
        Log.e(TAG, error.getMessage());
    }
}
);
~~~~

## Enable SNS Login in Virtusize for native WebView apps

Use the [Virtusize Auth SDK](https://github.com/virtusize/virtusize_auth_android)

## Examples

1. Kotlin example https://github.com/virtusize/integration_android/tree/master/sampleAppKotlin
2. Java example https://github.com/virtusize/integration_android/tree/master/sampleappjava

## License

Copyright (c) 2018-24 Virtusize CO LTD (https://www.virtusize.jp)
