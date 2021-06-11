package com.example.serverside.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.serverside.Common.Common;
import com.example.serverside.Model.Shipper;
import com.example.serverside.R;
import com.example.serverside.ViewHolder.ShipperViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;

public class ShipperManagement extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private DatabaseReference shippers;
    private FirebaseDatabase database;

    private FirebaseRecyclerAdapter<Shipper, ShipperViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipper_management);

        setupActionBar();
        initView();
        initFirebase();
        initRecyclerView();
        addShipper();
        LoadAllShipper();
    }

    private void setupActionBar() {

        getSupportActionBar().setTitle("Quản lí Shipper");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void LoadAllShipper() {

        FirebaseRecyclerOptions<Shipper> allShipper = new FirebaseRecyclerOptions.Builder<Shipper>()
                .setQuery(shippers, Shipper.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Shipper, ShipperViewHolder>(allShipper) {
            @Override
            protected void onBindViewHolder(@NonNull ShipperViewHolder shipperViewHolder, final int i, @NonNull final Shipper shipper) {

                shipperViewHolder.txtPhoneShipper.setText(shipper.getPhone());
                shipperViewHolder.txtNameShipper.setText(shipper.getName());

                shipperViewHolder.btnEditShipper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        showEditShipper(adapter.getRef(i).getKey(), shipper);
                    }
                });

                shipperViewHolder.btnRemoveShipper.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        removeShipper(adapter.getRef(i).getKey());
                    }
                });
            }

            @NonNull
            @Override
            public ShipperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(getApplicationContext())
                        .inflate(R.layout.row_shipper_management, parent, false);
                return new ShipperViewHolder(view);
            }
        };

        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void removeShipper(String key) {

        shippers.child(key)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(ShipperManagement.this, "remove success!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        adapter.notifyDataSetChanged();
    }

    private void showEditShipper(String key, Shipper shipper) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShipperManagement.this);
        alertDialog.setTitle("Cập nhật Shipper");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_shipper, null);

        final MaterialEditText metNameShipper = view.findViewById(R.id.metNameShipper);
        final MaterialEditText metPhoneShipper = view.findViewById(R.id.metPhoneShipper);
        final MaterialEditText metPasswordShipper = view.findViewById(R.id.metPasswordShipper);

        // Set data
        metNameShipper.setText(shipper.getName());
        metPhoneShipper.setText(shipper.getPhone());
        metPasswordShipper.setText(shipper.getPassword());

        alertDialog.setIcon(R.drawable.ic_baseline_local_shipping_24);
        alertDialog.setView(view);

        alertDialog.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                HashMap<String, Object> updateShipper = new HashMap<>();
                updateShipper.put("name", metNameShipper.getText().toString());
                updateShipper.put("phone", metPhoneShipper.getText().toString());
                updateShipper.put("password", metPasswordShipper.getText().toString());

                shippers.child(metPhoneShipper.getText().toString())
                        .updateChildren(updateShipper)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(ShipperManagement.this, "Shipper update!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
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

    private void initFirebase() {

        database = FirebaseDatabase.getInstance();
        shippers = database.getReference(Common.SHIPPER_TABLE);
    }

    private void initRecyclerView() {

        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    private void addShipper() {

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showAlertDialogAddShipper();
            }
        });
    }

    private void showAlertDialogAddShipper() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(ShipperManagement.this);
        alertDialog.setTitle("Create Shipper");

        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.add_shipper, null);

        final MaterialEditText metNameShipper = view.findViewById(R.id.metNameShipper);
        final MaterialEditText metPhoneShipper = view.findViewById(R.id.metPhoneShipper);
        final MaterialEditText metPasswordShipper = view.findViewById(R.id.metPasswordShipper);

        alertDialog.setIcon(R.drawable.ic_baseline_local_shipping_24);
        alertDialog.setView(view);

        alertDialog.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();

                Shipper shipper = new Shipper();
                shipper.setName(metNameShipper.getText().toString());
                shipper.setPhone(metPhoneShipper.getText().toString());
                shipper.setPassword(metPasswordShipper.getText().toString());

                shippers.child(metPhoneShipper.getText().toString())
                        .setValue(shipper)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {

                                Toast.makeText(ShipperManagement.this, "Shipper create!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(ShipperManagement.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void initView() {

        fab = findViewById(R.id.shipperManagementAct_fab);
        recyclerView = findViewById(R.id.shipperManagementAct_recyclerView);
    }

    @Override
    public boolean onSupportNavigateUp() {

        onBackPressed();
        return true;
    }
}