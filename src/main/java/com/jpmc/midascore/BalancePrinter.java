package com.jpmc.midascore;

import com.jpmc.midascore.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class BalancePrinter {

    private final UserRepository userRepository;

    public BalancePrinter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void printBalances() throws InterruptedException {
        // 1️⃣ Wait for Kafka to finish processing transactions
        Thread.sleep(3000);

        // 2️⃣ Print all users and balances
        userRepository.findAll().forEach(user ->
                System.out.println(
                        "USER => id=" + user.getId()
                                + ", name=" + user.getName()
                                + ", balance=" + user.getBalance()
                )
        );
    }
}
