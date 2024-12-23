package com.reliaquest.api.service;

import com.reliaquest.server.model.CreateMockEmployeeInput;
import com.reliaquest.server.model.DeleteMockEmployeeInput;
import com.reliaquest.server.model.MockEmployee;
import com.reliaquest.server.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    RestClient restClient;


    public List<MockEmployee> getAllEmployees() {
        return invokeGetEmployees();
    }

    private List<MockEmployee> invokeGetEmployees() {
        Response<List<MockEmployee>> getResponse = restClient.get()
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
        return getResponse != null ? getResponse.data() : Collections.emptyList();
    }


    public List<MockEmployee> getEmployeesByNameSearch(String searchString) {
        return invokeGetEmployees().stream()
                .filter(mockEmployee -> mockEmployee.getName().contains(searchString))
                .collect(Collectors.toList());
    }

    public MockEmployee getEmployeeById(String id) {
        return restClient.get()
                .uri("/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange((request, response) -> {
                    if (response.getStatusCode().isSameCodeAs(HttpStatus.NOT_FOUND)) {
                        throw HttpServerErrorException.create(HttpStatus.NOT_FOUND, "Employee not found", response.getHeaders(), "Employee not found".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    }
                    return Objects.requireNonNull(response.bodyTo(new ParameterizedTypeReference<Response<MockEmployee>>() {}), "Employee not found").data();
                });
    }


    public Integer getHighestSalaryOfEmployees() {
        return invokeGetEmployees()
                .stream()
                .map(mockEmployee -> mockEmployee.getSalary())
                .max(Integer::compare).orElse(0);
    }


    public List<String> getTopTenHighestEarningEmployeeNames() {
        return invokeGetEmployees()
                .stream()
                .sorted(Comparator.comparingInt(MockEmployee::getSalary).reversed())
                .limit(10)
                .map(MockEmployee::getName)
                .collect(Collectors.toList());
    }


    public MockEmployee createEmployee(CreateMockEmployeeInput employeeInput) {
        Response<MockEmployee> createResponse = restClient.post().body(employeeInput).retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        (request, response) -> {
                            throw HttpServerErrorException.create(response.getStatusCode(), response.getStatusText(), response.getHeaders(), response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                        })
                .body(new ParameterizedTypeReference<>() {});
        return Objects.requireNonNull(createResponse, "Create Employee Failed").data();
    }


    public String deleteEmployeeById(String id) {
        MockEmployee employeeById = getEmployeeById(id);
        DeleteMockEmployeeInput deleteMockEmployeeInput = new DeleteMockEmployeeInput();
        deleteMockEmployeeInput.setName(employeeById.getName());
        restClient.method(HttpMethod.DELETE).body(deleteMockEmployeeInput).retrieve().toBodilessEntity();
        return employeeById.getName();
    }
}
