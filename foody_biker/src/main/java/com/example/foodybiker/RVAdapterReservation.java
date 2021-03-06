package com.example.foodybiker;

import android.animation.LayoutTransition;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class RVAdapterReservation extends RecyclerView.Adapter<RVAdapterReservation.CardViewHolder>{

    private List<Reservation> reservations;
    private ReservationFragment fatherFragment;
    private boolean orderActive;
    private FirebaseUser firebaseUser;
    private int incRejected;

    RVAdapterReservation(List<Reservation> reservations, ReservationFragment fatherFragment, boolean orderActive){
        this.reservations = reservations;
        this.fatherFragment = fatherFragment;
        this.orderActive = orderActive;
    }

    void setOrderActive(boolean orderActive) {
        this.orderActive = orderActive;
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reservation_item, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder pvh, final int i) {
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        final Reservation currentRes = reservations.get(i);
        final int pos = i;
        pvh.restaurantName.setText(currentRes.getRestaurantName());
        pvh.restaurantAddress.setText(currentRes.getRestaurantAddress());
        pvh.restaurantTime.setText(currentRes.getRestaurantPickupTime());
        pvh.userName.setText(currentRes.getUserName());
        pvh.userAddress.setText(currentRes.getUserAddress());
        pvh.userTime.setText(currentRes.getUserDeliveryTime());

        if(orderActive){
            pvh.accept.setVisibility(View.GONE);
            pvh.decline.setVisibility(View.GONE);
            pvh.lastDiv.setVisibility(View.INVISIBLE);
        }else{
            pvh.accept.setVisibility(View.VISIBLE);
            pvh.decline.setVisibility(View.VISIBLE);
            pvh.lastDiv.setVisibility(View.VISIBLE);

            pvh.accept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DatabaseReference databaseB = FirebaseDatabase.getInstance().getReference()
                            .child("Bikers").child(firebaseUser.getUid());
                    HashMap<String, Object> childB = new HashMap<>();
                    childB.put("status", "busy");
                    databaseB.updateChildren(childB).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(fatherFragment.getContext(), R.string.error_order, Toast.LENGTH_SHORT).show();
                        }
                    });

                    DatabaseReference database = FirebaseDatabase.getInstance().getReference()
                            .child("reservations").child("Bikers").child(firebaseUser.getUid());
                    HashMap<String, Object> child = new HashMap<>();
                    ReservationDBBiker reservation = new ReservationDBBiker(reservations.get(pos).getReservationID(),
                            reservations.get(pos).getUserDeliveryTime(), reservations.get(pos).getRestaurantPickupTime(),
                            reservations.get(pos).getRestaurantName(), reservations.get(pos).getUserName(),
                            reservations.get(pos).getRestaurantAddress(), reservations.get(pos).getUserAddress(),
                            reservations.get(pos).getRestaurantID());
                    reservation.setUserPhone(reservations.get(pos).getUserPhone());
                    reservation.setRestPhone(reservations.get(pos).getRestPhone());
                    reservation.setStatus("accepted");
                    child.put(reservations.get(pos).getReservationID(), reservation);
                    database.updateChildren(child).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(fatherFragment.getContext(), R.string.error_order, Toast.LENGTH_SHORT).show();
                        }
                    });

                    DatabaseReference databaseRest = FirebaseDatabase.getInstance().getReference()
                            .child("reservations").child("restaurant").child(reservations.get(pos).getRestaurantID())
                            .child(reservations.get(pos).getReservationID());
                    HashMap<String, Object> childRest = new HashMap<>();
                    childRest.put("biker", true);
                    childRest.put("bikerID", firebaseUser.getUid());
                    databaseRest.updateChildren(childRest).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(fatherFragment.getContext(), R.string.error_order, Toast.LENGTH_SHORT).show();
                        }
                    });

                    fatherFragment.setActiveReservation(currentRes);
                    fatherFragment.removeItem(pos);
                    fatherFragment.updateTitles();

                    incRejected = 0;
                    //Here I delete the other reservations when one is accepted
                    DatabaseReference databaseBiker = FirebaseDatabase.getInstance().getReference()
                            .child("reservations").child("Bikers").child(firebaseUser.getUid());
                    DatabaseReference databaseRests = FirebaseDatabase.getInstance().getReference()
                            .child("reservations").child("restaurant");
                    for(Reservation res : reservations){
                        incRejected++;
                        //Here I generate the map to update the biker node in the db
                        databaseBiker.child(res.getReservationID()).removeValue()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(fatherFragment.getContext(), R.string.error_order, Toast.LENGTH_SHORT).show();
                                }
                            });
                        //Setting the new status of the waiting biker for all the reservation rejected
                        databaseRests.child(res.getRestaurantID()).child(res.getReservationID())
                                .child("waitingBiker").setValue(false).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(fatherFragment.getContext(), R.string.error_order, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    final DatabaseReference databaseRej = FirebaseDatabase.getInstance().getReference()
                            .child("archive").child("Bikers").child(firebaseUser.getUid()).child("rejected");
                    databaseRej.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int count = dataSnapshot.getValue(int.class);
                                databaseRej.setValue(count+incRejected);
                            } else {
                                databaseRej.setValue(incRejected);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                    //Removing all the pending delivery and update the title
                    fatherFragment.removeAllItem();
                    fatherFragment.updateTitles();
                }
            });

            pvh.decline.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Calendar calendar = Calendar.getInstance();
                    String monthYear = calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.YEAR);
                    String date = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH)+1) + "-" +
                            calendar.get(Calendar.DAY_OF_MONTH);
                    DatabaseReference databaseSelf = FirebaseDatabase.getInstance().getReference()
                            .child("archive").child("Bikers").child(firebaseUser.getUid()).child(monthYear);
                    ReservationDBBiker reservation = new ReservationDBBiker(reservations.get(pos).getReservationID(),
                            reservations.get(pos).getUserDeliveryTime(), reservations.get(pos).getRestaurantPickupTime(),
                            reservations.get(pos).getRestaurantName(), reservations.get(pos).getUserName(),
                            reservations.get(pos).getRestaurantAddress(), reservations.get(pos).getUserAddress(),
                            reservations.get(pos).getRestaurantID());
                    reservation.setDate(date);
                    HashMap<String, Object> childSelf = new HashMap<>();
                    reservation.setStatus("rejected");
                    childSelf.put(reservations.get(pos).getReservationID(), reservation);
                    databaseSelf.updateChildren(childSelf).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(fatherFragment.getContext(), R.string.error_order, Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            DatabaseReference databaseDelete = FirebaseDatabase.getInstance().getReference()
                                    .child("reservations").child("Bikers").child(firebaseUser.getUid());
                            databaseDelete.child(reservations.get(pos).getReservationID()).removeValue();

                            fatherFragment.removeItem(pos);
                            fatherFragment.updateTitles();
                        }
                    });

                    DatabaseReference databaseRest = FirebaseDatabase.getInstance().getReference()
                            .child("reservations").child("restaurant").child(reservations.get(pos).getRestaurantID())
                            .child(reservations.get(pos).getReservationID());
                    HashMap<String, Object> childRest = new HashMap<>();
                    childRest.put("waitingBiker", false);
                    databaseRest.updateChildren(childRest).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(fatherFragment.getContext(), R.string.error_order, Toast.LENGTH_SHORT).show();
                        }
                    });

                    final DatabaseReference databaseRej = FirebaseDatabase.getInstance().getReference()
                            .child("archive").child("Bikers").child(firebaseUser.getUid()).child("rejected");
                    databaseRej.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                int count = dataSnapshot.getValue(int.class);
                                count ++;
                                databaseRej.setValue(count);
                            } else {
                                databaseRej.setValue(1);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            });
        }

    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


    static class CardViewHolder extends RecyclerView.ViewHolder {

        TextView restaurantName, restaurantAddress, restaurantTime, userName, userAddress, userTime;
        View lastDiv;
        MaterialButton accept, decline;

        CardViewHolder(View itemView) {
            super(itemView);
            restaurantName =  itemView.findViewById(R.id.pending_order_restaurant_name);
            restaurantAddress = itemView.findViewById(R.id.pending_order_restaurant_address);
            restaurantTime = itemView.findViewById(R.id.pending_order_pickup_time);
            userName = itemView.findViewById(R.id.pending_order_user_name);
            userAddress = itemView.findViewById(R.id.pending_order_user_address);
            userTime = itemView.findViewById(R.id.pending_order_deliver_time);
            lastDiv = itemView.findViewById(R.id.pending_order_user_div);
            accept = itemView.findViewById(R.id.pending_order_accept);
            decline = itemView.findViewById(R.id.pending_order_decline);

            CardView cv = itemView.findViewById(R.id.pending_order_card);
            ConstraintLayout layout = itemView.findViewById(R.id.pending_order_main_layout);

            cv.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);
            layout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        }
    }
}