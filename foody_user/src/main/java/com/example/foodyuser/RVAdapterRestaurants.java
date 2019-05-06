package com.example.foodyuser;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionValues;
import android.util.Log;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class RVAdapterRestaurants  extends RecyclerView.Adapter<RVAdapterRestaurants.CardViewHolder>{

    private ArrayList<Restaurant> restaurants;

    RVAdapterRestaurants(ArrayList<Restaurant> restaurants){
        this.restaurants = restaurants;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.restaurant_card_display, viewGroup, false);

        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull final CardViewHolder cardViewHolder, final int i) {

        cardViewHolder.restaurantName.setText(restaurants.get(i).getUsername());
        cardViewHolder.restaurantDescription.setText(restaurants.get(i).getKitchensString());
        cardViewHolder.restaurantDeliveryPrice.setText(restaurants.get(i).getDeliveryPriceString());
        cardViewHolder.restaurantDistance.setText(restaurants.get(i).getDistanceString());
        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        mStorageRef.child("images/"+restaurants.get(i).getUid()+"_profile.jpeg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Glide
                                .with(cardViewHolder.restaurantBackground.getContext())
                                .load(uri)
                                .into(cardViewHolder.restaurantBackground);
                    }
                });

        cardViewHolder.card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TITLECHECK","Clicked");
                Intent intent = new Intent(v.getContext(), RestaurantShow.class);
                intent.putExtra("restaurant_name", restaurants.get(i).getUid());

                File storage = cardViewHolder.card.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                String filename = cardViewHolder.card.getContext().getString(R.string.order_file_name);
                File f = new File(storage, filename);
                if (f.exists())
                    f.delete();

                //Start the Intent
                cardViewHolder.card.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {

        ImageView restaurantBackground;
        ImageView restaurantShadow;
        TextView restaurantName;
        TextView restaurantDescription;
        TextView restaurantDeliveryPrice;
        CardView card;
        TextView restaurantDistance;

        CardViewHolder(View itemView) {
            super(itemView);
            restaurantBackground = itemView.findViewById(R.id.restaurant_background);
            restaurantShadow = itemView.findViewById(R.id.restaurant_shadow);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            restaurantDescription = itemView.findViewById(R.id.restaurant_description);
            restaurantDeliveryPrice = itemView.findViewById(R.id.restaurant_delivery_price);
            restaurantDistance = itemView.findViewById(R.id.restaurant_distance);
            card = itemView.findViewById(R.id.cv);
        }
    }


    //This method will filter the list
    //here we are passing the filtered data
    //and assigning it to the list with notifydatasetchanged method
    public void filterList(ArrayList<Restaurant> filtedNames) {
        this.restaurants = filtedNames;
        notifyDataSetChanged();
    }
}
