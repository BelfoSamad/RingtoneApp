package com.devalutix.ringtoneapp.pojo;

public class Ringtone {

    private String ringtoneId;
    private String ringtoneTitle;
    private String ringtoneThumbnailUrl;

    public Ringtone(String ringtoneId, String ringtoneTitle, String ringtoneThumbnailUrl) {
        this.ringtoneId = ringtoneId;
        this.ringtoneTitle = ringtoneTitle;
        this.ringtoneThumbnailUrl = ringtoneThumbnailUrl;
    }

    public String getRingtoneId() {
        return ringtoneId;
    }

    public String getRingtoneTitle() {
        return ringtoneTitle;
    }

    public String getRingtoneThumbnailUrl() {
        return ringtoneThumbnailUrl;
    }
}
