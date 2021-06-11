package com.example.orderfood.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.RelativeLayout;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Database.Database;
import com.example.orderfood.Helper.RecyclerItemTouchHelper;
import com.example.orderfood.Interface.RecyclerItemTouchHelperListener;
import com.example.orderfood.Model.Favorites;
import com.example.orderfood.R;
import com.example.orderfood.Adapter.FavoritesAdapter;
import com.example.orderfood.ViewHolder.FavoritesViewHolder;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FavoritesFoodList extends AppCompatActivity implements RecyclerItemTouchHelperListener {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference reference;

    FavoritesAdapter favoritesAdapter;

    RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites_food_list);

        // Setup actionBar
        getSupportActionBar().setTitle("Yêu thích");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        initView();
        configRecyclerView();
        eventDeleteItem();
        loadFavorites();
    }

    private void loadFavorites() {

        favoritesAdapter = new FavoritesAdapter(this, new Database(this)
                .getAllFavorites(Common.currentUser.getPhone()));
        recyclerView.setAdapter(favoritesAdapter);
    }

    private void eventDeleteItem() {

        // Swipe to delete
        ItemTouchHelper.SimpleCallback simpleCallback = new RecyclerItemTouchHelper(0,
                ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(recyclerView);
    }

    private void configRecyclerView() {

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recyclerView.getContext(),
                R.anim.layout_slide_left);
        recyclerView.setLayoutAnimation(controller);
//        recyclerView.setHasFixedSize(true);
    }

    private void initView() {

        recyclerView = findViewById(R.id.favoritesFoodLisAct_recyclerView);
        rootLayout = findViewById(R.id.favoritesFoodLisAct_rootLayout);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {

        if (viewHolder instanceof FavoritesViewHolder){
            String name = ((FavoritesAdapter) recyclerView.getAdapter()).getItem(position).getFoodName();
            final Favorites deleteItem = ((FavoritesAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            favoritesAdapter.removeItem(viewHolder.getAdapterPosition());
            new Database(getBaseContext()).removeFromFavorites(deleteItem.getFoodId(), Common.currentUser.getPhone());

            // Make snackBar
            Snackbar snackbar = Snackbar.make(rootLayout, name + "xóa khỏi danh sách!", Snackbar.LENGTH_LONG);
            snackbar.setAction("Hoàn lại", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    favoritesAdapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getApplicationContext()).addToFavorite(deleteItem);
                }
            });

            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}