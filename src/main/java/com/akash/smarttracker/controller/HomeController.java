package com.akash.smarttracker.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Optional;
import jakarta.servlet.http.HttpSession;

import com.akash.smarttracker.model.Expense;
import com.akash.smarttracker.repository.ExpenseRepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

@Controller
public class HomeController {

    @Autowired
    private ExpenseRepository expenseRepository;
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
    @PostMapping("/login")
    public String login(String username, String password, HttpSession session) {

        if (username.equals("admin") && password.equals("admin123")) {
            session.setAttribute("user", username);
            return "redirect:/";
        }

        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(Model model,HttpSession session) {
    	
    	if (session.getAttribute("user") == null) {
    	    return "redirect:/login";
    	}
    	
    	var expenses = expenseRepository.findAll();

    	double total = expenses.stream()
    	        .mapToDouble(Expense::getAmount)
    	        .sum();
    	
    	int count = expenses.size();

    	model.addAttribute("expenses", expenses);
    	model.addAttribute("total", total);
    	model.addAttribute("count", count);
    	
    	double foodTotal = expenses.stream()
    	        .filter(e -> "Food".equalsIgnoreCase(e.getCategory()))
    	        .mapToDouble(Expense::getAmount)
    	        .sum();

    	double travelTotal = expenses.stream()
    	        .filter(e -> "Travel".equalsIgnoreCase(e.getCategory()))
    	        .mapToDouble(Expense::getAmount)
    	        .sum();

    	double shoppingTotal = expenses.stream()
    	        .filter(e -> "Shopping".equalsIgnoreCase(e.getCategory()))
    	        .mapToDouble(Expense::getAmount)
    	        .sum();

    	double billsTotal = expenses.stream()
    	        .filter(e -> "Bills".equalsIgnoreCase(e.getCategory()))
    	        .mapToDouble(Expense::getAmount)
    	        .sum();

    	double educationTotal = expenses.stream()
    	        .filter(e -> "Education".equalsIgnoreCase(e.getCategory()))
    	        .mapToDouble(Expense::getAmount)
    	        .sum();

    	double otherTotal = expenses.stream()
    	        .filter(e -> "Other".equalsIgnoreCase(e.getCategory()))
    	        .mapToDouble(Expense::getAmount)
    	        .sum();

    	model.addAttribute("foodTotal", foodTotal);
    	model.addAttribute("travelTotal", travelTotal);
    	model.addAttribute("shoppingTotal", shoppingTotal);
    	model.addAttribute("billsTotal", billsTotal);
    	model.addAttribute("educationTotal", educationTotal);
    	model.addAttribute("otherTotal", otherTotal);
    	
        return "index";
    }

    @PostMapping("/addExpense")
    public String addExpense(Expense expense) {
        expenseRepository.save(expense);
        return "redirect:/";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteExpense(@PathVariable Long id) {
        expenseRepository.deleteById(id);
        return "redirect:/";
    }
    
    @GetMapping("/edit/{id}")
    public String editExpense(@PathVariable Long id, Model model) {

        Optional<Expense> expense = expenseRepository.findById(id);

        model.addAttribute("expense", expense.get());

        return "edit";
    }
    
    @PostMapping("/updateExpense")
    public String updateExpense(Expense expense) {

        expenseRepository.save(expense);

        return "redirect:/";
        
        
        }
    @GetMapping("/filter")
    public String filterExpenses(@RequestParam String category,
                                 Model model,
                                 HttpSession session) {

        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }

        var expenses = expenseRepository.findByCategory(category);

        double total = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();

        int count = expenses.size();

        model.addAttribute("expenses", expenses);
        model.addAttribute("total", total);
        model.addAttribute("count", count);

        return "index";
    }
    
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadPdf() {

        var expenses = expenseRepository.findAll();

        Document document = new Document();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, out);

            document.open();

            document.add(new Paragraph("Smart Expense Tracker Report"));
            document.add(new Paragraph(" "));

            for (Expense expense : expenses) {

                document.add(new Paragraph(
                        "Title: " + expense.getTitle()
                        + " | Amount: ₹" + expense.getAmount()
                        + " | Category: " + expense.getCategory()
                ));
            }

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        ByteArrayInputStream bis = new ByteArrayInputStream(out.toByteArray());

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=expenses.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }
}