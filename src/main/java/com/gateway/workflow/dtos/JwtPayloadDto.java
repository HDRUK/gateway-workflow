package com.gateway.workflow.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@ToString
public class JwtPayloadDto {

    private String sub;
    @JsonProperty("_id")
    private String id;
    private String iat;
}
