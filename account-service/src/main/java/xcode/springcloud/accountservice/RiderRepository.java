package xcode.springcloud.accountservice;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface RiderRepository extends
        CrudRepository<Rider, Long> {

    List<Rider> findByFirstName(String firstName);
    List<Rider> findByLastName(String lastName);
    List<Rider> findByFirstNameAndLastName(String firstName, String lastName);
}

