package com.example.orderfood.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.orderfood.Common.Common;
import com.example.orderfood.Model.Rating;
import com.example.orderfood.R;
import com.example.orderfood.ViewHolder.ShowCommentViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.shadow.ShadowDrawableWrapper;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowComment extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout mSwipeRefreshLayout;
    FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder> adapter;

    String foodID = "";

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null)
            adapter.stopListening();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_comment);

        // Setup actionBar
        getSupportActionBar().setTitle("Bình luận");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Firebase
        database = FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = findViewById(R.id.showCommentActRecyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Swipe layout
        mSwipeRefreshLayout = findViewById(R.id.showCommentAct_SwipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (getIntent() != null)
                    foodID = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if (!foodID.isEmpty() && foodID != null){
                    Query query = ratingTbl.child(foodID);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull Rating rating) {

                            Log.d("error", rating.getRateValue());
                            Log.d("error1", rating.getComment());
                            Log.d("error2", rating.getUserPhone());

                            showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRateValue()));
                            showCommentViewHolder.txtComment.setText(rating.getComment());
                            showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.row_commnent, parent, false);
                            return new ShowCommentViewHolder(view);
                        }
                    };

                    loadComment(foodID);
                }
            }
        });

        // Thread to load comment on first launch
        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {

                mSwipeRefreshLayout.setRefreshing(true);

                if (getIntent() != null)
                    foodID = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                if (!foodID.isEmpty() && foodID != null){
                    Query query = ratingTbl.child(foodID);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>()
                            .setQuery(query, Rating.class)
                            .build();

                    adapter = new FirebaseRecyclerAdapter<Rating, ShowCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull ShowCommentViewHolder showCommentViewHolder, int i, @NonNull Rating rating) {

                            Log.d("error", rating.getRateValue());
                            Log.d("error1", rating.getComment());
                            Log.d("error2", rating.getUserPhone());
                            showCommentViewHolder.ratingBar.setRating(Float.parseFloat(rating.getRateValue()));
                            showCommentViewHolder.txtComment.setText(rating.getComment());
                            showCommentViewHolder.txtUserPhone.setText(rating.getUserPhone());
                        }

                        @NonNull
                        @Override
                        public ShowCommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                            View view = LayoutInflater.from(parent.getContext())
                                    .inflate(R.layout.row_commnent, parent, false);
                            return new ShowCommentViewHolder(view);
                        }
                    };

                    loadComment(foodID);
                }
            }
        });
    }

    private void loadComment(String foodID) {

        adapter.startListening();

        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
