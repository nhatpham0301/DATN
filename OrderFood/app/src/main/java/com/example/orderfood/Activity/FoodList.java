package com.example.orderfood.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Database.Database;
import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.Model.Category;
import com.example.orderfood.Model.Favorites;
import com.example.orderfood.Model.Foods;
import com.example.orderfood.Model.Order;
import com.example.orderfood.R;
import com.example.orderfood.ViewHolder.FoodViewHolder;
import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.stepstone.apprating.C;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class FoodList extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    RecyclerView recyclerView;
    CounterFab fab;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseRecyclerAdapter adapter;
    String CategoryId = "CategoryId";
    String CategoryName = "CategoryName";
    DecimalFormat formatter;

    //Database Favorite
    Database dbLocal;

    // Search foodList
    FirebaseRecyclerAdapter<Foods, FoodViewHolder> searchAdapter;
    List<String> suggestList;
    MaterialSearchBar materialSearchBar;
    SwipeRefreshLayout srlLayout;

    // Facebook share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    // Create target from picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

            SharePhoto sharePhoto = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();
            if (ShareDialog.canShow(SharePhotoContent.class)){

                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(sharePhoto)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        // Setup actionBar
        if (getIntent() != null) {
            CategoryName = getIntent().getStringExtra(Category.CATEGORY_NAME).toLowerCase();
        }
        getSupportActionBar().setTitle(CategoryName);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Init facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);

        addControl();
        addEvent();
    }

    private void loadFood(String id) {
        FirebaseRecyclerOptions<Foods> options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                .setQuery(reference.orderByChild("menuId").equalTo(id), Foods.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Foods, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final FoodViewHolder viewHolder, final int i, final Foods model) {
                viewHolder.txtFood.setText(model.getName());
                int price = Integer.parseInt(model.getPrice());
                viewHolder.txtPrice.setText(formatter.format(price) + " VND");
                Glide.with(getApplicationContext())
                        .load(model.getImage())
                        .placeholder(R.drawable.my_bg)
                        .into(viewHolder.imageFood);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {
                        Intent intent = new Intent(getApplicationContext(), FoodDetail.class);
                        intent.putExtra(Foods.FOOD_ID, adapter.getRef(position).getKey());
                        intent.putExtra(Foods.FOOD_NAME, model.getName());
                        startActivity(intent);
                    }
                });

                //add Favorite
                if(dbLocal.isFavorite(adapter.getRef(i).getKey(), Common.currentUser.getPhone()))
                    viewHolder.imageFavorite.setImageResource(R.drawable.ic_favorite_black);
                
                viewHolder.imageFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef(i).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(model.getPrice());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());

                        if(!dbLocal.isFavorite(adapter.getRef(i).getKey(), Common.currentUser.getPhone())){
                            dbLocal.addToFavorite(favorites);
                            viewHolder.imageFavorite.setImageResource(R.drawable.ic_favorite_black);
                            Toast.makeText(FoodList.this, ""+ model.getName() + " đã được thêm vào ds yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            dbLocal.removeFromFavorites(adapter.getRef(i).getKey(), Common.currentUser.getPhone());
                            viewHolder.imageFavorite.setImageResource(R.drawable.ic_favorite_border_black);
                            Toast.makeText(FoodList.this, "" + model.getName() + " đã được xóa khỏi ds yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // click to share
                viewHolder.imageShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.get().load(model.getImage()).into(target);
                    }
                });

                viewHolder.imageCart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        boolean isExists = new Database(getBaseContext()).checkFoodExists(
                                adapter.getRef(i).getKey(), Common.currentUser.getPhone()
                        );

                        if (!isExists) {
                            new Database(getApplicationContext()).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    adapter.getRef(i).getKey(),
                                    model.getName(),
                                    "1",
                                    model.getPrice(),
                                    model.getDiscount(),
                                    model.getImage()
                            ));
                        }else {
                            new Database(getApplicationContext()).increaseCart(
                                    Common.currentUser.getPhone(), adapter.getRef(i).getKey()
                            );
                        }

                        Toast.makeText(FoodList.this, "Thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_food, parent, false);
                return new FoodViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void addEvent() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Cart.class);
                startActivity(intent);
            }
        });

        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
    }

    private void addControl() {
        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Foods");
        fab = findViewById(R.id.fab);

        dbLocal = new Database(this);
        formatter = new DecimalFormat("###,###,###");

        if(getIntent() != null)
            CategoryId = getIntent().getStringExtra(Category.CATEGORY_ID);

        if(!CategoryId.isEmpty() && CategoryId != null) {

            if(Common.isConnectToInternet(getBaseContext())) {
                loadFood(CategoryId);
            } else {
                Toast.makeText(FoodList.this, "Please Check your connection !!!", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        suggestList = new ArrayList<>();
        materialSearchBar = findViewById(R.id.searchBar);
        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<String> suggest = new ArrayList<String>();
                for(String search: suggestList)
                {
                    if(search.toLowerCase().contains(materialSearchBar.getText().toLowerCase()))
                        suggest.add(search);
                }
                materialSearchBar.setLastSuggestions(suggest);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if(!enabled)
                    recyclerView.setAdapter(adapter);
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text);
            }

            @Override
            public void onButtonClicked(int buttonCode) {
            }
        });
        srlLayout = findViewById(R.id.srlLayoutFoodList);
        srlLayout.setColorSchemeColors(getResources().getColor(R.color.colorGreen), getResources().getColor(R.color.colorBlue));
        srlLayout.setOnRefreshListener(this);
    }

    private void startSearch(CharSequence text) {
        Toast.makeText(this, ""+text, Toast.LENGTH_SHORT).show();
        FirebaseRecyclerOptions<Foods> options =
                        new FirebaseRecyclerOptions.Builder<Foods>()
                        .setQuery(reference.orderByChild("name").equalTo(text.toString()), Foods.class)
                        .build();
        searchAdapter = new FirebaseRecyclerAdapter<Foods, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(final FoodViewHolder viewHolder, final int i,final Foods model) {
                viewHolder.txtFood.setText(model.getName());
                viewHolder.txtPrice.setText(formatter.format(Integer.parseInt(model.getPrice())) + " VND");
                Glide.with(getApplicationContext())
                        .load(model.getImage())
                        .placeholder(R.drawable.my_bg)
                        .into(viewHolder.imageFood);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {
                        Intent intent = new Intent(getApplicationContext(), FoodDetail.class);
                        intent.putExtra(Foods.FOOD_ID, searchAdapter.getRef(position).getKey());
                        intent.putExtra(Foods.FOOD_NAME, model.getName());
                        startActivity(intent);
                    }
                });
                //add Favorite
                if(dbLocal.isFavorite(adapter.getRef(i).getKey(), Common.currentUser.getPhone()))
                    viewHolder.imageFavorite.setImageResource(R.drawable.ic_favorite_black);

                viewHolder.imageFavorite.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Favorites favorites = new Favorites();
                        favorites.setFoodId(adapter.getRef(i).getKey());
                        favorites.setFoodName(model.getName());
                        favorites.setFoodDescription(model.getDescription());
                        favorites.setUserPhone(Common.currentUser.getPhone());
                        favorites.setFoodPrice(model.getPrice());
                        favorites.setFoodDiscount(model.getDiscount());
                        favorites.setFoodImage(model.getImage());
                        favorites.setFoodMenuId(model.getMenuId());

                        if(!dbLocal.isFavorite(adapter.getRef(i).getKey(), Common.currentUser.getPhone())){
                            dbLocal.addToFavorite(favorites);
                            viewHolder.imageFavorite.setImageResource(R.drawable.ic_favorite_black);
                            Toast.makeText(FoodList.this, ""+ model.getName() + " đã được thêm vào ds yêu thích", Toast.LENGTH_SHORT).show();
                        } else {
                            dbLocal.removeFromFavorites(adapter.getRef(i).getKey(), Common.currentUser.getPhone());
                            viewHolder.imageFavorite.setImageResource(R.drawable.ic_favorite_border_black);
                            Toast.makeText(FoodList.this, "" + model.getName() + " đã được xóa khỏi ds yêu thích", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                // click to share
                viewHolder.imageShare.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Picasso.get().load(model.getImage()).into(target);
                    }
                });

            }
            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_food, parent, false);
                return new FoodViewHolder(view);
            }
        };
        searchAdapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(searchAdapter);
    }

    private void loadSuggest() {
        reference.orderByChild("menuId").equalTo(CategoryId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren())
                        {
                            Foods item = ds.getValue(Foods.class);
                            suggestList.add(item.getName());

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadFood(CategoryId);
                srlLayout.setRefreshing(false);
            }
        }, 1500);
    }

    //    @Override
//    protected void onStop() {
//        super.onStop();
//        adapter.stopListening();
//        searchAdapter.stopListening();
//    }


    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
    }
}
