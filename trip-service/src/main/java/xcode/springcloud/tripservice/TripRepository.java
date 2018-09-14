package xcode.springcloud.tripservice;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface TripRepository extends
        CrudRepository<Trip, Long> {

    List<Trip> findByDriverId(long driverId);
    List<Trip> findByRiderId(long riderId);
    List<Trip> findByDriverIdAndRiderId(long driverId, long riderId);
}
