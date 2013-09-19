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
package grytsenko.contacts.web.service;

import static grytsenko.contacts.web.data.mapper.ContactsMapper.asContact;
import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.data.Employee;
import grytsenko.contacts.web.data.EmployeeDetails;
import grytsenko.contacts.web.data.EmployeesDetailsRepository;
import grytsenko.contacts.web.data.EmployeesRepository;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Searches a contacts.
 */
@Service
public class SearchContactsService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SearchContactsService.class);

    @Autowired
    EmployeesRepository employeesRepository;
    @Autowired
    EmployeesDetailsRepository employeesDetailsRepository;

    /**
     * Finds contact of employee.
     * 
     * @param employeeUid
     *            the unique identifier of employee.
     * 
     * @return the found contact or <code>null</code> if contact not found.
     */
    public Contact findEmployee(String employeeUid) {
        if (!StringUtils.hasLength(employeeUid)) {
            throw new IllegalArgumentException("User not defined.");
        }

        LOGGER.debug("Search employee {}.", employeeUid);
        Employee employee = employeesRepository.findByUid(employeeUid);

        return createContact(employee);
    }

    /**
     * Finds contacts of coworkers of employee.
     * 
     * @param employeeUid
     *            the unique identifier of employee.
     * 
     * @return the list of found contacts.
     */
    public List<Contact> findCoworkers(String employeeUid) {
        if (!StringUtils.hasLength(employeeUid)) {
            throw new IllegalArgumentException("UID not defined.");
        }

        LOGGER.debug("Search employee {}.", employeeUid);
        Employee employee = employeesRepository.findByUid(employeeUid);
        String location = employee.getLocation();
        LOGGER.debug("Location of {} is {}.", employeeUid, location);

        LOGGER.debug("Search employees from {}.", location);
        List<Employee> coworkers = employeesRepository.findByLocation(location);
        LOGGER.debug("Found {} employees.", coworkers.size());

        List<Contact> contacts = new ArrayList<Contact>();
        for (Employee coworker : coworkers) {
            String coworkerUid = coworker.getUid();
            if (employeeUid.equals(coworkerUid)) {
                continue;
            }

            contacts.add(createContact(coworker));
        }

        return contacts;
    }

    private Contact createContact(Employee employee) {
        String uid = employee.getUid();
        EmployeeDetails details = employeesDetailsRepository.findOne(uid);
        if (details != null) {
            LOGGER.debug("Found details for {}.", employee);
        }
        return asContact(employee, details);
    }

}
