package uk.co.nstauthority.fieldconsents.flarevent.flare.longterm;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;

@Entity
@Table(name = "flare_long_term_years")
public class FlareLongTermYear extends EmissionLongTermYear {
}
