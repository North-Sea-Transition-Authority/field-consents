package uk.co.nstauthority.fieldconsents.startapplication;

import java.util.LinkedHashSet;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Service
public class StartApplicationControllerHelperService {

  public static final String APPLICATION_TYPE_FLASH_ATTRIBUTE = "applicationType";

  public Map<String, String> getApplicationTypesMap(AssetType assetType) {
    LinkedHashSet<ApplicationType> appTypes = ApplicationType.getForAssetType(assetType);
    return appTypes.stream()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, ApplicationType::getDisplayName));
  }
}
