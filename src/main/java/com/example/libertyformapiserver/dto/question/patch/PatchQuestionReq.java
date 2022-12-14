package com.example.libertyformapiserver.dto.question.patch;

import com.example.libertyformapiserver.domain.Question;
import com.example.libertyformapiserver.domain.QuestionType;
import com.example.libertyformapiserver.domain.Survey;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PatchQuestionReq {
    @ApiModelProperty(
            example = "1"
    )
    private long questionId;

    @ApiModelProperty(
            example = "3"
    )
    private long questionTypeId;

    @ApiModelProperty(
            example = "당신이 좋아하는 과일은 어떤걸까요?"
    )
    @NotBlank(message = "질문 제목을 입력해주세요.")
    @Size(min = 1, max = 30, message = "질문 제목은 1 ~ 30자 이내만 가능합니다.")
    private String name;

    @ApiModelProperty(
            example = "예를들어서 사과, 오렌지, 포도가 있습니다."
    )
    @Size(max = 100, message = "질문 설명은 100자 이내만 가능합니다.")
    private String description;

    @ApiModelProperty(
            example = "2"
    )
    private int number;

    @ApiModelProperty(
            example = "false"
    )
    private boolean answerRequired;

    public Question toEntity(Survey survey, QuestionType questionType){
        return Question.builder()
                .survey(survey)
                .questionType(questionType)
                .name(name)
                .description(description)
                .number(number)
                .answerRequired(answerRequired)
                .build();
    }

    public static PatchQuestionReq toDto(Question question){
        return PatchQuestionReq.builder()
                .questionTypeId(question.getQuestionType().getId())
                .name(question.getName())
                .description(question.getDescription())
                .number(question.getNumber())
                .answerRequired(question.isAnswerRequired())
                .build();
    }
}
