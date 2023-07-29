package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.database.Entity;

@Entity(tableName = "accounts")
public class Account {

    private int id;
    private String name;
    private String password;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
