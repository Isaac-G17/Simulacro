package com.stockbridge.inventory;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración contra un PostgreSQL real (Testcontainers): HTTP + seguridad + JPA + Flyway.
 * Sufijo "IT": no corre con "mvn test" (solo *Test). Ejecútalo con Docker encendido:
 *   mvn test -Dtest=ApplicationIT
 * Cada prueba crea sus propios datos (códigos y emails únicos) para no depender del orden de ejecución.
 */
class ApplicationIT extends AbstractIntegrationTest {

    private static final String ADMIN = "admin@stockbridge.com";
    private static final String BODEGA = "bodega@stockbridge.com";
    private static final String SEED_PASSWORD = "Admin123*";

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

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        mockMvc.perform(json(post("/auth/login"), credentials(ADMIN, SEED_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void login_withWrongPassword_returns401ProblemDetail() throws Exception {
        mockMvc.perform(json(post("/auth/login"), credentials(ADMIN, "incorrecta")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Credenciales inválidas"));
    }

    @Test
    void tamperedToken_returns401() throws Exception {
        String token = login(ADMIN);
        String tampered = token.substring(0, token.length() - 2) + (token.endsWith("AA") ? "BB" : "AA");

        mockMvc.perform(get("/api/products").header("Authorization", "Bearer " + tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void employeeCreatingProduct_returns403() throws Exception {
        String employee = registerEmployee();

        mockMvc.perform(json(post("/api/products"), productJson(uniqueCode(), 5)).header("Authorization", bearer(employee)))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidProduct_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(json(post("/api/products"), """
                        {"code":"", "name":"Monitor", "category":"MONITORES", "price":-5, "stock":1}
                        """).header("Authorization", bearer(login(ADMIN))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").exists())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void employeeReadingAnotherEmployeesRequest_returns403() throws Exception {
        long productId = createProduct(10);
        String owner = registerEmployee();
        String intruder = registerEmployee();
        long requestId = createRequest(owner, productId, 2);

        mockMvc.perform(get("/api/requests/{id}", requestId).header("Authorization", bearer(owner)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/requests/{id}", requestId).header("Authorization", bearer(intruder)))
                .andExpect(status().isForbidden());
    }

    @Test
    void fullFlow_createApproveDeliver_decreasesStockAndRegistersMovement() throws Exception {
        long productId = createProduct(10);
        String employee = registerEmployee();
        String warehouse = login(BODEGA);
        long requestId = createRequest(employee, productId, 4);

        mockMvc.perform(patch("/api/requests/{id}/approve", requestId).header("Authorization", bearer(warehouse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROBADA"));

        mockMvc.perform(patch("/api/requests/{id}/deliver", requestId).header("Authorization", bearer(warehouse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGADA"));

        mockMvc.perform(get("/api/products/{id}", productId).header("Authorization", bearer(warehouse)))
                .andExpect(jsonPath("$.stock").value(6));

        mockMvc.perform(get("/api/products/{id}/movements", productId).header("Authorization", bearer(warehouse)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("SALIDA"))
                .andExpect(jsonPath("$[0].quantity").value(4))
                .andExpect(jsonPath("$[*].requestId", hasItem((int) requestId)));
    }

    @Test
    void deliveringPendingRequest_returns409AndKeepsStock() throws Exception {
        long productId = createProduct(10);
        long requestId = createRequest(registerEmployee(), productId, 3);
        String warehouse = login(BODEGA);

        mockMvc.perform(patch("/api/requests/{id}/deliver", requestId).header("Authorization", bearer(warehouse)))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/api/products/{id}", productId).header("Authorization", bearer(warehouse)))
                .andExpect(jsonPath("$.stock").value(10));
    }

    @Test
    void requestingMoreThanStock_returns409() throws Exception {
        long productId = createProduct(2);

        mockMvc.perform(json(post("/api/requests"), requestJson(productId, 5))
                        .header("Authorization", bearer(registerEmployee())))
                .andExpect(status().isConflict());
    }

    // ---------- helpers ----------

    private String login(String email) throws Exception {
        return token(mockMvc.perform(json(post("/auth/login"), credentials(email, SEED_PASSWORD)))
                .andExpect(status().isOk()));
    }

    private String registerEmployee() throws Exception {
        String body = """
                {"fullName":"Empleado Prueba", "email":"%s@test.com", "password":"Secreta123"}
                """.formatted(UUID.randomUUID());
        return token(mockMvc.perform(json(post("/auth/register"), body)).andExpect(status().isCreated()));
    }

    private long createProduct(int stock) throws Exception {
        return id(mockMvc.perform(json(post("/api/products"), productJson(uniqueCode(), stock))
                        .header("Authorization", bearer(login(ADMIN))))
                .andExpect(status().isCreated()));
    }

    private long createRequest(String employeeToken, long productId, int quantity) throws Exception {
        return id(mockMvc.perform(json(post("/api/requests"), requestJson(productId, quantity))
                        .header("Authorization", bearer(employeeToken)))
                .andExpect(status().isCreated()));
    }

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder builder, String body) {
        return builder.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    private static String credentials(String email, String password) {
        return """
                {"email":"%s", "password":"%s"}
                """.formatted(email, password);
    }

    private static String productJson(String code, int stock) {
        return """
                {"code":"%s", "name":"Monitor prueba", "category":"MONITORES", "price":650000, "stock":%d}
                """.formatted(code, stock);
    }

    private static String requestJson(long productId, int quantity) {
        return """
                {"productId":%d, "quantity":%d, "observation":"Prueba de integración"}
                """.formatted(productId, quantity);
    }

    private static String uniqueCode() {
        return "IT-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String token(ResultActions result) throws Exception {
        return JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.token");
    }

    private static long id(ResultActions result) throws Exception {
        return ((Number) JsonPath.read(result.andReturn().getResponse().getContentAsString(), "$.id")).longValue();
    }
}
