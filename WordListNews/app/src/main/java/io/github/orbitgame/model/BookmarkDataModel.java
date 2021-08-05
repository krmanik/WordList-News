package io.github.orbitgame.model;

public class BookmarkDataModel {
    private String title;
    private String url;

    public BookmarkDataModel (String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }
}
