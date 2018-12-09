package org.stuba.fei.socialapp;

import java.util.List;

public interface Callback {
    void onCallback(UserPojo pojo);
    void onCallback2(List<UserPojo> pojos);
    void onCallback(PostPojo pojo);
    void onCallback3(List<PostPojo> pojos);
}