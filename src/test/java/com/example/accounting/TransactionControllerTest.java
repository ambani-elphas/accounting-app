package com.example.accounting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
                                  "amount":125.50,
                                  "type":"EXPENSE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.description").value("Office supplies"));
    }

    @Test
    void shouldListTransactions() throws Exception {
        mockMvc.perform(get("/api/transactions"))
                .andExpect(status().isOk());
    }
}
