package org.stuba.fei.socialapp;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.crashlytics.android.Crashlytics.TAG;

public class FireStoreUtils {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FireStoreUtils() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

    }



    public void addPost(PostPojo pp) {
        CollectionReference colRef = db.collection("posts");
        colRef.add(pp);
    }

    public void getPosts(final Callback myCallback) {
        db.collection("posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<PostPojo> list = new ArrayList<>();
                    list = task.getResult().toObjects(PostPojo.class);
                    myCallback.onCallback3(list);
                        System.out.print(list);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void getPostsForUser(String uid, final Callback myCallback) {
        db.collection("posts").whereEqualTo("userid", uid).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<PostPojo> list = new ArrayList<>();
                    list = (ArrayList<PostPojo>) task.getResult().toObjects(PostPojo.class);
                    myCallback.onCallback3(list);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void getUserUID(String uid, final Callback myCallback) {
       /* db.collection("users").document(uid).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<UserPojo> list = new ArrayList<>();
                    list = task.getResult().toObjects(UserPojo.class);
                    myCallback.onCallback2(list);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });*/
        DocumentReference docRef = db.collection("users").document(uid);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserPojo up = documentSnapshot.toObject(UserPojo.class);
                    myCallback.onCallback(up);
                }
            }
        });
    }

    public void updatePocetPrispevkov(String uId, Integer pocetPrispevkov) {
        DocumentReference docRef = db.collection("users").document(uId);
        docRef.update("numberOfPosts",pocetPrispevkov.toString());
    }

    public void createUser(FirebaseUser user) {
        db.collection("users").document(user.getUid()).set(new UserPojo(user.getEmail(), new Date().toString(), "0"));
    }

    public void getUser(FirebaseUser user, final Callback myCallback){
        DocumentReference docRef = db.collection("users").document(user.getUid());

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    UserPojo up = documentSnapshot.toObject(UserPojo.class);
                    myCallback.onCallback(up);
                }
            }
        });

    }
}
