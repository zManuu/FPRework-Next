package de.fantasypixel.rework.modules.account;

import de.fantasypixel.rework.framework.database.Entity;
import lombok.*;

import javax.annotation.Nullable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "accounts")
public class Account {

    @Nullable private Integer id;
    private String playerUuid;
    private String name;
    @Nullable private String password;
    private String lastLogin; // date-time

}
