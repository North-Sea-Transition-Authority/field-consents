package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@ExtendWith(MockitoExtension.class)
class VentServiceTest {

  @Mock
  private VentRepository ventRepository;

  private VentService ventService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    ventService = new VentService(ventRepository);
    applicationVersion = VentTestUtil.ventAppVersion;
  }

  @Test
  void getVentsForApplicationVersion_noVents() {
    when(ventRepository.findAllByApplicationVersionOrderByIdAsc(applicationVersion))
        .thenReturn(new ArrayList<>());

    List<Vent> vents = ventService.getVentsForApplicationVersion(applicationVersion);
    assertThat(vents).isEmpty();

    boolean ventsExist = ventService.ventsExistForApplicationVersion(applicationVersion);
    assertThat(ventsExist).isFalse();
  }

  @Test
  void getVentsForApplicationVersion_manyVents() {
    when(ventRepository.findAllByApplicationVersionOrderByIdAsc(applicationVersion))
        .thenReturn(VentTestUtil.vents);

    List<Vent> vents = ventService.getVentsForApplicationVersion(applicationVersion);
    assertThat(vents)
        .containsExactly(
            VentTestUtil.ventHp,
            VentTestUtil.ventLp,
            VentTestUtil.ventOther
        );

    boolean ventsExist = ventService.ventsExistForApplicationVersion(applicationVersion);
    assertThat(ventsExist).isTrue();
  }

  @Test
  void getVentOrError_ventExists() {
    when(ventRepository.findByApplicationVersionAndVentNo(applicationVersion, VentTestUtil.ventNoHp))
        .thenReturn(Optional.of(VentTestUtil.ventHp));

    Vent vent = ventService.getVentOrError(applicationVersion, VentTestUtil.ventNoHp);

    assertThat(vent).isEqualTo(VentTestUtil.ventHp);
  }

  @Test
  void getVentOrError_noVentExists() {
    when(ventRepository.findByApplicationVersionAndVentNo(applicationVersion, VentTestUtil.badVentNo))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> ventService.getVentOrError(applicationVersion, VentTestUtil.badVentNo))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Vent with application_version_id %s and vent_no %s not found"
            .formatted(applicationVersion.getId(), VentTestUtil.badVentNo));
  }

  @Test
  void deleteVent() {
    ventService.deleteVent(VentTestUtil.ventHp);

    verify(ventRepository, times(1)).delete(VentTestUtil.ventHp);
  }

  @Test
  void saveNewVent() {
    when(ventRepository.findAllByApplicationVersionOrderByIdAsc(applicationVersion))
        .thenReturn(VentTestUtil.vents);
    ventService.saveNewVent(applicationVersion, VentTestUtil.ventFormHp2);

    ArgumentCaptor<Vent> ventArgumentCaptor = ArgumentCaptor.forClass(Vent.class);
    verify(ventRepository, times(1)).save(ventArgumentCaptor.capture());
  }

  @Test
  void updateVentFromForm() {
    ventService.updateVentFromForm(VentTestUtil.ventHp, VentTestUtil.ventFormHp2);

    ArgumentCaptor<Vent> ventArgumentCaptor = ArgumentCaptor.forClass(Vent.class);
    verify(ventRepository, times(1)).save(ventArgumentCaptor.capture());
  }

}
