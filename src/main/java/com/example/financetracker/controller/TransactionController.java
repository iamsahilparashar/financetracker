package com.example.financetracker.controller;

import com.example.financetracker.dto.request.TransactionRequest;
import com.example.financetracker.dto.response.TransactionResponse;
import com.example.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/v2/transactions")
@RequiredArgsConstructor
public class TransactionController {

	private final TransactionService transactionService;

	@PostMapping
	public ResponseEntity<TransactionResponse> createTransaction(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody TransactionRequest request) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(transactionService
				.createTransaction(userId, request));
	}

	@GetMapping("/{id}")
	public ResponseEntity<TransactionResponse> getTransaction(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long id) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity.ok(
			transactionService.getTransaction(userId, id));
	}

	@GetMapping
	public ResponseEntity<Slice<TransactionResponse>> getAllTransactions(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam(required = false)
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime cursor,
		@RequestParam(defaultValue = "20") int size) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity.ok(
			transactionService.getAllTransactions(
				userId, cursor, size));
	}

	@PutMapping("/{id}")
	public ResponseEntity<TransactionResponse> updateTransaction(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long id,
		@Valid @RequestBody TransactionRequest request) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity.ok(
			transactionService.updateTransaction(
				userId, id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteTransaction(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long id) {

		Long userId = extractUserId(userDetails);
		transactionService.deleteTransaction(userId, id);
		return ResponseEntity.noContent().build();
	}

	private Long extractUserId(UserDetails userDetails) {
		return Long.parseLong(userDetails.getUsername());
	}
}