package grytsenko.contacts.web.data.mapper;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.data.Employee;
import grytsenko.contacts.web.data.EmployeeDetails;

/**
 * Provides facilities for mapping data from repositories to contacts.
 */
public final class ContactsMapper {

    /**
     * Represents information about employee as contact.
     * 
     * @param employee
     *            the information about employee.
     * @param details
     *            the additional information about employee (optional).
     * 
     * @return the created contact.
     */
    public static Contact asContact(Employee employee, EmployeeDetails details) {
        Contact contact = new Contact();

        contact.setUid(employee.getUid());

        contact.setFirstName(employee.getFirstName());
        contact.setLastName(employee.getLastName());

        contact.setPhotoUrl(employee.getPhotoUrl());

        contact.setMail(employee.getMail());
        contact.setPhone(employee.getPhone());

        contact.setLocation(employee.getLocation());

        if (details != null) {
            contact.setSkype(details.getSkype());
            contact.setPosition(details.getPosition());
        }

        contact.setVersion(buildVersion(employee, details));

        return contact;
    }

    private static String buildVersion(Employee employee,
            EmployeeDetails details) {
        String major = employee.getVersion();
        String minor = details != null ? "." + details.getVersion() : "";
        return major + minor;
    }

    private ContactsMapper() {
    }

}
