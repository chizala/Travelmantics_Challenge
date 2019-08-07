package com.example.travelmantics_challenge;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.widget.Toast.LENGTH_LONG;

public class DealActivity extends AppCompatActivity {
    /*private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;*/

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    EditText txtTitle;
    EditText txtPrice;
    EditText txtDescription;
    TravelDeal deal;
    ImageView imageView;
    public  static final int PICTURE_RESULT=42;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deal);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
       //FirebaseUtil.openFBReference("traveldeals",this);

        mFirebaseDatabase = FirebaseUtil.mFireDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        txtTitle=(EditText) findViewById(R.id.txtTitle);
        txtPrice=(EditText) findViewById(R.id.txtPrice);
        txtDescription=(EditText)findViewById(R.id.txtDescription);
        imageView =(ImageView) findViewById(R.id.image);
        Intent intent =getIntent();
        TravelDeal deal=(TravelDeal) intent.getSerializableExtra("Deal");
        if(deal==null){
           deal = new TravelDeal();

        }

        this.deal=deal;
        txtTitle.setText(deal.getTitle());
        txtDescription.setText(deal.getDescription());
        txtPrice.setText(deal.getPrice());
        showImage(deal.getImageUrl());
        Button btnImage=findViewById(R.id.btnImage);
        btnImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    Intent intent =new Intent(Intent.ACTION_GET_CONTENT) ;
                    intent.setType("image/jpeg");
                    intent.putExtra(intent.EXTRA_LOCAL_ONLY,true);
                    startActivityForResult(intent.createChooser(intent,"insert picture"),PICTURE_RESULT);


            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.save_menu,menu);
        if(FirebaseUtil.isAdmin){
            menu.findItem(R.id.delete_menu).setVisible(true);
            menu.findItem(R.id.save_menu).setVisible(true);
            enabledEditTexts(true);
            findViewById(R.id.btnImage).setEnabled(true);
        }else{
            menu.findItem(R.id.delete_menu).setVisible(false);
            menu.findItem(R.id.save_menu).setVisible(false);
            enabledEditTexts(false);
            findViewById(R.id.btnImage).setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()){
            case R.id.save_menu:
                
               saveDeal();
                Toast.makeText(this,"Deal saved", LENGTH_LONG).show();
                clean();
                backToList();
                return true;
            case R.id.delete_menu:
                deleteDeal();
                Toast.makeText(this,"Deal deleted"+R.id.delete_menu, LENGTH_LONG).show();
                backToList();
                return  true;
            default:
                return super.onOptionsItemSelected(item);


        }
       


    
}

    private void clean() {
         txtDescription.setText("");
         txtPrice.setText("");
         txtTitle.setText("");
         txtTitle.requestFocus();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICTURE_RESULT&&resultCode==RESULT_OK){
            Uri imageUri =data.getData();

           final StorageReference ref =FirebaseUtil.mStorageRef.child(imageUri.getLastPathSegment());
            UploadTask filepath = ref.putFile(imageUri);
            filepath.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {
                    Task<Uri> firebaseUri = taskSnapshot.getStorage().getDownloadUrl();
                    firebaseUri.addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String link= uri.toString();
                            String pictureName=taskSnapshot.getStorage().getPath();
                            deal.setImageUrl(link);
                            deal.setImageName(pictureName);
                            Log.d("Url",link);
                            Log.d("Name",pictureName);
                            showImage(link);

                        }
                    });
                }
            });

        }
    }

    private void saveDeal() {
            deal.setTitle(txtTitle.getText().toString());
            deal.setPrice(txtPrice.getText().toString());
            deal.setDescription(txtDescription.getText().toString());
            if(deal.getId()==null){
                mDatabaseReference.push().setValue(deal);
            }else{
                mDatabaseReference.child(deal.getId()).setValue(deal);

            }



    }
    private void deleteDeal(){
        if(deal==null){
            Toast.makeText(this,"please save", Toast.LENGTH_SHORT).show();
            return;
        }
        mDatabaseReference.child(deal.getId()).removeValue();
        if(deal.getImageName()!=null&&deal.getImageName().isEmpty()==false){
            StorageReference picRef=FirebaseUtil.mStorage.getReference().child(deal.getImageName());
            picRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                  Log.d("Delete image","image successfully deleted");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Delete image",e.getMessage());
                }
            });
        }
    }
    private void backToList(){

        Intent intent =new Intent(this,ListActivity.class);
        startActivity(intent);


    }
    private void enabledEditTexts(boolean isEnabled){
      txtTitle.setEnabled(isEnabled);
      txtPrice.setEnabled(isEnabled);
      txtDescription.setEnabled(isEnabled);

    }
    public  void showImage(String url){
        if(url!=null&&url.isEmpty()==false){
            int width= Resources.getSystem().getDisplayMetrics().widthPixels;
            Picasso.with(this)
                    .load(url)
                    .resize(width,width*2/3)
                    .centerCrop()
                    .into(imageView);
        }

    }
    }
