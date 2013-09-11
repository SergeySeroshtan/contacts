package grytsenko.contacts.web.app;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.service.SearchContactsService;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller for home page.
 */
@Controller
@RequestMapping("/home")
public class HomeController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(HomeController.class);

    @Autowired
    SearchContactsService searchContactsService;

    /**
     * Finds contact of current user.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String home(Principal principal, Model model) {
        String username = principal.getName();
        LOGGER.debug("Get contact of {}.", username);

        Contact contact = searchContactsService.findEmployee(username);
        model.addAttribute("contact", contact);

        return "/home";
    }
}
