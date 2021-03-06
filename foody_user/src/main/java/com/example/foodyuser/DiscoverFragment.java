package com.example.foodyuser;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class DiscoverFragment extends Fragment {

    private static final int AUTOCOMPLETE_REQUEST_CODE = 9;

    private ImageButton searchButton;
    private TextView address, insertDelivDiscovery;
    private boolean setted = false;
    private SharedPreferences.Editor edit;
    private ImageView house, mainBackground;
    private CardView cardOrderHome;
    private final String RESTAURANT_IMAGES = "RestaurantImages";

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_discover, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        init(view);
    }

    private void init(View view){
        final SharedPreferences sharedPref = view.getContext().getSharedPreferences("myPreference", MODE_PRIVATE);
        edit = sharedPref.edit();
        searchButton = view.findViewById(R.id.discover_search_button);
        address = view.findViewById(R.id.discover_search_address);
        house = view.findViewById(R.id.house_discover);
        mainBackground = view.findViewById(R.id.main_image);
        cardOrderHome = view.findViewById(R.id.deliver_home_card);
        insertDelivDiscovery = view.findViewById(R.id.insert_deliv_discover);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!setted)
                    Toast.makeText(address.getContext(), R.string.address_not_selected, Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(getActivity(), SearchRestaurant.class);
                    File root = v.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File directory = new File(root.getPath()+File.separator+RESTAURANT_IMAGES);
                    sharedPref.edit().remove("positionSearch").apply();
                    if(directory.exists()){
                        for(File child: directory.listFiles())
                            child.delete();
                        directory.delete();
                    }
                    sharedPref.edit().putBoolean("restaurantFetches",false).apply();
                    //Delete Image directory

                    startActivity(intent);
                }
            }
        });

        address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                address.setClickable(false);
                addressActivity();
            }
        });

        if(sharedPref.contains("address")){
            Glide
                    .with(view)
                    .load(R.drawable.house_icon)
                    .into(house);

            insertDelivDiscovery.setText(getString(R.string.insert_deliv_address));
            cardOrderHome.setVisibility(View.VISIBLE);
            cardOrderHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SearchRestaurant.class);
                    File root = v.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File directory = new File(root.getPath()+File.separator+RESTAURANT_IMAGES);
                    sharedPref.edit().remove("positionSearch").apply();
                    if(directory.exists()){
                        for(File child: directory.listFiles())
                            child.delete();
                        directory.delete();
                    }
                    sharedPref.edit().putBoolean("restaurantFetches",false).apply();
                    //Delete Image directory
                    sharedPref.edit().putString("delivery_address", sharedPref.getString("address","")).apply();
                    startActivity(intent);
                }
            });

        }else{
            cardOrderHome.setVisibility(View.GONE);
            insertDelivDiscovery.setText(getString(R.string.insert_deliv_address_no_home));
        }


        Glide
                .with(view)
                .load(R.drawable.main_screen_background)
                .into(mainBackground);
    }

    public void addressActivity() {
        if (!Places.isInitialized()) {
            Places.initialize(address.getContext(), BuildConfig.ApiKey);
        }

        final List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.ADDRESS, Place.Field.LAT_LNG);
        Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(address.getContext());
        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.d("PLACE", "Place: " + place.getAddress() + " LAT_LNG " + place.getLatLng());
                if(place.getAddress().equals(""))
                    setted = false;
                address.setText(place.getAddress());
                address.setTextColor(getContext().getColor(R.color.primaryText));
                setted = true;
                searchButton.setEnabled(true);
                edit.putString("delivery_address", place.getAddress());
                edit.apply();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        address.setClickable(true);
    }
}
