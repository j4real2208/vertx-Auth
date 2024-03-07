package com.jojo.auth.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Name {
    String firstName;
    String lastName;

    @JsonCreator
    public Name(@JsonProperty("firstName") String firstName, @JsonProperty("lastName") String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }
}
