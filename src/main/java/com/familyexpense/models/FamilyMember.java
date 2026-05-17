package com.familyexpense.models;

public class FamilyMember {
    private int id;
    private String name;
    private String emoji;
    private String role; // Papa, Mummy, Beta, Beti, Dada, Dadi, etc.
    private boolean active;

    public FamilyMember() {}

    public FamilyMember(String name, String emoji, String role) {
        this.name = name;
        this.emoji = emoji;
        this.role = role;
        this.active = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getDisplayName() {
        return emoji + " " + name;
    }

    @Override
    public String toString() {
        return name;
    }
}
