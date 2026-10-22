package com.inspaya.fixr.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class FixrUserResponseDto {
    private String username;
    private String firstname;
    private String middleName;
    private String lastname;
    private String email;
    private String blindEmailIndex;
}
