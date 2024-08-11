package de.fantasypixel.rework.modules.friends;

import de.fantasypixel.rework.framework.database.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "friends")
public class Friend {

    @Nullable private Integer id;
    private int accountId1;
    private int accountId2;

}
