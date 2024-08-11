package de.fantasypixel.rework.modules.friends;

import de.fantasypixel.rework.framework.database.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity(tableName = "friend_requests")
public class FriendRequest {

    @Nullable
    private Integer id;
    private int requestingAccountId;
    private int receivingAccountId;

}