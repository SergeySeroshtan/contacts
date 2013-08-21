package contacts.rest;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import contacts.model.Contact;
import contacts.service.SearchContactsService;

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
        String userName = principal.getName();

        LOGGER.debug("Search contact of {}.", userName);

        return searchContactsService.findByUser(userName);
    }

    /**
     * Returns contacts of all coworkers, i.e. people from the same office.
     */
    @RequestMapping(value = "coworkers", method = RequestMethod.GET)
    @ResponseBody
    public List<Contact> coworkers(Principal principal) {
        String userLocation = searchContactsService
                .findLocationOfUser(principal.getName());

        LOGGER.debug("Search contacts for people from {}.", userLocation);

        List<Contact> coworkersContacts = searchContactsService
                .findByLocation(userLocation);
        LOGGER.debug("Found {} contacts.", coworkersContacts.size());

        return coworkersContacts;
    }

}
