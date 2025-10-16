package com.technicalchallenge.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationResult {
    private boolean valid = true;
    private List<FieldValidationError> validationErrors = new ArrayList<>();

    public boolean isValid() {
        return valid;
    }

    public void addError(String fieldName, String errorMessage, String severity) {

        if (severity.equalsIgnoreCase("ERROR")) {
            this.valid = false;
        }
        this.validationErrors.add(new FieldValidationError(fieldName, errorMessage, severity));
    }

    @Override
    public String toString() {
        return "ValidationResult{" + "valid=" + valid + ", errors=" + validationErrors + '}';
    }
}
