package net.bitbylogic.preferences.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.bitbylogic.orm.annotation.Column;
import net.bitbylogic.orm.data.BormObject;

import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Preference extends BormObject {

    @Column(primaryKey = true)
    private UUID id;

    @Column
    private UUID userId;

    @Column
    private String typeId;

    @Setter
    @Column(dataType = "MEDIUMTEXT")
    private Object value;

}
