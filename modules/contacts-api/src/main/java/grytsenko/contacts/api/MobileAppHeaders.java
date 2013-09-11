package grytsenko.contacts.api;

/**
 * The custom headers, that can be used by mobile applications.
 */
public interface MobileAppHeaders {

    /**
     * Platform of mobile application.
     */
    public interface Platform {

        /**
         * The header name.
         */
        String HEADER_NAME = "Mobile-App-Platform";

        /**
         * The header value for Android platform.
         */
        String ANDROID = "Android";

    }

}
