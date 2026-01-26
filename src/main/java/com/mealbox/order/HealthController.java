package com.mealbox.order;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/order/health")
    public String health() {
        return "Order Service is UP";
    }
}
