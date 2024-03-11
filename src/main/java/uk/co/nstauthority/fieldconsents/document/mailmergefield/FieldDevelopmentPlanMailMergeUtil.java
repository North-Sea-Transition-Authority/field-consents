package uk.co.nstauthority.fieldconsents.document.mailmergefield;

import java.util.function.Function;
import java.util.function.Supplier;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldDevelopmentPlan;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

class FieldDevelopmentPlanMailMergeUtil {

  static String getDate(Field field) throws Exception {
    return extractingOrElseThrow(
        field,
        FieldDevelopmentPlan::getDate,
        date -> DateUtils.format(date, DateUtils.LONG_DATE),
        () -> new Exception("Field Development Plan for field %s does not have a date".formatted(field.getFieldName()))
    );
  }

  static String getTitle(Field field) throws Exception {
    return extractingOrElseThrow(
        field,
        FieldDevelopmentPlan::getTitle,
        Function.identity(),
        () -> new Exception("Field Development Plan for field %s does not have a title".formatted(field.getFieldName()))
    );
  }

  private static <T> String extractingOrElseThrow(
      Field field,
      Function<FieldDevelopmentPlan, T> extractingFunction,
      Function<T, String> mappingFunction,
      Supplier<Exception> exceptionSupplier
  ) throws Exception {
    var fieldDevelopmentPlan = field.getFieldDevelopmentPlan();
    if (fieldDevelopmentPlan == null) {
      throw new Exception("Field Development Plan does not exist for field %s".formatted(field.getFieldName()));
    }

    var extractedValue = extractingFunction.apply(fieldDevelopmentPlan);
    if (extractedValue == null) {
      throw exceptionSupplier.get();
    }

    return mappingFunction.apply(extractedValue);
  }

}
