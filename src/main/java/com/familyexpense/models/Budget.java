package com.familyexpense.models;

public class Budget {
    private int id;
    private double monthlyBudget;
    private int month;
    private int year;
    private double totalIncome;

    public Budget() {}

    public Budget(double monthlyBudget, int month, int year) {
        this.monthlyBudget = monthlyBudget;
        this.month = month;
        this.year = year;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public double getMonthlyBudget() { return monthlyBudget; }
    public void setMonthlyBudget(double monthlyBudget) { this.monthlyBudget = monthlyBudget; }

    public int getMonth() { return month; }
    public void setMonth(int month) { this.month = month; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public double getTotalIncome() { return totalIncome; }
    public void setTotalIncome(double totalIncome) { this.totalIncome = totalIncome; }

    public String getFormattedBudget() {
        return String.format("₹%.2f", monthlyBudget);
    }

    @Override
    public String toString() {
        return "Budget{month=" + month + "/" + year + ", budget=" + monthlyBudget + "}";
    }
}
