package grytsenko.contacts.rest.service;

import grytsenko.contacts.common.model.Contact;
import grytsenko.contacts.common.util.StringUtils;
import grytsenko.contacts.rest.repository.ContactsRepository;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Searches a contacts.
 */
@Service
public class SearchContactsService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SearchContactsService.class);

    @Autowired
    ContactsRepository contactsRepository;

    /**
     * Finds contact of single person.
     * 
     * @return the found contact or <code>null</code> if contact was not found.
     */
    public Contact findByUser(String username) {
        if (StringUtils.isNullOrEmpty(username)) {
            throw new IllegalArgumentException("User not defined.");
        }

        return contactsRepository.findByUsername(username);
    }

    /**
     * Determines location of user.
     * 
     * @return the location of user.
     */
    public String findLocationOfUser(String username) {
        if (StringUtils.isNullOrEmpty(username)) {
            throw new IllegalArgumentException("User not defined.");
        }

        LOGGER.debug("Get location of user {}.", username);

        Contact contact = findByUser(username);
        String location = contact.getLocation();
        LOGGER.debug("Location of user {} is {}.", username, location);

        return location;
    }

    /**
     * Finds contacts of people by location.
     */
    public List<Contact> findByLocation(String location) {
        if (StringUtils.isNullOrEmpty(location)) {
            throw new IllegalStateException("Location not defined.");
        }

        List<Contact> contacts = contactsRepository.findByLocation(location);

        return contacts;
    }

}
