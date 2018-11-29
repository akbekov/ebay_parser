/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author takbekov
 */
public class Item {

    private int id;
    private String url;
    private String asin;
    private String title;
    private int status;
    private String brand;

    public Item() {
    }

    public Item(int id, String url, String asin, String title, int status, String brand) {
        this.id = id;
        this.url = url;
        this.asin = url;
        this.title = url;
        this.status = status;
        this.brand = brand;
    }

    public int getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getAsin() {
        return asin;
    }

    public String getTitle() {
        return title;
    }

    public int getStatus() {
        return status;
    }

    public String getBrand() {
        return brand;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

}
