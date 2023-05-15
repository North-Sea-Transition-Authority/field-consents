package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum ConsentLengthType implements Displayable {
  SHORT_TERM("Short term consent", 10, EnumSet.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT)),
  ANNUAL("Annual consent", 20, EnumSet.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT)),
  LONG_TERM("Long term consent", 30, EnumSet.of(ApplicationType.PRODUCTION));

  private final String displayName;

  private final int displayOrder;

  private final EnumSet<ApplicationType> applicationTypes;

  ConsentLengthType(String displayName, int displayOrder, EnumSet<ApplicationType> applicationTypes) {
    this.displayName = displayName;
    this.displayOrder = displayOrder;
    this.applicationTypes = applicationTypes;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public String getShortDisplayName() {
    return displayName.replace(" consent", "");
  }

  public Set<ApplicationType> getApplicationTypes() {
    return applicationTypes;
  }

  public static LinkedHashSet<ConsentLengthType> getForApplicationType(ApplicationType appType) {
    return Arrays.stream(ConsentLengthType.values())
        .filter(type -> type.getApplicationTypes().contains(appType))
        .sorted(Comparator.comparing(ConsentLengthType::getDisplayOrder))
        .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  public static Map<String, String> getWorkAreaOptions() {
    return Arrays.stream(ConsentLengthType.values())
        .sorted(Comparator.comparingInt(Displayable::getDisplayOrder))
        .collect(StreamUtils.toLinkedHashMap(Displayable::getEnumName, ConsentLengthType::getShortDisplayName));
  }
}
