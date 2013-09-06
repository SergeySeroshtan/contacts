package grytsenko.contacts.web.service;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.repository.LdapEmployee;

/**
 * Provides facilities for creating contacts.
 */
public final class ContactFactory {

    /**
     * Creates contact for given employee.
     * 
     * @param employee
     *            the information about employee.
     * 
     * @return the created contact.
     */
    public static Contact createContact(LdapEmployee employee) {
        Contact contact = new Contact();

        contact.setUsername(employee.getUsername());

        contact.setFirstName(employee.getFirstName());
        contact.setLastName(employee.getLastName());

        contact.setPhotoUrl(employee.getPhotoUrl());

        contact.setMail(employee.getMail());
        contact.setPhone(employee.getPhone());

        contact.setLocation(employee.getLocation());
        contact.setVersion(employee.getVersion());

        return contact;
    }

    private ContactFactory() {
    }

}
