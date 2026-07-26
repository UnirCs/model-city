package com.modelcity.common.exception;

public class ResourceNotFoundException extends ModelCityException {

    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";

    public ResourceNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public ResourceNotFoundException(String resourceName, Object id) {
        super(ERROR_CODE, String.format("%s with id '%s' not found", resourceName, id));
    }
}

