package com.example.foodyuser;

import android.net.Uri;
import android.util.JsonReader;

import com.example.foody_library.Review;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;

class JsonHandler {

    ArrayList<OrderItem> getOrders(File file){
        ArrayList<OrderItem> orders;
        try {
            orders = readOrdersFromJSON(file);
        } catch (IOException e){
            e.getMessage();
            return new ArrayList<>();
        }
        return orders;
    }

    ArrayList<Card> getCards(File file) {
        ArrayList<Card> cards;
        try {
            cards = readFromJSON(file);
        } catch (IOException e) {
            e.getMessage();
            return new ArrayList<>();
        }
        return cards;
    }

    ArrayList<Reservation> getReservations(File file){
        ArrayList<Reservation> reservations;
        try {
            reservations = readResFromFile(file);
        } catch (IOException e){
            e.getMessage();
            return new ArrayList<>();
        }
        return reservations;
    }

    ArrayList<Review> getReviews(File file){
        ArrayList<Review> reviews;
        try{
            reviews = readRevFromFile(file);
        }catch(IOException e){
            e.getMessage();
            return new ArrayList<>();
        }
        return reviews;
    }

    private ArrayList<OrderItem> readOrdersFromJSON (File path) throws  IOException {
        ArrayList<OrderItem> orders = new ArrayList<>();
        FileInputStream fin = new FileInputStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(fin, StandardCharsets.UTF_8));
        try {
            reader.beginObject();
            if (reader.nextName().equals("Order"))
                orders = readMultipleOrders(reader);
        }finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                e.getMessage();
            }
        }
        return orders;
    }

    private ArrayList<Card> readFromJSON (File path) throws IOException {
        ArrayList<Card> cards = new ArrayList<>();
        FileInputStream fin = new FileInputStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(fin, StandardCharsets.UTF_8));
        try {
            reader.beginObject();
            if (reader.nextName().equals("Card"))
                cards = readMultipleCards(reader);
        } finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                e.getMessage();
            }
        }
        return cards;
    }

    private ArrayList<Review> readRevFromFile(File path) throws  IOException{
        ArrayList<Review> reviews = new ArrayList<>();
        FileInputStream fin = new FileInputStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(fin, StandardCharsets.UTF_8));
        try{
            reader.beginObject();
            if(reader.nextName().equals("Review"))
                reviews = readMultipleReviews(reader);
        }finally {
            try{
                reader.close();
            }
            catch (IOException e){
                e.getMessage();
            }
        }
        return  reviews;
    }

    private ArrayList<Reservation> readResFromFile (File path) throws IOException {
        ArrayList<Reservation> reservations = new ArrayList<>();
        FileInputStream fin = new FileInputStream(path);
        JsonReader reader = new JsonReader(new InputStreamReader(fin, StandardCharsets.UTF_8));
        try {
            reader.beginObject();
            if (reader.nextName().equals("Reservation"))
                reservations = readMultipleReservations(reader);
        } finally {
            try {
                reader.close();
            }
            catch (IOException e) {
                e.getMessage();
            }
        }

        return reservations;
    }

    private ArrayList<OrderItem> readMultipleOrders(JsonReader reader) throws IOException{
        ArrayList<OrderItem> orders = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()){
            orders.add(readSingleOrder(reader));
        }
        reader.endArray();
        return orders;
    }

    private ArrayList<Card> readMultipleCards(JsonReader reader) throws IOException{
        ArrayList<Card> cards = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()){
            cards.add(readSingleCard(reader));
        }
        reader.endArray();
        return cards;
    }

    private  ArrayList<Review> readMultipleReviews(JsonReader reader) throws  IOException{
        ArrayList<Review> reviews = new ArrayList<>();

        reader.beginArray();
        while(reader.hasNext()){
            reviews.add(readSingleReview(reader));
        }
        reader.endArray();
        return reviews;
    }

    private ArrayList<Reservation> readMultipleReservations(JsonReader reader) throws  IOException{
        ArrayList<Reservation> reservations = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()){
            reservations.add(readSingleReservation(reader));
        }
        reader.endArray();
        return reservations;
    }

    private Review readSingleReview(JsonReader reader) throws IOException{
        String reviewID = null, userID = null, userName = null, imagePath = null, note = null;
        float rating = 0;

        reader.beginObject();
        while (reader.hasNext()){
            String name = reader.nextName();
            switch(name){
                case "reviewID":
                    reviewID = reader.nextString();
                    break;
                case "userID":
                    userID = reader.nextString();
                    break;
                case "userName":
                    userName = reader.nextString();
                    break;
                case "imagePath":
                    imagePath = reader.nextString();
                    break;
                case "note":
                    note = reader.nextString();
                    break;
                case "rating":
                    rating = (float)reader.nextDouble();
                    break;
                default:
                    reader.skipValue();
                    break;
            }

        }
        reader.endObject();
        Review review = new Review(reviewID, userID, userName, imagePath, note, rating);
        return  review;
    }

    private Reservation readSingleReservation(JsonReader reader) throws IOException{
        String reservationID = null, userName = null, userPhone = null, userLevel = null, userEmail = null,
                userAddress = null, resNote = null, orderTime = null;
        ArrayList<Dish> dishesOrdered = new ArrayList<>();
        boolean accepted = false;
        int toBePrepared = 0;
        Reservation.prepStatus preparationStatus = Reservation.prepStatus.PENDING;

        reader.beginObject();
        while(reader.hasNext()){
            String name = reader.nextName();
            switch (name) {
                case "reservationID":
                    reservationID = reader.nextString();
                    break;
                case "Dish":
                    dishesOrdered = readMultipleDishes(reader);
                    break;
                case "userName":
                    userName = reader.nextString();
                    break;
                case "userPhone":
                    userPhone = reader.nextString();
                    break;
                case "userLevel":
                    userLevel = reader.nextString();
                    break;
                case "userEmail":
                    userEmail = reader.nextString();
                    break;
                case "userAddress":
                    userAddress = reader.nextString();
                    break;
                case "resNote":
                    resNote = reader.nextString();
                    break;
                case "orderTime":
                    orderTime = reader.nextString();
                    break;
                case "accepted":
                    accepted = reader.nextBoolean();
                    break;
                case "preparationStatus":
                    String help = reader.nextString();
                    if (help.compareTo("Pending") == 0)
                        preparationStatus = Reservation.prepStatus.PENDING;
                    else if (help.compareTo("Doing") == 0)
                        preparationStatus = Reservation.prepStatus.DOING;
                    else if (help.compareTo("Done") == 0)
                        preparationStatus = Reservation.prepStatus.DONE;
                    break;
                case "toBePrepared":
                    toBePrepared = reader.nextInt();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        Reservation result = new Reservation(reservationID, dishesOrdered, preparationStatus, accepted, orderTime, userName,
                userPhone, resNote, userLevel, userEmail, userAddress);
        result.setToBePrepared(toBePrepared);
        return result;
    }

    private OrderItem readSingleOrder(JsonReader reader) throws IOException{
        int pieces = 0;
        String orderName = null;
        double price = 0.0;

        reader.beginObject();
        while(reader.hasNext()){
            String name = reader.nextName();
            switch (name) {
                case "pieces":
                    pieces = reader.nextInt();
                    break;
                case "orderName":
                    orderName = reader.nextString();
                    break;
                case "price":
                    price = reader.nextDouble();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new OrderItem(pieces, orderName, (float) price);
    }

    private Card readSingleCard(JsonReader reader) throws IOException{
        String title = null;
        ArrayList<Dish> dishes = new ArrayList<>();

        reader.beginObject();
        while(reader.hasNext()){
            String name = reader.nextName();
            switch (name) {
                case "title":
                    title = reader.nextString();
                    break;
                case "Dish":
                    dishes = readMultipleDishes(reader);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return new Card(title, dishes);
    }

    private ArrayList<Dish> readMultipleDishes(JsonReader reader) throws IOException {
        ArrayList<Dish> dishes = new ArrayList<>();

        reader.beginArray();
        while (reader.hasNext()){
            dishes.add(readSingleDish(reader));
        }
        reader.endArray();
        return dishes;
    }

    private Dish readSingleDish(JsonReader reader) throws IOException {
        String dishName = null;
        String dishDescription = null;
        String price = null;
        int quantity = 0;
        boolean available = true, prepared = false;
        Uri image = null;

        reader.beginObject();
        while(reader.hasNext()){
            String name = reader.nextName();
            switch (name) {
                case "available":
                    available = reader.nextBoolean();
                    break;
                case "dishName":
                    dishName = reader.nextString();
                    break;
                case "dishDescription":
                    dishDescription = reader.nextString();
                    break;
                case "price":
                    price = reader.nextString();
                    break;
                case "image":
                    image = Uri.parse(reader.nextString().replace('\\', Character.MIN_VALUE));
                    break;
                case "quantity":
                    quantity = reader.nextInt();
                    break;
                case "prepared":
                    prepared = reader.nextBoolean();
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        Dish result = new Dish(dishName, dishDescription, Float.valueOf(Objects.requireNonNull(price)), image);
        result.setQuantity(quantity);
        result.setPrepared(prepared);
        result.setAvailable(available);
        return result;
    }

    String ordersToJSON(ArrayList<OrderItem> orders){
        JSONObject obj = new JSONObject();
        JSONArray objOrdArray = new JSONArray();
        try {
            for (OrderItem ord : orders) {
                JSONObject objOrd = new JSONObject();
                objOrd.put("pieces", ord.getPieces());
                objOrd.put("orderName", ord.getOrderName());
                objOrd.put("price", ord.getPrice());
                objOrdArray.put(objOrd);
            }
            obj.put("Order", objOrdArray);
        }
        catch (JSONException e){
            e.getMessage();
            return "Error String";
        }
        return obj.toString();
    }

    String reviewsToJSON(ArrayList<Review> reviews){
        JSONObject obj = new JSONObject();
        JSONArray objRevArray = new JSONArray();
        try{
            for(Review r :reviews){
                JSONObject objRev = new JSONObject();
                objRev.put("reviewID", r.getReviewID());
                objRev.put("userID", r.getUserID());
                objRev.put("userName", r.getUserName());
                objRev.put("imagePath", r.getImagePath());
                objRev.put("note", r.getNote());
                objRev.put("rating", r.getRating());
                objRevArray.put(objRev);
            }
            obj.put("Review",objRevArray);

        }catch(JSONException e){
            e.getMessage();
            return "Error String";
        }
        return  obj.toString();
    }

    String toJSON (ArrayList<Card> cards){
        JSONObject obj = new JSONObject();
        JSONArray objCardArray = new JSONArray();
        try {
            for (Card card1 : cards) {
                JSONObject objCard = new JSONObject();
                objCard.put("title", card1.getTitle());
                ArrayList<Dish> dishes = card1.getDishes();
                JSONArray objDishArray = new JSONArray();
                for (Dish dish : dishes) {
                    JSONObject objDish = new JSONObject();
                    objDish.put("dishName", dish.getDishName());
                    objDish.put("dishDescription", dish.getDishDescription());
                    objDish.put("price", dish.getPrice());
                    objDish.put("image", dish.getImage());
                    objDish.put("available", dish.isAvailable());
                    objDishArray.put(objDish);
                }
                objCard.put("Dish", objDishArray);
                objCardArray.put(objCard);
            }
            obj.put("Card", objCardArray);
        }
        catch (JSONException e){
            e.getMessage();
            return "Error String";
        }
        return obj.toString();
    }

    String resToJSON (ArrayList<Reservation> reservations){
        JSONObject obj = new JSONObject();
        JSONArray objCardArray = new JSONArray();
        try {
            for (Reservation res1 : reservations) {
                JSONObject objRes = new JSONObject();
                objRes.put("reservationID", res1.getReservationID());
                objRes.put("userName", res1.getUserName());
                objRes.put("userPhone", res1.getUserPhone());
                objRes.put("userLevel", res1.getUserLevel());
                objRes.put("userEmail", res1.getUserEmail());
                objRes.put("userAddress", res1.getUserAddress());
                objRes.put("resNote", res1.getResNote());
                objRes.put("orderTime", res1.getOrderTime());
                objRes.put("accepted", res1.isAccepted());
                objRes.put("preparationStatus", res1.getPreparationStatusString());
                objRes.put("toBePrepared", res1.getToBePrepared());
                ArrayList<Dish> dishes = res1.getDishesOrdered();
                JSONArray objDishArray = new JSONArray();
                for (Dish dish : dishes) {
                    JSONObject objDish = new JSONObject();
                    objDish.put("dishName", dish.getDishName());
                    objDish.put("dishDescription", dish.getDishDescription());
                    objDish.put("price", dish.getPrice());
                    objDish.put("image", dish.getImage());
                    objDish.put("available", dish.isAvailable());
                    objDish.put("quantity", dish.getQuantity());
                    objDish.put("prepared", dish.isPrepared());
                    objDishArray.put(objDish);
                }
                objRes.put("Dish", objDishArray);
                objCardArray.put(objRes);
            }
            obj.put("Reservation", objCardArray);
        }
        catch (JSONException e){
            e.getMessage();
            return "Error String";
        }
        return obj.toString();
    }

    void saveStringToFile(String json, File file){
        FileOutputStream outputStream = null;
        try{
            outputStream = new FileOutputStream(file);
            outputStream.write(json.getBytes());
            outputStream.close();
        } catch (IOException e){
            e.getMessage();
        } finally {
            if (outputStream != null){
                try{
                    outputStream.close();
                } catch (IOException e){
                    e.getMessage();
                }
            }
        }
    }
}
