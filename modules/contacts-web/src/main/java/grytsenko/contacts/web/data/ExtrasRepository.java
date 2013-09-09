package grytsenko.contacts.web.data;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository of details about employees, that uses database as data source.
 */
public interface ExtrasRepository extends JpaRepository<Extras, Integer> {

    /**
     * Finds extras for the specific employee.
     */
    Extras findByUsername(String username);

}
