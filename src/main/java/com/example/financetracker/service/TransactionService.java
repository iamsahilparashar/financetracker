package com.example.financetracker.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.financetracker.dto.request.CategorySplitRequest;
import com.example.financetracker.dto.request.TransactionRequest;
import com.example.financetracker.dto.response.CategorySplitResponse;
import com.example.financetracker.dto.response.TransactionResponse;
import com.example.financetracker.entity.Category;
import com.example.financetracker.entity.Transaction;
import com.example.financetracker.entity.TransactionCategory;
import com.example.financetracker.entity.User;
import com.example.financetracker.repository.CategoryRepository;
import com.example.financetracker.repository.TransactionCategoryRepository;
import com.example.financetracker.repository.TransactionRepository;
import com.example.financetracker.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;
	private final UserRepository userRepository;
	private final CategoryRepository categoryRepository;
	private final TransactionCategoryRepository transactionCategoryRepository;

	// ─── CREATE ───────────────────────────────────────────

	@Transactional
	public TransactionResponse createTransaction(
		Long loggedInUserId,
		TransactionRequest request) {

		// 1. validate split percentages sum to 100
		validateSplitPercentages(request.getCategories());

		// 2. fetch the logged in user
		User user = userRepository.findById(loggedInUserId)
			.orElseThrow(() ->
				new RuntimeException("User not found"));

		// 3. resolve sender — shadow user if needed
		User sender = resolveUser(
			request.getSenderPhone(),
			request.getSenderName()
		);

		// 4. resolve receiver — shadow user if needed
		User receiver = resolveUser(
			request.getReceiverPhone(),
			request.getReceiverName()
		);

		// 5. build and save transaction
		Transaction transaction = Transaction.builder()
			.user(user)
			.amount(request.getAmount())
			.source(request.getSource())
			.description(request.getDescription())
			.transactionType(request.getTransactionType())
			.datetime(request.getDatetime())
			.sender(sender)
			.receiver(receiver)
			.build();

		transaction = transactionRepository.save(transaction);

		// 6. save category splits
		if (request.getCategories() != null
			&& !request.getCategories().isEmpty()) {
			saveCategories(transaction, request.getCategories());
		}

		return mapToResponseWithCategories(transaction,
			transactionCategoryRepository
				.findByTransactionId(transaction.getId()));
	}

	// ─── READ SINGLE ──────────────────────────────────────

	public TransactionResponse getTransaction(
		Long loggedInUserId,
		Long transactionId) {

		Transaction transaction = transactionRepository
			.findById(transactionId)
			.orElseThrow(() ->
				new RuntimeException("Transaction not found"));

		if (!transaction.getUser().getId().equals(loggedInUserId)) {
			throw new RuntimeException("Access denied");
		}

		List<TransactionCategory> categories =
			transactionCategoryRepository
				.findByTransactionId(transactionId);

		return mapToResponseWithCategories(transaction, categories);
	}

	// ─── READ ALL — cursor based pagination ───────────────

	public Slice<TransactionResponse> getAllTransactions(
		Long loggedInUserId,
		LocalDateTime cursor,
		int size) {

		if (cursor == null) cursor = LocalDateTime.now();

		Slice<Transaction> transactions = transactionRepository
			.findByUserIdAndDatetimeBeforeOrderByDatetimeDesc(
				loggedInUserId,
				cursor,
				PageRequest.of(0, size)
			);

		// batch fetch — fix N+1
		List<Long> transactionIds = transactions.getContent()
			.stream()
			.map(Transaction::getId)
			.toList();

		Map<Long, List<TransactionCategory>> categoryMap =
			transactionCategoryRepository
				.findByTransactionIdIn(transactionIds)
				.stream()
				.collect(Collectors.groupingBy(
					tc -> tc.getTransaction().getId()
				));

		return transactions.map(t ->
			mapToResponseWithCategories(t,
				categoryMap.getOrDefault(
					t.getId(), List.of()))
		);
	}

	// ─── UPDATE ───────────────────────────────────────────

	@Transactional
	public TransactionResponse updateTransaction(
		Long loggedInUserId,
		Long transactionId,
		TransactionRequest request) {

		Transaction transaction = transactionRepository
			.findById(transactionId)
			.orElseThrow(() ->
				new RuntimeException("Transaction not found"));

		if (!transaction.getUser().getId().equals(loggedInUserId)) {
			throw new RuntimeException("Access denied");
		}

		validateSplitPercentages(request.getCategories());

		transaction.setAmount(request.getAmount());
		transaction.setSource(request.getSource());
		transaction.setDescription(request.getDescription());
		transaction.setTransactionType(request.getTransactionType());
		transaction.setDatetime(request.getDatetime());
		transaction.setSender(resolveUser(
			request.getSenderPhone(),
			request.getSenderName()
		));
		transaction.setReceiver(resolveUser(
			request.getReceiverPhone(),
			request.getReceiverName()
		));

		transactionRepository.save(transaction);

		// delete old categories and re-save
		transactionCategoryRepository
			.deleteByTransactionId(transactionId);

		if (request.getCategories() != null
			&& !request.getCategories().isEmpty()) {
			saveCategories(transaction, request.getCategories());
		}

		List<TransactionCategory> updatedCategories =
			transactionCategoryRepository
				.findByTransactionId(transactionId);

		return mapToResponseWithCategories(
			transaction, updatedCategories);
	}

	// ─── DELETE ───────────────────────────────────────────

	@Transactional
	public void deleteTransaction(
		Long loggedInUserId,
		Long transactionId) {

		Transaction transaction = transactionRepository
			.findById(transactionId)
			.orElseThrow(() ->
				new RuntimeException("Transaction not found"));

		if (!transaction.getUser().getId().equals(loggedInUserId)) {
			throw new RuntimeException("Access denied");
		}

		transactionRepository.delete(transaction);
	}

	// ─── PRIVATE HELPERS ──────────────────────────────────

	private void validateSplitPercentages(
		List<CategorySplitRequest> categories) {

		if (categories == null || categories.isEmpty()) return;

		BigDecimal total = categories.stream()
			.map(CategorySplitRequest::getSplitPercentage)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		if (total.compareTo(new BigDecimal("100.00")) != 0) {
			throw new RuntimeException(
				"Split percentages must sum to 100. Got: "
					+ total);
		}
	}

	private User resolveUser(String phone, String name) {
		if (phone == null || phone.isBlank()) return null;

		return userRepository.findByPhone(phone)
			.orElseGet(() -> userRepository.save(
				User.builder()
					.phone(phone)
					.name(name != null ? name : "Unknown")
					.isApplicationUser(false)
					.build()
			));
	}

	private void saveCategories(
		Transaction transaction,
		List<CategorySplitRequest> categoryRequests) {

		List<TransactionCategory> transactionCategories =
			categoryRequests.stream().map(req -> {
				Category category = categoryRepository
					.findById(req.getCategoryId())
					.orElseThrow(() ->
						new RuntimeException(
							"Category not found: "
								+ req.getCategoryId()));

				return TransactionCategory.builder()
					.transaction(transaction)
					.category(category)
					.splitPercentage(req.getSplitPercentage())
					.build();
			}).toList();

		transactionCategoryRepository.saveAll(transactionCategories);
	}

	private TransactionResponse mapToResponseWithCategories(
		Transaction t,
		List<TransactionCategory> categories) {

		List<CategorySplitResponse> categoryResponses = categories
			.stream()
			.map(tc -> CategorySplitResponse.builder()
				.categoryId(tc.getCategory().getId())
				.categoryName(tc.getCategory().getName())
				.splitPercentage(tc.getSplitPercentage())
				.build())
			.toList();

		return TransactionResponse.builder()
			.id(t.getId())
			.amount(t.getAmount())
			.source(t.getSource())
			.description(t.getDescription())
			.transactionType(t.getTransactionType())
			.datetime(t.getDatetime())
			.senderName(t.getSender() != null
				? t.getSender().getName() : null)
			.receiverName(t.getReceiver() != null
				? t.getReceiver().getName() : null)
			.categories(categoryResponses)
			.build();
	}
}