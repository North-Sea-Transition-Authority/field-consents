package uk.co.nstauthority.fieldconsents.application.summary;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummarySection;
import uk.co.nstauthority.fieldconsents.summary.SummarySectionService;

@Service
public class ApplicationSummaryService {

  private final List<SummarySectionService<ApplicationVersion>> summarySectionServices;

  @Autowired
  ApplicationSummaryService(List<SummarySectionService<ApplicationVersion>> summarySectionServices) {
    this.summarySectionServices = summarySectionServices;
  }

  public List<SummarySection> getSummarySections(ApplicationVersion applicationVersion) {
    return summarySectionServices.stream()
        .map(summarySectionService -> summarySectionService.getSummarySection(applicationVersion))
        .flatMap(Optional::stream)
        .sorted(Comparator.comparing(SummarySection::displayOrder))
        .toList();
  }
}
