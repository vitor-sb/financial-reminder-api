//package com.project.manager.finance_management.integration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.project.manager.finance_management.adapters.dtos.authentication.AuthenticationDTO;
//import com.project.manager.finance_management.domains.models.user.Role;
//import com.project.manager.finance_management.domains.models.user.User;
//import com.project.manager.finance_management.domains.repositories.jpa.UserRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
//
//import java.util.List;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//class AuthenticationControllerTestExample {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private String validUserName = "User test";
//    private String validUserEmail = "test@example.com";
//    private String validUserPassword = "password123";
//
//    @BeforeEach
//    public void setup() {
//        userRepository.deleteAll();
//
//        User user = new User();
//        user.setName(validUserName);
//        user.setEmail(validUserEmail);
//        user.setPassword(new BCryptPasswordEncoder().encode(validUserPassword));
//        user.setRoles(List.of(Role.USER));
//        userRepository.save(user);
//    }
//
//    @Test
//    public void testLoginWithValidCredentials() throws Exception {
//        AuthenticationDTO dto = new AuthenticationDTO(validUserEmail, validUserPassword);
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(MockMvcResultMatchers.status().isOk())
//                .andExpect(MockMvcResultMatchers.jsonPath("$.token").exists());
//    }
//
//    @Test
//    public void testLoginWithInvalidCredentials() throws Exception {
//        AuthenticationDTO dto = new AuthenticationDTO("invalid@example.com", "invalidPassword");
//
//        mockMvc.perform(MockMvcRequestBuilders.post("/auth/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(dto)))
//                .andExpect(MockMvcResultMatchers.status().is(403));
//    }
//}