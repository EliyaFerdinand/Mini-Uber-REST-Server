package xcode.springcloud.dispatchservice;

import org.springframework.beans.factory.annotation.Autowired;

public class TripServiceFeignController
{
    @Autowired
    private TripServiceFeignClient tripServiceFeignClient;
}
