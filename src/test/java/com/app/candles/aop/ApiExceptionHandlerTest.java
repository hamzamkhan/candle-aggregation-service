package com.app.candles.aop;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import static org.junit.jupiter.api.Assertions.*;

class ApiExceptionHandlerTest {
    
    private ApiExceptionHandler exceptionHandler;
    
    @BeforeEach
    void setUp() {
        exceptionHandler = new ApiExceptionHandler();
    }
    
    @Test
    void badRequestHandlesIllegalArgumentException() {
        String errorMessage = "Invalid interval: 2m";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);
        
        ProblemDetail result = exceptionHandler.badRequest(exception);
        
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Bad Request", result.getTitle());
        assertEquals(errorMessage, result.getDetail());
    }
    
    @Test
    void badRequestHandlesEmptyMessage() {
        IllegalArgumentException exception = new IllegalArgumentException();
        
        ProblemDetail result = exceptionHandler.badRequest(exception);
        
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Bad Request", result.getTitle());
        assertNull(result.getDetail());
    }
    
    @Test
    void badRequestHandlesNullMessage() {
        IllegalArgumentException exception = new IllegalArgumentException((String) null);
        
        ProblemDetail result = exceptionHandler.badRequest(exception);
        
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("Bad Request", result.getTitle());
    }
}

