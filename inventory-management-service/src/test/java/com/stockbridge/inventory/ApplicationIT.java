package com.stockbridge.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sufijo "IT": no corre con "mvn test" (solo *Test). Ejecútalo con Docker encendido:
 *   mvn test -Dtest=ApplicationIT
 */
class ApplicationIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads_andFlywayMigrates() throws Exception {
        mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() throws Exception {
        mockMvc.perform(get("/api/products")).andExpect(status().isUnauthorized());
    }

    // TODO: login -> token; EMPLEADO creando producto -> 403; flujo crear -> aprobar -> entregar
}
