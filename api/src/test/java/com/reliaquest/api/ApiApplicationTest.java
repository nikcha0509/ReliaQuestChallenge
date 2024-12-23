package com.reliaquest.api;

import com.reliaquest.api.service.EmployeeService;
import com.reliaquest.server.model.CreateMockEmployeeInput;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiApplication.class)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiApplicationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Order(1)
    public void getEmployeeTest() throws Exception {
        ResultActions response = mvc.perform(get("/api/employee"));
        response.andExpect(status().isOk()).andDo(print()).andExpect(jsonPath("$.size()", greaterThanOrEqualTo(1)));;
    }

    @Test
    @Order(2)
    public void createEmployeeTest() throws Exception {
        CreateMockEmployeeInput input = new CreateMockEmployeeInput();
        input.setName("Nikhil Chaudhari");
        input.setSalary(400000);
        input.setAge(33);
        input.setTitle("Government Strategist");
        ResultActions response = mvc.perform(post("/api/employee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)));

        response.andDo(print()).
                andExpect(status().isOk())
                .andExpect(jsonPath("$.employee_name", is(input.getName())))
                .andExpect(jsonPath("$.employee_salary", is(input.getSalary())))
                .andExpect(jsonPath("$.employee_title", is(input.getTitle())));
    }

    @Test
    @Order(3)
    public void getByNameEmployeeTest() throws Exception {
        ResultActions response = mvc.perform(get("/api/employee/search/{searchString}", "Nikhil"));

        response.andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.size()", greaterThanOrEqualTo(1)));
    }
}