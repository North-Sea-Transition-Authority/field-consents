package uk.co.nstauthority.fieldconsents.audit;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;

@Entity
@Table(name = "audit_revisions")
@RevisionEntity(AuditRevisionListener.class)
public class AuditRevision {

  @Id
  @RevisionNumber
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "rev")
  private long id;

  @RevisionTimestamp
  private Date createdDateTime;

  private long userWuaId;

  public long getUserWuaId() {
    return userWuaId;
  }

  public void setUserWuaId(long webUserAccountId) {
    this.userWuaId = webUserAccountId;
  }
}
