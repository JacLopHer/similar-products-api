package com.company.similarproducts.infrastructure.adapter.rest.exception;
import com.company.similarproducts.domain.exception.ProductNotFoundException;
import com.company.similarproducts.domain.model.ProductId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import static org.assertj.core.api.Assertions.*;
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {
    private GlobalExceptionHandler exceptionHandler;
    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }
    @Test
    @DisplayName("Should handle ProductNotFoundException with 404")
    void shouldHandleProductNotFoundWith404() {
        ProductId productId = new ProductId("999");
        ProductNotFoundException exception = new ProductNotFoundException(productId);
        ProblemDetail result = exceptionHandler.handleProductNotFound(exception);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("Product Not Found");
        assertThat(result.getDetail()).contains("999");
    }
    @Test
    @DisplayName("Should handle IllegalArgumentException with 400")
    void shouldHandleIllegalArgumentWith400() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid input");
        ProblemDetail result = exceptionHandler.handleIllegalArgument(exception);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Invalid Request");
        assertThat(result.getDetail()).isEqualTo("Invalid input");
    }
    @Test
    @DisplayName("Should handle generic Exception with 500")
    void shouldHandleGenericExceptionWith500() {
        Exception exception = new RuntimeException("Unexpected error");
        ProblemDetail result = exceptionHandler.handleGenericException(exception);
        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
    }
}
