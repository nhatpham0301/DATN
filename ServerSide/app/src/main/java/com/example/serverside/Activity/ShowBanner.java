package com.example.serverside.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.serverside.Common.Common;
import com.example.serverside.Model.Banner;
import com.example.serverside.R;
import com.example.serverside.ViewHolder.BannerViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShowBanner extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    RelativeLayout relativeLayout;

    FloatingActionButton fab;

    // Firebase
    FirebaseDatabase firebaseDatabase;
    DatabaseReference referenceBanners;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    FirebaseRecyclerAdapter<Banner, BannerViewHolder> adapterBanner;

    // Add new Banner
    MaterialEditText metNameFood, metFoodId;
    Button btnUpLoad, btnSelect;

    Banner newBanner;
    Uri filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_banner);

        setupActionBar();

        relativeLayout = findViewById(R.id.showBannerAct_relativeLayout);
        initFirebase();
        setUpFab();
        setUpRecyclerView();
        loadListBanner();
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle("Quảng cáo");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpRecyclerView() {

        recyclerView = findViewById(R.id.showBannerAct_recyclerView);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void loadListBanner() {

        FirebaseRecyclerOptions<Banner> banners = new FirebaseRecyclerOptions.Builder<Banner>()
                .setQuery(referenceBanners,  Banner.class)
                .build();

        adapterBanner = new FirebaseRecyclerAdapter<Banner, BannerViewHolder>(banners) {
            @Override
            protected void onBindViewHolder(@NonNull BannerViewHolder bannerViewHolder, int i, @NonNull Banner banner) {

                bannerViewHolder.txtNameBanner.setText(banner.getName());
                Glide.with(getApplicationContext()).load(banner.getImage()).into(bannerViewHolder.imageBanner);
            }

            @NonNull
            @Override
            public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.row_banner, parent, false);
                return new BannerViewHolder(view);
            }
        };

        adapterBanner.startListening();

        // Set adapter
        adapterBanner.notifyDataSetChanged();
        recyclerView.setAdapter(adapterBanner);
    }

    private void setUpFab() {

        fab = findViewById(R.id.showBannerAct_fab);

        // Add new banner
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAddBannerDialog();
            }
        });
    }

    private void showAddBannerDialog() {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Thêm quảng cáo");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_banner = inflater.inflate(R.layout.add_new_banner, null);

        btnSelect = add_banner.findViewById(R.id.btnSelectFoodBanner);
        btnUpLoad = add_banner.findViewById(R.id.btnUploadFoodBanner);
        metFoodId = add_banner.findViewById(R.id.metFoodIdBanner);
        metNameFood = add_banner.findViewById(R.id.metFoodNameBanner);

        // Set event for select picture from phone
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseImage();
            }
        });

        // Set event for upload picture after select
        btnUpLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        // Setup AlertDialog
        alertDialog.setView(add_banner);
        alertDialog.setIcon(R.drawable.ic_banner_black_24dp);

        alertDialog.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                if (newBanner != null) {
                    referenceBanners.push().setValue(newBanner);
                    Snackbar.make(relativeLayout, R.string.New_food
                                    + newBanner.getName()
                                    + R.string.was_added,
                            Snackbar.LENGTH_SHORT).show();
                }
                loadListBanner();
            }
        });

        alertDialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
                newBanner = null;

                loadListBanner();
            }
        });

        alertDialog.show();
    }

    private void uploadImage() {

        if (filePath != null){
            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Upload...");
            pd.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("image/" + imageName);
            imageFolder.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            pd.dismiss();
                            Toast.makeText(ShowBanner.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            newBanner = new Banner();
                                            newBanner.setImage(uri.toString());
                                            newBanner.setId(metFoodId.getText().toString());
                                            newBanner.setName(metNameFood.getText().toString());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(ShowBanner.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            pd.setMessage("Uploaded " + progress + "%");
                        }
                    });
        }
    }

    private void chooseImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.Select_picture)), Common.PICK_IMAGE_REQUEST);
    }

    private void initFirebase() {

        firebaseDatabase = FirebaseDatabase.getInstance();
        referenceBanners = firebaseDatabase.getReference("Banner");
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            btnSelect.setText("Image Select!!!");
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {

        if (item.getTitle().equals(Common.UPDATE)) {
            showUpdateBannerDialog(adapterBanner.getRef(item.getOrder()).getKey(), adapterBanner.getItem(item.getOrder()));
        } else if (item.getTitle().equals(Common.DELETE)) {
            deleteBanner(adapterBanner.getRef(item.getOrder()).getKey());
        }

        return super.onContextItemSelected(item);
    }

    private void showUpdateBannerDialog(final String key, final Banner item) {

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Cập nhật quảng cáo");

        final LayoutInflater inflater = this.getLayoutInflater();
        View add_banner = inflater.inflate(R.layout.add_new_banner, null);

        btnSelect = add_banner.findViewById(R.id.btnSelectFoodBanner);
        btnUpLoad = add_banner.findViewById(R.id.btnUploadFoodBanner);
        metFoodId= add_banner.findViewById(R.id.metFoodIdBanner);
        metNameFood = add_banner.findViewById(R.id.metFoodNameBanner);

        // Set value default for banner
        metFoodId.setText(item.getId());
        metNameFood.setText(item.getName());

        // Setup alertDialog
        alertDialog.setView(add_banner);
        alertDialog.setIcon(R.drawable.ic_banner_black_24dp);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        btnUpLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                item.setId(metFoodId.getText().toString());
                item.setName(metNameFood.getText().toString());

                // Make update
                Map<String, Object> updateBanner = new HashMap<>();
                updateBanner.put(getString(R.string.Firebase_Banner_id), item.getId());
                updateBanner.put(getString(R.string.Firebase_Banner_name), item.getName());
                updateBanner.put(getString(R.string.Firebase_Banner_image), item.getImage());

                referenceBanners.child(key)
                        .updateChildren(updateBanner)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                Snackbar.make(relativeLayout, R.string.Updated, Snackbar.LENGTH_SHORT).show();
                                loadListBanner();
                            }
                        });
            }
        });

        alertDialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                loadListBanner();
            }
        });
        alertDialog.show();    }

    private void changeImage(final Banner item) {

        if (filePath != null){

            final ProgressDialog pd = new ProgressDialog(this);
            pd.setMessage("Upload...");
            pd.show();

            String imageName = UUID.randomUUID().toString();
            final StorageReference imageFolder = storageReference.child("image/" + imageName);
            imageFolder.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();
                            Toast.makeText(ShowBanner.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            item.setImage(uri.toString());
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(ShowBanner.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            pd.setMessage("Uploaded " + progress + "%");
                        }
                    });
        }
    }

    private void deleteBanner(String key) {
        referenceBanners.child(key).removeValue();
        Snackbar.make(relativeLayout, newBanner.getName() + R.string.was_deleted, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        super.onStop();

        adapterBanner.stopListening();
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
