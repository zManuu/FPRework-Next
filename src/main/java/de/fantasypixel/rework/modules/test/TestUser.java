package de.fantasypixel.rework.modules.test;

import de.fantasypixel.rework.utils.database.Entity;

@Entity(tableName = "user")
public class TestUser {

    private int id;
    private String name;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
