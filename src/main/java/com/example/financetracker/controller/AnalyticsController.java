package com.example.financetracker.controller;

import com.example.financetracker.dto.response.AnalyticsSummaryResponse;
import com.example.financetracker.dto.response.CategorySpendResponse;
import com.example.financetracker.dto.response.TrendResponse;
import com.example.financetracker.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/v2/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

	private final AnalyticsService analyticsService;

	@GetMapping("/summary")
	public ResponseEntity<AnalyticsSummaryResponse> getSummary(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime from,
		@RequestParam
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime to) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity.ok(
			analyticsService.getSummary(userId, from, to));
	}

	@GetMapping("/category")
	public ResponseEntity<List<CategorySpendResponse>> getCategorySpending(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime from,
		@RequestParam
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime to) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity.ok(
			analyticsService.getCategoryWiseSpending(
				userId, from, to));
	}

	@GetMapping("/trends")
	public ResponseEntity<List<TrendResponse>> getTrends(
		@AuthenticationPrincipal UserDetails userDetails,
		@RequestParam
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime from,
		@RequestParam
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime to) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity.ok(
			analyticsService.getTrends(userId, from, to));
	}

	private Long extractUserId(UserDetails userDetails) {
		return Long.parseLong(userDetails.getUsername());
	}
}