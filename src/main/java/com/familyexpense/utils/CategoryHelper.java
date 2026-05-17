package com.familyexpense.utils;

import java.util.LinkedHashMap;
import java.util.Map;

public class CategoryHelper {

    public static final Map<String, String> CATEGORIES = new LinkedHashMap<>();

    static {
        CATEGORIES.put("Grocery", "🛒");
        CATEGORIES.put("Rent", "🏠");
        CATEGORIES.put("Medicine", "💊");
        CATEGORIES.put("School", "📚");
        CATEGORIES.put("Travel", "🚗");
        CATEGORIES.put("Food", "🍔");
        CATEGORIES.put("Electricity", "💡");
        CATEGORIES.put("Mobile", "📱");
        CATEGORIES.put("Other", "🎉");
    }

    public static final Map<String, String> MEMBER_EMOJIS = new LinkedHashMap<>();

    static {
        MEMBER_EMOJIS.put("Papa", "👨");
        MEMBER_EMOJIS.put("Mummy", "👩");
        MEMBER_EMOJIS.put("Beta / Son", "👦");
        MEMBER_EMOJIS.put("Beti / Daughter", "👧");
        MEMBER_EMOJIS.put("Dada", "👴");
        MEMBER_EMOJIS.put("Dadi", "👵");
        MEMBER_EMOJIS.put("Other", "👤");
    }

    public static String getEmoji(String category) {
        return CATEGORIES.getOrDefault(category, "🎉");
    }

    public static String[] getCategoryNames() {
        return CATEGORIES.keySet().toArray(new String[0]);
    }

    public static String[] getMemberRoles() {
        return MEMBER_EMOJIS.keySet().toArray(new String[0]);
    }

    public static String getEmojiForRole(String role) {
        return MEMBER_EMOJIS.getOrDefault(role, "👤");
    }
}
