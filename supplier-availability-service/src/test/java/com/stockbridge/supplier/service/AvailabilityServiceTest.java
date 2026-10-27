package com.stockbridge.supplier.service;

import com.stockbridge.supplier.dto.AvailabilityRequest;
import com.stockbridge.supplier.dto.AvailabilityResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AvailabilityServiceTest {

    private final AvailabilityService service = new AvailabilityService();

    @Test
    void check_shouldReturnSameResponse_forSameInput() {
        AvailabilityRequest request = new AvailabilityRequest("MON-001", 10, "MONITORES");

        assertThat(service.check(request)).isEqualTo(service.check(request));
    }

    @Test
    void check_shouldReturnValuesWithinRanges() {
        AvailabilityResponse response = service.check(new AvailabilityRequest("TEC-001", 1, "PERIFERICOS"));

        assertThat(response.supplierCode()).matches("SUP-00[1-5]");
        assertThat(response.availableQuantity()).isBetween(0, 49);
        assertThat(response.estimatedDeliveryDays()).isBetween(1, 7);
    }

    @Test
    void check_availableDependsOnRequestedQuantity() {
        int stock = service.check(new AvailabilityRequest("MOU-001", 1, "PERIFERICOS")).availableQuantity();

        assertThat(service.check(new AvailabilityRequest("MOU-001", stock, "PERIFERICOS")).available()).isTrue();
        assertThat(service.check(new AvailabilityRequest("MOU-001", stock + 1, "PERIFERICOS")).available()).isFalse();
    }

    @Test
    void check_ignoresCaseAndSpaces() {
        AvailabilityResponse upper = service.check(new AvailabilityRequest("MON-001", 5, "MONITORES"));
        AvailabilityResponse lower = service.check(new AvailabilityRequest(" mon-001 ", 5, "monitores"));

        assertThat(lower).isEqualTo(upper);
    }
}
