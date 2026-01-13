package com.business.exception;

public class CategoryMissingException extends RuntimeException {
    public CategoryMissingException(String message) {
        super(message);
    }
}
