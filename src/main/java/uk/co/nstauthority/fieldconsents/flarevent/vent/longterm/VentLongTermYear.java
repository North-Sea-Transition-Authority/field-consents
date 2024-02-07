package uk.co.nstauthority.fieldconsents.flarevent.vent.longterm;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;

@Entity
@Table(name = "vent_long_term_years")
public class VentLongTermYear extends EmissionLongTermYear {
}
