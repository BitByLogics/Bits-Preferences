package net.bitbylogic.preferences.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bitbylogic.apibylogic.database.hikari.annotation.HikariStatementData;
import net.bitbylogic.apibylogic.database.hikari.data.HikariObject;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Preference extends HikariObject {

    @HikariStatementData(dataType = "VARCHAR(36)", primaryKey = true)
    private UUID id;

    @HikariStatementData(dataType = "VARCHAR(36)", allowNull = false)
    private UUID userId;

    @HikariStatementData(dataType = "VARCHAR(150)", allowNull = false)
    private String typeId;

    @Setter
    @HikariStatementData(dataType = "MEDIUMTEXT")
    private Object value;

}
