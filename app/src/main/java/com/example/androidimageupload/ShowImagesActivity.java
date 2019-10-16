package com.example.androidimageupload;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowImagesActivity extends AppCompatActivity {

    private RecyclerView showImagesrecyclerView;
    private ShowImagesAdapter showImagesAdapter;

    // Firebase database reference
    private DatabaseReference databaseReference;
    private List<UploadModel> uploadModelList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_images);
        showImagesrecyclerView = findViewById(R.id.show_images_recycler_view);
        showImagesrecyclerView.setHasFixedSize(true);
        showImagesrecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        uploadModelList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");

        // downloading data from the database
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    UploadModel uploadModel = postSnapshot.getValue(UploadModel.class);
                    uploadModelList.add(uploadModel);
                }
                showImagesAdapter = new ShowImagesAdapter(ShowImagesActivity.this, uploadModelList);
                showImagesrecyclerView.setAdapter(showImagesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
}
