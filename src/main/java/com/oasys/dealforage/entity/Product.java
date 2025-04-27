package com.oasys.dealforage.entity;

public class Product {

    private String asin;
    private byte[] image;
    private long newprice;
    private long pricedifference;
    private long dealscore;
    private String rootcat;
    private int savingspercent;
    private String title;
    private long usedprice;
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

    public long getNewprice() {
        return newprice;
    }

    public void setNewprice(long newprice) {
        this.newprice = newprice;
    }

    public long getPricedifference() {
        return pricedifference;
    }

    public void setPricedifference(long pricedifference) {
        this.pricedifference = pricedifference;
    }

    public long getDealscore() {
        return dealscore;
    }

    public void setDealscore(long dealscore) {
        this.dealscore = dealscore;
    }

    public String getRootcat() {
        return rootcat;
    }

    public void setRootcat(String rootcat) {
        this.rootcat = rootcat;
    }

    public int getSavingspercent() {
        return savingspercent;
    }

    public void setSavingspercent(int savingspercent) {
        this.savingspercent = savingspercent;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getUsedprice() {
        return usedprice;
    }

    public void setUsedprice(long usedprice) {
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



}
