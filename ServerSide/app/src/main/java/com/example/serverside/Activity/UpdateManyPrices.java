package com.example.serverside.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.serverside.Model.Foods;
import com.example.serverside.Model.UpdatePrice;
import com.example.serverside.R;
import com.example.serverside.ViewHolder.UpdateManyPriceViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class UpdateManyPrices extends AppCompatActivity {

    Button btnCancel, btnUpdate;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    DatabaseReference reference;
    FirebaseRecyclerAdapter<Foods, UpdateManyPriceViewHolder> adapter;

    FirebaseRecyclerOptions<Foods> options;
    UpdatePrice modelPrice;
    List<UpdatePrice> updateFood, price;
    String CategoryID = "";
    ProgressDialog progressDialog;
    private Timer timer = new Timer();
    private final long DELAY = 1000;
    private long LAST_TEXT_EDIT = 0;
    Handler handler = new Handler();
    private String S_EDIT_TEXT = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_many_prices);

        getSupportActionBar().setTitle("Cập nhật giá");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addControl();
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
        addEvent();
    }

    private void addEvent() {
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();
            }
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               updatePrice();
                Toast.makeText(UpdateManyPrices.this, "Update!!!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePrice() {
        progressDialog.setMessage("Upload...");
        progressDialog.show();
        for(UpdatePrice model : price){
            reference.child(model.getKey()).child("price").setValue(model.getPrice());
        }
        progressDialog.dismiss();
    }

    private void addControl() {
        recyclerView = findViewById(R.id.recycler_updatePrice);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        reference = FirebaseDatabase.getInstance().getReference("Foods");
        btnCancel = findViewById(R.id.btnCancelUpdatePrice);
        btnUpdate = findViewById(R.id.btnUpdatePrice);
        updateFood = new ArrayList<>();
        price = new ArrayList<>();
        progressDialog = new ProgressDialog(this);

        if(getIntent() != null){
            CategoryID = getIntent().getStringExtra("CategoryId");
        }

        if(!CategoryID.isEmpty() && CategoryID != null){
            loadMenu(CategoryID);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }

    private void loadMenu(String categoryID) {
       options = new FirebaseRecyclerOptions.Builder<Foods>()
               .setQuery(reference.orderByChild("menuId").equalTo(categoryID), Foods.class)
               .build();

        adapter = new FirebaseRecyclerAdapter<Foods, UpdateManyPriceViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final UpdateManyPriceViewHolder updateManyPriceViewHolder,final int i, Foods foods) {
                updateManyPriceViewHolder.idFood.setText(adapter.getRef(i).getKey());
                updateManyPriceViewHolder.nameFood.setText(foods.getName());
                updateManyPriceViewHolder.priceOld.setText(foods.getPrice());

                final Runnable input_finish_checker = new Runnable() {
                    @Override
                    public void run() {
                        if (System.currentTimeMillis() > (LAST_TEXT_EDIT + DELAY - 500)) {
                            modelPrice = new UpdatePrice(adapter.getRef(i).getKey(),S_EDIT_TEXT);
                            if(price.size() == 0)
                                price.add(modelPrice);
                            else {
                                for(int j = 0; j < price.size(); j++){
                                    UpdatePrice model = price.get(j);
                                    if(S_EDIT_TEXT == ""){
                                        if(model.getKey() == modelPrice.getKey()){
                                            price.remove(j);
                                        }
                                    } else {
                                        if (model.getKey() == modelPrice.getKey()) {
                                            price.get(j).setPrice(S_EDIT_TEXT);
                                            break;
                                        }

                                        if (j == price.size() - 1) {
                                            price.add(modelPrice);
                                            Toast.makeText(UpdateManyPrices.this, "" + price.size(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                                Toast.makeText(UpdateManyPrices.this, "" + S_EDIT_TEXT, Toast.LENGTH_SHORT).show();
                                for(UpdatePrice model1 : price){
                                    Log.d("PRICE", model1.getKey());
                                    Log.d("PRICE", model1.getPrice());
                                }
                            }
                        }
                    }
                };

                updateManyPriceViewHolder.priceNew.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        handler.removeCallbacks(input_finish_checker);
                    }

                    @Override
                    public void afterTextChanged(final Editable s) {
                        if (s.length() >= 3) {
                            LAST_TEXT_EDIT = System.currentTimeMillis();
                            S_EDIT_TEXT = s.toString();
                            handler.postDelayed(input_finish_checker, DELAY);
                        } else if(s.length() == 1){
                            LAST_TEXT_EDIT = System.currentTimeMillis();
                            S_EDIT_TEXT = "";
                            handler.postDelayed(input_finish_checker, 500);
                        }
                    }
                });

                modelPrice = new UpdatePrice(adapter.getRef(i).getKey(),
                        updateManyPriceViewHolder.priceOld.getText().toString());
                updateFood.add(modelPrice);

            }

            @NonNull
            @Override
            public UpdateManyPriceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_update_prices, parent, false);
                return new UpdateManyPriceViewHolder(view);
            }
        };
    }
}