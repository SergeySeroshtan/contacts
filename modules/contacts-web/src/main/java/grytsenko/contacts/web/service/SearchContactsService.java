package grytsenko.contacts.web.service;

import static grytsenko.contacts.web.service.ContactFactory.createContact;
import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.repository.LdapEmployee;
import grytsenko.contacts.web.repository.LdapEmployeesRepository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Searches a contacts.
 */
@Service
public class SearchContactsService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SearchContactsService.class);

    @Autowired
    LdapEmployeesRepository ldapEmployeesRepository;

    /**
     * Finds contact of user.
     * 
     * @param username
     *            the name of user.
     * 
     * @return the found contact or <code>null</code> if contact not found.
     */
    public Contact findByUsername(String username) {
        if (!StringUtils.hasLength(username)) {
            throw new IllegalArgumentException("User not defined.");
        }

        LOGGER.debug("Search employee {} in DS.", username);
        LdapEmployee employee = ldapEmployeesRepository
                .findByUsername(username);

        return createContact(employee);
    }

    /**
     * Determines location of person.
     * 
     * @param username
     *            the name of user.
     * 
     * @return the location of user.
     */
    public String findLocationOfUser(String username) {
        if (!StringUtils.hasLength(username)) {
            throw new IllegalArgumentException("User not defined.");
        }

        LOGGER.debug("Search employee {} in DS.", username);
        LdapEmployee employee = ldapEmployeesRepository
                .findByUsername(username);
        String location = employee.getLocation();
        LOGGER.debug("Location of {} is {}.", username, location);

        return location;
    }

    /**
     * Finds contacts of people by location.
     * 
     * @param location
     *            the name of location.
     * 
     * @return the list of found contacts.
     */
    public List<Contact> findByLocation(String location) {
        if (!StringUtils.hasLength(location)) {
            throw new IllegalStateException("Location not defined.");
        }

        LOGGER.debug("Search employees from {} in DS.", location);
        List<LdapEmployee> employees = ldapEmployeesRepository
                .findByLocation(location);
        LOGGER.debug("Found {} employees in DS.", employees.size());

        List<Contact> contacts = new ArrayList<Contact>();
        for (LdapEmployee employee : employees) {
            contacts.add(createContact(employee));
        }

        return contacts;
    }

}
