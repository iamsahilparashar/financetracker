package com.example.financetracker.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.financetracker.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponse {
	private Long id;
	private BigDecimal amount;
	private String source;
	private String description;
	private TransactionType transactionType;
	private LocalDateTime datetime;
	private String senderName;
	private String receiverName;
	private List<CategorySplitResponse> categories;
}