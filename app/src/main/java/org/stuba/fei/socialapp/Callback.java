package org.stuba.fei.socialapp;

import java.util.List;

public interface Callback {
    void onCallback(UserPojo pojo);
    void onCallback(PostPojo pojo);
    void onCallback(List<PostPojo> pojos);
}