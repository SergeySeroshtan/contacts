package grytsenko.contacts.web.util;

import grytsenko.contacts.api.Contact;

import java.util.Comparator;

import org.springframework.util.StringUtils;

/**
 * Compares contacts by full name of employee.
 */
public final class FullNameComparator implements Comparator<Contact> {

    @Override
    public int compare(Contact first, Contact second) {
        validateName(first);
        validateName(second);

        int lastNames = first.getLastName().compareTo(second.getLastName());
        if (lastNames != 0) {
            return lastNames;
        }

        return first.getFirstName().compareTo(second.getFirstName());
    }

    private void validateName(Contact contact) {
        if (!StringUtils.hasText(contact.getFirstName())) {
            throw new IllegalArgumentException("First name not defined.");
        }
        if (!StringUtils.hasText(contact.getLastName())) {
            throw new IllegalArgumentException("Last name not defined.");
        }
    }

}
