package org.stuba.fei.socialapp;

import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import java.util.Date;
import java.util.Map;

public class FireStoreUtils {

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public FireStoreUtils() {

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build();
        firestore.setFirestoreSettings(settings);

    }

    public void updatePocetPrispevkov(String uId, Integer pocetPrispevkov) {
        DocumentReference docRef = db.collection("users").document(uId);
        docRef.update("numberOfPosts",pocetPrispevkov.toString());
    }

    public void createUser(FirebaseUser user) {
        db.collection("users").document(user.getUid()).set(new UserPojo(user.getEmail(), new Date().toString(), "0"));
    }

    public void getUser(FirebaseUser user, final MyCallback myCallback){
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
