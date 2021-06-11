package com.example.orderfood.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Database.Database;
import com.example.orderfood.Model.Foods;
import com.example.orderfood.Model.Order;
import com.example.orderfood.Model.Rating;
import com.example.orderfood.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener {

    TextView txtFoodName, txtFoodPrice, txtFoodDescription;
    ImageView imageFood;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton fabRating;
    CounterFab fabCart;
    ElegantNumberButton btnNumber;
    RatingBar ratingBar;
    Button btnShowComment;

    String foodId = "foodId";
    String foodName = "foodName";

    FirebaseDatabase database;
    DatabaseReference reference;
    DatabaseReference ratingDB;

    Foods currentFood;
    int price;
    String number;
    DecimalFormat formatter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        // Setup actionBar
        if (getIntent() != null) {
            foodName = getIntent().getStringExtra(Foods.FOOD_NAME).toLowerCase();
        }
        getSupportActionBar().setTitle(foodName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        addControl();
        addEvent();
    }

    private void addEvent() {

        btnNumber.setOnClickListener(new ElegantNumberButton.OnClickListener() {
            @Override
            public void onClick(View view) {
                number = btnNumber.getNumber();
                price = Integer.parseInt(currentFood.getPrice()) * Integer.parseInt(number);
                txtFoodPrice.setText(formatter.format(price) + " VND");
            }
        });


        btnShowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(FoodDetail.this, ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID, foodId);
                startActivity(intent);
            }
        });
        fabCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = btnNumber.getNumber();
                boolean isExists = new Database(getBaseContext()).checkFoodExists(
                        foodId, Common.currentUser.getPhone()
                );

                if (!isExists) {
                    new Database(getApplicationContext()).addToCart(new Order(
                            Common.currentUser.getPhone(),
                            foodId,
                            currentFood.getName(),
                            number,
                            currentFood.getPrice(),
                            currentFood.getDiscount(),
                            currentFood.getImage()
                    ));
                } else {
                    new Database(getApplicationContext()).increaseCart(
                            Common.currentUser.getPhone(), foodId
                    );
                }

                Toast.makeText(FoodDetail.this, "Thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });

        fabCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));

        checkRating();
    }

    private void checkRating() {

        DatabaseReference countOrder = database.getReference(Common.ORDER_FOOD_WITH_PHONE_TABLE)
                .child(foodId)
                .child(Common.currentUser.getPhone());
        countOrder.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    long countOrderWithPhone = dataSnapshot.getChildrenCount();
                    compareOrderWithRating(countOrderWithPhone);
                }
                else {
                    fabRating.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Toast.makeText(FoodDetail.this, "Bạn chưa ăn món này!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void compareOrderWithRating(final long countOrder){

        DatabaseReference countRating = database.getReference(Common.RATING_TABLE).child(foodId);
        Query query = countRating.orderByChild("userPhone").equalTo(Common.currentUser.getPhone());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()){
                    final long countRatingWithPhone = dataSnapshot.getChildrenCount();

                    fabRating.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            // Allow Rating
                            if (countRatingWithPhone < countOrder){
                                showRatingDialog();
                            }else {
                                Toast.makeText(FoodDetail.this, "Bạn đã hết lượt đánh giá!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else {
                    fabRating.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            showRatingDialog();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setNegativeButtonText("Hủy")
                .setPositiveButtonText("Đánh giá")
                .setNoteDescriptions(Arrays.asList("Rất tệ", "Không ngon", "Bình thường", "Ngon", "Rất ngon"))
                .setDefaultRating(1)
                .setTitle("Rate is good")
                .setDescription("Vui lòng đánh giá sao và để lại cảm nhận của bạn!")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Viết đánh giá của bạn ở đây ...")
                .setHintTextColor(android.R.color.white)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();
    }

    private void addControl() {
        formatter = new DecimalFormat("###,###,###");
        txtFoodName = findViewById(R.id.txtNameFoodDetail);
        txtFoodPrice = findViewById(R.id.txtPriceFoodDetail);
        txtFoodDescription = findViewById(R.id.txtDescription);
        imageFood = findViewById(R.id.imageFoodDetail);
        fabCart = findViewById(R.id.fabCartFoodDetail);
        btnNumber = findViewById(R.id.numberButtonFoodDetail);
        fabRating = findViewById(R.id.fabFoodDetailRating);
        ratingBar = findViewById(R.id.ratingBar);
        btnShowComment = findViewById(R.id.foodDetailAct_btnShowComment);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Foods");
        ratingDB = FirebaseDatabase.getInstance().getReference("Rating");

        if (getIntent() != null)
            foodId = getIntent().getStringExtra(Foods.FOOD_ID);
        if (!foodId.isEmpty() && foodId != null) {

            if (Common.isConnectToInternet(getBaseContext())) {
                loadFoodDetail(foodId);
                loadRatingFood(foodId);
            } else {
                Toast.makeText(FoodDetail.this, "Please Check your connection !!!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void loadRatingFood(String foodId) {
        Query foodRating = ratingDB.child(foodId);

        foodRating.addValueEventListener(new ValueEventListener() {
            int count = 0, sum = 0;

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {

                    Rating item = ds.getValue(Rating.class);
                    sum += Integer.parseInt(item.getRateValue());
                    count++;
                }
                if (count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void loadFoodDetail(String Id) {
        reference.child(Id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Foods.class);

                Glide.with(getApplicationContext())
                        .load(currentFood.getImage())
                        .placeholder(R.drawable.my_bg)
                        .into(imageFood);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                txtFoodName.setText(currentFood.getName());
                int price = Integer.parseInt(currentFood.getPrice());
                txtFoodPrice.setText(formatter.format(price) + " VND");
                txtFoodDescription.setText(currentFood.getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, String comment) {
        final String phone = Common.currentUser.getPhone();
        final Rating rating = new Rating(phone, foodId, String.valueOf(value), comment);

        // Fix use can rate multiple times
        ratingDB.child(foodId).push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Toast.makeText(FoodDetail.this, "Cảm ơn bạn đã để lại đánh giá!", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
