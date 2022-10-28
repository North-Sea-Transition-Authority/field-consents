package uk.co.nstauthority.fieldconsents.flarevent.flare;

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
class FlareServiceTest {

  @Mock
  private FlareRepository flareRepository;

  private FlareService flareService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    flareService = new FlareService(flareRepository);
    applicationVersion = FlareTestUtil.flareAppVersion;
  }

  @Test
  void getFlaresForApplicationVersion_noFlares() {
    when(flareRepository.findAllByApplicationVersionOrderByIdAsc(applicationVersion))
        .thenReturn(new ArrayList<>());

    List<Flare> flares = flareService.getFlaresForApplicationVersion(applicationVersion);
    assertThat(flares).isEmpty();

    boolean flaresExist = flareService.flaresExistForApplicationVersion(applicationVersion);
    assertThat(flaresExist).isFalse();
  }

  @Test
  void getFlaresForApplicationVersion_manyFlares() {
    when(flareRepository.findAllByApplicationVersionOrderByIdAsc(applicationVersion))
        .thenReturn(FlareTestUtil.flares);

    List<Flare> flares = flareService.getFlaresForApplicationVersion(applicationVersion);
    assertThat(flares)
        .containsExactly(
            FlareTestUtil.flareHp,
            FlareTestUtil.flareMp,
            FlareTestUtil.flareLp,
            FlareTestUtil.flareLpp
        );

    boolean flaresExist = flareService.flaresExistForApplicationVersion(applicationVersion);
    assertThat(flaresExist).isTrue();
  }

  @Test
  void getFlareOrError_flareExists() {
    when(flareRepository.findByApplicationVersionAndFlareNo(applicationVersion, FlareTestUtil.flareNoHp))
        .thenReturn(Optional.of(FlareTestUtil.flareHp));

    Flare flare = flareService.getFlareOrError(applicationVersion, FlareTestUtil.flareNoHp);

    assertThat(flare).isEqualTo(FlareTestUtil.flareHp);
  }

  @Test
  void getFlareOrError_noFlareExists() {
    when(flareRepository.findByApplicationVersionAndFlareNo(applicationVersion, FlareTestUtil.badFlareNo))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> flareService.getFlareOrError(applicationVersion, FlareTestUtil.badFlareNo))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Flare with application_version_id %s and flare_no %s not found"
            .formatted(applicationVersion.getId(), FlareTestUtil.badFlareNo));
  }

  @Test
  void deleteFlare() {
    flareService.deleteFlare(FlareTestUtil.flareHp);

    verify(flareRepository, times(1)).delete(FlareTestUtil.flareHp);
  }

  @Test
  void saveNewFlare() {
    when(flareRepository.findAllByApplicationVersionOrderByIdAsc(applicationVersion))
        .thenReturn(FlareTestUtil.flares);
    flareService.saveNewFlare(applicationVersion, FlareTestUtil.flareFormHp2);

    ArgumentCaptor<Flare> flareArgumentCaptor = ArgumentCaptor.forClass(Flare.class);
    verify(flareRepository, times(1)).save(flareArgumentCaptor.capture());
  }

  @Test
  void updateFlareFromForm() {
    flareService.updateFlareFromForm(FlareTestUtil.flareHp, FlareTestUtil.flareFormHp2);

    ArgumentCaptor<Flare> flareArgumentCaptor = ArgumentCaptor.forClass(Flare.class);
    verify(flareRepository, times(1)).save(flareArgumentCaptor.capture());
  }

}
