package com.backend.stockAllocation.exception;

public class BadRequestException extends  RuntimeException{
    public BadRequestException(String message) { super(message); }

}
