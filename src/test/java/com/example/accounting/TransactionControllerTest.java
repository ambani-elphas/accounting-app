package com.example.accounting;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldCreateTransaction() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description":"Office supplies",
                                  "category":"Operations",
                                  "amount":125.50,
                                  "type":"EXPENSE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Office supplies"))
                .andExpect(jsonPath("$.category").value("Operations"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    void shouldGetTransactionById() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description":"Invoice payment",
                                  "category":"Revenue",
                                  "amount":500,
                                  "type":"INCOME"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(get("/api/transactions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Invoice payment"))
                .andExpect(jsonPath("$.category").value("Revenue"));
    }

    @Test
    void shouldReturnNotFoundForUnknownTransactionId() throws Exception {
        mockMvc.perform(get("/api/transactions/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found"));
    }

    @Test
    void shouldDeleteTransaction() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description":"Subscription",
                                  "category":"Software",
                                  "amount":30,
                                  "type":"EXPENSE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String id = JsonPath.read(createResult.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(delete("/api/transactions/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/transactions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldFilterTransactionsByTypeAndCategory() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "description":"Client payment",
                          "category":"Revenue",
                          "amount":1000,
                          "type":"INCOME"
                        }
                        """));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "description":"Cloud hosting",
                          "category":"Operations",
                          "amount":200,
                          "type":"EXPENSE"
                        }
                        """));

        mockMvc.perform(get("/api/transactions")
                        .param("type", "EXPENSE")
                        .param("category", "operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Cloud hosting"))
                .andExpect(jsonPath("$[0].type").value("EXPENSE"));
    }

    @Test
    void shouldReturnBadRequestForMissingCategory() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description":"Missing category",
                                  "amount":10,
                                  "type":"EXPENSE"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    void shouldReturnBadRequestForInvalidType() throws Exception {
        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description":"Bad type",
                                  "category":"Revenue",
                                  "amount":10,
                                  "type":"INVALID"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed request"));
    }

    @Test
    void shouldCalculateSummary() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "description":"Client payment",
                          "category":"Revenue",
                          "amount":1000,
                          "type":"INCOME"
                        }
                        """));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "description":"Hosting",
                          "category":"Operations",
                          "amount":200,
                          "type":"EXPENSE"
                        }
                        """));

        mockMvc.perform(get("/api/transactions/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.income").value(1000))
                .andExpect(jsonPath("$.expenses").value(200))
                .andExpect(jsonPath("$.balance").value(800));
    }
}
