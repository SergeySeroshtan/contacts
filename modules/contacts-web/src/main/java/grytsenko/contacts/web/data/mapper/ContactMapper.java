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
package grytsenko.contacts.web.data.mapper;

import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.data.EmployeeDetails;
import grytsenko.contacts.web.data.EmployeeRecord;

import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;

/**
 * Helps to get contact information.
 */
public final class ContactMapper {

    /**
     * Gets contact of employee.
     * 
     * @param record
     *            the information about employee.
     * @param details
     *            the detailed information about employee (optional).
     * 
     * @return the created contact.
     */
    public static Contact map(EmployeeRecord record, EmployeeDetails details) {
        if (record == null) {
            throw new IllegalArgumentException("Invalid record.");
        }
        Contact contact = new Contact();

        Mapper mapper = new DozerBeanMapper();
        mapper.map(record, contact);
        if (details != null) {
            mapper.map(details, contact);
        }

        contact.setVersion(mapVersion(record, details));

        return contact;
    }

    private static String mapVersion(EmployeeRecord record,
            EmployeeDetails details) {
        String major = record.getVersion();
        String minor = details != null ? "." + details.getVersion() : "";
        return major + minor;
    }

    private ContactMapper() {
    }

}
