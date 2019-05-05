package com.example.foodyuser;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;


public class UserFragment extends Fragment {

    private FloatingActionButton editMode;
    private CircleImageView profilePicture;
    private TextView name;
    private TextView email;
    private TextView address;
    private TextView phoneNumber;
    private TextView bio;
    private final String PLACEHOLDER_CAMERA="PlaceCamera.jpg";
    private File storageDir;
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor edit;
    private FirebaseAuth firebaseAuth;

    public UserFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void init(View view){
        sharedPref = view.getContext().getSharedPreferences("myPreference", MODE_PRIVATE);
        edit = sharedPref.edit();
        firebaseAuth = FirebaseAuth.getInstance();
        profilePicture = view.findViewById(R.id.profilePicture);
        this.editMode = view.findViewById(R.id.edit_mode);
        this.name = view.findViewById(R.id.userName);
        this.email = view.findViewById(R.id.emailAddress);
        this.address = view.findViewById(R.id.address);
        this.phoneNumber = view.findViewById(R.id.phoneNumber);
        this.bio = view.findViewById(R.id.bio);

        //setup of the Shared Preferences to save value in (key, value) format
        if (!email.getText().toString().equals("email")) {
            final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("endUsers");
            Query query = database.child(firebaseAuth.getCurrentUser().getUid());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        UserInfo info = ds.getValue(UserInfo.class);
                        name.setText(info.getUsername());
                        email.setText(info.getEmail());
                        address.setText(info.getAddress());
                        phoneNumber.setText(info.getNumberPhone());
                        bio.setText(info.getBiography());
                        edit.putString("name", info.getUsername());
                        edit.putString("email", info.getEmail());
                        if (!address.getText().toString().equals(getResources().getString(R.string.address_hint)))
                            edit.putString("address", info.getAddress());
                        if (!phoneNumber.getText().toString().equals(""))
                            edit.putString("phoneNumber", info.getNumberPhone());
                        if (!bio.getText().toString().equals(""))
                            edit.putString("bio", info.getBiography());
                        edit.apply();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.d("SWSW", databaseError.getMessage());
                    name.setText(sharedPref.getString("name", getResources().getString(R.string.name_hint)));
                    email.setText(sharedPref.getString("email", getResources().getString(R.string.email_hint)));
                    address.setText(sharedPref.getString("address", getResources().getString(R.string.address_hint)));
                    phoneNumber.setText(sharedPref.getString("phoneNumber", getResources().getString(R.string.phone_hint)));
                    bio.setText(sharedPref.getString("bio", getResources().getString(R.string.bio_hint)));
                }
            });
        }


        final String PROFILE_IMAGE = "ProfileImage.jpg";
        final File f = new File(storageDir, PROFILE_IMAGE);

        if(f.exists()){
            profilePicture.setImageURI(Uri.fromFile(f));
        } else{
            StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
            mStorageRef.child("images/users/" + firebaseAuth.getCurrentUser().getUid() + ".jpeg").getDownloadUrl()
                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Glide
                                    .with(profilePicture.getContext())
                                    .load(uri)
                                    .into(profilePicture);
                            //TODO: salvare l'uri qua nel path dell'immagine profilo
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Glide
                            .with(profilePicture.getContext())
                            .load(R.drawable.profile_placeholder)
                            .into(profilePicture);
                }
            });
        }

        editMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Setup.class);
                File pl = new File(storageDir, PLACEHOLDER_CAMERA);
                if(!pl.delete()){
                    System.out.println("Delete Failure");
                }
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Context context = Objects.requireNonNull(getActivity()).getApplicationContext();
        sharedPref = context.getSharedPreferences("myPreference", MODE_PRIVATE);
        storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        init(Objects.requireNonNull(getView()));
    }
}
