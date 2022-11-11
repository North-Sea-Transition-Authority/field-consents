package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@Service
public class ConsentLengthControllerHelperService {

  private static final int START_YEAR_LIST_SIZE = 5;

  Map<String, String> getLongTermConsentYearsMap() {
    List<String> startYearList = new ArrayList<>();

    for (int index = 0; index < START_YEAR_LIST_SIZE; index++) {
      startYearList.add(String.valueOf(LocalDate.now().getYear() + index));
    }

    return startYearList
        .stream()
        .collect(StreamUtils.toLinkedHashMap(Function.identity(), Function.identity()));
  }

  public Map<String, String> getConsentTypesMap(Application application) {
    LinkedHashSet<ConsentLengthType> types = ConsentLengthType.getForApplicationType(application.getType());
    return types.stream()
        .collect(StreamUtils.toLinkedHashMap(Enum::name, ConsentLengthType::getDisplayName));
  }

  public Map<String, String> getAnnualConsentYearsMap() {
    return Stream.of(
        String.valueOf(LocalDate.now().getYear() + 1),
        String.valueOf(LocalDate.now().getYear() + 2)
    ).collect(StreamUtils.toLinkedHashMap(Function.identity(), Function.identity()));
  }
}
