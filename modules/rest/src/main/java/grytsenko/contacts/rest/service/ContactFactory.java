package grytsenko.contacts.rest.service;

import grytsenko.contacts.common.model.Contact;
import grytsenko.contacts.rest.repository.DsContact;

/**
 * Provides facilities for creating contacts.
 */
public final class ContactFactory {

    /**
     * Creates contact using data from DS.
     * 
     * @param dsContact
     *            the data from DS.
     * 
     * @return the created contact.
     */
    public static Contact createContact(DsContact dsContact) {
        Contact contact = new Contact();

        contact.setUsername(dsContact.getUsername());

        contact.setFirstName(dsContact.getFirstName());
        contact.setLastName(dsContact.getLastName());

        contact.setPhotoUrl(dsContact.getPhotoUrl());

        contact.setMail(dsContact.getMail());
        contact.setPhone(dsContact.getPhone());

        contact.setLocation(dsContact.getLocation());
        contact.setVersion(dsContact.getVersion());

        return contact;
    }

    private ContactFactory() {
    }

}
