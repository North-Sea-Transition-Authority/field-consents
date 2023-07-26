package uk.co.nstauthority.fieldconsents.application.duplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService.APPLICATION_VERSION_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService.FIND_REPOSITORY_METHOD_ERROR_MESSAGE;
import static uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService.GET_APPLICATION_VERSION_METHOD_SETTER_ERROR_MESSAGE;
import static uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService.ID_FIELD_NAME;
import static uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService.SET_APPLICATION_VERSION_METHOD_NAME;
import static uk.co.nstauthority.fieldconsents.application.duplication.ApplicationDuplicationService.UNEXPECTED_REPOSITORY_METHOD_RETURN_TYPE_ERROR_MESSAGE;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.repository.CrudRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileUsage;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceRepository;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetRepository;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;

@ExtendWith(MockitoExtension.class)
class ApplicationDuplicationServiceTest {

  private static TestBaseClass TEST_BASE_ENTITY_1;
  private static TestBaseClass TEST_BASE_ENTITY_2;
  private static TestBaseClass TEST_BASE_ENTITY_3;
  private static BadTestBaseClass1 BAD_TEST_BASE_ENTITY_1;
  private static BadTestBaseClass2 BAD_TEST_BASE_ENTITY_2;
  private static BadTestBaseClass3 BAD_TEST_BASE_ENTITY_3;
  private static BadTestBaseClass4 BAD_TEST_BASE_ENTITY_4;

  @Mock
  private static Test1DuplicationRepository testDuplicationSource1;

  @Mock
  private static Test2DuplicationRepository testDuplicationSource2;

  @Mock
  private static Test3DuplicationRepository testDuplicationSource3;

  @Mock
  private static Test4DuplicationRepository testDuplicationSource4;

  @Mock
  private static Test5DuplicationRepository testDuplicationSource5;

  @Mock
  private static Test6DuplicationRepository testDuplicationSource6;

  @Mock
  private static Test7DuplicationRepository testDuplicationSource7;

  @Mock
  private static Test8DuplicationRepository testDuplicationSource8;

  @Mock
  private static Test9DuplicationRepository testDuplicationSource9;

  @Mock
  private EntityManager entityManager;

  private final List<DuplicationSource> duplicationSources = new ArrayList<>();

  @Mock
  private ApplicationAssetRepository applicationAssetRepository;

  @Mock
  private ApplicationAssetLicenceRepository applicationAssetLicenceRepository;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  private ApplicationDuplicationService applicationDuplicationService;

  private ApplicationVersion applicationVersion;

  private ApplicationVersion targetApplicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    targetApplicationVersion =
        ApplicationTestUtil.getSubmittedApplicationVersionWithTypeIdAndVersionNumber(ApplicationType.FLARE, 2, 2);
    TEST_BASE_ENTITY_1 = new TestBaseClass(1, applicationVersion, "a1", "a2");
    TEST_BASE_ENTITY_2 = new TestBaseClass(2, applicationVersion, "b1", "b2");
    TEST_BASE_ENTITY_3 = new TestBaseClass(3, applicationVersion, "c1", "c2");
    BAD_TEST_BASE_ENTITY_1 = new BadTestBaseClass1(4, applicationVersion, "d1", "d2");
    BAD_TEST_BASE_ENTITY_2 = new BadTestBaseClass2(5, applicationVersion, "e1", "e2");
    BAD_TEST_BASE_ENTITY_3 = new BadTestBaseClass3(6, applicationVersion, "f1", "f2");
    BAD_TEST_BASE_ENTITY_4 = new BadTestBaseClass4(7, applicationVersion, "g1", "g2");
  }

  @Test
  void duplicateApplicationSections() {
    duplicationSources.add(testDuplicationSource1);
    duplicationSources.add(testDuplicationSource2);
    applicationDuplicationService = new ApplicationDuplicationService(
        entityManager, duplicationSources, applicationAssetRepository,
        applicationAssetLicenceRepository, fieldConsentsFileService);

    when(testDuplicationSource1.findAllByApplicationVersion(applicationVersion))
        .thenReturn(List.of(TEST_BASE_ENTITY_1, TEST_BASE_ENTITY_2));
    when(testDuplicationSource2.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(TEST_BASE_ENTITY_3));
    when(applicationAssetRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.secondaryAssets);
    when(applicationAssetLicenceRepository.findAllByApplicationAsset(ApplicationAssetTestUtil.fieldAsset2))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2Licences);
    when(applicationAssetLicenceRepository.findAllByApplicationAsset(ApplicationAssetTestUtil.fieldAsset3))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset3Licences);

    applicationDuplicationService.duplicateApplicationSections(applicationVersion, targetApplicationVersion);

    // check the TestBaseClass copies and the ApplicationVersions are updated
    ArgumentCaptor<TestBaseClass> testBaseClassArgumentCaptor = ArgumentCaptor.forClass(TestBaseClass.class);
    verify(entityManager, Mockito.times(3)).persist(testBaseClassArgumentCaptor.capture());
    var testBaseEntityCopies = testBaseClassArgumentCaptor.getAllValues();
    assertThat(testBaseEntityCopies)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields(ID_FIELD_NAME, APPLICATION_VERSION_FIELD_NAME)
        .containsOnly(TEST_BASE_ENTITY_1, TEST_BASE_ENTITY_2, TEST_BASE_ENTITY_3);
    assertThat(testBaseEntityCopies)
        .extracting(APPLICATION_VERSION_FIELD_NAME)
        .containsOnly(targetApplicationVersion, targetApplicationVersion, targetApplicationVersion);
    assertThat(testBaseEntityCopies)
        .extracting(ID_FIELD_NAME)
        .isNotIn(TEST_BASE_ENTITY_1.id, TEST_BASE_ENTITY_2.id, TEST_BASE_ENTITY_3.id);

    // check the ApplicationAsset copies and the ApplicationVersions are updated
    ArgumentCaptor<ApplicationAsset> applicationAssetArgumentCaptor = ArgumentCaptor.forClass(ApplicationAsset.class);
    verify(entityManager, Mockito.times(2)).persist(applicationAssetArgumentCaptor.capture());
    var applicationAssetCopies = applicationAssetArgumentCaptor.getAllValues();
    assertThat(applicationAssetCopies)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields(ID_FIELD_NAME, APPLICATION_VERSION_FIELD_NAME)
        .containsOnly(ApplicationAssetTestUtil.fieldAsset2, ApplicationAssetTestUtil.fieldAsset3);
    assertThat(applicationAssetCopies)
        .extracting(APPLICATION_VERSION_FIELD_NAME)
        .containsOnly(targetApplicationVersion, targetApplicationVersion);
    assertThat(applicationAssetCopies)
        .extracting(ID_FIELD_NAME)
        .isNotIn(ApplicationAssetTestUtil.fieldAsset2.getId(), ApplicationAssetTestUtil.fieldAsset3.getId());

    // check the ApplicationAssetLicence copies and the ApplicationVersions and ApplicationAssets are updated
    ArgumentCaptor<ApplicationAssetLicence> applicationAssetLicenceArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationAssetLicence.class);
    verify(entityManager, Mockito.times(4)).persist(applicationAssetLicenceArgumentCaptor.capture());
    var applicationAssetLicenceCopies = applicationAssetLicenceArgumentCaptor.getAllValues();
    var applicationAssetFieldName = "applicationAsset";
    assertThat(applicationAssetLicenceCopies)
        .usingRecursiveFieldByFieldElementComparatorIgnoringFields(ID_FIELD_NAME, APPLICATION_VERSION_FIELD_NAME, applicationAssetFieldName)
        .containsOnly(
            ApplicationAssetTestUtil.fieldAsset2Licence1,
            ApplicationAssetTestUtil.fieldAsset2Licence2,
            ApplicationAssetTestUtil.fieldAsset3Licence1,
            ApplicationAssetTestUtil.fieldAsset3Licence2
        );
    assertThat(applicationAssetLicenceCopies)
        .extracting(APPLICATION_VERSION_FIELD_NAME)
        .containsOnly(targetApplicationVersion, targetApplicationVersion, targetApplicationVersion, targetApplicationVersion);
    assertThat(applicationAssetLicenceCopies)
        .extracting(applicationAssetFieldName)
        .containsOnly(applicationAssetCopies.get(0),
            applicationAssetCopies.get(0),
            applicationAssetCopies.get(1),
            applicationAssetCopies.get(1));
    assertThat(applicationAssetLicenceCopies)
        .extracting(ID_FIELD_NAME)
        .isNotIn(
            ApplicationAssetTestUtil.fieldAsset2Licence1.getId(),
            ApplicationAssetTestUtil.fieldAsset2Licence2.getId(),
            ApplicationAssetTestUtil.fieldAsset3Licence1.getId(),
            ApplicationAssetTestUtil.fieldAsset3Licence2.getId()
        );

    // check file copy is called
    verify(fieldConsentsFileService, times(1))
        .copyUploadedFiles(
            ApplicationVersionFileUsage.supportingDocumentFrom(applicationVersion),
            ApplicationVersionFileUsage.supportingDocumentFrom(targetApplicationVersion)
        );
  }

  @ParameterizedTest
  @MethodSource("getBadDuplicationSources")
  void duplicateApplicationSections_whenRepoMethodNotFound_thenError(DuplicationSource badDuplicationSource) {
    duplicationSources.add(badDuplicationSource);
    applicationDuplicationService = new ApplicationDuplicationService(
        entityManager, duplicationSources, applicationAssetRepository,
        applicationAssetLicenceRepository, fieldConsentsFileService);

    assertThatThrownBy(() ->
        applicationDuplicationService
            .duplicateApplicationSections(applicationVersion, targetApplicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(FIND_REPOSITORY_METHOD_ERROR_MESSAGE
            .formatted(DuplicateThisOnUpdate.class.getName(),
                badDuplicationSource.getClass().getName()));
  }

  private static Stream<Arguments> getBadDuplicationSources() {
    return Stream.of(
        Arguments.of(testDuplicationSource3),
        Arguments.of(testDuplicationSource4)
    );
  }

  @Test
  void duplicateApplicationSections_whenRepoMethodWithUnhandledReturnType_thenError() throws NoSuchMethodException {
    duplicationSources.add(testDuplicationSource5);
    applicationDuplicationService = new ApplicationDuplicationService(
        entityManager, duplicationSources, applicationAssetRepository,
        applicationAssetLicenceRepository, fieldConsentsFileService);

    var repoMethod = testDuplicationSource5.getClass().getMethod("getFirstByApplicationVersion", ApplicationVersion.class);

    assertThatThrownBy(() ->
        applicationDuplicationService
            .duplicateApplicationSections(applicationVersion, targetApplicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(UNEXPECTED_REPOSITORY_METHOD_RETURN_TYPE_ERROR_MESSAGE
            .formatted(repoMethod.getName(), repoMethod.getReturnType().getName()));
  }

  @Test
  void duplicateApplicationSections_whenMissingSetMethod_thenError() {
    duplicationSources.add(testDuplicationSource6);
    applicationDuplicationService = new ApplicationDuplicationService(
        entityManager, duplicationSources, applicationAssetRepository,
        applicationAssetLicenceRepository, fieldConsentsFileService);

    when(testDuplicationSource6.findAllByApplicationVersion(applicationVersion))
        .thenReturn(List.of(BAD_TEST_BASE_ENTITY_1));

    assertThatThrownBy(() ->
        applicationDuplicationService
            .duplicateApplicationSections(applicationVersion, targetApplicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(GET_APPLICATION_VERSION_METHOD_SETTER_ERROR_MESSAGE
            .formatted(SET_APPLICATION_VERSION_METHOD_NAME, BAD_TEST_BASE_ENTITY_1.getClass().getName()));
  }

  @Test
  void duplicateApplicationSections_whenSetMethodWrongNumberOfArguments_thenError() {
    duplicationSources.add(testDuplicationSource7);
    applicationDuplicationService = new ApplicationDuplicationService(
        entityManager, duplicationSources, applicationAssetRepository,
        applicationAssetLicenceRepository, fieldConsentsFileService);

    when(testDuplicationSource7.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(BAD_TEST_BASE_ENTITY_2));

    assertThatThrownBy(() ->
        applicationDuplicationService
            .duplicateApplicationSections(applicationVersion, targetApplicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(GET_APPLICATION_VERSION_METHOD_SETTER_ERROR_MESSAGE
            .formatted(SET_APPLICATION_VERSION_METHOD_NAME, BAD_TEST_BASE_ENTITY_2.getClass().getName()));
  }

  @Test
  void duplicateApplicationSections_whenSetMethodWrongArgumentType_thenError() {
    duplicationSources.add(testDuplicationSource8);
    applicationDuplicationService = new ApplicationDuplicationService(
        entityManager, duplicationSources, applicationAssetRepository,
        applicationAssetLicenceRepository, fieldConsentsFileService);

    when(testDuplicationSource8.findAllByApplicationVersion(applicationVersion))
        .thenReturn(List.of(BAD_TEST_BASE_ENTITY_3));

    assertThatThrownBy(() ->
        applicationDuplicationService
            .duplicateApplicationSections(applicationVersion, targetApplicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(GET_APPLICATION_VERSION_METHOD_SETTER_ERROR_MESSAGE
            .formatted(SET_APPLICATION_VERSION_METHOD_NAME, BAD_TEST_BASE_ENTITY_3.getClass().getName()));
  }

  @Test
  void duplicateApplicationSections_whenSetMethodWrongReturnType_thenError() {
    duplicationSources.add(testDuplicationSource9);
    applicationDuplicationService = new ApplicationDuplicationService(
        entityManager, duplicationSources, applicationAssetRepository,
        applicationAssetLicenceRepository, fieldConsentsFileService);

    when(testDuplicationSource9.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(BAD_TEST_BASE_ENTITY_4));

    assertThatThrownBy(() ->
        applicationDuplicationService
            .duplicateApplicationSections(applicationVersion, targetApplicationVersion))
        .isInstanceOf(RuntimeException.class)
        .hasMessage(GET_APPLICATION_VERSION_METHOD_SETTER_ERROR_MESSAGE
            .formatted(SET_APPLICATION_VERSION_METHOD_NAME, BAD_TEST_BASE_ENTITY_4.getClass().getName()));
  }

  private interface Test1DuplicationRepository extends CrudRepository<TestBaseClass, Integer>, DuplicationSource {
    @DuplicateThisOnUpdate
    List<TestBaseClass> findAllByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private interface Test2DuplicationRepository extends CrudRepository<TestBaseClass, Integer>, DuplicationSource {
    @DuplicateThisOnUpdate
    Optional<TestBaseClass> findByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private interface Test3DuplicationRepository extends CrudRepository<TestBaseClass, Integer>, DuplicationSource {
    List<TestBaseClass> findAllByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private interface Test4DuplicationRepository extends CrudRepository<TestBaseClass, Integer>, DuplicationSource {
    Optional<TestBaseClass> findByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private interface Test5DuplicationRepository extends CrudRepository<TestBaseClass, Integer>, DuplicationSource {
    @DuplicateThisOnUpdate
    TestBaseClass getFirstByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private interface Test6DuplicationRepository extends CrudRepository<BadTestBaseClass1, Integer>, DuplicationSource {
    @DuplicateThisOnUpdate
    List<BadTestBaseClass1> findAllByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private interface Test7DuplicationRepository extends CrudRepository<BadTestBaseClass2, Integer>, DuplicationSource {
    @DuplicateThisOnUpdate
    Optional<BadTestBaseClass2> findByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private interface Test8DuplicationRepository extends CrudRepository<BadTestBaseClass3, Integer>, DuplicationSource {
    @DuplicateThisOnUpdate
    List<BadTestBaseClass3> findAllByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private interface Test9DuplicationRepository extends CrudRepository<BadTestBaseClass4, Integer>, DuplicationSource {
    @DuplicateThisOnUpdate
    Optional<BadTestBaseClass4> findByApplicationVersion(ApplicationVersion applicationVersion);
  }

  private static class TestBaseClass {
    Integer id;
    ApplicationVersion applicationVersion;
    String field1;
    String field2;

    public TestBaseClass() {
    }

    public TestBaseClass(Integer id, ApplicationVersion applicationVersion, String field1, String field2) {
      this.id = id;
      this.applicationVersion = applicationVersion;
      this.field1 = field1;
      this.field2 = field2;
    }

    public ApplicationVersion getApplicationVersion() {
      return applicationVersion;
    }

    public void setApplicationVersion(ApplicationVersion applicationVersion) {
      this.applicationVersion = applicationVersion;
    }
  }

  private static class BadTestBaseClass1 {
    Integer id;
    ApplicationVersion applicationVersion;
    String field1;
    String field2;

    public BadTestBaseClass1() {
    }

    public BadTestBaseClass1(Integer id, ApplicationVersion applicationVersion, String field1, String field2) {
      this.id = id;
      this.applicationVersion = applicationVersion;
      this.field1 = field1;
      this.field2 = field2;
    }
  }

  private static class BadTestBaseClass2 {
    Integer id;
    ApplicationVersion applicationVersion;
    String field1;
    String field2;

    public BadTestBaseClass2() {
    }

    public BadTestBaseClass2(Integer id, ApplicationVersion applicationVersion, String field1, String field2) {
      this.id = id;
      this.applicationVersion = applicationVersion;
      this.field1 = field1;
      this.field2 = field2;
    }

    public void setApplicationVersion(ApplicationVersion applicationVersion, Integer id) {
      this.applicationVersion = applicationVersion;
    }
  }

  private static class BadTestBaseClass3 {
    Integer id;
    ApplicationVersion applicationVersion;
    String field1;
    String field2;

    public BadTestBaseClass3() {
    }

    public BadTestBaseClass3(Integer id, ApplicationVersion applicationVersion, String field1, String field2) {
      this.id = id;
      this.applicationVersion = applicationVersion;
      this.field1 = field1;
      this.field2 = field2;
    }

    public void setApplicationVersion(Integer id) {
      this.applicationVersion = new ApplicationVersion();
    }
  }

  private static class BadTestBaseClass4 {
    Integer id;
    ApplicationVersion applicationVersion;
    String field1;
    String field2;

    public BadTestBaseClass4() {
    }

    public BadTestBaseClass4(Integer id, ApplicationVersion applicationVersion, String field1, String field2) {
      this.id = id;
      this.applicationVersion = applicationVersion;
      this.field1 = field1;
      this.field2 = field2;
    }

    public ApplicationVersion setApplicationVersion(ApplicationVersion applicationVersion) {
      this.applicationVersion = applicationVersion;
      return applicationVersion;
    }
  }
}
