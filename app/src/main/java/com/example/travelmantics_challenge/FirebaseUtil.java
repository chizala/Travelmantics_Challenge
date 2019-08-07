package com.example.travelmantics_challenge;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FirebaseUtil {
    public static  FirebaseDatabase mFireDatabase;
    public static  DatabaseReference mDatabaseReference;
    private static FirebaseUtil firebaseUtil;
    public static ArrayList<TravelDeal> mDeals;
    public static FirebaseAuth mFirebaseAuth;
    private static ListActivity  caller;
    private static final int RC_SIGN_IN=123;
    public  static FirebaseAuth.AuthStateListener mAuthListener;
    public static boolean isAdmin;
    public static FirebaseStorage mStorage;
    public static StorageReference mStorageRef;




    private FirebaseUtil(){}
    public  static void openFBReference(String ref, final ListActivity callerActivity){
        if (firebaseUtil==null){
            firebaseUtil = new FirebaseUtil();
            mFireDatabase =FirebaseDatabase.getInstance();
            mFirebaseAuth =FirebaseAuth.getInstance();
            caller=callerActivity;
            mAuthListener = new FirebaseAuth.AuthStateListener() {
                @Override
                public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                    if(firebaseAuth.getCurrentUser()==null) {
                        FirebaseUtil.signIn();
                    }else {
                        String userId=firebaseAuth.getUid();
                        checkAdmin(userId);
                    }
                    Toast.makeText(callerActivity.getBaseContext(),"Welcome back",Toast.LENGTH_LONG).show();

                }
            };
            connectStorage();

        }
        mDeals=new ArrayList<TravelDeal>();
        mDatabaseReference=mFireDatabase.getReference().child(ref);



    }
    public static void signIn(){
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());



// Create and launch sign-in intent
        caller.startActivityForResult(
                AuthUI.getInstance()
                        // Choose authentication providers
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }
    public static void attachListener(){
        mFirebaseAuth.addAuthStateListener(mAuthListener);

    }
    public  static  void detachListener(){
        mFirebaseAuth.removeAuthStateListener(mAuthListener);
    }
    public  static void checkAdmin(String uid){
        FirebaseUtil.isAdmin=false;
        DatabaseReference ref=mFireDatabase.getReference().child("administrators")
                .child(uid);
        ChildEventListener listener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                FirebaseUtil.isAdmin=true;
                caller.showMenu();
                Log.d("Admin","You are an adminstrator");

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {



            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        }; ref.addChildEventListener(listener);

    }
    public static void connectStorage(){
        mStorage =FirebaseStorage.getInstance();
        mStorageRef=mStorage.getReference().child("deals_picture");
    }
}
