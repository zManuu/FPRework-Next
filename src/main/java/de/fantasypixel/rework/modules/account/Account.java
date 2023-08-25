package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.utils.database.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@Entity(tableName = "accounts")
public class Account {

    private int id;
    private String name;
    private String password;

}
