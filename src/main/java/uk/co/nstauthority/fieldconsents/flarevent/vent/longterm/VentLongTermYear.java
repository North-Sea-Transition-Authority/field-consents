package uk.co.nstauthority.fieldconsents.flarevent.vent.longterm;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;

@Entity
@Audited
@Table(name = "vent_long_term_years")
public class VentLongTermYear extends EmissionLongTermYear {
}
