package com.zhang.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class User {
    private Integer id;
    private String name;
    private Integer salary;

    public User(int i, String jack) {
        this.id = i;
        this.name = jack;

    }
}
