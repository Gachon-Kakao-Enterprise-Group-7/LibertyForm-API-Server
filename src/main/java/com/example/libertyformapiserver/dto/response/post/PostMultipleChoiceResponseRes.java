package com.example.libertyformapiserver.dto.response.post;

import com.example.libertyformapiserver.domain.MultipleChoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
@Builder
public class PostMultipleChoiceResponseRes {
    private long choiceId;

    private long multipleChoiceResponseId;

    static public PostMultipleChoiceResponseRes toDto (MultipleChoice multipleChoiceResponse){
        return PostMultipleChoiceResponseRes.builder()
                .choiceId(multipleChoiceResponse.getChoice().getId())
                .multipleChoiceResponseId(multipleChoiceResponse.getMultipleChoiceResponse().getId())
                .build();
    }

    static public List<PostMultipleChoiceResponseRes> toListDto(List<MultipleChoice> multipleChoiceResponseList){
        return multipleChoiceResponseList.stream().map(m -> toDto(m)).collect(Collectors.toList());
    }
}
