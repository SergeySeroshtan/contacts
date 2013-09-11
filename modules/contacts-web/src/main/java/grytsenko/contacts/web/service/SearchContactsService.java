package grytsenko.contacts.web.service;

import static grytsenko.contacts.web.service.ContactFactory.createContact;
import grytsenko.contacts.api.Contact;
import grytsenko.contacts.web.data.Employee;
import grytsenko.contacts.web.data.EmployeesRepository;
import grytsenko.contacts.web.data.Extras;
import grytsenko.contacts.web.data.ExtrasRepository;

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
    ExtrasRepository extrasRepository;

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
        Extras extras = extrasRepository.findOne(employeeUid);

        return createContact(employee, extras);
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
            Extras extras = extrasRepository.findOne(coworkerUid);
            contacts.add(createContact(coworker, extras));
        }

        return contacts;
    }

}
