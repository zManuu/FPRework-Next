package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.framework.database.Entity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nullable;

@Setter
@Getter
@Builder
@Entity(tableName = "accounts")
public class Account {

    @Nullable
    private Integer id;
    private String name;
    private String password;

}
