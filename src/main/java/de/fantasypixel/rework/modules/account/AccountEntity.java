package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.modules.Entity;

public class AccountEntity extends Entity {

    private String name;
    private String password;
    private int playtimeMinutes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPlaytimeMinutes() {
        return playtimeMinutes;
    }

    public void setPlaytimeMinutes(int playtimeMinutes) {
        this.playtimeMinutes = playtimeMinutes;
    }
}
