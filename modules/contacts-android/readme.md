# Summary

This applications performs contacts synchronization on Android smartphones.

The minimum supported version of platform is 2.3.

### Libraries

This applications uses [Spring for Android][library:spring] to access REST API.

### Getting Started

To work on this project you can use these tools: [Git][tool:git], [Maven][tool:maven], [Android Configurator for Eclipse][tool:android.m2e] and [Android SDK Tools][tool:android.sdk].

To build application using Maven, follow next steps:

1. Install Android SDK and update it.
1. Create environment variable `ANDROID_HOME`, which points to directory with Android SDK.
1. Build project using command `mvn clean package`.
1. Deploy application using command `mvn android:deploy`.

If you use [Eclipse][tool:eclipse], follow next steps:

1. Install Eclipse.
1. Install Android Configurator fo Eclipse.
1. Import project using `File > Import > Maven > Existing Maven Projects`.
1. Run application using `Run > Run As > Android Application`.

### Synchronization

Synchronization includes two passes:

1. During the first pass, application synchronizes all data except photos.
1. During the second pass, application downloads and updates photos.

The second pass is optional and is disabled by default.
During each synchronization, application tries to download all photos that is not synchronized yet.
If photo already synchronized, application will update it only when its URL will be changed.

Application updates contacts in address book in next cases:

1. The downloaded contact has another version.
1. Application was updated since the last synchronization.

# User Guide

### Install and Run

1. Install application.
1. Go to `Settings > Accounts > Add Account` (or `Contacts -> Accounts -> Add Account`).
1. Select `Corporate Contacts`.
1. Enter your name and password.
1. Press button `Sign In`.
1. Go to `Settings > Accounts > Corporate Contacts` (or `Contacts -> Accounts -> Corporate Contacts`).
1. Run synchronization.
1. Wait until synchronization will be completed.

When you synchronize contacts first time or after update of application, synchronization can take a few minutes.

### Settings

You can customize synchronization using `Sync Settings`:

1. `Load Photos` - enable or disable synchronization of photos.
1. `Use Mobile Networks` - enable or disable synchronization in mobile networks.
1. `Group for Coworkers` - change title for group for coworkers.

[tool:git]: http://git-scm.com/
[tool:maven]: http://maven.apache.org/
[tool:android.m2e]: http://rgladwell.github.io/m2e-android/
[tool:android.sdk]: http://developer.android.com/sdk/
[tool:eclipse]: http://www.eclipse.org/

[library:spring]: http://projects.spring.io/spring-android/
