package com.example.financetracker.service;

import com.example.financetracker.dto.request.CategoryRequest;
import com.example.financetracker.dto.response.CategoryResponse;
import com.example.financetracker.entity.Category;
import com.example.financetracker.entity.User;
import com.example.financetracker.repository.CategoryRepository;
import com.example.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final UserRepository userRepository;

	// ─── CREATE ───────────────────────────────────────────

	public CategoryResponse createCategory(
		Long userId,
		CategoryRequest request) {

		// check among active categories only
		if (categoryRepository.existsByNameAndUserIdAndIsActiveTrue(
			request.getName(), userId)) {
			throw new RuntimeException(
				"Category already exists: " + request.getName());
		}

		User user = userRepository.findById(userId)
			.orElseThrow(() ->
				new RuntimeException("User not found"));

		Category category = Category.builder()
			.name(request.getName())
			.user(user)
			.build();

		return mapToResponse(categoryRepository.save(category));
	}

	// ─── READ ALL ─────────────────────────────────────────

	public List<CategoryResponse> getAllCategories(Long userId) {
		return categoryRepository
			.findByUserIdAndIsActiveTrue(userId)
			.stream()
			.map(this::mapToResponse)
			.toList();
	}

	// ─── UPDATE ───────────────────────────────────────────

	public CategoryResponse updateCategory(
		Long userId,
		Long categoryId,
		CategoryRequest request) {

		// only allow update on active category
		Category category = categoryRepository
			.findByIdAndIsActiveTrue(categoryId)
			.orElseThrow(() ->
				new RuntimeException("Category not found"));

		if (!category.getUser().getId().equals(userId)) {
			throw new RuntimeException("Access denied");
		}

		// check new name doesn't conflict with other active categories
		if (categoryRepository.existsByNameAndUserIdAndIsActiveTrue(
			request.getName(), userId)) {
			throw new RuntimeException(
				"Category already exists: " + request.getName());
		}

		category.setName(request.getName());
		return mapToResponse(categoryRepository.save(category));
	}

	// ─── SOFT DELETE ──────────────────────────────────────

	public void deleteCategory(
		Long userId,
		Long categoryId) {

		Category category = categoryRepository
			.findByIdAndIsActiveTrue(categoryId)
			.orElseThrow(() ->
				new RuntimeException("Category not found"));

		if (!category.getUser().getId().equals(userId)) {
			throw new RuntimeException("Access denied");
		}

		// soft delete — just mark inactive
		category.setIsActive(false);
		categoryRepository.save(category);
	}

	// ─── PRIVATE HELPERS ──────────────────────────────────

	private CategoryResponse mapToResponse(Category category) {
		return CategoryResponse.builder()
			.id(category.getId())
			.name(category.getName())
			.build();
	}
}