## Build and Deploy

To work on this project you can use these tools: [Git][tool:git], [Maven][tool:maven], [Eclipse][tool:eclipse], [Android Configurator for Eclipse][tool:android.m2e] and [Android SDK Tools][tool:android.sdk].

To build application using Maven, follow next steps:

1. Install Android SDK and update it.
1. Create environment variable `ANDROID_HOME`, which points to directory, that contains Android SDK.
1. Build project using command `mvn clean package`.
1. Deploy application using command `mvn android:deploy`.

To work with application in IDE, follow next steps:

1. Install Eclipse.
1. Install Android Configurator fo Eclipse.
1. Import project into eclipse using `File > Import > Maven > Existing Maven Projects`.
1. Run applciation using `Run > Run As > Android Application`.

## User Guide

#### Install and Run

1. Install application.
1. Go to `Settings > Accounts > Add Account`.
1. Select `Corporate Contacts`.
1. Enter your name and password.
1. Press button `Sign In`.
1. Go to `Settings > Accounts > Corporate Contacts`.
1. Run synchronization.
1. Wait until synchronization will be completed.

#### Settings

You can customize synchronization using `Sync Settings`:

1. `Sync Photos` - enables or disables synchronization of photos for contacts. By default, synchronization of photos is disabled.
1. `Sync in Mobile Networks` - enables or disables synchronization in mobile networks. By default, synchronization is enabled only for Wi-Fi networks.

[tool:git]: http://git-scm.com/
[tool:maven]: http://maven.apache.org/
[tool:eclipse]: http://www.eclipse.org/
[tool:android.m2e]: http://rgladwell.github.io/m2e-android/
[tool:android.sdk]: http://developer.android.com/sdk/
