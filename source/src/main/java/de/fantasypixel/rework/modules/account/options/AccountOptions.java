package de.fantasypixel.rework.modules.account.options;

import de.fantasypixel.rework.framework.database.Entity;
import lombok.*;

import javax.annotation.Nullable;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "account-options")
public class AccountOptions {

    @Nullable private Integer id;
    private int accountId;
    private String languageKey;

}
