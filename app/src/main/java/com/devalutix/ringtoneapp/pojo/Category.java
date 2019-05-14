package com.devalutix.ringtoneapp.pojo;

public class Category {

    private String categoryTitle;
    private String categoryThumbnailUrl;
    private String categoryCardColor;

    public Category(String categoryTitle, String categoryThumbnailUrl, String categoryCardColor) {
        this.categoryTitle = categoryTitle;
        this.categoryThumbnailUrl = categoryThumbnailUrl;
        this.categoryCardColor = categoryCardColor;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public String getCategoryThumbnailUrl() {
        return categoryThumbnailUrl;
    }

    public String getCategoryCardColor() {
        return categoryCardColor;
    }
}
