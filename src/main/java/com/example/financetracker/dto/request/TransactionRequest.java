package com.example.financetracker.dto.request;


import com.example.financetracker.enums.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequest {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String source;

    private String description;

    @NotNull(message = "Transaction type is required")
    private TransactionType transactionType;

    @NotNull(message = "Datetime is required")
    private LocalDateTime datetime;

    // phone number of sender/receiver
    private String senderPhone;
    private String senderName;
    private String receiverPhone;
    private String receiverName;

    private List<CategorySplitRequest> categories;
}