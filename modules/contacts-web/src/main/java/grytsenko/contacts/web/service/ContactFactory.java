package grytsenko.contacts.web.service;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.data.Employee;
import grytsenko.contacts.web.data.Extras;

/**
 * Provides facilities for creating contacts.
 */
public final class ContactFactory {

    /**
     * Creates contact for given employee.
     * 
     * @param employee
     *            the information about employee.
     * @param extras
     *            the extra information (can be <code>null</code>).
     * 
     * @return the created contact.
     */
    public static Contact createContact(Employee employee, Extras extras) {
        Contact contact = new Contact();

        contact.setUsername(employee.getUsername());

        contact.setFirstName(employee.getFirstName());
        contact.setLastName(employee.getLastName());

        contact.setPhotoUrl(employee.getPhotoUrl());

        contact.setMail(employee.getMail());
        contact.setPhone(employee.getPhone());

        contact.setLocation(employee.getLocation());

        if (extras != null) {
            contact.setSkype(extras.getSkype());
            contact.setPosition(extras.getPosition());
        }

        contact.setVersion(getVersion(employee, extras));

        return contact;
    }

    private static String getVersion(Employee employee, Extras extras) {
        String major = employee.getVersion();
        String minor = extras != null ? extras.getVersion() : "";

        return major + minor;
    }

    private ContactFactory() {
    }

}
