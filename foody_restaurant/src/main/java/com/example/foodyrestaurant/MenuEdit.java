package com.example.foodyrestaurant;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import static java.security.AccessController.getContext;

public class MenuEdit extends AppCompatActivity {

    private RecyclerView recyclerMenu;
    private RVAdapterEdit recyclerAdapter;

    LinearLayoutManager llm;
    private FloatingActionButton mainFAB;
    private ArrayList<Card> cards;
    private JsonHandler jsonHandler;
    private final String JSON_PATH = "menu.json";
    private final String JSON_COPY = "menuCopy.json";
    private File storageDir;
    private ImageButton back;
    private ImageButton save;
    private ImageButton edit;
    private ImageButton exit;
    private ImageView plus, trash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_edit);

        init();


    }

    private void init(){
        final File fileTmp = new File(storageDir, JSON_COPY);

        jsonHandler = new JsonHandler();
        storageDir =  getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        File file = new File(storageDir, JSON_PATH);
        recyclerMenu = findViewById(R.id.menu_edit);
        llm = new LinearLayoutManager(this);
        recyclerMenu.setLayoutManager(llm);
        mainFAB = findViewById(R.id.mainFAB);
        cards = jsonHandler.getCards(file);

        save = findViewById(R.id.saveButton);
        back = findViewById(R.id.backButton);
        edit = findViewById(R.id.editButton);
        exit = findViewById(R.id.endButton);
        plus = findViewById(R.id.plus);
        trash = findViewById(R.id.trash);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String json = jsonHandler.toJSON(cards);
                storageDir =  getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                File file = new File(storageDir, JSON_PATH);
                jsonHandler.saveStringToFile(json, file);
                finish();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                back();
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exitEdit();
            }
        });


        /*
        cards = new ArrayList<>();

        ArrayList<Dish> dishes = new ArrayList<>();
        dishes.add(new Dish("Margerita","Pomodoro, Mozzarella, Basilico","3,50 €", null));
        dishes.add(new Dish("Vegetariana","Verdure di Stagione, Pomodoro, Mozzarella","8,00 €", null));
        dishes.add(new Dish("Quattro Stagioni","Pomodoro, Mozzarella, Prosciutto, Carciofi, Funghi, Olive, Grana a Scaglie","6,50 €", null));
        dishes.add(new Dish("Quattro Formaggi","Mozzarella, Gorgonzola, Fontina, Stracchino","7,00 €", null));
        Card c = new Card("Pizza");
        c.setDishes(dishes);
        cards.add(c);

        dishes = new ArrayList<>();
        dishes.add(new Dish("Pasta al Pomodoro","Rigationi, Pomodoro, Parmigiano, Basilico","3,50 €", null));
        dishes.add(new Dish("Carbonara","Spaghetti, Uova, Guanciale, Pecorino, Pepe Nero","8,00 €", null));
        dishes.add(new Dish("Pasta alla Norma","Pomodoro, Pancetta, Melanzane, Grana a Scaglie","6,50 €", null));
        dishes.add(new Dish("Puttanesca","Pomodoro, Peperoncino, Pancetta, Parmigiano","7,00 €", null));
        c = new Card("Primi");
        c.setDishes(dishes);
        cards.add(c);

        dishes = new ArrayList<>();
        dishes.add(new Dish("Braciola Di Maiale","Braciola, Spezie","3,50 €", null));
        dishes.add(new Dish("Stinco Alla Birra","Stinco di Maiale, Birra","8,00 €", null));
        dishes.add(new Dish("Cotoletta e Patatine","Cotoletta di Maiale, Patatine","6,50 €", null));
        dishes.add(new Dish("Filetto al pepe verde","Filetto di Maiale, Salsa alla Senape, Pepe verde in grani","7,00 €", null));
        c = new Card("Secondi");
        c.setDishes(dishes);
        cards.add(c);
        */


        recyclerAdapter = new RVAdapterEdit(cards);
        recyclerMenu.setAdapter(recyclerAdapter);

        mainFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Card c = new Card("PLACEHOLDER TRY");
                cards.add(c);
                recyclerAdapter.notifyItemInserted(cards.size()-1);
            }
        });

    }

    private void back(){
        finish();
    }
  
    private void edit(){
        animateToEdit(edit, save, exit, mainFAB, plus, trash);
    }

    private void exitEdit(){
        animateToNormal(edit, save, exit, mainFAB, plus, trash);
    }

    private void animateToEdit(final ImageButton edit,final ImageButton save,final ImageButton end,
                               FloatingActionButton fab, final ImageView plus, final ImageView trash){
        int shortAnimDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);


        edit.animate().alpha(0.0f).setDuration(shortAnimDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                edit.setVisibility(View.GONE);
            }
        }).start();
        edit.animate().scaleX(0.2f).scaleY(0.2f).setDuration(shortAnimDuration).start();

        save.animate().alpha(0.0f).setDuration(shortAnimDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                save.setVisibility(View.GONE);
            }
        }).start();
        save.animate().scaleX(0.2f).scaleY(0.2f).setDuration(shortAnimDuration).start();

        end.setScaleY(0.2f);
        end.setScaleX(0.2f);
        end.setAlpha(0.0f);
        end.setVisibility(View.VISIBLE);
        end.animate().alpha(1.0f).setDuration(shortAnimDuration).start();
        end.animate().scaleX(1f).scaleY(1f).setDuration(shortAnimDuration).setListener(null).start();

        fab.setBackgroundTintList(getColorStateList(R.color.errorColor));


        for(int i = 0; i< cards.size(); i++){
            cards.get(i).setEditing(true);
        }

        for(int i = 0; i< cards.size(); i++){
            if(recyclerAdapter.normalToEdit(recyclerMenu.findViewHolderForAdapterPosition(i))==false)
                recyclerAdapter.notifyItemChanged(i);
        }

        plus.animate().alpha(0.0f).setDuration(shortAnimDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                plus.setVisibility(View.GONE);
            }
        }).start();
        plus.animate().scaleX(0.2f).scaleY(0.2f).setDuration(shortAnimDuration).start();


        trash.setScaleY(0.2f);
        trash.setScaleX(0.2f);
        trash.setAlpha(0.0f);
        trash.setVisibility(View.VISIBLE);
        trash.animate().alpha(1.0f).setDuration(shortAnimDuration).setListener(null).start();
        trash.animate().scaleX(1.f).scaleY(1.f).setDuration(shortAnimDuration).setListener(null).start();
    }

    private void animateToNormal(final ImageButton edit,final ImageButton save,final ImageButton end,
                                 FloatingActionButton fab,final ImageView plus,final ImageView trash){
        int shortAnimDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);


        edit.setScaleY(0.2f);
        edit.setScaleX(0.2f);
        edit.setAlpha(0.0f);
        edit.setVisibility(View.VISIBLE);
        edit.animate().alpha(1.0f).setDuration(shortAnimDuration).setListener(null).start();
        edit.animate().scaleX(1.f).scaleY(1.f).setDuration(shortAnimDuration).setListener(null).start();

        save.setScaleY(0.2f);
        save.setScaleX(0.2f);
        save.setAlpha(0.0f);
        save.setVisibility(View.VISIBLE);
        save.animate().alpha(1.0f).setDuration(shortAnimDuration).setListener(null).start();
        save.animate().scaleX(1.f).scaleY(1.f).setDuration(shortAnimDuration).setListener(null).start();

        end.animate().alpha(0.0f).setDuration(shortAnimDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                end.setVisibility(View.GONE);
            }
        }).start();
        end.animate().scaleX(0.2f).scaleY(0.2f).setDuration(shortAnimDuration).start();

        fab.setBackgroundTintList(getColorStateList(R.color.colorAccent));


        for(int i = 0; i< cards.size(); i++){
            cards.get(i).setEditing(false);
            cards.get(i).setSelected(false);
        }

        for(int i = 0; i< cards.size(); i++){
            if(recyclerAdapter.editToNormal(recyclerMenu.findViewHolderForAdapterPosition(i), i) == false)
                recyclerAdapter.notifyItemChanged(i);
        }

        trash.animate().alpha(0.0f).setDuration(shortAnimDuration).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                trash.setVisibility(View.GONE);
            }
        }).start();
        trash.animate().scaleX(0.2f).scaleY(0.2f).setDuration(shortAnimDuration).start();


        plus.setScaleY(0.2f);
        plus.setScaleX(0.2f);
        plus.setAlpha(0.0f);
        plus.setVisibility(View.VISIBLE);
        plus.animate().alpha(1.0f).setDuration(shortAnimDuration).setListener(null).start();
        plus.animate().scaleX(1.f).scaleY(1.f).setDuration(shortAnimDuration).setListener(null).start();

    }

}
