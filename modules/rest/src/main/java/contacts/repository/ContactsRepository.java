package contacts.repository;


import java.util.List;

import contacts.model.Contact;

/**
 * Repository of contacts.
 */
public interface ContactsRepository {

    /**
     * Finds contact of concrete person.
     * 
     * @return the found contact or <code>null</code> if contact was not found.
     */
    Contact findByUsername(String username);

    /**
     * Finds all contacts of people from one location.
     */
    List<Contact> findByLocation(String location);

}