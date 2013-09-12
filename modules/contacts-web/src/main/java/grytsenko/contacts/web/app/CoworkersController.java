package grytsenko.contacts.web.app;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.service.SearchContactsService;
import grytsenko.contacts.web.util.FullNameComparator;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Provides handlers for requests for page "Coworkers".
 */
@Controller
@RequestMapping(Views.COWORKERS)
public class CoworkersController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CoworkersController.class);

    @Autowired
    SearchContactsService searchContactsService;

    /**
     * Finds contacts of coworkers of current user.
     */
    @RequestMapping(method = RequestMethod.GET)
    public String coworkers(Principal principal, Model model) {
        String username = principal.getName();
        LOGGER.debug("Get coworkers of {}.", username);

        List<Contact> contacts = searchContactsService.findCoworkers(username);
        Collections.sort(contacts, new FullNameComparator());
        model.addAttribute("contacts", contacts);

        return Views.COWORKERS;
    }

}
