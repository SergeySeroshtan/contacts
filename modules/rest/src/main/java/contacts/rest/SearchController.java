package contacts.rest;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    public Contact search(Principal principal) {
        String userName = principal.getName();

        LOGGER.debug("Search contact of {}.", userName);

        return searchContactsService.findByUser(userName);
    }

    /**
     * Searches a contacts.
     * 
     * <p>
     * If locations are not defined, then location of current user will be used.
     */
    @RequestMapping(value = "search", method = RequestMethod.GET)
    @ResponseBody
    public List<Contact> search(
            Principal principal,
            @RequestParam(value = "locations", required = false) String[] locations) {
        if (locations == null || locations.length == 0) {
            String userLocation = searchContactsService
                    .findLocationOfUser(principal.getName());
            locations = new String[] { userLocation };
        }

        LOGGER.debug("Search contacts for people from {}.",
                Arrays.toString(locations));

        Set<String> uniqueLocations = new HashSet<String>();
        Collections.addAll(uniqueLocations, locations);

        List<Contact> allContacts = new ArrayList<Contact>();
        for (String location : uniqueLocations) {
            LOGGER.debug("Search contacts for people from {}.", location);
            List<Contact> localContacts = searchContactsService
                    .findByLocation(location);
            LOGGER.debug("Found {} contacts for {}.", localContacts.size(),
                    location);
            allContacts.addAll(localContacts);
        }
        LOGGER.debug("Found {} contacts.", allContacts.size());

        return allContacts;
    }

}
