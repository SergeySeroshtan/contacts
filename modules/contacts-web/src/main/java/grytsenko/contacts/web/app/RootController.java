package grytsenko.contacts.web.app;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Provides handlers for common requests.
 */
@Controller
public class RootController {

    /**
     * Redirects user to home page.
     */
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String home() {
        return Views.HOME_REDIRECT;
    }

    /**
     * Moves user to login page.
     */
    @RequestMapping(value = Views.LOGIN, method = RequestMethod.GET)
    public String login() {
        return Views.LOGIN;
    }

}
