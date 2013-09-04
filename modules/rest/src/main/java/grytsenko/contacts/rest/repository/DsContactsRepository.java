package grytsenko.contacts.rest.repository;

import static java.text.MessageFormat.format;

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
 * 
 * <p>
 * This repository is the main data source.
 */
@Repository
public class DsContactsRepository {

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
    protected String usernameAttrId;
    @Value("#{ldapProperties['ldap.user.firstname']}")
    protected String firstnameAttrId;
    @Value("#{ldapProperties['ldap.user.lastname']}")
    protected String lastnameAttrId;
    @Value("#{ldapProperties['ldap.user.photoUrl']}")
    protected String photoUrlAttrId;
    @Value("#{ldapProperties['ldap.user.mail']}")
    protected String mailAttrId;
    @Value("#{ldapProperties['ldap.user.phone']}")
    protected String phoneAttrId;
    @Value("#{ldapProperties['ldap.user.location']}")
    protected String locationAttrId;
    @Value("#{ldapProperties['ldap.user.version']}")
    protected String versionAttrId;

    /**
     * Finds contact of user.
     * 
     * @param username
     *            the name of user.
     * 
     * @return the found contact or <code>null</code> if contact not found.
     */
    public DsContact findByUsername(String username) {
        LOGGER.debug("Search by username: {}.", username);

        String filter = format(filterByUsernameTemplate, username);
        List<DsContact> contacts = findByFilter(filter);

        return contacts.isEmpty() ? null : contacts.get(0);
    }

    /**
     * Finds contacts of all users in one location.
     * 
     * @param location
     *            the name of location.
     * 
     * @return the list of found contacts.
     */
    public List<DsContact> findByLocation(String location) {
        LOGGER.debug("Search by location: {}.", location);

        String filter = format(filterByLocationTemplate, location);
        return findByFilter(filter);
    }

    private List<DsContact> findByFilter(String filter) {
        LOGGER.debug("Search by filter: {}.", filter);

        LdapTemplate template = new LdapTemplate(ldapContextSource);

        String[] attrs = new String[] { usernameAttrId, firstnameAttrId,
                lastnameAttrId, photoUrlAttrId, mailAttrId, phoneAttrId,
                locationAttrId, versionAttrId };
        @SuppressWarnings("unchecked")
        List<DsContact> contacts = template.search(usersGroup, filter,
                SearchControls.ONELEVEL_SCOPE, attrs, new DsContactMapper());

        LOGGER.debug("Found {} contacts.", contacts.size());

        return contacts;
    }

    /**
     * Creates contact using data from DS.
     */
    private class DsContactMapper implements AttributesMapper {

        @Override
        public DsContact mapFromAttributes(Attributes attrs)
                throws NamingException {
            DsContact contact = new DsContact();

            contact.setUsername(asString(usernameAttrId, attrs));

            contact.setFirstName(asString(firstnameAttrId, attrs));
            contact.setLastName(asString(lastnameAttrId, attrs));

            contact.setPhotoUrl(asString(photoUrlAttrId, attrs));

            contact.setMail(asString(mailAttrId, attrs));
            contact.setPhone(asPhone(phoneAttrId, attrs));

            contact.setLocation(asString(locationAttrId, attrs));

            contact.setVersion(asString(versionAttrId, attrs));

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
