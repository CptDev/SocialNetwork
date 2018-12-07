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

    public void getPosts() {
        db.collection("posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<PostPojo> list = new ArrayList<>();
                    list = task.getResult().toObjects(PostPojo.class);
//                        list.adddocument);
                        System.out.print("hey");
//                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    public void getPostsForUser(FirebaseUser user) {
        db.collection("posts").whereEqualTo("userid", user.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<PostPojo> list = new ArrayList<>();
                    list = task.getResult().toObjects(PostPojo.class);
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
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
