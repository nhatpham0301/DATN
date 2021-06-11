package com.example.orderfood.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;

import com.example.orderfood.Adapter.OrderAdapter;
import com.example.orderfood.Common.Common;
import com.example.orderfood.Database.Database;
import com.example.orderfood.Interface.ItemClickListener;
import com.example.orderfood.Model.Banner;
import com.example.orderfood.Model.Category;
import com.example.orderfood.Model.Request;
import com.example.orderfood.Model.Token;
import com.example.orderfood.R;
import com.example.orderfood.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.ArrayList;
import java.util.HashMap;

import dmax.dialog.SpotsDialog;
import edmt.dev.edmtslider.Animations.DescriptionAnimation;
import edmt.dev.edmtslider.SliderLayout;
import edmt.dev.edmtslider.SliderTypes.BaseSliderView;
import edmt.dev.edmtslider.SliderTypes.TextSliderView;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Home extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener{

    SwipeRefreshLayout srlLayout;
    FirebaseDatabase database;
    DatabaseReference reference;
    Toolbar toolbar;
    CounterFab fab;
    DrawerLayout drawer;
    NavigationView navigationView;
    View viewHeader;
    TextView txtFullName;
    RecyclerView recyclerView;
    FirebaseRecyclerAdapter adapter;

    SliderLayout mSlider;
    // Slider
    HashMap<String, String> imageList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        addControl();

        toolbar.setTitle("Menu");
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });


        if(Common.isConnectToInternet(getBaseContext())){
            loadMenu();
        } else {
            Toast.makeText(Home.this, "Vui lòng kiểm tra internet !!!", Toast.LENGTH_SHORT).show();
            return;
        }

        updateToken(FirebaseInstanceId.getInstance().getToken());
        addEvent();

        // Setup Slider
        // Need call function after you init firebase
        setupSlider();

    }

    private void setupSlider() {

        mSlider = findViewById(R.id.slider);
        imageList = new HashMap<>();

        final DatabaseReference banners = database.getReference(getString(R.string.Banner));
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    Banner banner = postSnapshot.getValue(Banner.class);
                    // We will concat string name and id like
                    // PIZZA@@@01 => And we will use PIZZA for show description, 01 for food id to click
                    imageList.put(banner.getName() + "@@@" + banner.getId(), banner.getImage());
                }

                for (String key : imageList.keySet()){
                    String[] keySplit = key.split("@@@");
                    String nameOfFood = keySplit[0];
                    String idOfFood = keySplit[1];

                    // Create Slider
                    final TextSliderView textSliderView = new TextSliderView(getBaseContext());
                    textSliderView.description(nameOfFood)
                            .image(imageList.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit);

                    // Add extra bundle
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString(Category.CATEGORY_ID, idOfFood);
                    textSliderView.getBundle().putString(Category.CATEGORY_NAME, nameOfFood);

                    mSlider.addSlider(textSliderView);

                    // Remove event after finish
                    banners.removeEventListener(this);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mSlider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mSlider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mSlider.setCustomAnimation(new DescriptionAnimation());
        mSlider.setDuration(4000);
    }

    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference reference = db.getReference("Tokens");
        Token data = new Token(token, false);
        reference.child(Common.currentUser.getPhone()).setValue(data);
    }

    private void loadMenu() {

        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Animation
        recyclerView.getAdapter().notifyDataSetChanged();
        recyclerView.scheduleLayoutAnimation();
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

        viewHeader = navigationView.getHeaderView(0);
        txtFullName = viewHeader.findViewById(R.id.txtFullName);
        txtFullName.setText(Common.currentUser.getName());
    }

    @SuppressLint("ResourceAsColor")
    private void addControl() {
        toolbar = findViewById(R.id.toolbar);
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Category");

        // animation right call when after getInstance database
        FirebaseRecyclerOptions<Category> optionsCategory =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(reference, Category.class)
                        .build();
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(optionsCategory) {
            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.row_menu, parent, false);

                return new MenuViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(MenuViewHolder viewHolder, int i, final Category model) {
                viewHolder.txtNameMenu.setText(model.getName());
                Glide.with(getApplicationContext())
                        .load(model.getImage())
                        .placeholder(R.drawable.my_bg)
                        .into(viewHolder.imageMenu);
                final Category clickItem = model;
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {
                        Intent intent = new Intent(getApplicationContext(), FoodList.class);
                        intent.putExtra(Category.CATEGORY_ID, adapter.getRef(position).getKey());
                        intent.putExtra(Category.CATEGORY_NAME, model.getName());
                        startActivity(intent);
                        Toast.makeText(Home.this, "" + clickItem.getName(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };


        fab = findViewById(R.id.fab);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        recyclerView = findViewById(R.id.recycler_menu);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(
                recyclerView.getContext(),
                R.anim.layout_fall_down);
        recyclerView.setLayoutAnimation(controller);

        srlLayout = findViewById(R.id.srlLayoutHome);
        srlLayout.setColorSchemeColors(R.color.colorGreen, R.color.colorBlue);
        srlLayout.setOnRefreshListener(this);

        Paper.init(this);
    }

    @SuppressWarnings("StatemenWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item){
       switch (item.getItemId()){
           case R.id.nav_menu:
               Toast.makeText(this, "menu", Toast.LENGTH_SHORT).show();
               break;
           case R.id.nav_homeAddress:
               showHomeAddressDialog();
               break;
           case R.id.nav_cart:
               startActivity(new Intent(getApplicationContext(), Cart.class));
               break;
           case R.id.nav_order:
               startActivity(new Intent(getApplicationContext(), Orders.class));
               break;
           case R.id.nav_list_favorites:
               startActivity(new Intent(getApplicationContext(), FavoritesFoodList.class));
               break;
           case R.id.nav_logout:

               // delete user
               FirebaseAuth.getInstance().signOut();

               //delete database
               new Database(this).cleanToCart(Common.currentUser.getPhone());

               Intent intent = new Intent(getApplicationContext(), MainActivity.class);
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
               startActivity(intent);
               Toast.makeText(this, "Đăng xuất", Toast.LENGTH_SHORT).show();
               break;
       }
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void showDialogSettingsSubscribe() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle(R.string.Subscribe_news);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View viewDialog = layoutInflater.inflate(R.layout.settings_subscribe, null);

        final CheckBox ckbSubscribeNews = viewDialog.findViewById(R.id.ckbSubNews);
        alertDialog.setView(viewDialog);

        // Remember state of checkbox
        Paper.init(this);
        String isSubscribeNews = Paper.book().read("sub_news");
        if (isSubscribeNews == null || TextUtils.isEmpty(isSubscribeNews) || isSubscribeNews.equals("false"))
            ckbSubscribeNews.setChecked(false);
        else ckbSubscribeNews.setChecked(true);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (ckbSubscribeNews.isChecked()) {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                    Paper.book().write("sub_news", "true");
                }else {

                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                    Paper.book().write("sub_news", "false");
                }
            }
        });
        alertDialog.show();
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Địa chỉ nhà");
        alertDialog.setMessage(R.string.please_fill_all_information);

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View viewDialog = layoutInflater.inflate(R.layout.row_home_address, null);

        final MaterialEditText editHomeAddress = viewDialog.findViewById(R.id.edtHomeAddress);
        editHomeAddress.setText(Common.currentUser.getHomeAddress());

        alertDialog.setView(viewDialog);
        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Common.currentUser.setHomeAddress(editHomeAddress.getText().toString());

                FirebaseDatabase.getInstance().getReference("Users")
                        .child(Common.currentUser.getPhone())
                        .setValue(Common.currentUser)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(Home.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        alertDialog.show();
    }

    @Override
    public void onRefresh() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadMenu();
                srlLayout.setRefreshing(false);
            }
        }, 1500);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        loadMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.menu_search){

            Intent intent = new Intent(getApplicationContext(), SearchAllFood.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
        mSlider.stopAutoCycle();
    }
}
