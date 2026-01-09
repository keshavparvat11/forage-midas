package com.jpmc.midascore;

import com.jpmc.midascore.repository.UserRepository;
import org.springframework.stereotype.Component;

@Component
public class DebugBalanceService {

    private final UserRepository userRepository;

    public DebugBalanceService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void printWaldorf() {
        userRepository.findAll().forEach(user -> {
            if ("waldorf".equalsIgnoreCase(user.getName())) {
                System.out.println(
                        "FINAL WALDORF BALANCE = " + user.getBalance()
                );
            }
        });
    }
}
