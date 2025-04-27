package com.oasys.dealforage.entity;

public class Product {

    private String asin;
    private byte[] image;
    private String newprice;
    private String pricedifference;
    private String dealscore;
    private String rootcat;
    private String savingspercent;
    private String title;
    private String usedprice;
    private String updated_at;

    private String lastchange;
    private String lastupdate;
    private int source;



    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getNewprice() {
        return newprice;
    }

    public void setNewprice(String newprice) {
        this.newprice = newprice;
    }

    public String getPricedifference() {
        return pricedifference;
    }

    public void setPricedifference(String pricedifference) {
        this.pricedifference = pricedifference;
    }

    public String getDealscore() {
        return dealscore;
    }

    public void setDealscore(String dealscore) {
        this.dealscore = dealscore;
    }

    public String getRootcat() {
        return rootcat;
    }

    public void setRootcat(String rootcat) {
        this.rootcat = rootcat;
    }

    public String getSavingspercent() {
        return savingspercent;
    }

    public void setSavingspercent(String savingspercent) {
        this.savingspercent = savingspercent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUsedprice() {
        return usedprice;
    }

    public void setUsedprice(String usedprice) {
        this.usedprice = usedprice;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getLastchange() {
        return lastchange;
    }

    public void setLastchange(String lastchange) {
        this.lastchange = lastchange;
    }

    public String getLastupdate() {
        return lastupdate;
    }

    public void setLastupdate(String lastupdate) {
        this.lastupdate = lastupdate;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Product{" +
                "asin='" + asin + '\'' +
                ", image=" + (image != null ? "[binary data]" : "null") +
                ", newprice=" + newprice +
                ", pricedifference=" + pricedifference +
                ", dealscore=" + dealscore +
                ", rootcat='" + rootcat + '\'' +
                ", savingspercent=" + savingspercent +
                ", title='" + title + '\'' +
                ", usedprice=" + usedprice +
                ", updated_at='" + updated_at + '\'' +
                ", lastchange='" + lastchange + '\'' +
                ", lastupdate='" + lastupdate + '\'' +
                ", source=" + source +
                '}';
    }

}
