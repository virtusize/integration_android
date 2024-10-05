### Next Release
- Update the commonly used web view URL

### 2.6.2

- Fix the animation of loading dots for the InPage components

### 2.6.1

- Removed Japanese link in README-COMPOSE.md
- Fixed an issue where the SDK could not retain the email login session.
- Bump virtusize-auth to 1.0.6

## 2.6.0

* Added Jetpack Compose UI component for VirtusizeButton
* Added Jetpack Compose UI component for VirtusizeInPageStandard
* Added Jetpack Compose UI component for VirtusizeInPageMini
* Added README for integrating Jetpack Compose UI components

## 2.5.5

* Updated Size Recommendation API related URL, request model, response model.
* Added environment related size recommendation base URLs.
* Updated unit test

## 2.5.1

* Handle SNS auth for web view apps
* Change back the SDK webview URL

## 2.5.0

* Perform Virtusize SNS authentication with Chrome Custom Tabs

## 2.4.2

* Create a submodule `virtusize-core`
* If you continue to integrate our SDK using JitPack, the new artifact name is `com.github.virtusize.integration_android:virtusize:${virtusize_version}`

## 2.4.1

* Use the staging API URLs for staging
* Add the `testing` env for internal use

## 2.4.0

* Add ktlint
* Setup Github Actions for CI/CD
* **⚠️Important⚠️**
  The SDK has been uploaded to `MavenCentral` and the group ID has been changed to `com.virtusize.android`
  New artifact name: `com.virtusize.android:virtusize:${virtusize_version}`

## 2.3.1

* Provide the solution for the WebView apps that load Virtusize Fit Illustrator

## 2.3.0

* Change the get-size endpoint from `get-size-new` to `get-size`
* Prevent from updating the selected user product ID when the event `user-added-product` is fired
* Remove the `Proxima Nova` font and use the `Roboto` font for English texts
* Add the MIT license
* Improve the integration by binding each Virtusize widget with a `VirtusizeProduct` object using the `Virtusize.setVirtusizeView` function

## 2.2.3

* Handle the user-deleted-product event from the Virtusize webview

## 2.2.2

* Allow the Flutter SDK to use the API functionality
* Allow creating a VirtusizeButton programmatically
* Fix the wrong logic of which language to display when a user doesn't set a language when initializing the Virtusize
* Fix the UI of the product images

## 2.2.1

* Enable SNS login buttons in the web view

## 2.2

* Fix SNS Login on the web version of Virtusize integration through the SDK

## 2.1.4

* Adjust the shadow of InPage to be less dark and obvious

## 2.1.3

* A crash that happens when a user's body profile is empty

## 2.1.2

* Enable font size changes of InPage
* Ensure InPage displays the correct message when a user is logged out

## 2.1.1

* Remove virtusizeControllerShouldClose from VirtusizeMessageHandler
* Fix a crash caused by HttpURLConnection when there's no internet
* Fix a crash caused by forcing unwrapping storeProduct and productTypes

## 2.1

* InPage Release: Add InPage UI components Virtusize InPage Standard and Virtusize InPage Mini views

## 2.0.5

* Make the web view scrollable when the soft keyboard is open
* Upgrade versions of Gradle dependencies

## 2.0.4

* Fix not being able to hide the space of the Virtusize button when the visibility is set to gone in xml

## 2.0.3

* Remove app_name from strings.xml in libsource to avoid overwriting the application's name
* Improve the way the SDK prints logs about invalid product data check

## 2.0.2

* Update the new event API URL
* Enable setting up the user ID after the app is launched

## 2.0.1

* Fix the Order API error saying external_product_id is not provided or not a string

## 2.0

* Release the New Version of the Virtusize Integration

## 1.7.1

* Update the product data check URL
* Fix some unit tests

## 1.7

* Fix the unit testing errors caused by ErrorResponseHandler
* Change the fit illustrator URL to the new version

## 1.6.3

* Remove unnecessary logs

## 1.6.2

* Fix the Null Region Value Issue. Add the default value of region for the Order API

## 1.6.1

* Fix when parsing a JSON response, the string value of some param is "null" instead of "" when it is NULL in the response
* Fix the param key strings for the UserData parser
* Add arguments for ErrorResponseHandler to be able to pass back more error info
* Add unit testing for network code

## 1.6

* Add the Order API

## 1.5.3

* Remove toast message for volley network error

## 1.5.2

* Update proguard rules

## 1.5.1

* Fix language Parameter, specify Android SDK
* Update parameters sent to widget

## 1.5

* Add functionality to unregister Virtusize event listener

## 1.4.1

* Update docs and installation guide regarding button styling based on XML

## 1.4

* Fix button styling. Style of button can now be edited in XML

## 1.3

* Fix Dom storage bug

## 1.2

* Update default button style
* Send Json data as boolean rather than string

## 1.1

* Refactor: send event when fit illustrator button clicked

## 1.0

* First release.
* Refactor: spacing in readme