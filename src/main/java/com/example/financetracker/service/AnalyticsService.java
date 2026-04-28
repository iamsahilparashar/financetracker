package com.example.financetracker.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.example.financetracker.dto.response.AnalyticsSummaryResponse;
import com.example.financetracker.dto.response.CategorySpendResponse;
import com.example.financetracker.dto.response.TrendResponse;
import com.example.financetracker.enums.TransactionType;
import com.example.financetracker.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

	private final TransactionRepository transactionRepository;

	// ─── SUMMARY ──────────────────────────────────────────
	// total credit, total debit, net balance

	public AnalyticsSummaryResponse getSummary(
		Long userId,
		LocalDateTime from,
		LocalDateTime to) {

		BigDecimal totalCredit = transactionRepository
			.sumByUserIdAndTypeAndDateRange(
				userId,
				TransactionType.CREDIT,
				from,
				to
			);

		BigDecimal totalDebit = transactionRepository
			.sumByUserIdAndTypeAndDateRange(
				userId,
				TransactionType.DEBIT,
				from,
				to
			);

		// handle nulls — no transactions in range
		totalCredit = totalCredit != null
			? totalCredit : BigDecimal.ZERO;
		totalDebit = totalDebit != null
			? totalDebit : BigDecimal.ZERO;

		BigDecimal netBalance = totalCredit.subtract(totalDebit);

		return AnalyticsSummaryResponse.builder()
			.totalCredit(totalCredit)
			.totalDebit(totalDebit)
			.netBalance(netBalance)
			.build();
	}

	// ─── CATEGORY WISE SPENDING ───────────────────────────

	public List<CategorySpendResponse> getCategoryWiseSpending(
		Long userId,
		LocalDateTime from,
		LocalDateTime to) {

		List<Object[]> rawResults = transactionRepository
			.findCategoryWiseSpending(userId, from, to);

		return rawResults.stream()
			.map(row -> CategorySpendResponse.builder()
				.categoryName((String) row[0])
				.totalSpent((BigDecimal) row[1])
				.build())
			.toList();
	}

	// ─── TRENDS ───────────────────────────────────────────
	// category wise spending by month
	// + percentage change vs previous month

	public List<TrendResponse> getTrends(
		Long userId,
		LocalDateTime from,
		LocalDateTime to) {

		List<Object[]> rawResults = transactionRepository
			.findCategoryWiseSpendingByMonth(userId, from, to);

		// map raw results to intermediate structure
		// { categoryName -> { month -> totalSpent } }
		// LinkedHashMap preserves insertion order (month ASC)
		Map<String, Map<String, BigDecimal>> categoryMonthMap =
			new LinkedHashMap<>();

		for (Object[] row : rawResults) {
			String categoryName = (String) row[0];
			String month = (String) row[1];
			BigDecimal totalSpent = (BigDecimal) row[2];

			categoryMonthMap
				.computeIfAbsent(categoryName,
					k -> new LinkedHashMap<>())
				.put(month, totalSpent);
		}

		// calculate % change per category per month
		List<TrendResponse> trends = new ArrayList<>();

		categoryMonthMap.forEach((category, monthlyData) -> {
			List<String> months = new ArrayList<>(
				monthlyData.keySet());

			for (int i = 0; i < months.size(); i++) {
				String month = months.get(i);
				BigDecimal currentSpend = monthlyData.get(month);
				Double changePercentage = null;

				if (i > 0) {
					String prevMonth = months.get(i - 1);
					BigDecimal previousSpend =
						monthlyData.get(prevMonth);

					if (previousSpend.compareTo(BigDecimal.ZERO) != 0) {
						// change% = ((current - previous) / previous) * 100
						changePercentage = currentSpend
							.subtract(previousSpend)
							.divide(previousSpend, 4,
								RoundingMode.HALF_UP)
							.multiply(BigDecimal.valueOf(100))
							.doubleValue();
					}
				}

				trends.add(TrendResponse.builder()
					.categoryName(category)
					.month(month)
					.totalSpent(currentSpend)
					.changePercentage(changePercentage)
					.build());
			}
		});

		return trends;
	}
}