package com.example.demo.entity;

public enum VoteCategory {
    BREAKFAST("Breakfast", "Nonushta"),
    LUNCH("Lunch", "Obed"),
    SNACK("Afternoon snack", "Poldnik");

    private final String english;
    private final String uzbek;

    VoteCategory(String english, String uzbek) {
        this.english = english;
        this.uzbek = uzbek;
    }

    public String label(String languageCode) {
        if (languageCode != null && languageCode.startsWith("uz")) {
            return uzbek;
        }
        return english;
    }

    public static VoteCategory fromName(String name) {
        for (VoteCategory category : values()) {
            if (category.name().equalsIgnoreCase(name) || category.english.equalsIgnoreCase(name) || category.uzbek.equalsIgnoreCase(name)) {
                return category;
            }
        }
        return null;
    }
}
