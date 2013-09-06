package grytsenko.contacts.web.repository;

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
 * Repository of employees, that uses directory service as data source.
 */
@Repository
public class LdapEmployeesRepository {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(LdapEmployeesRepository.class);

    @Autowired
    LdapContextSource ldapContextSource;

    @Value("#{ldapProperties['ldap.employees']}")
    String usersGroup;

    @Value("#{ldapProperties['ldap.employees.filter.username']}")
    String filterByUsernameTemplate;
    @Value("#{ldapProperties['ldap.employees.filter.location']}")
    String filterByLocationTemplate;

    @Value("#{ldapProperties['ldap.employee.username']}")
    String usernameAttrId;
    @Value("#{ldapProperties['ldap.employee.firstname']}")
    String firstnameAttrId;
    @Value("#{ldapProperties['ldap.employee.lastname']}")
    String lastnameAttrId;
    @Value("#{ldapProperties['ldap.employee.photoUrl']}")
    String photoUrlAttrId;
    @Value("#{ldapProperties['ldap.employee.mail']}")
    String mailAttrId;
    @Value("#{ldapProperties['ldap.employee.phone']}")
    String phoneAttrId;
    @Value("#{ldapProperties['ldap.employee.location']}")
    String locationAttrId;
    @Value("#{ldapProperties['ldap.employee.version']}")
    String versionAttrId;

    /**
     * Finds employee.
     * 
     * @param username
     *            the unique identifier of employee..
     * 
     * @return the found employee or <code>null</code> if employee not found.
     */
    public LdapEmployee findByUsername(String username) {
        LOGGER.debug("Search by username: {}.", username);

        String filter = format(filterByUsernameTemplate, username);
        List<LdapEmployee> contacts = findByFilter(filter);

        return contacts.isEmpty() ? null : contacts.get(0);
    }

    /**
     * Finds all employees from specified location.
     * 
     * @param location
     *            the name of location.
     * 
     * @return the list of found employees.
     */
    public List<LdapEmployee> findByLocation(String location) {
        LOGGER.debug("Search by location: {}.", location);

        String filter = format(filterByLocationTemplate, location);
        return findByFilter(filter);
    }

    private List<LdapEmployee> findByFilter(String filter) {
        LOGGER.debug("Search by filter: {}.", filter);

        LdapTemplate template = new LdapTemplate(ldapContextSource);

        String[] attrs = new String[] { usernameAttrId, firstnameAttrId,
                lastnameAttrId, photoUrlAttrId, mailAttrId, phoneAttrId,
                locationAttrId, versionAttrId };
        @SuppressWarnings("unchecked")
        List<LdapEmployee> contacts = template.search(usersGroup, filter,
                SearchControls.ONELEVEL_SCOPE, attrs, new EmployeeMapper());

        LOGGER.debug("Found {} contacts.", contacts.size());

        return contacts;
    }

    /**
     * Creates employee from attributes.
     */
    private class EmployeeMapper implements AttributesMapper {

        @Override
        public LdapEmployee mapFromAttributes(Attributes attrs)
                throws NamingException {
            LdapEmployee employee = new LdapEmployee();

            employee.setUsername(asString(usernameAttrId, attrs));

            employee.setFirstName(asString(firstnameAttrId, attrs));
            employee.setLastName(asString(lastnameAttrId, attrs));

            employee.setPhotoUrl(asString(photoUrlAttrId, attrs));

            employee.setMail(asString(mailAttrId, attrs));
            employee.setPhone(asPhone(phoneAttrId, attrs));

            employee.setLocation(asString(locationAttrId, attrs));

            employee.setVersion(asString(versionAttrId, attrs));

            return employee;
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
                return null;
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
            if (!StringUtils.hasLength(value)) {
                return null;
            }

            String digits = value.replaceAll("\\D+", "");
            if (!StringUtils.hasLength(digits)) {
                return null;
            }

            return '+' + digits;
        }

    }

}
