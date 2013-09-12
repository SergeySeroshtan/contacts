package grytsenko.contacts.web.data;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Stores additional information about employees.
 */
public interface EmployeesDetailsRepository extends
        JpaRepository<EmployeeDetails, String> {
}
