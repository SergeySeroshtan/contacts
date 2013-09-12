package grytsenko.contacts.web.app;

/**
 * The useful constants for mapping requests.
 * 
 * <p>
 * Paths are matched to views.
 */
public interface Views {

    /**
     * Login page.
     */
    String LOGIN = "/login";

    /**
     * Home page.
     */
    String HOME = "/home";

    /**
     * Redirect for home page.
     */
    String HOME_REDIRECT = "redirect:" + HOME;

    /**
     * Page with coworkers.
     */
    String COWORKERS = "/coworkers";

}
