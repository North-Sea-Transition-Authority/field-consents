package uk.co.nstauthority.fieldconsents.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.file.FieldConsentsFileTestUtil.GENERIC_FILE_DESCRIPTION;
import static uk.co.nstauthority.fieldconsents.file.FieldConsentsFileTestUtil.createUploadedFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.co.fivium.fileuploadlibrary.FileUploadLibraryUtils;
import uk.co.fivium.fileuploadlibrary.core.FileService;
import uk.co.fivium.fileuploadlibrary.core.FileUsage;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.fivium.fileuploadlibrary.fds.UploadedFileForm;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class FieldConsentsFileServiceTest {

  private static final String USAGE_ID = UUID.randomUUID().toString();
  private static final String USAGE_TYPE = "TestUsage";
  private static final String DOCUMENT_TYPE = "supporting-document";
  private static final FieldConsentsFileUsage DEFAULT_USAGE = new TestFieldConsentsFileUsage(USAGE_ID, USAGE_TYPE, DOCUMENT_TYPE);

  @Mock
  private FileService fileService;

  @InjectMocks
  private FieldConsentsFileService fieldConsentsFileService;

  @Captor
  private ArgumentCaptor<Function<FileUsage.Builder, FileUsage>> fileUsageFunctionCaptor;

  private FieldConsentsFileUsage fileUsage;

  private List<UploadedFile> uploadedFiles;
  private List<UUID> uploadedFileIds;
  private List<UploadedFileForm> uploadedFileForms;

  @BeforeEach
  void setUp() {
    fileUsage = new TestFieldConsentsFileUsage(USAGE_ID, USAGE_TYPE, DOCUMENT_TYPE);

    uploadedFiles = new ArrayList<>();
    uploadedFiles.add(createUploadedFile(fileUsage));

    uploadedFileIds = uploadedFiles.stream().map(UploadedFile::getId).toList();
    uploadedFileForms = uploadedFiles.stream().map(FileUploadLibraryUtils::asForm).toList();
  }

  @Test
  void saveDocuments() {
    doAnswer(invocation -> {
      var builderFunction = invocation.getArgument(1, Function.class);
      builderFunction.apply(FileUsage.newBuilder());
      return null;
    })
        .when(fileService)
        .updateUsageAndDescription(any(UploadedFile.class), any(), anyString());

    when(fileService.findAll(uploadedFileIds)).thenReturn(uploadedFiles);

    fieldConsentsFileService.saveDocuments(fileUsage, uploadedFileForms);

    for (var uploadedFile : uploadedFiles) {
      verify(fileService).updateUsageAndDescription(
          eq(uploadedFile),
          fileUsageFunctionCaptor.capture(),
          eq(GENERIC_FILE_DESCRIPTION)
      );
      assertThat(fileUsageFunctionCaptor.getValue().apply(FileUsage.newBuilder()))
          .extracting(
              FileUsage::usageId,
              FileUsage::usageType,
              FileUsage::documentType
          ).containsExactly(
              USAGE_ID,
              USAGE_TYPE,
              DOCUMENT_TYPE
          );
    }
  }

  @Test
  void getUploadedFileForms() {
    when(fileService.findAll(uploadedFileIds)).thenReturn(uploadedFiles);
    assertThat(fieldConsentsFileService.getUploadedFileForms(uploadedFileIds))
        .usingRecursiveFieldByFieldElementComparator()
        .containsExactlyElementsOf(uploadedFileForms);
  }

  @Test
  void getFileNotFoundException() {
    var fileId = UUID.randomUUID();
    assertThat(fieldConsentsFileService.getFileNotFoundException(fileId, fileUsage))
        .isInstanceOf(ResponseStatusException.class)
        .matches(e -> e.getStatusCode().value() == HttpStatus.NOT_FOUND.value())
        .hasMessageContaining("File [%s] does not exist for %s [%s]".formatted(fileId, USAGE_TYPE, USAGE_ID));
  }

  @Test
  void getUploadedFiles() {
    when(fileService.findAll(fileUsage.usageId(), fileUsage.usageType(), fileUsage.documentType())).thenReturn(
        uploadedFiles);
    assertThat(fieldConsentsFileService.getUploadedFiles(fileUsage)).isEqualTo(uploadedFiles);
  }

  @ParameterizedTest
  @MethodSource("throwIfFileDoesNotBelongToUsageThrowsParams")
  void throwIfFileDoesNotBelongToUsageThrows(UploadedFile uploadedFile, FieldConsentsFileUsage fileUsage) {
    assertThatThrownBy(() -> fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, fileUsage))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining(
            "File [%s] does not exist for %s [%s]".formatted(uploadedFile.getId(), fileUsage.usageType(), fileUsage.usageId()));
  }

  private static Stream<Arguments> throwIfFileDoesNotBelongToUsageThrowsParams() {
    var unmodifiedFile = createUploadedFile(DEFAULT_USAGE);

    return Stream.of(
        Arguments.of(unmodifiedFile, new TestFieldConsentsFileUsage(USAGE_ID + "_", USAGE_TYPE, DOCUMENT_TYPE)),
        Arguments.of(unmodifiedFile, new TestFieldConsentsFileUsage(USAGE_ID, USAGE_TYPE + "_", DOCUMENT_TYPE)),
        Arguments.of(unmodifiedFile, new TestFieldConsentsFileUsage(USAGE_ID, USAGE_TYPE, DOCUMENT_TYPE + "_")),
        Arguments.of(unmodifiedFile, new TestFieldConsentsFileUsage(null, USAGE_TYPE, DOCUMENT_TYPE)),
        Arguments.of(unmodifiedFile, new TestFieldConsentsFileUsage(USAGE_ID, null, DOCUMENT_TYPE)),
        Arguments.of(unmodifiedFile, new TestFieldConsentsFileUsage(USAGE_ID, USAGE_TYPE, null)),
        Arguments.of(createUploadedFile(new TestFieldConsentsFileUsage(USAGE_ID + "_", USAGE_TYPE, DOCUMENT_TYPE)), DEFAULT_USAGE),
        Arguments.of(createUploadedFile(new TestFieldConsentsFileUsage(USAGE_ID, USAGE_TYPE + "_", DOCUMENT_TYPE)), DEFAULT_USAGE),
        Arguments.of(createUploadedFile(new TestFieldConsentsFileUsage(USAGE_ID, USAGE_TYPE, DOCUMENT_TYPE + "_")), DEFAULT_USAGE),
        Arguments.of(createUploadedFile(new TestFieldConsentsFileUsage(null, USAGE_TYPE, DOCUMENT_TYPE)), DEFAULT_USAGE),
        Arguments.of(createUploadedFile(new TestFieldConsentsFileUsage(USAGE_ID, null, DOCUMENT_TYPE)), DEFAULT_USAGE),
        Arguments.of(createUploadedFile(new TestFieldConsentsFileUsage(USAGE_ID, USAGE_TYPE, null)), DEFAULT_USAGE)
    );
  }

  @ParameterizedTest
  @MethodSource("throwIfFileDoesNotBelongToUsageDoesNotThrowParams")
  void throwIfFileDoesNotBelongToUsageDoesNotThrow(UploadedFile uploadedFile, FieldConsentsFileUsage fileUsage) {
    assertThatNoException()
        .isThrownBy(() -> fieldConsentsFileService.throwIfFileDoesNotBelongToUsage(uploadedFile, fileUsage));
  }

  private static Stream<Arguments> throwIfFileDoesNotBelongToUsageDoesNotThrowParams() {
    var emptyUsage = new TestFieldConsentsFileUsage(null, null, null);

    return Stream.of(
        Arguments.of(createUploadedFile(DEFAULT_USAGE), DEFAULT_USAGE),
        Arguments.of(createUploadedFile(emptyUsage), emptyUsage)
    );
  }

  @Test
  void fileBelongsToUser() {
    var serviceUserDetail = ServiceUserDetailTestUtil.Builder().build();
    var uploadedFile = new UploadedFile();
    uploadedFile.setUploadedBy("1");

    assertThat(fieldConsentsFileService.fileBelongsToUser(uploadedFile, serviceUserDetail)).isTrue();
  }

  @Test
  void fileBelongsToUser_noUploadedByOnUploadedFile() {
    var serviceUserDetail = ServiceUserDetailTestUtil.Builder().build();
    var uploadedFile = new UploadedFile();

    assertThat(fieldConsentsFileService.fileBelongsToUser(uploadedFile, serviceUserDetail)).isFalse();
  }

  @Test
  void fileBelongsToUser_wuaIdsDoNotMatch() {
    var serviceUserDetail = ServiceUserDetailTestUtil.Builder().build();
    var uploadedFile = new UploadedFile();
    uploadedFile.setUploadedBy(String.valueOf(serviceUserDetail.wuaId() + 1));

    assertThat(fieldConsentsFileService.fileBelongsToUser(uploadedFile, serviceUserDetail)).isFalse();
  }

  @Test
  void copyUploadedFiles() {
    var newUsage = new TestFieldConsentsFileUsage(
        USAGE_ID + "_new",
        USAGE_TYPE + "_new",
        DOCUMENT_TYPE + "_new"
    );

    when(fileService.findAll(fileUsage.usageId(), fileUsage.usageType(), fileUsage.documentType())).thenReturn(uploadedFiles);

    fieldConsentsFileService.copyUploadedFiles(fileUsage, newUsage);

    for (var uploadedFile : uploadedFiles) {
      verify(fileService).copy(eq(uploadedFile), fileUsageFunctionCaptor.capture());
      assertThat(fileUsageFunctionCaptor.getValue().apply(FileUsage.newBuilder()))
          .extracting(
              FileUsage::usageId,
              FileUsage::usageType,
              FileUsage::documentType
          )
          .containsExactly(
              newUsage.usageId,
              newUsage.usageType,
              newUsage.documentType
          );
    }
  }

  private record TestFieldConsentsFileUsage(
      String usageId,
      String usageType,
      String documentType
  ) implements FieldConsentsFileUsage {
  }

}
