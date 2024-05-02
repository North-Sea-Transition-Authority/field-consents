package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;

@Entity
@Table(name = "application_consent_document_generation_data")
public class ConsentDocumentGenerationData {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @ManyToOne
  @JoinColumn(name = "consent_id")
  private Consent consent;

  private String documentTemplateMnemonic;

  private String documentTitle;

  private String pdfHtmlContent;

  @Column(name = "pdf_mail_merge_data")
  @JdbcTypeCode(SqlTypes.JSON)
  private Map<String, String> mailMergeResolvedValuesByMnemonic;

  public ConsentDocumentGenerationData() {
  }

  public ConsentDocumentGenerationData(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public Consent getConsent() {
    return consent;
  }

  public void setConsent(Consent consent) {
    this.consent = consent;
  }

  public String getDocumentTemplateMnemonic() {
    return documentTemplateMnemonic;
  }

  public void setDocumentTemplateMnemonic(String documentTemplateMnemonic) {
    this.documentTemplateMnemonic = documentTemplateMnemonic;
  }

  public String getDocumentTitle() {
    return documentTitle;
  }

  public void setDocumentTitle(String documentTitle) {
    this.documentTitle = documentTitle;
  }

  public String getPdfHtmlContent() {
    return pdfHtmlContent;
  }

  public void setPdfHtmlContent(String materialisedPdfHtmlContent) {
    this.pdfHtmlContent = materialisedPdfHtmlContent;
  }

  public Map<String, String> getMailMergeResolvedValuesByMnemonic() {
    return mailMergeResolvedValuesByMnemonic;
  }

  public void setMailMergeResolvedValuesByMnemonic(Map<String, String> mailMergeResolvedValuesByMnemonic) {
    this.mailMergeResolvedValuesByMnemonic = mailMergeResolvedValuesByMnemonic;
  }
}
