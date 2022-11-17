package uk.co.nstauthority.fieldconsents.flarevent.flare.flarereport;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
class FlareReportPeriodControllerHelperService {

  Map<String, String> getReportEndYearsMap(ApplicationVersion applicationVersion) {
    Integer applicationCreatedYear = applicationVersion.getApplication().getCreatedLocalDate().getYear();
    var yearsMap = new LinkedHashMap<String, String>();
    yearsMap.put(String.valueOf(applicationCreatedYear - 1), String.valueOf(applicationCreatedYear - 1));
    yearsMap.put(String.valueOf(applicationCreatedYear), String.valueOf(applicationCreatedYear));
    return yearsMap;
  }

}
