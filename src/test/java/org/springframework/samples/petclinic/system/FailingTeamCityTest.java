package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

class FailingTeamCityTest {

    @Test
    void failsToValidateTeamCityFailureHandling() {
        fail("Intentional failure to validate TeamCity run failure handling.");
    }
}
