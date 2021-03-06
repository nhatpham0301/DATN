package com.example.orderfood.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.widget.Toast;

import com.example.orderfood.Model.Favorites;
import com.example.orderfood.Model.Order;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.List;

public class Database extends SQLiteAssetHelper {

    private static final String DB_NAME = "EatIt.db";
    private static final int DB_VER = 2;
    public Database(Context context) {
        super(context, DB_NAME, null, DB_VER);
    }
    // Cart

    public boolean checkFoodExists(String foodId, String userPhone){

        boolean flag = false;
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        String SQLQuery = String.format("SELECT * FROM OrderDetail WHERE UserPhone='%s' AND ProductId='%s';", userPhone, foodId);
        cursor = db.rawQuery(SQLQuery, null);

        if (cursor.getCount() > 0)
            flag = true;
        else flag = false;

        cursor.close();

        return flag;
    }

    public int getCountCart(String userPhone) {
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT COUNT(*) FROM OrderDetail WHERE UserPhone='%s';", userPhone);
        Cursor cursor =  db.rawQuery(query, null);
        if(cursor.moveToFirst()){
            do{
                count = cursor.getInt(0);
            }while (cursor.moveToNext());
        }
        return count;
    }

    public void updateCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity='%s' WHERE ProductId='%s' AND UserPhone='%s';",
                order.getQuantity(),
                order.getProductId(),
                order.getUserPhone());
        db.execSQL(query);
    }

    public void increaseCart(String userPhone, String foodId) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("UPDATE OrderDetail SET Quantity=Quantity + 1 WHERE ProductId='%s' AND UserPhone='%s';",
                foodId, userPhone);
        db.execSQL(query);
    }

    public List<Order> getCarts(String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"UserPhone","ProductId, ProductName, Quantity, Price, Discount, Image"};
        String sqlTable = "OrderDetail";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db,sqlSelect, "UserPhone=?", new String[]{userPhone}, null, null, null);

        final List<Order> result = new ArrayList<>();
        if(c.moveToFirst())
        {
            do {
                result.add(new Order(c.getString(c.getColumnIndex("UserPhone")),
                                     c.getString(c.getColumnIndex("ProductId")),
                                     c.getString(c.getColumnIndex("ProductName")),
                                     c.getString(c.getColumnIndex("Quantity")),
                                     c.getString(c.getColumnIndex("Price")),
                                     c.getString(c.getColumnIndex("Discount")),
                                     c.getString(c.getColumnIndex("Image"))
                                    ));
            } while (c.moveToNext());
        }
        return result;
    }

    public void addToCart(Order order) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT OR REPLACE INTO OrderDetail(UserPhone, ProductId, ProductName, Quantity, Price, Discount, Image) VALUES('%s', '%s','%s','%s','%s','%s', '%s');",
                order.getUserPhone(),
                order.getProductId(),
                order.getProductName(),
                order.getQuantity(),
                order.getPrice(),
                order.getDiscount(),
                order.getImage());
        db.execSQL(query);
    }

    public void cleanToCart(String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM OrderDetail WHERE UserPhone='%s';", userPhone);
        db.execSQL(query);
    }

    public void removeFromCart(String productId, String phone) {

        SQLiteDatabase db = getReadableDatabase();
        String SQLQuery = String.format("DELETE FROM OrderDetail WHERE UserPhone='%s' AND ProductId='%s';",
                phone, productId);
        db.execSQL(SQLQuery);
    }

    //Favorites
    public void addToFavorite(Favorites food){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("INSERT INTO Favorites(FoodId, FoodName, " +
                        "FoodImage, FoodPrice, FoodMenuId, FoodDiscount, " +
                        "FoodDescription, UserPhone)" +
                        " VALUES('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s');",
                food.getFoodId(), food.getFoodName(), food.getFoodImage(),
                food.getFoodPrice(), food.getFoodMenuId(), food.getFoodDiscount(),
                food.getFoodDescription(), food.getUserPhone());
        db.execSQL(query);
    }

    public void removeFromFavorites(String idFood, String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("DELETE FROM Favorites WHERE FoodId = '%s' AND UserPhone = '%s';", idFood, userPhone);
        db.execSQL(query);
    }

    public boolean isFavorite(String idFood, String userPhone){
        SQLiteDatabase db = getReadableDatabase();
        String query = String.format("SELECT * FROM Favorites WHERE FoodId = '%s' AND UserPhone = '%s';",idFood, userPhone);
        Cursor cursor =  db.rawQuery(query, null);
        if(cursor.getCount() <= 0){
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    public List<Favorites> getAllFavorites(String userPhone) {
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        String[] sqlSelect = {"UserPhone","FoodId", "FoodName", "FoodImage",
                "FoodPrice", "FoodMenuId", "FoodDiscount", "FoodDescription"};
        String sqlTable = "Favorites";

        qb.setTables(sqlTable);
        Cursor c = qb.query(db,sqlSelect, "UserPhone=?", new String[]{userPhone}, null, null, null);

        final List<Favorites> result = new ArrayList<>();
        if(c.moveToFirst())
        {
            do {
                result.add(new Favorites(
                        c.getString(c.getColumnIndex("UserPhone")),
                        c.getString(c.getColumnIndex("FoodId")),
                        c.getString(c.getColumnIndex("FoodName")),
                        c.getString(c.getColumnIndex("FoodPrice")),
                        c.getString(c.getColumnIndex("FoodMenuId")),
                        c.getString(c.getColumnIndex("FoodImage")),
                        c.getString(c.getColumnIndex("FoodDiscount")),
                        c.getString(c.getColumnIndex("FoodDescription"))
                ));
            } while (c.moveToNext());
        }
        return result;
    }
}
