package uk.co.nstauthority.fieldconsents.assets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;

@Service
public class ApplicationAssetService {

  private final FieldService fieldService;

  private final TerminalService terminalService;

  private final ApplicationAssetRepository applicationAssetRepository;

  @Autowired
  public ApplicationAssetService(FieldService fieldService,
                                 TerminalService terminalService,
                                 ApplicationAssetRepository applicationAssetRepository) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.applicationAssetRepository = applicationAssetRepository;
  }

  public void createAssetRecordForField(ApplicationVersion applicationVersion, Integer fieldId) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setFieldId(fieldId);
    applicationAsset.setCachedFieldName(fieldService.getFieldOrError(fieldId, null).fieldName());
    applicationAsset.setAssetRole(AssetRole.PRIMARY);
    applicationAssetRepository.save(applicationAsset);
  }

  public void createAssetRecordForTerminal(ApplicationVersion applicationVersion, Integer terminalId) {
    ApplicationAsset applicationAsset = new ApplicationAsset();
    applicationAsset.setApplicationVersion(applicationVersion);
    applicationAsset.setTerminalId(terminalId);
    applicationAsset.setCachedTerminalName(terminalService.getTerminalOrError(terminalId, null).terminalName());
    applicationAsset.setAssetRole(AssetRole.PRIMARY);
    applicationAssetRepository.save(applicationAsset);
  }
}
