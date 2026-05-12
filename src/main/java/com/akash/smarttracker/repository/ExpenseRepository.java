package com.akash.smarttracker.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.akash.smarttracker.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategory(String category);

}