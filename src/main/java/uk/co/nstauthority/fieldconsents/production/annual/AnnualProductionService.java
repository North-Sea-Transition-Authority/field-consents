package uk.co.nstauthority.fieldconsents.production.annual;

import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.consentlength.AnnualUtil;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.unit.ApplicationUnitService;
import uk.co.nstauthority.fieldconsents.production.ProductionRowService;
import uk.co.nstauthority.fieldconsents.production.ProductionView;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroup;
import uk.co.nstauthority.fieldconsents.summary.SummaryGroupType;

@Service
public class AnnualProductionService {

  private final ProductionRowService productionRowService;

  private final ConsentLengthService consentLengthService;

  private final AnnualProductionMonthRepository annualProductionMonthRepository;

  private final ApplicationUnitService applicationUnitService;

  @Autowired
  public AnnualProductionService(ProductionRowService productionRowService,
                                 ConsentLengthService consentLengthService,
                                 AnnualProductionMonthRepository annualProductionMonthRepository,
                                 ApplicationUnitService applicationUnitService) {
    this.productionRowService = productionRowService;
    this.consentLengthService = consentLengthService;
    this.annualProductionMonthRepository = annualProductionMonthRepository;
    this.applicationUnitService = applicationUnitService;
  }

  private List<AnnualProductionMonth> getAnnualProductionMonths(ApplicationVersion applicationVersion) {
    return annualProductionMonthRepository.findAllByApplicationVersion(applicationVersion);
  }

  public boolean annualProductionMonthsExist(ApplicationVersion applicationVersion) {
    return annualProductionMonthRepository.existsByApplicationVersion(applicationVersion);
  }

  public boolean annualProductionMonthsComplete(ApplicationVersion applicationVersion) {
    Integer annualConsentYear = consentLengthService.getConsentLengthDetails(applicationVersion).getAnnualConsentYear();

    // if there is no difference between the expected year months and the existing year months of production data then
    // the production data is complete
    return CollectionUtils.disjunction(getExistingProductionYearMonths(applicationVersion),
        AnnualUtil.getExpectedYearMonths(annualConsentYear)).isEmpty();
  }

  private List<YearMonth> getExistingProductionYearMonths(ApplicationVersion applicationVersion) {
    // return a list of years and months for any production data we have
    return getAnnualProductionMonths(applicationVersion).stream().map(productionMonth ->
        YearMonth.of(productionMonth.getYear(), productionMonth.getMonth())).toList();
  }

  public AnnualProductionForm getAnnualProductionForm(ApplicationVersion applicationVersion) {
    ConsentLengthDetails consentLengthDetails = consentLengthService.getConsentLengthDetails(applicationVersion);

    String year = String.valueOf(consentLengthDetails.getAnnualConsentYear());
    var previousProductionRows = getAnnualProductionMonths(applicationVersion);
    var monthForms = initializeAnnualProductionMonthForms();
    var mergedForms = mergeExistingMonthDetailsWithForms(monthForms, previousProductionRows);

    return new AnnualProductionForm(mergedForms, year);
  }

  private List<AnnualProductionMonthForm> mergeExistingMonthDetailsWithForms(List<AnnualProductionMonthForm> monthForms,
                                                                             List<AnnualProductionMonth> previousProductionRows) {

    var mapOfPreviousProductionRows = previousProductionRows.stream().collect(
        Collectors.toMap(AnnualProductionMonth::getMonth, Function.identity()));
    List<AnnualProductionMonthForm> mergedList = new ArrayList<>();
    for (var annualProductionMonthForm : monthForms) {
      var previousProductionRow = mapOfPreviousProductionRows.get(
          Month.valueOf(annualProductionMonthForm.getMonth().toUpperCase())
      );
      AnnualProductionMonthForm mergedAnnualProductionMonthForm;
      if (previousProductionRow != null) {
        mergedAnnualProductionMonthForm = new AnnualProductionMonthForm();
        mergedAnnualProductionMonthForm.setMonth(previousProductionRow.getMonth());
        productionRowService.populateFormWithPreviousProductionRow(previousProductionRow, mergedAnnualProductionMonthForm);
      } else {
        mergedAnnualProductionMonthForm = annualProductionMonthForm;
      }
      mergedList.add(mergedAnnualProductionMonthForm);
    }
    return mergedList;
  }

  /** Initializes a list of AnnualProductionMonthForm with months from January to December. */
  private List<AnnualProductionMonthForm> initializeAnnualProductionMonthForms() {
    List<AnnualProductionMonthForm> annualProductionMonthForms = new LinkedList<>();
    for (Month month : Month.values()) {
      AnnualProductionMonthForm productionMonthForm = new AnnualProductionMonthForm();
      productionMonthForm.setMonth(month);
      annualProductionMonthForms.add(productionMonthForm);
    }
    return annualProductionMonthForms;
  }

  @Transactional
  public void saveAnnualProductionDetails(ApplicationVersion applicationVersion, AnnualProductionForm form) {
    List<AnnualProductionMonthForm> annualProductionMonthForms = form.getAnnualProductionMonthForms();
    annualProductionMonthRepository.deleteAllByApplicationVersion(applicationVersion);

    annualProductionMonthForms.forEach(monthProductionForm -> saveAnnualProductionMonthDetails(
        applicationVersion,
        monthProductionForm,
        form.getYear()
    ));
  }

  public void saveAnnualProductionMonthDetails(ApplicationVersion applicationVersion,
                                               AnnualProductionMonthForm productionMonthForm,
                                               String year) {

    AnnualProductionMonth annualProductionMonth = new AnnualProductionMonth();

    annualProductionMonth.setYear(Integer.parseInt(year));
    annualProductionMonth.setMonth(Month.valueOf(productionMonthForm.getMonth().toUpperCase()));
    annualProductionMonth.setApplicationVersion(applicationVersion);
    try {
      productionRowService.updateProductionRowFromForm(productionMonthForm, annualProductionMonth);
    } catch (NoSuchElementException e) {
      throw new RuntimeException(e);
    }
    annualProductionMonthRepository.save(annualProductionMonth);
  }

  public SummaryGroup<ProductionView> getProductionAnnualSummaryGroup(ApplicationVersion applicationVersion) {
    var annualProductionMonths = getAnnualProductionMonths(applicationVersion);

    if (annualProductionMonths.isEmpty()) {
      return null;
    }

    var oilUnit = applicationUnitService.getProductionOilUnit(applicationVersion);
    var gasUnit = applicationUnitService.getProductionGasUnit(applicationVersion);
    var averageUnit = applicationUnitService.getProductionAverageUnit(applicationVersion);
    return new SummaryGroup<>(
        null,
        SummaryGroupType.PRODUCTION_ANNUAL,
        ProductionView.class,
        ProductionView.fromAnnual(annualProductionMonths, oilUnit, gasUnit, averageUnit)
    );
  }
}
