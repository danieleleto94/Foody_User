package com.example.foodyuser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.EventListener;

public class RVAdapterRestaurants  extends RecyclerView.Adapter<RVAdapterRestaurants.CardViewHolder>{

    private ArrayList<Restaurant> restaurants;
    private final String DIRECTORY_IMAGES = "showImages";

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

        final Restaurant currentRes = restaurants.get(i);

        cardViewHolder.restaurantName.setText(restaurants.get(i).getUsername());
        cardViewHolder.restaurantDeliveryPrice.setText(restaurants.get(i).getDeliveryPriceString());

        if(currentRes.getTotalMean() == -1)
            cardViewHolder.restaurantReview.setText("?");
        else
            cardViewHolder.restaurantReview.setText(String.format("%.1f",currentRes.getTotalMean()));

        File imageFile = new File(currentRes.getImagePath());
        RequestOptions options = new RequestOptions();
        options.signature(new ObjectKey(currentRes.getImagePath()+" "+imageFile.lastModified()));

        Glide
                .with(cardViewHolder.card)
                .setDefaultRequestOptions(options)
                .load(currentRes.getImagePath())
                .into(cardViewHolder.restaurantBackground);


        if (restaurants.get(i).isOpen()) {
            cardViewHolder.restaurantDescription.setText(restaurants.get(i).getKitchensString());
            Glide
                    .with(cardViewHolder.restaurantBackground.getContext())
                    .load(R.drawable.shadow_restaurants)
                    .into(cardViewHolder.restaurantShadow);
            cardViewHolder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), RestaurantView.class);
                    intent.putExtra("restaurant_id", restaurants.get(i).getUid());
                    intent.putExtra("restaurant_name", restaurants.get(i).getUsername());
                    intent.putExtra("restaurant_address", restaurants.get(i).getAddress());

                    File storage = cardViewHolder.card.getContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    String filename = cardViewHolder.card.getContext().getString(R.string.order_file_name);
                    File f = new File(storage, filename);
                    if (f.exists())
                        f.delete();
                    SharedPreferences shared = cardViewHolder.restaurantBackground.getContext().getSharedPreferences("myPreference", Context.MODE_PRIVATE);
                    shared.edit().remove("notes").apply();
                    if(shared.contains("selectedTime"))
                        shared.edit().remove("selectedTime").apply();

                    Log.d("MADPATH",""+currentRes.getImagePath());

                    //Delete directory images RestaurantView
                    File root = cardViewHolder.card.getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File dir = new File(root.getPath()+File.separator+DIRECTORY_IMAGES);
                    if(dir.exists()){
                        for(File child: dir.listFiles())
                            child.delete();
                        dir.delete();
                    }

                    //Start the Intent
                    cardViewHolder.card.getContext().startActivity(intent);
                }
            });
        }else{
            Glide
                    .with(cardViewHolder.restaurantBackground.getContext())
                    .load(R.drawable.background_closed)
                    .into(cardViewHolder.restaurantShadow);
            cardViewHolder.restaurantDescription.setText("CLOSED");
            cardViewHolder.card.setOnClickListener(null);
        }


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
        TextView restaurantReview;

        CardViewHolder(View itemView) {
            super(itemView);
            restaurantBackground = itemView.findViewById(R.id.restaurant_background);
            restaurantShadow = itemView.findViewById(R.id.restaurant_shadow);
            restaurantName = itemView.findViewById(R.id.restaurant_name);
            restaurantDescription = itemView.findViewById(R.id.restaurant_description);
            restaurantDeliveryPrice = itemView.findViewById(R.id.restaurant_delivery_price);
            restaurantReview = itemView.findViewById(R.id.restaurant_review);
            card = itemView.findViewById(R.id.cv);
        }
    }


    public void filterList(ArrayList<Restaurant> filtedNames) {
        this.restaurants = filtedNames;
        notifyDataSetChanged();
    }
}
