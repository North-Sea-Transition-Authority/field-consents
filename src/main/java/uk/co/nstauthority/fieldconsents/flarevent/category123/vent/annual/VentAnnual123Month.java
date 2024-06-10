package uk.co.nstauthority.fieldconsents.flarevent.category123.vent.annual;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.flarevent.category123.vent.Vent123Row;

@Entity
@Audited
@Table(name = "vent_annual_123_months")
public class VentAnnual123Month extends Vent123Row {
}
