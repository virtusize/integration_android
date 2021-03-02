# Android SDK 実装ガイド

[![](https://jitpack.io/v/virtusize/integration_android.svg)](https://jitpack.io/#virtusize/integration_android)

[English](README.md)

Virtusize helps retailers to illustrate the size and fit of clothing, shoes and bags online, by letting customers compare the
measurements of an item they want to buy (on a retailer's product page) with an item that they already own (a reference item).
This is done by comparing the silhouettes of the retailer's product with the silhouette of the customer's reference Item.
Virtusize is a widget which opens when clicking on the Virtusize button, which is located next to the size selection on the product page.

Read more about Virtusize at https://www.virtusize.com

You need a unique API key and an Admin account, only available to Virtusize customers. [Contact our sales team](mailto:sales@virtusize.com) to become a customer.

> This is the integration script for native Android apps only. For web integration, refer to the developer documentation on https://developers.virtusize.com. For iOS integration, refer to https://github.com/virtusize/integration_ios



## 対応バージョン

- minSdkVersion >= 21
- compileSdkVersion >= 30
- Setup in AppCompatActivity


## はじめに

If you'd like to continue using the old Version 1.x.x, refer to the branch [v1](https://github.com/virtusize/integration_android/tree/v1).

#### 1. Virtusize SDKを実装する

- rootの `build.gradle`ファイルに下記のdependencyを追加

  ```groovy
  allprojects {
      repositories {
          maven { url 'https://jitpack.io' }
      }
  }
  ```

- In your appのbuild.gradleファイルに下記のdependencyを追加

  ```groovy
  dependencies {
      implementation 'com.github.virtusize:integration_android:2.1'
  }
  ```



#### 2. Proguardの設定

Proguardをお使いの場合、Proguardのルールファイルに下記のルールを追加

```
-keep class com.virtusize.libsource.**
```



## セットアップ

#### 1. VirtusizeBuilderを使用

**Config**をセットアップするために、**VirtusizeBuilder**を使ってアプリ内クラスの`onCreate`にてバーチャサイズオブジェクトをイニシャライズします。

 **VirtusizeBuilder の設定方法は下記です：**

##### A. APIキーの設定（必須）

​	各クライアント様ごとに割り当てられたAPIキーを設定します

##### B. UserIdの設定（[Order API](#order-apiについて)が使われている場合必須）

​	String形式にてユーザーがアプリでログインしている場合にUser IDを設定。アプリローンチ後にユーザーIDを設定することも可能	です。詳しくは[Order API](#order-apiについて)の1項を参照してください。

##### C. Envの設定リージョンの設定が可能です。

​	デフォルトでは`GLOBAL`に設定されています。

​	**実装可能例：**

​	`VirtusizeEnvironment.STAGING`, 

​	`VirtusizeEnvironment.GLOBAL`, 

​	`VirtusizeEnvironment.JAPAN` and

​	`VirtusizeEnvironment.KOREA`.

##### D. Languageの設定

​	実装する言語を設定します。デフォルトではVirtusizeEnvironmentの言語設定に従います。

​	**実装可能例：**

​	`VirtusizeLanguage.EN`, 

​	`VirtusizeLanguage.JP` and

​	`VirtusizeLanguage.KR`.

##### E. ShowSGIの設定

​	実装がSGIフローを利用してユーザーにワードローブへのSGIを利用した追加機能を使わせるかどうかをブーリアン値で設定します。**SGIを使用するかどうかはご担当者にご相談ください。**デフォルトでは`false`に設定されています。

##### F. AllowedLanguagesの設定

​	ユーザーが切り替え可能な言語を設定します。デフォルトでは全ての言語に切り替え可能になっています。

##### G. DetailsPanelCardsの設定

​	サービス内の商品詳細タブで表示する項目を設定します。デフォルトでは表示可能な項目全てが表示される設定です。

​	**設定可能な項目：** 

​	`VirtusizeInfoCategory.MODEL_INFO`, 

​	`VirtusizeInfoCategory.GENERAL_FIT`, 

​	`VirtusizeInfoCategory.BRAND_SIZING` and 

​	`VirtusizeInfoCategory.MATERIAL`.

- **Kotlinの場合の例**

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
- **Javaの場合の例**

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



#### 2. バーチャサイズのボタンの追加

バーチャサイズのボタンはバーチャサイズのサービスを立ち上げるUIエレメントです。ボタン追加にはレイアウトXMLファイル内に追加します。

##### (1) バーチャサイズのボタンスタイルを利用する場合

XML内で`app:virtusizeButtonStyle="default_style"`を使用することで、デフォルトのボタンスタイルを利用できます。

- XML

  ```xml
  <com.virtusize.libsource.ui.VirtusizeButton
      android:id="@+id/exampleVirtusizeButton"
      app:virtusizeButtonStyle="default_style"
      android:layout_width="wrap_content"
      android:layout_height="wrap_content" />
  ```

 あるいはプログラムで下記のように実装も可能です

- Kotlin

  ```kotlin
  exampleVirtusizeButton.buttonStyle = VirtusizeButtonStyle.DEFAULT_STYLE
  ```

- Java

  ```java
  virtusizeButton.setButtonStyle(VirtusizeButtonStyle.DEFAULT_STYLE);
  ```

##### (2) ボタンのスタイルをカスタマイズする場合

任意のボタンスタイルを下記のような形でバーチャサイズ適用することも可能です。

```xml
<com.virtusize.libsource.ui.VirtusizeButton
    android:id="@+id/exampleVirtusizeButton"
    style="@style/Widget.AppCompat.Button.Colored"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/virtusize_button_text" />
```



#### 3. バーチャサイズボタンの設定

Activity内にて、商品詳細ページから送られる`imageUrl`と`externalId`を使ってバーチャサイズボタンを設定します。`imageUrl`は商品画像、`externalId`はAPIキーをもとに商品を参照するために使われます。

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
  VirtusizeButton exampleVirtusizeButton;
  @Override
  protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      exampleVirtusizeButton = findViewById(R.id.exampleVirtusizeButton);
      App app = (App) getApplication();
      app.Virtusize.setupVirtusizeButton(virtusizeButton, new VirtusizeProduct("694", "https://www.publicdomainpictures.net/pictures/120000/velka/dress-1950-vintage-style.jpg"));
  }
  ```



#### 4. バーチャサイズウィジェットをマニュアルで閉じる

- Kotlin

  ```kotlin
  exampleVirtusizeButton.dismissVirtusizeView()
  ```

- Java

  ```java
  exampleVirtusizeButton.dismissVirtusizeView();
  ```



#### 5. Virtusize Message Handlerの設定

activityあるいはfragmentのライフサイクルが終わる、あるいは除去される前にMessage Handler解除するのを忘れないようにしてください。

- Kotlin

  ```kotlin
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
       (application as App).Virtusize.registerMessageHandler(activityMessageHandler)
       //...
   }
  ```

- Java

  ```java
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
   }
  ```



#### 6. Virtusize Message Handlerの解除

Message Handlerはアクティビティあるいはfragmentのライフサイクルに紐づいていますが、バーチャサイズのオブジェクトはアプリケーションのライフサイクルに紐づいています。なので、Message Handlerを解除し忘れた場合、アクティビティが無効な場合やFragmentが除去された後でもイベントを参照し続けてしまいます。Message Handlerをどこに登録したかによりますんが、Super methodが呼び出される前に`onPause`か`onStop`にて解除する必要がある場合があります。 



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