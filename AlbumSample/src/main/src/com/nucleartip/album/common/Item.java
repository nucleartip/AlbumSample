package com.nucleartip.album.common;

public class Item {
    private String mainHeader;
    private String secondaryHeader;
    private String imageUri;
    
    public String getImageUri(){
    	return imageUri;
    }
    public String getMainHeader() {
        return mainHeader;
    }

    public void setMainHeader(String mainHeader) {
        this.mainHeader = mainHeader;
    }

    public String getSecondaryHeader() {
        return secondaryHeader;
    }

    public void setSecondaryHeader(String secondaryHeader) {
        this.secondaryHeader = secondaryHeader;
    }
}
