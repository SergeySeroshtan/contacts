package grytsenko.contacts.rest.repository;

import static java.text.MessageFormat.format;
import grytsenko.contacts.common.model.Contact;

import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * Repository of contacts in directory service.
 */
@Repository
public class DsContactsRepository implements ContactsRepository {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(DsContactsRepository.class);

    @Autowired
    protected LdapContextSource ldapContextSource;

    @Value("#{ldapProperties['ldap.users']}")
    protected String usersGroup;

    @Value("#{ldapProperties['ldap.users.filter.username']}")
    protected String filterByUsernameTemplate;
    @Value("#{ldapProperties['ldap.users.filter.location']}")
    protected String filterByLocationTemplate;

    @Value("#{ldapProperties['ldap.user.username']}")
    protected String usernameAttr;
    @Value("#{ldapProperties['ldap.user.firstname']}")
    protected String firstnameAttr;
    @Value("#{ldapProperties['ldap.user.lastname']}")
    protected String lastnameAttr;
    @Value("#{ldapProperties['ldap.user.photoUrl']}")
    protected String photoUrlAttr;
    @Value("#{ldapProperties['ldap.user.mail']}")
    protected String mailAttr;
    @Value("#{ldapProperties['ldap.user.phone']}")
    protected String phoneAttr;
    @Value("#{ldapProperties['ldap.user.location']}")
    protected String locationAttr;

    @Value("#{ldapProperties['ldap.user.updatedAt']}")
    protected String updatedAtAttr;

    @Override
    public Contact findByUsername(String username) {
        LOGGER.debug("Search contact for {}.", username);

        String filter = format(filterByUsernameTemplate, username);
        List<Contact> contacts = findUsingFilter(filter);

        if (contacts.isEmpty()) {
            LOGGER.debug("Contact for {} was not found.", username);
            return null;
        }

        return contacts.get(0);
    }

    @Override
    public List<Contact> findByLocation(String location) {
        LOGGER.debug("Search contacts of people from {}.", location);

        String filter = format(filterByLocationTemplate, location);
        return findUsingFilter(filter);
    }

    private List<Contact> findUsingFilter(String filter) {
        LOGGER.debug("Find contacts using filter '{}'.", filter);

        LdapTemplate template = new LdapTemplate(ldapContextSource);

        String[] attrs = new String[] { usernameAttr, firstnameAttr,
                lastnameAttr, photoUrlAttr, mailAttr, phoneAttr, locationAttr,
                updatedAtAttr };
        @SuppressWarnings("unchecked")
        List<Contact> contacts = template.search(usersGroup, filter,
                SearchControls.ONELEVEL_SCOPE, attrs, new ContactMapper());

        LOGGER.debug("Found {} contacts.", contacts.size());

        return contacts;
    }

    /**
     * Factory, that creates a contact using data from DS.
     */
    private class ContactMapper implements AttributesMapper {

        @Override
        public Contact mapFromAttributes(Attributes attrs)
                throws NamingException {
            Contact contact = new Contact();

            contact.setUsername(asString(usernameAttr, attrs));

            contact.setFirstName(asString(firstnameAttr, attrs));
            contact.setLastName(asString(lastnameAttr, attrs));

            contact.setPhotoUrl(asString(photoUrlAttr, attrs));

            contact.setMail(asString(mailAttr, attrs));
            contact.setPhone(asPhone(phoneAttr, attrs));

            contact.setLocation(asString(locationAttr, attrs));

            contact.setVersion(asString(updatedAtAttr, attrs));

            return contact;
        }

        /**
         * Returns string value of attribute.
         * 
         * <p>
         * If attribute is not found, then returns empty string.
         */
        private String asString(String attrId, Attributes attrs)
                throws NamingException {
            Attribute attr = attrs.get(attrId);
            if (attr == null) {
                return "";
            }

            return (String) attr.get();
        }

        /**
         * Considers that attribute contains phone number.
         * 
         * <p>
         * Parses and returns this phone number.
         */
        private String asPhone(String attrId, Attributes attrs)
                throws NamingException {
            String value = asString(attrId, attrs);
            String digits = value.replaceAll("\\D+", "");

            if (!StringUtils.hasLength(digits)) {
                return "";
            }

            return '+' + digits;
        }

    }

}
