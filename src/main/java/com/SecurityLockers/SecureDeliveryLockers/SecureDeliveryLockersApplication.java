package com.SecurityLockers.SecureDeliveryLockers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@EnableScheduling
@SpringBootApplication
public class SecureDeliveryLockersApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecureDeliveryLockersApplication.class, args);
	}

}
