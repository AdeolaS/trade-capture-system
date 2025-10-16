package com.technicalchallenge.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor

public class FieldValidationError {
    private String fieldName;
    private String errorMessage;

    // Error or Warning
    private String severity;
}
