package com.jpmc.midascore.kafka;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.foundation.Incentive;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import com.jpmc.midascore.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Component
public class TransactionListener {

    private final UserRepository userRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final RestTemplate restTemplate;

    public TransactionListener(
            UserRepository userRepository,
            TransactionRecordRepository transactionRecordRepository,
            RestTemplate restTemplate
    ) {
        this.userRepository = userRepository;
        this.transactionRecordRepository = transactionRecordRepository;
        this.restTemplate = restTemplate;
    }


    @KafkaListener(
            topics = "${general.kafka-topic}",
            groupId = "midas-core"
    )

    @Transactional
    public void receive(Transaction transaction) {

        var senderOpt = userRepository.findById(transaction.getSenderId());
        var recipientOpt = userRepository.findById(transaction.getRecipientId());

        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {
            return;
        }

        UserRecord sender = senderOpt.get();
        UserRecord recipient = recipientOpt.get();

        BigDecimal amount = BigDecimal.valueOf(transaction.getAmount());

        if (sender.getBalance().compareTo(amount) < 0) {
            return;
        }

        // ✅ STEP 5 — CALL INCENTIVES API
        Incentive incentive = restTemplate.postForObject(
                "http://localhost:8080/incentive",
                transaction,
                Incentive.class
        );

        BigDecimal incentiveAmount =
                incentive != null ? incentive.getAmount() : BigDecimal.ZERO;

        // ✅ STEP 6 — UPDATE BALANCES
        sender.setBalance(sender.getBalance().subtract(amount));

        recipient.setBalance(
                recipient.getBalance()
                        .add(amount)
                        .add(incentiveAmount)
        );

        // ✅ STEP 7 — SAVE TRANSACTION WITH INCENTIVE
        transactionRecordRepository.save(
                new TransactionRecord(sender, recipient, amount, incentiveAmount)
        );

        userRepository.save(sender);
        userRepository.save(recipient);
    }

}
