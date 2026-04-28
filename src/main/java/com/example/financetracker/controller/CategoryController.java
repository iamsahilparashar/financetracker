package com.example.financetracker.controller;

import com.example.financetracker.dto.request.CategoryRequest;
import com.example.financetracker.dto.response.CategoryResponse;
import com.example.financetracker.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v2/categories")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	public ResponseEntity<CategoryResponse> createCategory(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody CategoryRequest request) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity
			.status(HttpStatus.CREATED)
			.body(categoryService
				.createCategory(userId, request));
	}

	@GetMapping
	public ResponseEntity<List<CategoryResponse>> getAllCategories(
		@AuthenticationPrincipal UserDetails userDetails) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity.ok(
			categoryService.getAllCategories(userId));
	}

	@PutMapping("/{id}")
	public ResponseEntity<CategoryResponse> updateCategory(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long id,
		@Valid @RequestBody CategoryRequest request) {

		Long userId = extractUserId(userDetails);
		return ResponseEntity.ok(
			categoryService.updateCategory(userId, id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteCategory(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long id) {

		Long userId = extractUserId(userDetails);
		categoryService.deleteCategory(userId, id);
		return ResponseEntity.noContent().build();
	}

	private Long extractUserId(UserDetails userDetails) {
		return Long.parseLong(userDetails.getUsername());
	}
}