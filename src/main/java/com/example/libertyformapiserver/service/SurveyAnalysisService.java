package com.example.libertyformapiserver.service;

import com.example.libertyformapiserver.config.exception.BaseException;
import com.example.libertyformapiserver.config.status.BaseStatus;
import com.example.libertyformapiserver.config.type.TextType;
import com.example.libertyformapiserver.domain.*;
import com.example.libertyformapiserver.dto.question.post.PostQuestionRes;
import com.example.libertyformapiserver.dto.response.vo.ChoiceResponseVO;
import com.example.libertyformapiserver.dto.response.vo.NumericResponseVO;
import com.example.libertyformapiserver.dto.response.vo.TextResponseVO;
import com.example.libertyformapiserver.dto.surveyAnalysis.get.GetSurveyAnalysisRes;
import com.example.libertyformapiserver.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.libertyformapiserver.config.response.BaseResponseStatus.NOT_EXIST_SURVEY;
import static com.example.libertyformapiserver.config.response.BaseResponseStatus.NOT_MATCH_SURVEY;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class SurveyAnalysisService {
    private final ResponseRepository responseRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final TextResponseRepository textResponseRepository;
    private final TextAnalysisRepository textAnalysisRepository;
    private final NumericResponseRepository numericResponseRepository;
    private final ChoiceRepositoryCustom choiceRepositoryCustom;
    private final SingleChoiceResponseRepository singleChoiceResponseRepository;
    private final MultipleChoiceResponseRepository multipleChoiceResponseRepository;
    private final MultipleChoiceRepository multipleChoiceRepository;

    public GetSurveyAnalysisRes getSurveyAnalysis(long surveyId, long memberId){
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new BaseException(NOT_EXIST_SURVEY));

        if(survey.getMember().getId() != memberId){
            throw new BaseException(NOT_MATCH_SURVEY);
        }

        GetSurveyAnalysisRes analysisRes = getAllResponse(survey); // ?????? ?????? ????????????

        analysisRes = getSurveyResponse(analysisRes, survey); // ?????? ?????? ????????????

        Long responseCnt = responseRepository.countBySurveyId(surveyId); // ??? ????????? ??????
        analysisRes.setResponseCnt(responseCnt);

        return analysisRes;
    }

    /**
     * ?????? ?????????
     */
    // ?????? ????????? ????????????
    public GetSurveyAnalysisRes getAllResponse(Survey survey){
        GetSurveyAnalysisRes analysisRes = new GetSurveyAnalysisRes();

        List<Question> questions = questionRepository.findQuestionsBySurveyIdAndStatus(survey.getId(), BaseStatus.ACTIVE);

        for(int i = 0; i < questions.size(); i++){
            Question question = questions.get(i);
            int questionTypeId = (int) question.getQuestionType().getId();
            long q_id = question.getId();

            List<Choice> choices;
            List<Integer> responses;
            ChoiceResponseVO choiceResponseVO;
            switch(questionTypeId){
                case 1: case 2: // ?????????, ?????????
                    List<TextResponse> textResponses = textResponseRepository.findByQuestionId(q_id);

                    if(textResponses.isEmpty())
                        continue;

                    TextResponseVO textResponseVO = new TextResponseVO(textResponses.size(), PostQuestionRes.toDto(question));
                    textResponseVO.setResponses(textResponses);

                    if(questionTypeId == 2) // ???????????? ?????? ???????????? ??????
                        textResponseVO.setEmotions(textResponses);

                    TextAnalysis textAnalysis = textAnalysisRepository.findById(q_id)
                            .orElseGet(() -> null);

                    if(textAnalysis != null) // ?????? ???????????? ????????? ?????? ??????
                        textResponseVO.setWordCloudImgUrl(textAnalysis.getWordCloudImgUrl());

                    analysisRes.addTextResponse(textResponseVO);
                    break;
                case 3: // ?????? ??????
                    choices = choiceRepositoryCustom.findChoiceWithJoinSingleChoiceByQuestion(question);
                    responses = singleChoiceResponseRepository.findByQuestionId(question.getId())
                            .stream().map(c -> c.getChoice().getNumber()).collect(Collectors.toList());

                    if(responses.isEmpty())
                        continue;

                    choiceResponseVO = new ChoiceResponseVO(responses.size(), PostQuestionRes.toDto(question));
                    choiceResponseVO.setChoices(choices);
                    choiceResponseVO.setResponses(responses);

                    analysisRes.addChoiceResponse(choiceResponseVO);
                    break;
                case 4: // ?????? ??????
                    List<Integer> multipleResponses = new ArrayList<>();

                    choices = choiceRepositoryCustom.findChoiceWithJoinSingleChoiceByQuestion(question);
                    List<MultipleChoiceResponse> multipleChoiceResponses = multipleChoiceResponseRepository.findByQuestionId(question.getId());

                    // ????????? ?????? ?????? ?????? ?????? ????????????
                    multipleChoiceResponses.stream()
                            .forEach(mcr ->
                                multipleChoiceRepository.findByMultipleChoiceResponse(mcr)
                                        .stream()
                                        .forEach(mc -> multipleResponses.add(mc.getChoice().getNumber())));

                    if(multipleResponses.isEmpty())
                        continue;

                    choiceResponseVO = new ChoiceResponseVO(multipleResponses.size(), PostQuestionRes.toDto(question));
                    choiceResponseVO.setChoices(choices);
                    choiceResponseVO.setResponses(multipleResponses);

                    analysisRes.addChoiceResponse(choiceResponseVO);
                    break;
                case 5: case 6: // ?????? ???, ?????? ??????
                    List<NumericResponse> numericResponses = numericResponseRepository.findByQuestionId(q_id);

                    NumericResponseVO numericResponseVO = new NumericResponseVO(numericResponses.size(), PostQuestionRes.toDto(question));
                    numericResponseVO.setResponses(numericResponses);

                    analysisRes.addNumericResponse(numericResponseVO);
                    break;
            }
        }

        return analysisRes;
    }

    // ????????? ?????? ?????? ????????????
    public GetSurveyAnalysisRes getSurveyResponse(GetSurveyAnalysisRes res, Survey survey){
        res.setTitle(survey.getName());
        res.setCreatedDate(survey.getCreatedAt());
        res.setExpiredDate(survey.getExpirationDate());
        return res;
    }
}
