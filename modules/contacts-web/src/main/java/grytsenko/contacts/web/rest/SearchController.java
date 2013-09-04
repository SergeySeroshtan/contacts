package grytsenko.contacts.web.rest;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.service.SearchContactsService;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Processes requests to search contacts.
 */
@Controller
public class SearchController {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SearchController.class);

    @Autowired
    SearchContactsService searchContactsService;

    /**
     * Finds contact of current user.
     */
    @RequestMapping(value = "my", method = RequestMethod.GET)
    @ResponseBody
    public Contact my(Principal principal) {
        String username = principal.getName();
        LOGGER.debug("Get contact of {}.", username);

        return searchContactsService.findByUsername(username);
    }

    /**
     * Returns contacts of all coworkers, i.e. people from the same office.
     */
    @RequestMapping(value = "coworkers", method = RequestMethod.GET)
    @ResponseBody
    public List<Contact> coworkers(Principal principal) {
        String username = principal.getName();
        LOGGER.debug("Get coworkers of {}.", username);

        String location = searchContactsService.findLocationOfUser(username);
        List<Contact> contacts = searchContactsService.findByLocation(location);
        LOGGER.debug("Found {} coworkers.", contacts.size());

        return contacts;
    }

}
