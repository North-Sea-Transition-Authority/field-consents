package uk.co.nstauthority.fieldconsents.flarevent.category123.flare.annual;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.flarevent.category123.flare.Flare123Row;

@Entity
@Audited
@Table(name = "flare_annual_123_months")
public class FlareAnnual123Month extends Flare123Row {
}
