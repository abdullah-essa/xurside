package com.xurside.adrianapp.models;

public class VideoList {
    private String id;
    private String Title;
    private String sent;
    private String deliver_date;
    private String link;

    //    public String file_name;
    public VideoList(String id, String sent, String title, String deliver_date, String link) {
        this.id = id;
        this.sent = sent;
        this.Title = title;
        this.deliver_date = deliver_date;
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getSent() {
        return sent;
    }

    public void setSent(String sent) {
        this.sent = sent;
    }

    public String getDeliver_date() {
        return deliver_date;
    }

    public void setDeliver_date(String deliver_date) {
        this.deliver_date = deliver_date;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

