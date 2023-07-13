package uk.co.nstauthority.fieldconsents.application.duplication;

import jakarta.persistence.EntityManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceRepository;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetRepository;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationDocumentService;
import uk.co.nstauthority.fieldconsents.util.ReflectionUtil;

@Service
public class ApplicationDuplicationService {

  static final String ID_FIELD_NAME = "id";

  static final String APPLICATION_VERSION_FIELD_NAME = "applicationVersion";

  static final String SET_APPLICATION_VERSION_METHOD_NAME = "setApplicationVersion";

  static final String FIND_REPOSITORY_METHOD_ERROR_MESSAGE =
      "Cannot find repository method with annotation %s in %s for duplication processing";

  static final String UNEXPECTED_REPOSITORY_METHOD_RETURN_TYPE_ERROR_MESSAGE =
      "Cannot invoke repository method %s as the return type is unexpected %s";

  static final String COPY_SOURCE_CLASS_ERROR_MESSAGE = "Failed to copy source class %s and set the application version id %s";

  static final String GET_APPLICATION_VERSION_METHOD_SETTER_ERROR_MESSAGE = "Cannot find appropriate %s method in class %s";

  private final EntityManager entityManager;

  private final List<DuplicationSource> duplicationSources;

  private final ApplicationAssetRepository applicationAssetRepository;

  private final ApplicationAssetLicenceRepository applicationAssetLicenceRepository;

  private final SupportingInformationDocumentService supportingInformationDocumentService;

  @Autowired
  public ApplicationDuplicationService(EntityManager entityManager,
                                       List<DuplicationSource> duplicationSources,
                                       ApplicationAssetRepository applicationAssetRepository,
                                       ApplicationAssetLicenceRepository applicationAssetLicenceRepository,
                                       SupportingInformationDocumentService supportingInformationDocumentService) {
    this.entityManager = entityManager;
    this.duplicationSources = duplicationSources;
    this.applicationAssetRepository = applicationAssetRepository;
    this.applicationAssetLicenceRepository = applicationAssetLicenceRepository;
    this.supportingInformationDocumentService = supportingInformationDocumentService;
  }

  @Transactional
  public void duplicateApplicationSections(ApplicationVersion sourceApplicationVersion,
                                           ApplicationVersion targetApplicationVersion) {

    // duplicate all application form data
    for (var applicationDuplicationSource : duplicationSources) {
      var repoMethod = ReflectionUtil.getAllMethods(applicationDuplicationSource.getClass())
          .stream()
          .filter(method -> Objects.nonNull(AnnotationUtils.findAnnotation(method, DuplicateThisOnUpdate.class)))
          .findFirst()
          .orElseThrow(() -> new RuntimeException(FIND_REPOSITORY_METHOD_ERROR_MESSAGE
              .formatted(DuplicateThisOnUpdate.class.getName(),
                  applicationDuplicationSource.getClass().getName())));

      try {
        if (repoMethod.getReturnType().equals(Optional.class)) {
          var entityToDuplicateOptional = (Optional<?>) repoMethod.invoke(applicationDuplicationSource, sourceApplicationVersion);
          entityToDuplicateOptional
              .ifPresent(entityToDuplicate ->
                  duplicateAndSetApplicationVersion(targetApplicationVersion, entityToDuplicate));
        } else if (repoMethod.getReturnType().equals(List.class)) {
          var entitiesToDuplicate = (List<?>) repoMethod.invoke(applicationDuplicationSource, sourceApplicationVersion);
          entitiesToDuplicate
              .forEach(entityToDuplicate ->
                  duplicateAndSetApplicationVersion(targetApplicationVersion, entityToDuplicate));
        } else {
          throw new RuntimeException(UNEXPECTED_REPOSITORY_METHOD_RETURN_TYPE_ERROR_MESSAGE
              .formatted(repoMethod.getName(), repoMethod.getReturnType().getName()));
        }
      } catch (IllegalAccessException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    }

    // duplicate the assets data, this is done more manually as we need to set the new application asset foreign keys too
    duplicateApplicationAssetsData(sourceApplicationVersion, targetApplicationVersion);

    // duplicate the supporting information file uploads
    supportingInformationDocumentService.copyUploadedFiles(sourceApplicationVersion, targetApplicationVersion);
  }

  @SuppressWarnings("unchecked")
  private <T> T duplicateAndSetApplicationVersion(ApplicationVersion applicationVersion, T source) {
    try {
      T target = DuplicationUtil.instantiateBlankInstance((Class<T>) source.getClass());
      DuplicationUtil.copyProperties(source, target, ID_FIELD_NAME, APPLICATION_VERSION_FIELD_NAME);
      getApplicationVersionSetterMethod(source).invoke(target, applicationVersion);
      entityManager.persist(target);
      return target;
    } catch (InvocationTargetException | IllegalAccessException exception) {
      throw new RuntimeException(
          COPY_SOURCE_CLASS_ERROR_MESSAGE.formatted(source.getClass().getName(), applicationVersion.getId()));
    }
  }

  private <T> Method getApplicationVersionSetterMethod(T source) {
    return ReflectionUtil.getAllMethods(source.getClass())
        .stream()
        .filter(method -> method.getName().equals(SET_APPLICATION_VERSION_METHOD_NAME))
        .filter(method -> method.getParameterTypes().length == 1)
        .filter(method -> method.getParameterTypes()[0].equals(ApplicationVersion.class))
        .filter(method -> method.getReturnType().equals(Void.TYPE))
        .findFirst()
        .orElseThrow(() -> new RuntimeException(GET_APPLICATION_VERSION_METHOD_SETTER_ERROR_MESSAGE
            .formatted(SET_APPLICATION_VERSION_METHOD_NAME, source.getClass().getName())));
  }

  // This specific copying is here rather than a service ApplicationAssetDuplicationService as we didn't
  // want to split out the methods above (duplicateAndSetApplicationVersion, getApplicationVersionSetterMethod).
  // If we need to add more specific copying here then we should consider splitting this all out.
  private void duplicateApplicationAssetsData(ApplicationVersion sourceApplicationVersion,
                                              ApplicationVersion targetApplicationVersion) {
    var applicationAssets = applicationAssetRepository.findAllByApplicationVersion(sourceApplicationVersion);
    for (var applicationAsset : applicationAssets) {
      var newApplicationAsset = duplicateAndSetApplicationVersion(targetApplicationVersion, applicationAsset);
      var applicationAssetLicences = applicationAssetLicenceRepository.findAllByApplicationAsset(applicationAsset);
      for (var applicationAssetLicence : applicationAssetLicences) {
        var newApplicationAssetLicence = duplicateAndSetApplicationVersion(targetApplicationVersion, applicationAssetLicence);
        newApplicationAssetLicence.setApplicationAsset(newApplicationAsset);
        applicationAssetLicenceRepository.save(newApplicationAssetLicence);
      }
    }
  }
}
