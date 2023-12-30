package com.example.calculator.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
@Dao
public interface DrinkDAO {

    @Query("Select * From drink")
    List<Drink> getAll();

    @Insert
    void insertAll(Drink... drinks);

    @Delete
    void delete(Drink drink);


    @Query("DELETE FROM drink")
    public void deleteALL();

    @Update
    void update(Drink... drink);
}
