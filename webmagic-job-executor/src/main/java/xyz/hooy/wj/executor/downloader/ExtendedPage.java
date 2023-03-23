package xyz.hooy.wj.executor.downloader;

import us.codecraft.webmagic.Page;

public class ExtendedPage extends Page {

    private String contentType;

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
