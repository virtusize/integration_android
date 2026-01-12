# Virtusize Android Compose  実装ガイド

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
- Setup in Jetpack Compose

## はじめに

### 1. Virtusize SDKを実装する

appの`build.gradle`ファイルに下記のdependencyを追加

- Groovy (build.gradle)

  ```groovy
  dependencies {
    implementation 'com.virtusize.android:virtusize:2.12.16'
  }
  ```

- Kotlin (build.gradle.kts)

  ```kotlin
  dependencies {
    implementation("com.virtusize.android:virtusize:2.12.16")
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
| setShowSNSButtons    | Boolean                       | setShowSNSButtons(true)                                                                                                                                                                                                                        | Determines whether the integration will show the SNS buttons to the users                                                                                                      | No. By default, the integration enables the SNS buttons |
| setShowPrivacyPolicy | Boolean                       | setShowPrivacyPolicy(true)                                                                                                                                                                                                                     | Controls whether the privacy policy shows to users                                                                                                                             | No. By default, the privacy policy is shown                                                                                          |

```kotlin
override fun onCreate() {
  super.onCreate()

  // アプリケーション用に Virtusize インスタンスを初期化
  VirtusizeBuilder().init(this)
  // 必須なのは API キーのみ
  .setApiKey("15cc36e1d7dad62b8e11722ce1a245cb6c5e6692")
  // Order API を使用する場合はユーザーIDが必要
  .setUserId("123")
  // デフォルトでは、Virtusize の環境は GLOBAL に設定されている
  .setEnv(VirtusizeEnvironment.STAGING)
  // デフォルトでは、初期言語は Virtusize 環境に基づいて設定される
  .setLanguage(VirtusizeLanguage.EN)
  // デフォルトでは、ShowSGI は false に設定されている
  .setShowSGI(true)
  // デフォルトでは、Virtusize はすべての利用可能な言語を許可している
  .setAllowedLanguages(listOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))
  // デフォルトでは、商品詳細タブにすべての情報カテゴリが表示される
  .setDetailsPanelCards(setOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))
  // デフォルトでは、SNS ボタンが表示されます
  .setShowSNSButtons(true)
  //デフォルトでは、Virtusizeはプライバシーポリシーを表示します
  .setShowPrivacyPolicy(true)
  .build()
}
```

### 2. SNS認証を有効にする

SNS認証フローでは、Chrome Custom Tab を開いて、ユーザーがSNSアカウントでログインするためのウェブページを読み込む必要があります。
ログイン後、Chrome Custom Tab からアプリへログイン結果を返すために、カスタムURLスキームを定義する必要があります。

`AndroidManifest.xml` ファイルを編集し、インテントフィルターとカスタムURLスキーム用の `<data>` タグを追加してください。

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

**❗重要**

1. URL のホスト名は `sns-auth` にする必要があります。
2. URL スキームは、**アプリのパッケージIDから始まり、末尾が `.virtusize` で終わる必要があります**。また、**すべて小文字**で定義しなければなりません。
3. アプリのパッケージIDに含まれる **アンダースコア（\_）はハイフン（-）に置き換える必要があります**。
   例：`com.your_company.your_app` → `com.your-company.your-app`

### Virtusize SNSログインをWebViewアプリで有効にする方法

以下のいずれかの方法で、Virtusize SNSログインを有効にできます。

### 方法1: VirtusizeWebViewを使用する

##### **ステップ1: `WebView` を `VirtusizeWebView` に置き換える**

ユーザーがSNSを利用してログイン/新規アカウント登録を行えるようにするためには、VirtusizeのWeb版統合において、  
Kotlin/JavaファイルおよびXMLファイルの両方で 、既存の`WebView` を **`VirtusizeWebView`**　に置き換えてください。

- Kotlin/Java

  ```diff
  // Kotlin
  - var webView: WebView
  + var webView: VirtusizeWebView
  
  // Java
  - WebView webView;
  + VirtusizeWebView webView;
  ```

と

- XML

  ```diff
  - <WebView
  + <com.virtusize.libsource.VirtusizeWebView
      android:id="@+id/webView"
      android:layout_width="match_parent"
      android:layout_height="match_parent" />
  ```

##### ステップ2: Virtusize SNS認証用のActivity result launcherを設定

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
  // VirtusizeのSNS認証用Activity Result Launcherを登録する
  private ActivityResultLauncher<Intent> mLauncher = 
      registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(), 
          (ActivityResultCallback<ActivityResult>) result ->
              VirtusizeAuth.INSTANCE.handleVirtusizeSNSAuthResult(webView, result.getResultCode(), result.getData())
  );
  
  
  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
      super.onViewCreated(view, savedInstanceState)
      // ... その他のコード
    
      // WebView に Activity Result Launcher を設定する
      webView.setVirtusizeSNSAuthLauncher(virtusizeSNSAuthLauncher)
  }
  ```

### または

### 方法2: 標準のWebViewを使用する

##### ステップ1: WebViewの設定を確認

```kotlin
webView.settings.javaScriptEnabled = true
webView.settings.domStorageEnabled = true
webView.settings.databaseEnabled = true
webView.settings.setSupportMultipleWindows(true)
```

##### ステップ2: 以下のコードを追加

```kotlin
// VirtusizeのSNS認証用のActivity Result Launcherを登録する
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

            // その他のコード..... 
        }
    }

    webView.webChromeClient = object : WebChromeClient() {
        override fun onCreateWindow(
            view: WebView,
            dialog: Boolean,
            userGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            // titleポップアップウィンドウのリンクまたはリンクタイトルを取得する
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

            // その他のコード ..... 

            return super.onCreateWindow(view, dialog, userGesture, resultMsg)
        }
    }
}
```

### 3. Virtusize Message Handlerの登録（オプション）

アクティビティやフラグメントが終了したり削除されたりする前に、アクティビティ（activity）やフラグメント（fragment）のライフサイクル（lifecycle）・メソッドでメッセージ・ハンドラの登録を解除することを忘れないでください。方法については次のセクションを参照してください。

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

### 4. Virtusize Message Handler登録解除（オプション）

Message
Handlerはアクティビティ（activity）やフラグメント（fragment）のライフサイクル（lifecycle）に結びついていますが、Virtusizeライブラリオブジェクトはアプリケーションのライフサイクルに結びついています。そのため、Message
Handlerの登録解除を忘れると、アクティビティが終了したりフラグメントが削除されたりしても、イベントを聞き続けることになります。アクティビティの場合、ライフサイクルのどこでMessage
Handlerを登録したかによって、superメソッドが呼ばれる前に`onPause`または`onStop`
メソッドで登録を解除する必要があります。フラグメントの場合も、同様のガイドラインに従ってください。

```kotlin
private val activityMessageHandler: VirtusizeMessageHandler
override fun onPause() {
  // 常に onPause()（または実装によっては onStop()）でメッセージハンドラーの登録を解除してください。
  Virtusize.getInstance().unregisterMessageHandler(activityMessageHandler)
  super.onPause()
}
```

## Virtusize UI Components

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

**A. VirtusizeButton のために商品情報を準備する**

1. 次の情報を使って `VirtusizeProduct` オブジェクトを作成します：

    - Virtusize サーバー上で商品を識別するための `externalId`
    - 商品画像を表示するための `imageUrl`

    ```kotlin
    val product = VirtusizeProduct(
        // 商品の外部IDを設定
        externalId = "vs_dress",
        // 商品画像のURLを設定
        imageUrl = "http://www.image.com/goods/12345.jpg"
    )
    ```

**B. Add a VirtusizeButton**

```kotlin
VirtusizeButton(
    // VirtusizeButton に商品を設定
    product = product,

    // コンポーネントのレイアウトを調整（オプション）
    modifier = Modifier.align(Alignment.CenterHorizontally),

    // Virtusizeボタンの色を設定（オプション）
    // デフォルトは VirtusizeColors.teal() および VirtusizeColors.black()
    colors = VirtusizeButtonDefaults.colors(
        containerColor = VirtusizeColors.Teal,    // ボタン背景色
        contentColor = VirtusizeColors.White,     // ボタン内テキスト色
    ),

    // Virtusizeボタンのイベントを受け取る（オプション）
    onEvent = { event ->
        Log.i(VIRTUSIZE_BUTTON_TAG, event.name)
    },

    // Virtusizeボタンで発生したエラーを受け取る（オプション）
    onError = { error ->
        Log.e(VIRTUSIZE_BUTTON_TAG, error.message)
    },
)
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

- **VirtusizeInPageStandard 用の商品情報を準備する**

  1. 以下の情報を使用して `VirtusizeProduct` オブジェクトを作成します：

      - Virtusize サーバー上で商品を識別するための `externalId`
      - 商品画像の URL を指定する `imageUrl`

      ```kotlin
      val product = VirtusizeProduct(
          // 商品の外部IDを設定
          externalId = "vs_dress",
          // 商品画像のURLを設定
          imageUrl = "http://www.image.com/goods/12345.jpg"
      )
      ```

- **VirtusizeInPageStandard を追加する**

    ```kotlin
    VirtusizeInPageStandard(
        // VirtusizeInPageStandard に商品を設定
        product = product,

        // コンポーネントのレイアウトを調整（オプション）
        modifier = Modifier.padding(horizontal = 16.dp),

        // InPageStandard の背景色を調整（オプション）
        backgroundColor = VirtusizeColors.Black,

        // VirtusizeInPageStandard のイベントを受け取る（オプション）
        onEvent = { event ->
            Log.i(VIRTUSIZE_INPAGE_STANDARD_TAG, event.name)
        },

        // VirtusizeInPageStandard のエラーを受け取る（オプション）
        onError = { error ->
            Log.e(VIRTUSIZE_INPAGE_STANDARD_TAG, error.message)
        },
    )
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

- **VirtusizeInPageMini 用の商品情報を準備する**

  1. 以下の情報を使用して `VirtusizeProduct` オブジェクトを作成します：

      - Virtusize サーバー上で商品を識別するための `externalId`
      - 商品画像の URL を指定する `imageUrl`

      ```kotlin
      val product = VirtusizeProduct(
          // 商品の外部IDを設定
          externalId = "vs_dress",
          // 商品画像のURLを設定
          imageUrl = "http://www.image.com/goods/12345.jpg"
      )
      ```

- **VirtusizeInPageMini を追加する**

    ```kotlin
    VirtusizeInPageMini(
        // VirtusizeInPageMini に商品を設定
        product = product,

        // コンポーネントのレイアウトを調整（オプション）
        modifier = Modifier.padding(horizontal = 16.dp),

        // InPageMini の背景色を調整（オプション）
        backgroundColor = VirtusizeColors.Teal,

        // VirtusizeInPageMini のイベントを受け取る（オプション）
        onEvent = { event ->
            Log.i(VIRTUSIZE_INPAGE_MINI_TAG, event.name)
        },

        // VirtusizeInPageMini のエラーを受け取る（オプション）
        onError = { error ->
            Log.e(VIRTUSIZE_INPAGE_MINI_TAG, error.message)
        },
    )
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
// アプリが起動される前に、Application クラスの `onCreate` メソッド内で実行
override fun onCreate() {
    super.onCreate()
    VirtusizeBuilder().init(this)
        .setApiKey(api_key)
        .setUserId(user_id)
        .setEnv(VirtusizeEnvironment.STAGING)
        .build()
}

// アプリが起動した後に、Activity 内でユーザーIDを再設定（オプション）
Virtusize.getInstance().setUserID("user_id")

```

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

#### 3. 注文情報の送信

ユーザーが注文完了時、ActivityあるいはFragment内で `Viturisze#sendOrder`を呼び出してください。

* Kotlin

`onSuccess`と`onError`はオプションです。

~~~~kotlin
Virtusize.getInstance().sendOrder(
    order,
    // この成功コールバックは任意で、注文情報の送信に成功したときに呼び出されます
    onSuccess = {
        Log.i(TAG, "注文情報の送信に成功しました")
    },
    // このエラーコールバックも任意で、注文情報の送信中にエラーが発生した場合に呼び出されます
    onError = { error ->
        Log.e(TAG, error.message)
    }
)
~~~~

## ネイティブ WebView アプリで Virtusize の SNSログイン機能を有効にする

[Virtusize Auth SDK](https://github.com/virtusize/virtusize_auth_android)を利用してください。

## Examples

https://github.com/virtusize/integration_android/tree/master/sampleAppCompose

## License

Copyright (c) 2018-24 Virtusize CO LTD (https://www.virtusize.jp)
