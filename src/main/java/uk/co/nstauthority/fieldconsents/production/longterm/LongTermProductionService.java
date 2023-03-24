package uk.co.nstauthority.fieldconsents.production.longterm;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.production.ProductionRowService;

@Service
public class LongTermProductionService {

  private final ProductionRowService productionRowService;

  private final ConsentLengthService consentLengthService;

  private final LongTermProductionYearRepository longTermProductionYearRepository;

  @Autowired
  public LongTermProductionService(ProductionRowService productionRowService,
                                   ConsentLengthService consentLengthService,
                                   LongTermProductionYearRepository longTermProductionYearRepository) {
    this.productionRowService = productionRowService;
    this.consentLengthService = consentLengthService;
    this.longTermProductionYearRepository = longTermProductionYearRepository;
  }

  public List<LongTermProductionYear> getLongTermProductionYears(ApplicationVersion applicationVersion) {
    return longTermProductionYearRepository.findAllByApplicationVersionOrderByYearAsc(applicationVersion);
  }

  public boolean longTermProductionYearsExist(ApplicationVersion applicationVersion) {
    return longTermProductionYearRepository.existsByApplicationVersion(applicationVersion);
  }

  public boolean longTermProductionYearsComplete(ApplicationVersion applicationVersion) {
    // if there is no difference between the expected years and the existing years of production data then
    // the production data is complete
    return CollectionUtils.disjunction(getExistingProductionYears(applicationVersion),
        getExpectedProductionYears(applicationVersion)).isEmpty();
  }

  private List<Integer> getExistingProductionYears(ApplicationVersion applicationVersion) {
    // return a list of years for the long term production data we have
    return getLongTermProductionYears(applicationVersion).stream()
        .map(LongTermProductionYear::getYear).toList();
  }

  private List<Integer> getExpectedProductionYears(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);
    List<Integer> expectedProductionYears = new ArrayList<>();
    for (Integer year = consentLengthDetails.getLongTermStartYear();
         year <= consentLengthDetails.getLongTermEndYear();
         year++) {
      expectedProductionYears.add(year);
    }
    return expectedProductionYears;
  }

  LongTermProductionForm getLongTermProductionForm(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

    Integer startYear = consentLengthDetails.getLongTermStartYear();
    Integer endYear = consentLengthDetails.getLongTermEndYear();

    var previousProductionRows = getLongTermProductionYears(applicationVersion);

    // initialise stub long term production year forms between the start and end years
    var longTermProductionYearForms = initializeLongTermProductionYearForms(startYear, endYear);

    // merge any existing DB data into the year forms
    var mergedLongTermProductionYearForms =
        mergeExistingProductionYearDataIntoForms(longTermProductionYearForms, previousProductionRows);

    return new LongTermProductionForm(mergedLongTermProductionYearForms);
  }

  private List<LongTermProductionYearForm> initializeLongTermProductionYearForms(Integer startYear, Integer endYear) {

    List<LongTermProductionYearForm> longTermProductionYearForms = new ArrayList<>();
    for (Integer year = startYear; year <= endYear; year++) {
      LongTermProductionYearForm productionYearForm = new LongTermProductionYearForm();
      productionYearForm.setYear(year.toString());
      longTermProductionYearForms.add(productionYearForm);
    }

    return longTermProductionYearForms;
  }

  private List<LongTermProductionYearForm> mergeExistingProductionYearDataIntoForms(
      List<LongTermProductionYearForm> yearForms,
      List<LongTermProductionYear> previousProductionRows) {

    // if there is no existing production data then return the year forms untouched
    if (previousProductionRows.isEmpty()) {
      return yearForms;
    }

    Map<Integer, LongTermProductionYear> previousProductionRowsMap =
        previousProductionRows.stream().collect(
            Collectors.toMap(LongTermProductionYear::getYear, Function.identity())
        );

    // take the DB data for matched years and merge into the year forms
    List<LongTermProductionYearForm> mergedYearForms = new ArrayList<>();
    for (LongTermProductionYearForm productionYearForm : yearForms) {
      // get the DB data for the year in question
      var previousProductionRow = previousProductionRowsMap.get(Integer.valueOf(productionYearForm.getYear()));
      // if DB data exists for the current form year then merge into the form
      LongTermProductionYearForm mergedLongTermProductionYearForm;
      if (previousProductionRow != null) {
        mergedLongTermProductionYearForm = new LongTermProductionYearForm();
        mergedLongTermProductionYearForm.setYear(productionYearForm.getYear());
        // update merge DB data into new year form
        productionRowService.populateFormWithPreviousProductionRow(previousProductionRow, mergedLongTermProductionYearForm);
      } else {
        // copy forward the existing stub form
        mergedLongTermProductionYearForm = productionYearForm;
      }
      mergedYearForms.add(mergedLongTermProductionYearForm);
    }
    return mergedYearForms;
  }

  @Transactional
  public void saveLongTermProductionDetails(ApplicationVersion applicationVersion, LongTermProductionForm form) {
    List<LongTermProductionYearForm> longTermProductionYearForms = form.getLongTermProductionYearForms();

    // delete old DB data for the app version
    longTermProductionYearRepository.deleteAllByApplicationVersion(applicationVersion);

    // save new form data to the DB for each year
    longTermProductionYearForms.forEach(
        longTermProductionYearForm ->
            saveLongTermProductionYearDetails(applicationVersion, longTermProductionYearForm)
    );
  }

  void saveLongTermProductionYearDetails(ApplicationVersion applicationVersion,
                                         LongTermProductionYearForm longTermProductionYearForm) {
    LongTermProductionYear longTermProductionYear = new LongTermProductionYear();

    longTermProductionYear.setYear(Integer.parseInt(longTermProductionYearForm.getYear()));
    longTermProductionYear.setApplicationVersion(applicationVersion);

    try {
      productionRowService.updateProductionRowFromForm(longTermProductionYearForm, longTermProductionYear);
    } catch (NoSuchElementException e) {
      throw new RuntimeException(e);
    }
    longTermProductionYearRepository.save(longTermProductionYear);
  }
}
