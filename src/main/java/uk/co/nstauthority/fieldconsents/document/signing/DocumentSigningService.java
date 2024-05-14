package uk.co.nstauthority.fieldconsents.document.signing;

import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import uk.co.fivium.ftss.client.FtssClient;
import uk.co.fivium.ftss.client.FtssSignerProperties;
import uk.co.fivium.ftss.client.FtssVisualSignatureProperties;


@Service
public class DocumentSigningService {

  private final FtssClient ftssClient;
  private final FtssSignerProperties signerProperties;
  private final FtssVisualSignatureProperties visualSignatureProperties;

  DocumentSigningService(FtssClient ftssClient) {
    this.ftssClient = ftssClient;

    // TODO FTSS-89 organisation props can be updated once OGA identity is created with GlobalSign
    signerProperties = new FtssSignerProperties(
      "Oil and Gas Authority",
      "Fivium Ltd",
      "tech@fivium.co.uk",
      "London",
      "GB",
      "London",
      "On behalf of the Secretary of State",
      "North Sea Transition Authority");

    // TODO FCS-773 cover page design
    visualSignatureProperties = new FtssVisualSignatureProperties(
        new ClassPathResource("document-assets/nsta-logo-landscape-black.png"),
        new FtssVisualSignatureProperties.SignatureCoordinates(
            new FtssVisualSignatureProperties.Coordinate(0, 70, 211),
            new FtssVisualSignatureProperties.Coordinate(0, 221, 211),
            new FtssVisualSignatureProperties.Coordinate(0, 70, 147),
            new FtssVisualSignatureProperties.Coordinate(0, 221, 147)
        ),
      "Digitally signed by Oil and Gas Authority",
      null,
      null
    );
  }

  public InputStream applyDigitalSignature(InputStream document) {
    try {
      return ftssClient.signPdf(document, signerProperties, visualSignatureProperties);
    } catch (Exception e) {
      throw new RuntimeException("Failed to sign document", e);
    }
  }

}
