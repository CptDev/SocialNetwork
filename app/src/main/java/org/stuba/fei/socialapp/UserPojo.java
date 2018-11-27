package org.stuba.fei.socialapp;

public class UserPojo {

    private String uId;
    private String dateOfRegistration;
    private String numberOfPosts;

    public UserPojo(String uId, String dateOfRegistration, String numberOfPosts) {
        this.uId = uId;
        this.dateOfRegistration = dateOfRegistration;
        this.numberOfPosts = numberOfPosts;
    }

    public UserPojo() {}

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getDateOfRegistration() {
        return dateOfRegistration;
    }

    public void setDateOfRegistration(String dateOfRegistration) {
        this.dateOfRegistration = dateOfRegistration;
    }

    public String getNumberOfPosts() {
        return numberOfPosts;
    }

    public void setNumberOfPosts(String numberOfPosts) {
        this.numberOfPosts = numberOfPosts;
    }
}
