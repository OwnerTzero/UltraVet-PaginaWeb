package com.ultravet.veterinaria;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
class VeterinariaApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void adminDashboardRequiresAdminSession() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    void adminDashboardRendersWithAdminSession() throws Exception {
        mockMvc.perform(get("/admin")
                .with(user("admin").roles("ADMIN"))
                .sessionAttr("usuarioRol", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void publicPagesRender() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/servicios"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/adopcion"))
                .andExpect(status().isOk());
    }
}
