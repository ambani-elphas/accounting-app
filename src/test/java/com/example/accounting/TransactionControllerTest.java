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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void shouldUpdateTransaction() throws Exception {
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

        mockMvc.perform(put("/api/transactions/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description":"Invoice payment - corrected",
                                  "category":"Consulting",
                                  "amount":650,
                                  "type":"INCOME"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.description").value("Invoice payment - corrected"))
                .andExpect(jsonPath("$.category").value("Consulting"))
                .andExpect(jsonPath("$.amount").value(650));
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
                .andExpect(jsonPath("$.items[0].description").value("Cloud hosting"))
                .andExpect(jsonPath("$.items[0].type").value("EXPENSE"))
                .andExpect(jsonPath("$.totalItems").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldPaginateTransactionsForScale() throws Exception {
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/transactions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "description":"Transaction %d",
                              "category":"Operations",
                              "amount":10,
                              "type":"EXPENSE"
                            }
                            """.formatted(i)));
        }

        mockMvc.perform(get("/api/transactions")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalItems").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.items.length()").value(1));
    }

    @Test
    void shouldReturnCategorySummaries() throws Exception {
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
                          "description":"Another payment",
                          "category":"Revenue",
                          "amount":250,
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

        mockMvc.perform(get("/api/transactions/summary/by-category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Operations"))
                .andExpect(jsonPath("$[0].expenses").value(200))
                .andExpect(jsonPath("$[1].category").value("Revenue"))
                .andExpect(jsonPath("$[1].income").value(1250))
                .andExpect(jsonPath("$[1].balance").value(1250));
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
    void shouldReturnDashboardSummary() throws Exception {
        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "description":"Consulting invoice",
                          "category":"Revenue",
                          "amount":900,
                          "type":"INCOME"
                        }
                        """));

        mockMvc.perform(post("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "description":"Infrastructure",
                          "category":"Operations",
                          "amount":300,
                          "type":"EXPENSE"
                        }
                        """));

        mockMvc.perform(get("/api/transactions/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionCount").value(2))
                .andExpect(jsonPath("$.income").value(900))
                .andExpect(jsonPath("$.expenses").value(300))
                .andExpect(jsonPath("$.balance").value(600))
                .andExpect(jsonPath("$.recentTransactions[0].description").value("Infrastructure"));
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
