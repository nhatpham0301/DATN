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
import com.example.serverside.Interface.ItemClickListener;
import com.example.serverside.Model.Category;
import com.example.serverside.Model.Foods;
import com.example.serverside.R;
import com.example.serverside.ViewHolder.FoodVIewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.UUID;

public class FoodList extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    DatabaseReference reference;
    StorageReference storageReference;
    FirebaseRecyclerAdapter<Foods, FoodVIewHolder> adapter;
    RelativeLayout foodLayout;

    String CategoryId = "";
    String CategoryName = "Category Name";
    FloatingActionButton fab;
    Foods newFood;

    Uri saveUri;

    Button btnSelect, btnUpload;
    MaterialEditText editTextName, editTextDescription, editTextPrice, editTextDiscount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        addControl();
        setupActionBar();
        addEvent();
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle(CategoryName);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void addEvent() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
    }

    private void showDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Thêm món mới");
        alertDialog.setMessage("Vui lòng điền đầy đủ thông tin !");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_food = inflater.inflate(R.layout.row_add_new_food, null);

        btnSelect = add_food.findViewById(R.id.btnSelectFood);
        btnUpload = add_food.findViewById(R.id.btnUploadFood);
        editTextName = add_food.findViewById(R.id.editTextNameFood);
        editTextDescription = add_food.findViewById(R.id.editTextDescriptionFood);
        editTextPrice = add_food.findViewById(R.id.editTextPriceFood);
        editTextDiscount = add_food.findViewById(R.id.editTextDiscountFood);

        alertDialog.setView(add_food);
        alertDialog.setIcon(R.drawable.ic_cart);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setPositiveButton("Thêm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (newFood != null) {
//                    String idFood = String.valueOf(System.currentTimeMillis());
//                    newFood.setId(idFood);
//                    reference.child(idFood).setValue(newFood);
                    reference.push().setValue(newFood);
                    Snackbar.make(foodLayout, "New food" + newFood.getName() + "was added",
                            Snackbar.LENGTH_SHORT).show();

                }
            }
        });

        alertDialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void uploadImage() {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Upload...");
        pd.show();

        String imageName = UUID.randomUUID().toString();
        final StorageReference imageFolder = storageReference.child("image/" + imageName);
        if(saveUri != null) {
            imageFolder.putFile(saveUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            pd.dismiss();
                            Toast.makeText(FoodList.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
                            imageFolder.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            newFood = new Foods(
                                                    editTextDescription.getText().toString(),
                                                    editTextDiscount.getText().toString(),
                                                    uri.toString(),
                                                    CategoryId,
                                                    editTextName.getText().toString(),
                                                    editTextPrice.getText().toString()
                                            );
                                        }
                                    });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pd.dismiss();
                            Toast.makeText(FoodList.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            pd.setMessage("Uploaded " + progress + "%");
                        }
                    });
        } else {
            pd.dismiss();
            Toast.makeText(this, "Please select image", Toast.LENGTH_SHORT).show();
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            saveUri = data.getData();
            btnSelect.setText("Image Select!!!");
        }
    }

    private void addControl() {
        foodLayout = findViewById(R.id.foodLayout);
        recyclerView = findViewById(R.id.recycler_food);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        reference = FirebaseDatabase.getInstance().getReference("Foods");
        fab = findViewById(R.id.fabFoodList);
        storageReference = FirebaseStorage.getInstance().getReference();

        if (getIntent() != null) {
            CategoryId = getIntent().getStringExtra("CategoryId");
            CategoryName = getIntent().getStringExtra("CategoryName");
        }

        if (!CategoryId.isEmpty() && CategoryId != null) {
            loadFood(CategoryId);
        }
    }

    private void loadFood(String id) {
        FirebaseRecyclerOptions<Foods> options =
                new FirebaseRecyclerOptions.Builder<Foods>()
                        .setQuery(reference.orderByChild("menuId").equalTo(id), Foods.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Foods, FoodVIewHolder>(options) {
            @Override
            protected void onBindViewHolder(FoodVIewHolder foodVIewHolder, final int i, Foods foods) {
                foodVIewHolder.txtFood.setText(foods.getName());
                Glide.with(getApplicationContext())
                        .load(foods.getImage())
                        .placeholder(R.drawable.my_bg)
                        .into(foodVIewHolder.imageFood);
                foodVIewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void OnClick(View view, int position, boolean isLongClick) {

                    }
                });
            }

            @NonNull
            @Override
            public FoodVIewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_food_list, parent, false);
                return new FoodVIewHolder(view);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        if (item.getTitle().equals("Cập nhật")) {
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(), adapter.getItem(item.getOrder()));
        } else if (item.getTitle().equals("Xóa")) {
            deleteFood(adapter.getRef(item.getOrder()).getKey());
        } else if(item.getTitle().equals("Cập nhật giá")){
            Intent intent = new Intent(getApplicationContext(), UpdateManyPrices.class);
            intent.putExtra("CategoryId", CategoryId);
            startActivity(intent);
        }


        return super.onContextItemSelected(item);
    }

    private void deleteFood(String key) {
        reference.child(key).removeValue();
        Toast.makeText(this, "Delete!!!", Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(final String key, final Foods item) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Cập nhật món ăn");
        alertDialog.setMessage("Vui lòng điền đầy đủ thông tin !");

        final LayoutInflater inflater = this.getLayoutInflater();
        View add_food = inflater.inflate(R.layout.row_add_new_food, null);

        btnSelect = add_food.findViewById(R.id.btnSelectFood);
        btnUpload = add_food.findViewById(R.id.btnUploadFood);
        editTextName = add_food.findViewById(R.id.editTextNameFood);
        editTextDescription = add_food.findViewById(R.id.editTextDescriptionFood);
        editTextPrice = add_food.findViewById(R.id.editTextPriceFood);
        editTextDiscount = add_food.findViewById(R.id.editTextDiscountFood);

        editTextName.setText(item.getName());
        editTextDescription.setText(item.getDescription());
        editTextPrice.setText(item.getPrice());
        editTextDiscount.setText(item.getDiscount());

        alertDialog.setView(add_food);
        alertDialog.setIcon(R.drawable.ic_cart);

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });

        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String des = editTextDescription.getText().toString();
                String name = editTextName.getText().toString();
                String dis = editTextDiscount.getText().toString();
                String price = editTextPrice.getText().toString();
                if (des != null && name != null && dis != null && price != null) {
                    item.setDescription(des);
                    item.setName(name);
                    item.setDiscount(dis);
                    item.setPrice(price);
                    reference.child(key).setValue(item);

                    Snackbar.make(foodLayout, "Category" + item.getName() + "was edited",
                            Snackbar.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(FoodList.this, "Please enter information full", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void changeImage(final Foods item) {
        final ProgressDialog pd = new ProgressDialog(this);
        pd.setMessage("Upload...");
        pd.show();

        String imageName = UUID.randomUUID().toString();
        final StorageReference imageFolder = storageReference.child("image/" + imageName);
        imageFolder.putFile(saveUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Toast.makeText(FoodList.this, "Uploaded !!!", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(FoodList.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}
