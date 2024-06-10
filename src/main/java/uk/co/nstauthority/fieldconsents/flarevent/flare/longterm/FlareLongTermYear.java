package uk.co.nstauthority.fieldconsents.flarevent.flare.longterm;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.flarevent.EmissionLongTermYear;

@Entity
@Audited
@Table(name = "flare_long_term_years")
public class FlareLongTermYear extends EmissionLongTermYear {
}
