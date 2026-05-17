package com.familyexpense.models;

import java.time.LocalDateTime;

public class Expense {
    private int id;
    private int memberId;
    private String memberName;
    private double amount;
    private String category;
    private String categoryEmoji;
    private String date;
    private String note;
    private LocalDateTime createdAt;

    public Expense() {}

    public Expense(int memberId, double amount, String category, String date, String note) {
        this.memberId = memberId;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }

    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getCategoryEmoji() { return categoryEmoji; }
    public void setCategoryEmoji(String categoryEmoji) { this.categoryEmoji = categoryEmoji; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getFormattedAmount() {
        return String.format("₹%.2f", amount);
    }

    @Override
    public String toString() {
        return "Expense{id=" + id + ", member=" + memberName + ", amount=" + amount +
               ", category=" + category + ", date=" + date + "}";
    }
}
