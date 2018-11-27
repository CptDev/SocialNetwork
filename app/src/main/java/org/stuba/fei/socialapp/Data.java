package org.stuba.fei.socialapp;

/**
 * Created by Marian-PC on 16.11.2018.
 */

public class Data {
    public String data;
    public String url;
    public String user;
    public String date;

    public void setData(String data){
        this.data=data;
    }
    public String getData(){
        return this.data;
    }

    public void setUrl(String url){
        this.url=url;
    }
    public String getUrl(){
        return this.url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
