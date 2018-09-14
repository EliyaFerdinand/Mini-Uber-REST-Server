package xcode.springcloud.accountservice;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface DriverRepository extends
        CrudRepository<Driver, Long> {

    List<Driver> findByFirstName(String firstName);
    List<Driver> findByLastName(String lastName);
    List<Driver> findByFirstNameAndLastName(String firstName, String lastName);
}
