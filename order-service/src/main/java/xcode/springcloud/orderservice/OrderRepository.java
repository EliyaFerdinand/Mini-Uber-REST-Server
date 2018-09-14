package xcode.springcloud.orderservice;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends
        CrudRepository<Order, Long> {
}
