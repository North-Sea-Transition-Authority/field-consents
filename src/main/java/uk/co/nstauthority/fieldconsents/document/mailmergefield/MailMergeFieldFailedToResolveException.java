package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceDto;

public class MailMergeFieldFailedToResolveException extends IllegalStateException {

  public static MailMergeFieldFailedToResolveException mnemonicDoesNotExistOnDocumentInstance(
      String mnemonic,
      DocumentInstanceDto documentInstanceDto
  ) {
    return new MailMergeFieldFailedToResolveException(
        "%s does not exist on DocumentInstance [%s]".formatted(mnemonic, documentInstanceDto.id())
    );
  }

  public MailMergeFieldFailedToResolveException(String s) {
    super(s);
  }

  public MailMergeFieldFailedToResolveException(String s, Throwable cause) {
    super(s, cause);
  }
}
