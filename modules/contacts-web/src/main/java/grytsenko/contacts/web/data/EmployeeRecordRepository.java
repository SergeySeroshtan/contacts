/**
 * Copyright (C) 2013 Anton Grytsenko (anthony.grytsenko@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grytsenko.contacts.web.data;

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
 * Repository for the information about employees.
 */
@Repository
public class EmployeeRecordRepository {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EmployeeRecordRepository.class);

    @Autowired
    LdapContextSource ldapContextSource;

    @Value("#{ldapProperties['ldap.employees']}")
    String employeesBase;

    @Value("#{ldapProperties['ldap.employees.filter.uid']}")
    String filterByUidTemplate;
    @Value("#{ldapProperties['ldap.employees.filter.location']}")
    String filterByLocationTemplate;

    @Value("#{ldapProperties['ldap.employee.uid']}")
    String uidAttrId;
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
     * @param uid
     *            the unique identifier of employee..
     * 
     * @return the found employee or <code>null</code> if employee not found.
     */
    public EmployeeRecord findByUid(String uid) {
        LOGGER.debug("Search employee by uid: {}.", uid);

        String filter = format(filterByUidTemplate, uid);
        List<EmployeeRecord> contacts = findByFilter(filter);

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
    public List<EmployeeRecord> findByLocation(String location) {
        LOGGER.debug("Search employees by location: {}.", location);

        String filter = format(filterByLocationTemplate, location);
        return findByFilter(filter);
    }

    private List<EmployeeRecord> findByFilter(String filter) {
        LOGGER.debug("Search emplyees by filter: {}.", filter);

        LdapTemplate template = new LdapTemplate(ldapContextSource);

        String[] attrs = new String[] { uidAttrId, firstnameAttrId,
                lastnameAttrId, photoUrlAttrId, mailAttrId, phoneAttrId,
                locationAttrId, versionAttrId };

        @SuppressWarnings("unchecked")
        List<EmployeeRecord> employees = template.search(employeesBase, filter,
                SearchControls.ONELEVEL_SCOPE, attrs,
                new EmployeeRecordMapper());

        LOGGER.debug("Found {} employees.", employees.size());

        return employees;
    }

    /**
     * Creates employee from attributes.
     */
    private class EmployeeRecordMapper implements AttributesMapper {

        @Override
        public EmployeeRecord mapFromAttributes(Attributes attrs)
                throws NamingException {
            EmployeeRecord employee = new EmployeeRecord();

            employee.setUid(asString(uidAttrId, attrs));

            employee.setFirstName(asString(firstnameAttrId, attrs));
            employee.setLastName(asString(lastnameAttrId, attrs));

            employee.setPhotoUrl(asString(photoUrlAttrId, attrs));

            employee.setMail(asString(mailAttrId, attrs));
            employee.setPhone(asPhone(phoneAttrId, attrs));

            employee.setLocation(asString(locationAttrId, attrs));

            employee.setVersion(asDigits(versionAttrId, attrs));

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
         * Considers that attribute is the set of digits.
         */
        private String asDigits(String attrId, Attributes attrs)
                throws NamingException {
            String value = asString(attrId, attrs);
            if (!StringUtils.hasLength(value)) {
                return null;
            }

            return value.replaceAll("\\D+", "");
        }

        /**
         * Considers that attribute contains phone number.
         */
        private String asPhone(String attrId, Attributes attrs)
                throws NamingException {
            String digits = asDigits(attrId, attrs);
            if (!StringUtils.hasLength(digits)) {
                return null;
            }

            return '+' + digits;
        }

    }

}
