package org.example.orderservice.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message, Long id) {
        super(String.format("%s with id %d not found", message, id));
    }
}



