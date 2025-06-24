package com.nklcbdty.batch.nklcbdty.batch.email.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailSendRequest {
    private String to;
    private String subject;
    private String body;
}
