package uk.co.nstauthority.fieldconsents.assets.hubs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.hub.HubApi;
import uk.co.fivium.energyportalapi.generated.client.HubProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.HubsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Hub;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

@ExtendWith(MockitoExtension.class)
class HubServiceTest {

  private static final Hub HUB =
      Hub.newBuilder()
          .id(1)
          .name("hub name")
          .operator(OrganisationUnit.newBuilder()
              .organisationUnitId(100)
              .name("hub operator")
              .build()
          )
          .build();

  private static final HubWithOperatorJson HUB_WITH_OPERATOR_JSON = HubWithOperatorJson.from(HUB);

  private static final Map<String, Object> EXPECTED_QUERY_FIELDS = new HubProjectionRoot()
      .id()
      .name()
      .operator()
        .organisationUnitId()
        .name()
        .root()
      .getFields();

  @Mock
  private HubApi hubApi;

  @InjectMocks
  private HubService hubService;

  @Test
  void searchHubs() {
    var queryCaptor = ArgumentCaptor.forClass(HubsProjectionRoot.class);
    var requestPurpose = "request purpose";

    when(hubApi.search(
        eq(HUB.getName()),
        queryCaptor.capture(),
        eq(new RequestPurpose(requestPurpose)))
    )
        .thenReturn(List.of(HUB));

    assertThat(hubService.searchHubs(HUB.getName(), requestPurpose)).containsExactly(HUB_WITH_OPERATOR_JSON);

    assertThat(queryCaptor.getValue().getFields())
        .usingRecursiveComparison()
        .isEqualTo(EXPECTED_QUERY_FIELDS);
  }

  @Test
  void searchHubs_hubDoesNotHaveOperator() {
    var hub = Hub.newBuilder().build();

    when(hubApi.search(any(), any(), any())).thenReturn(List.of(hub));

    assertThat(hubService.searchHubs(HUB.getName(), "")).isEmpty();
  }

  @Test
  void findHubWithOperator() {
    var queryCaptor = ArgumentCaptor.forClass(HubProjectionRoot.class);
    var requestPurpose = "request purpose";

    when(hubApi.find(
        eq(HUB.getId()),
        queryCaptor.capture(),
        eq(new RequestPurpose(requestPurpose)))
    )
        .thenReturn(Optional.of(HUB));

    assertThat(hubService.findHubWithOperator(HUB.getId(), requestPurpose)).contains(HUB_WITH_OPERATOR_JSON);

    assertThat(queryCaptor.getValue().getFields())
        .usingRecursiveComparison()
        .isEqualTo(EXPECTED_QUERY_FIELDS);
  }

  @Test
  void findHubWithOperator_hubDoesNotHaveOperator() {
    var hub = Hub.newBuilder().build();

    when(hubApi.find(eq(HUB.getId()), any(), any())).thenReturn(Optional.of(hub));

    assertThat(hubService.findHubWithOperator(HUB.getId(), "")).isEmpty();
  }

  @Test
  void getHubWithOperator() {
    when(hubApi.find(eq(HUB.getId()), any(), any())).thenReturn(Optional.of(HUB));
    assertThat(hubService.getHubWithOperator(HUB.getId(), "")).isEqualTo(HUB_WITH_OPERATOR_JSON);
  }

  @Test
  void getHubWithOperator_hubNotFound() {
    when(hubApi.find(eq(HUB.getId()), any(), any())).thenReturn(Optional.empty());
    var hubId = HUB.getId();
    assertThatThrownBy(() -> hubService.getHubWithOperator(hubId, ""))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Hub [%s] not found".formatted(HUB.getId()));
  }
}