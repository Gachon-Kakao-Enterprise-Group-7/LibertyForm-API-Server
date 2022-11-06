package com.example.libertyformapiserver.dto.choice.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ChoiceVO {
    @ApiModelProperty(
            example = "3"
    )
    private long choiceId;
}
