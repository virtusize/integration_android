# Android SDK 実装ガイド

[![](https://jitpack.io/v/virtusize/integration_android.svg)](https://jitpack.io/#virtusize/integration_android)

[English](README.md)

Virtusize helps retailers to illustrate the size and fit of clothing, shoes and bags online, by letting customers compare the
measurements of an item they want to buy (on a retailer's product page) with an item that they already own (a reference item).
This is done by comparing the silhouettes of the retailer's product with the silhouette of the customer's reference Item.
Virtusize is a widget which opens when clicking on the Virtusize button, which is located next to the size selection on the product page.

Read more about Virtusize at [https://www.virtusize.jp](https://www.virtusize.jp/)

You need a unique API key and an Admin account, only available to Virtusize customers. [Contact our sales team](mailto:sales@virtusize.com) to become a customer.

> This is the integration script for native Android apps only. For web integration, refer to the developer documentation on https://developers.virtusize.com. For iOS integration, refer to https://github.com/virtusize/integration_ios



## 対応バージョン

- minSdkVersion >= 21
- compileSdkVersion >= 30
- Setup in AppCompatActivity



## はじめに

もし、1.x.xの古いバージョンを引き続きご利用いただく場合は、[v1](https://github.com/virtusize/integration_android/tree/v1)を参照くださいませ。


### 1. Virtusize SDKを実装する

rootの`build.gradle`ファイルに下記のdependencyを追加

```groovy
allprojects {
  repositories {
      maven { url 'https://jitpack.io' }
  }
}
```

In your appの`build.gradle`ファイルに下記のdependencyを追加

```groovy
dependencies {
  implementation 'com.github.virtusize:integration_android:2.1.1'
}
```



### 2. Proguardの設定

Proguardをお使いの場合、Proguardのルールファイルに下記のルールを追加

```
-keep class com.virtusize.libsource.**
```



## セットアップ

### 1. はじめに

アプリケーションクラスの`onCreate`メソッドでVirtusizeオブジェクトを**VirtusizeBuilder**を使って初期化し、設定を行います。可能な設定方法を以下の表に示します。

**VirtusizeBuilder**

| 項目                 | データ形式                          | 例                                                           | 説明                                                         | 要件                                                         |
| -------------------- | ----------------------------------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| setApiKey            | String                              | setApiKey("api_key")                                         | 固有のAPIキーは各Virtusizeクライアントに提供されます。       | あり。                                                       |
| setUserId            | String                              | setUserId("123")                                             | ユーザーがクライアントのアプリにログインしている場合に、クライアントから渡されます。 | あり。Order APIを使用する場合。                              |
| setEnv               | VirtusizeEnvironment                | setEnv(VirtusizeEnvironment.STAGING)                         | 環境は実装をしている環境を選択してください、`VirtusizeEnvironment.STAGING`,  `VirtusizeEnvironment.GLOBAL`, `VirtusizeEnvironment.JAPAN` or `VirtusizeEnvironment.KOREA`のいずれかです。 | 特になし。デフォルトでは、`VirtusizeEnvironment.GLOBAL`に設定されます。 |
| setLanguage          | VirtusizeLanguage                   | setLanguage(VirtusizeLanguage.EN)                            | インテグレーションをロードする際の初期言語を設定します。設定可能な値は以下：`VirtusizeLanguage.EN`, `VirtusizeLanguage.JP` および`VirtusizeLanguage.KR` | 特になし。デフォルトでは、初期言語はVirtusizeの環境に基づいて設定されます。 |
| setShowSGI           | Boolean                             | setShowSGI(true)                                             | ユーザーが生成したアイテムをワードローブに追加するために、SGIを取得してSGIフローを使用するかどうかを決定します。 | 特になし。デフォルトではShowSGIはfalseに設定されています。   |
| setAllowedLanguages  | `VirtusizeLanguage`列挙のリスト     | In Kotlin, setAllowedLanguages(mutableListOf(VirtusizeLanguage.EN, VirtusizeLanguage.JP))<br />In Java, setAllowedLanguages(Arrays.asList(VirtusizeLanguage.EN, VirtusizeLanguage.JP)) | ユーザーが言語選択ボタンより選択できる言語                   | 特になし。デフォルトでは、英語、日本語、韓国語など、表示可能なすべての言語が表示されるようになっています。 |
| setDetailsPanelCards | `VirtusizeInfoCategory`列挙のリスト | In Kotlin, setDetailsPanelCards(mutableListOf(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT))<br />In Java, setDetailsPanelCards(Arrays.asList(VirtusizeInfoCategory.BRAND_SIZING, VirtusizeInfoCategory.GENERAL_FIT)) | 商品詳細タブに表示する情報のカテゴリ。表示可能カテゴリは以下：`VirtusizeInfoCategory.MODELINFO`, `VirtusizeInfoCategory.GENERALFIT`, `VirtusizeInfoCategory.BRANDSIZING` および `VirtusizeInfoCategory.MATERIAL` | 特になし。デフォルトでは、商品詳細タブに表示可能なすべての情報カテゴリが表示されます。 |

- Kotlin

  ```kotlin
  lateinit var Virtusize: Virtusize
  override fun onCreate() {
      super.onCreate()
  
      // Initialize Virtusize instance for your application
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
  }
  ```



### 2. 商品詳細をセットする

1. アクティビティ内では、比較ビューに反映させるために商品の `imageUrl` と、API で商品を参照するために使用する `externalId` を渡して、商品の詳細を設定します。

   Kotlin

   ```kotlin
   (application as App)
       .Virtusize
       .setupVirtusizeProduct(
           VirtusizeProduct(
               externalId = "694",
               imageUrl = "http://www.image.com/goods/12345.jpg"
           )
       )
   ```

   Java

   ```java
   app.Virtusize.setupVirtusizeProduct(
           new VirtusizeProduct(
                   "694",
                   "http://www.image.com/goods/12345.jpg"
           )
   );
   ```



### 3.  Virtusize Message Handlerの登録（オプション）

アクティビティやフラグメントが終了したり削除されたりする前に、アクティビティ（activity）やフラグメント（fragment）のライフサイクル（lifecycle）・メソッドでメッセージ・ハンドラの登録を解除することを忘れないでください。方法については次のセクションを参照してください。

- Kotlin

  ```kotlin
  private val activityMessageHandler = object : VirtusizeMessageHandler {
      override fun onEvent(event: VirtusizeEvent) {
          Log.i(TAG, event.name)
      }
  
      override fun onError(error: VirtusizeError) {
          Log.e(TAG, error.message)
      }
  }
  
  override fun onCreate(savedInstanceState: Bundle?) {
      //...
      // Register message handler to listen to events from Virtusize
      (application as App).Virtusize.registerMessageHandler(activityMessageHandler)
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
          public void onEvent(@NotNull VirtusizeEvent event) {
              Log.i(TAG, event.getName());
          }
  
          @Override
          public void onError(@NonNull VirtusizeError error) {
              Log.e(TAG, error.getMessage());
          }
      }
      app.Virtusize.registerMessageHandler(virtusizeMessageHandler);
      //...
  }
  ```



### 4. Virtusize Message Handler登録解除（オプション）

Message Handlerはアクティビティ（activity）やフラグメント（fragment）のライフサイクル（lifecycle）に結びついていますが、Virtusizeライブラリオブジェクトはアプリケーションのライフサイクルに結びついています。そのため、Message Handlerの登録解除を忘れると、アクティビティが終了したりフラグメントが削除されたりしても、イベントを聞き続けることになります。アクティビティの場合、ライフサイクルのどこでMessage Handlerを登録したかによって、superメソッドが呼ばれる前に`onPause`または`onStop`メソッドで登録を解除する必要があります。フラグメントの場合も、同様のガイドラインに従ってください。

- Kotlin

  ```kotlin
  private val activityMessageHandler: VirtusizeMessageHandler
  override fun onPause() {
          // Always un register message handler in onPause() or depending on implementation onStop().
          (application as App).Virtusize.unregisterMessageHandler(activityMessageHandler)
          super.onPause()
  }
  ```

- Java

  ```java
  VirtusizeMessageHandler virtusizeMessageHandler;
  @Override
  protected void onPause() {
      app.Virtusize.unregisterMessageHandler(virtusizeMessageHandler);
      super.onPause();
  }
  ```



## Virtusize Views

SDKをセットアップした後、`VirtusizeView`を追加して、顧客が理想的なサイズを見つけられるようにします。Virtusize SDKはユーザーが使用するために2つの主要なUIコンポーネントを提供します。:

### 1. バーチャサイズボタン（Virtusize Button）

#### (1) はじめに

VirtusizeButtonはこのSDKの中でもっとシンプルなUIのボタンです。ユーザーが正しいサイズを見つけられるように、ウェブビューでアプリケーションを開きます。



#### (2) デフォルトスタイル

SDKのVirtusizeボタンには2つのデフォルトスタイルがあります。

|                          Teal Theme                          |                         Black Theme                          |
| :----------------------------------------------------------: | :----------------------------------------------------------: |
| <img src="https://user-images.githubusercontent.com/7802052/92671785-22817a00-f352-11ea-8ce9-6b4f7fcb43c4.png" /> | <img src="https://user-images.githubusercontent.com/7802052/92671771-172e4e80-f352-11ea-8443-dcb8b05f5a07.png" /> |

もしご希望であれば、ボタンのスタイルもカスタマイズすることができます。



#### (3) 使用方法

**A.  アクティビティのXMLレイアウトファイルにVirtusizeButtonを追加してください。**

私たちのデフォルトのボタンスタイルを使用するために、XMLで`app:virtusizeButtonStyle="virtusize_black "`または`app:virtusizeButtonStyle="virtusize_teal "`を設定します。

- XML

  ```xml
  <com.virtusize.libsource.ui.VirtusizeButton
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

**B. また、他のボタンスタイルを使用したり、ボタンの属性（テキスト、高さ、幅など）を定義することもできます。**

```xml
<com.virtusize.libsource.ui.VirtusizeButton
    android:id="@+id/exampleVirtusizeButton"
    style="@style/Widget.AppCompat.Button.Colored"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/virtusize_button_text" />
```

**C. アクティビティの**`setupVirtusizeView`**関数を使用して、VirtusizeボタンをVirtusize APIに接続します。**

- Kotlin

  ```kotlin
  (application as App).Virtusize.setupVirtusizeView(exampleVirtusizeButton)
  ```

- Java

  ```java
  app.Virtusize.setupVirtusizeView(virtusizeButton);
  ```



### 2. バーチャサイズ・インページ（Virtusize InPage）

#### (1) はじめに

Virtusize InPageは、私たちのサービスのスタートボタンのような役割を果たすボタンです。また、このボタンは、お客様が正しいサイズを見つけるためのフィッティングガイドとしても機能します。

##### **InPageの種類**

Virtusize SDKには2種類のInPageがあります。

|                       InPage Standard                        |                         InPage Mini                          |
| :----------------------------------------------------------: | :----------------------------------------------------------: |
| ![InPageStandard](https://user-images.githubusercontent.com/7802052/92671977-9cb1fe80-f352-11ea-803b-5e3cb3469be4.png) | ![InPageMini](https://user-images.githubusercontent.com/7802052/92671979-9e7bc200-f352-11ea-8594-ed441649855c.png) |

⚠️**注意事項**⚠️

1. InPageはVirtusizeボタンと一緒に導入することはできません。オンラインショップでは、InPageかVirtusizeボタンのどちらかをお選びください。

2. InPage Miniは、必ずInPage Standardと組み合わせてご利用ください。



#### (2) InPage Standard

##### A. デザインガイドライン

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
    - CTAボタンの背景色（[WebAIM contrast test](https://webaim.org/resources/contrastchecker/)で問題がなければ）
    - Inpageの横幅（アプリの横幅に合わせて変更可）

  - **変更不可**:
    - 形状やスペースなどのインターフェイスコンポーネント
    - フォント
    - CTA ボタンの形状
    - テキスト文言
    - ボタンシャドウ（削除も不可）
    - VIRTUSIZE ロゴと プライバシーポリシーのテキストが入ったフッター（削除も不可）

##### B.  使用方法

- **アクティビティのXMLレイアウトファイルにVirtusizeInPageStandを追加します。**

  私たちのデフォルトスタイルを使用するために、 `app:virtusizeInPageStandardStyle="virtusize_black"` または `app:virtusizeInPageStandardStyle="virtusize_teal"` を設定してください。

  CTAボタンの背景色を変更したい場合は、`app:inPageStandardButtonBackgroundColor="#123456 "`を使用できます。

  アプリ画面の端とInPageStandardの間の水平方向の余白を設定したい場合は、`app:inPageStandardHorizontalMargin="16dp"`とします。

  - XML

    ```xml
    <com.virtusize.libsource.ui.VirtusizeInPageStandard
        android:id="@+id/exampleVirtusizeInPageStandard"
        app:virtusizeInPageStandardStyle="virtusize_black"
        app:inPageStandardHorizontalMargin="16dp"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
    ```

    ```xml
    <com.virtusize.libsource.ui.VirtusizeInPageStandard
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
    ```

  - Java

    ```java
    virtusizeInPageStandard.setVirtusizeViewStyle(VirtusizeViewStyle.BLACK);
    virtusizeInPageStandard.setHorizontalMargin(ExtensionsKt.getDpInPx(16));
    virtusizeInPageStandard.setButtonBackgroundColor(ContextCompat.getColor(this, R.color.your_custom_color));
    ```

- **アクティビティで**`setupVirtusizeView`**関数を使用して、InPage StandardとVirtusize APIを接続します。**

  - Kotlin

    ```kotlin
    (application as App)
        .Virtusize
        .setupVirtusizeView(exampleVirtusizeInPageStandard)
    ```

  - Java

    ```java
    app.Virtusize.setupVirtusizeView(virtusizeInPageStandard);
    ```



#### (3) InPage Mini

こちらは、InPageのミニバージョンで、アプリに配置することができます。目立たないデザインなので、お客様が商品画像やサイズ表を閲覧するようなレイアウトに適しています。

##### A. デザインガイドライン

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
    - Proxima Nova
    - 14sp (メッセージ文言)
    - 12sp (ボタン内テキスト)

- ##### UI カスタマイゼーション

  - **変更可**
    - CTAボタンの背景色（[WebAIM contrast test](https://webaim.org/resources/contrastchecker/)で問題がなければ）
  - **変更不可:**
    - フォント
    - CTA ボタンの形状
    - テキスト文言

##### B. 使用方法

- **アクティビティのXMLレイアウトファイルにVirtusizeInPageMiniを追加します。**

  私たちのデフォルトスタイルを使用するには、`app:virtusizeInPageMiniStyle="virtusize_black"` または `app:virtusizeInPageMiniStyle="virtusize_teal"` を設定してください。

  バーの背景色を変更したい場合は、`app:inPageMiniBackgroundColor="#123456"` とします。

  - XML

    ```xml
    <com.virtusize.libsource.ui.VirtusizeInPageMini
        android:id="@+id/exampleVirtusizeInPageMini"
        app:virtusizeInPageMiniStyle="virtusize_teal"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content" />
    ```

    ```xml
    <com.virtusize.libsource.ui.VirtusizeInPageMini
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
    ```

  - Java

    ```java
    virtusizeInPageMini.setVirtusizeViewStyle(VirtusizeViewStyle.TEAL);
    virtusizeInPageMini.setInPageMiniBackgroundColor(ContextCompat.getColor(this, R.color.your_custom_color));
    ```

- **アクティビティで**`setupVirtusizeView`**関数を使用して、InPage MiniをVirtusize APIに接続します。**

  - Kotlin

    ```kotlin
    (application as App)
        .Virtusize
        .setupVirtusizeView(exampleVirtusizeInPageMini)
    ```

  - Java

    ```java
    app.Virtusize.setupVirtusizeView(virtusizeInPageMini);
    ```



## Order APIについて

The order APIはバーチャサイズがユーザーが購入した商品を`Purchase History`（購入履歴）の一部として表示するために必要で、これらの商品がユーザーが購入検討している商品と比較可能になります。

#### 1. 初期化

Virtusizeにリクエストを送信する前に、**user ID**が設定されていることを確認してください。以下、どちらの方法でも **user ID** を設定することが可能です。

​	- アプリローンチ前に、アプリ内クラスのonCreateにて設定

​	- アプリローンチ後に、アクティビティやフラグメントで設定

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
(application as App).Virtusize.setUserID("user_id")
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
app.Virtusize.setUserId("user_id");
~~~~



#### 2. 注文データ向けに*VirtusizeOrder* オブジェクトを作成

*VirtusizeOrder*オブジェクトはVirtusize.sendOrderに情報を送るもので、下記の項目が必要です。

注意: * 表記のある場合項目は必須項目です

**VirtusizeOrder**

| **項目**         | **データ形式**                          | **例**        | **詳細**                                 |
| ---------------- | --------------------------------------- | ------------- | ---------------------------------------- |
| externalOrderId* | String                                  | "20200601586" | クライアント様でご使用している注文IDです |
| items*           | VirtusizeOrderItem オブジェクトのリスト | 次項の表参照  | 注文商品の詳細リストです                 |

**VirtusizeOrderItem**

| **項目**   | **データ形式** | **例**                                   | **詳細**                                                     |
| ---------- | -------------- | ---------------------------------------- | ------------------------------------------------------------ |
| productId* | String         | "A001"                                   | 商品詳細ページで設定いただいているproductIDと同じもの        |
| size*      | String         | "S", "M"など.                            | サイズ名称                                                   |
| sizeAlias  | String         | "Small", "Large"など.                    | 前述のサイズ名称が商品詳細ページと異なる場合のAlias          |
| variantId  | String         | "A001_SIZES_RED"                         | 商品の SKU、色、サイズなどの情報を設定してください。         |
| imageUrl*  | String         | "http[]()://images.example.com/coat.jpg" | 商品画像の URL です。この画像がバーチャサイズのサービスに登録されます。 |
| color      | String         | "RED", "R'など.                          | 商品の色を設定してください。                                 |
| gender     | String         | "W", "Women"など.                        | 性別を設定してください。                                     |
| unitPrice* | Double         | 5100.00                                  | 最大12桁の商品単価を設定してください。                       |
| currency*  | String         | "JPY", "KRW", "USD"など.                 | 通貨コードを設定してください。                               |
| quantity*  | Int            | 1                                        | 購入数量を設定してください。                                 |
| url        | String         | "http[]()://example.com/products/A001"   | 商品ページのURLを設定してください。一般ユーザーがアクセス可能なURLで設定が必要です。 |

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



#### 3. 注文情報の送信

ユーザーが注文完了時、ActivityあるいはFragment内で `Virtusize.sendOrder`を呼び出してください。

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