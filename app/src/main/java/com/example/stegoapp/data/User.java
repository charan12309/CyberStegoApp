package com.example.stegoapp.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    public String username;
    /** PBKDF2 hash of the account password, never the password itself. */
    public String passwordHash;

    public User(@NonNull String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}
