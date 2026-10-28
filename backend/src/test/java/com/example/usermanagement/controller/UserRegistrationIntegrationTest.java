package com.example.usermanagement.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserRegistrationIntegrationTest {

    @Autowired
    private MockMvc mvc;

    private String body(String email, String password) {
        return """
                {"name":"Juan Rodriguez","email":"%s","password":"%s",
                 "phones":[{"number":"1234567","citycode":"1","contrycode":"57"}]}
                """.formatted(email, password);
    }

    @Test
    void registroExitosoDevuelve201ConJwt() throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("ok@dominio.cl", "Password1")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.token").value(org.hamcrest.Matchers.matchesPattern("^[\\w-]+\\.[\\w-]+\\.[\\w-]+$")))
                .andExpect(jsonPath("$.isactive").value(true))
                .andExpect(jsonPath("$.created").isNotEmpty())
                .andExpect(jsonPath("$.modified").isNotEmpty())
                .andExpect(jsonPath("$.last_login").isNotEmpty())
                .andExpect(jsonPath("$.phones[0].number").value("1234567"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void correoDuplicadoDevuelve409() throws Exception {
        String json = body("dup@dominio.cl", "Password1");
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("El correo ya registrado"));
    }

    @Test
    void correoInvalidoDevuelve400ConMensaje() throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("malo@dominio.org", "Password1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void contrasenaInvalidaDevuelve400ConMensaje() throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON)
                        .content(body("pass@dominio.cl", "hunter2")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }

    @Test
    void jsonMalFormadoDevuelve400ConMensaje() throws Exception {
        mvc.perform(post("/api/users/register").contentType(MediaType.APPLICATION_JSON).content("{no es json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").isNotEmpty());
    }
}
