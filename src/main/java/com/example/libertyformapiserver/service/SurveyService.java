package com.example.libertyformapiserver.service;

import com.example.libertyformapiserver.config.exception.BaseException;
import com.example.libertyformapiserver.config.status.BaseStatus;
import com.example.libertyformapiserver.domain.*;
import com.example.libertyformapiserver.dto.choice.patch.PatchChoiceReq;
import com.example.libertyformapiserver.dto.choice.post.PostChoiceRes;
import com.example.libertyformapiserver.dto.question.patch.PatchChoiceQuestionReq;
import com.example.libertyformapiserver.dto.question.patch.PatchQuestionReq;
import com.example.libertyformapiserver.dto.question.post.PostChoiceQuestionReq;
import com.example.libertyformapiserver.dto.choice.post.PostChoiceReq;
import com.example.libertyformapiserver.dto.question.post.PostQuestionReq;
import com.example.libertyformapiserver.dto.question.post.PostQuestionRes;
import com.example.libertyformapiserver.dto.survey.create.PostCreateSurveyReq;
import com.example.libertyformapiserver.dto.survey.create.PostCreateSurveyRes;
import com.example.libertyformapiserver.dto.survey.get.GetListSurveyRes;
import com.example.libertyformapiserver.dto.survey.get.GetSurveyInfoRes;
import com.example.libertyformapiserver.dto.survey.patch.PatchSurveyDeleteRes;
import com.example.libertyformapiserver.dto.survey.patch.PatchSurveyModifyReq;
import com.example.libertyformapiserver.dto.survey.patch.PatchSurveyModifyRes;
import com.example.libertyformapiserver.dto.survey.post.PostSurveyReq;
import com.example.libertyformapiserver.repository.*;
import com.example.libertyformapiserver.utils.algorithm.Algorithms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static com.example.libertyformapiserver.config.response.BaseResponseStatus.*;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class SurveyService {
    private final MemberRepository memberRepository;

    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;
    private final QuestionTypeRepository questionTypeRepository;

    private final ObjectStorageService objectStorageService;

    private final FlaskService flaskService;

    // ????????? ??????
    @Transactional(readOnly = false, rollbackFor = {Exception.class, BaseException.class})
    public PostCreateSurveyRes createSurvey(PostCreateSurveyReq surveyReqDto, long memberId
            , MultipartFile thumbnailImgFile, List<MultipartFile> questionImgFiles) {
        PostSurveyReq postSurveyReq = surveyReqDto.getSurvey();

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseException(INVALID_MEMBER));

        Survey survey = postSurveyReq.toEntity(member);
        survey.generateCode();
        surveyRepository.save(survey);

        PostCreateSurveyRes createSurveyResDto = new PostCreateSurveyRes(survey);

        // ?????????, ????????? ?????? ????????????
        List<PostQuestionReq> postQuestionReqList = surveyReqDto.getQuestions();
        List<PostChoiceQuestionReq> postChoiceQuestionReqList = surveyReqDto.getChoiceQuestions();

        // ????????? ?????? DTO -> Entity ??????
        List<PostQuestionRes> questionResList = getQuestionListEntity(postQuestionReqList, survey);
        createSurveyResDto.setQuestions(questionResList);

        // ????????? ?????? DTO -> Entity ??????
        createSurveyResDto = getChoiceQuestionListEntity(postChoiceQuestionReqList, createSurveyResDto, survey);

        // ?????????, ?????? ????????? Object Storage??? ?????????
        objectStorageService.uploadThumbnailImg(survey, thumbnailImgFile);
        createSurveyResDto.setThumbnailImgUrl(survey.getThumbnailImg());

        List<Question> questionList = questionRepository.findQuestionsBySurveyIdAndStatus(survey.getId(), BaseStatus.ACTIVE);
        objectStorageService.uploadQuestionImgs(questionList, questionImgFiles);

        checkQuestionNumber(createSurveyResDto.getQuestions());
        return createSurveyResDto;
    }

    // ????????? ?????? ??????
    public GetListSurveyRes getAllUserSurvey(long memberId) {
        List<Survey> surveys = surveyRepository.findSurveysByMemberIdAndStatus(memberId, BaseStatus.ACTIVE);

        return GetListSurveyRes.listEntitytoDto(surveys);
    }

    // ???????????? ????????? ?????? ??????
    public GetSurveyInfoRes getSurveyInfo(String code) {
        Survey survey = surveyRepository.findByCodeAndStatus(code, BaseStatus.ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_EXIST_SURVEY));

        return getSurveyInfo(survey);
    }

    // ????????? ??????
    @Transactional(readOnly = false, rollbackFor = {Exception.class, BaseException.class})
    public void modifySurvey(PatchSurveyModifyReq surveyModifyReq, long memberId) {
        long surveyId = surveyModifyReq.getSurvey().getSurveyId();
        Survey survey = surveyRepository.findById(surveyId).orElseThrow(
                () -> new BaseException(NOT_EXIST_SURVEY));

        survey.update(surveyModifyReq.getSurvey());

        List<Question> questions = survey.getQuestions();
        // ????????? ????????? inActive ?????? ????????? ?????? ?????? InActive??? ????????????.
        questions.forEach(q -> {
            q.changeStatusInActive();
            q.getChoices().forEach(c -> c.changeStatusInActive());
        });

        PatchSurveyModifyRes res = new PatchSurveyModifyRes(survey, questions);

        // ????????? ?????? ????????????
        res = modifyQuestions(res, surveyModifyReq.getQuestions());

        // ????????? ????????????
        res = modifyChoiceQuestions(res, surveyModifyReq.getChoiceQuestions());

        // ?????? ?????? ???????????? ????????????
        checkModifyNumber(res);

        // ?????? ????????? ??????, ????????? ????????????
        insertNewQuestionsAndChoices(res);

        surveyRepository.save(survey);

        System.out.println("adsad");
    }

    // ????????? ??????
    @Transactional(readOnly = false)
    public PatchSurveyDeleteRes deleteSurvey(long surveyId, long memberId) {
        Survey survey = surveyRepository.findById(surveyId).orElseThrow
                (() -> new BaseException(NOT_EXIST_SURVEY));

        if (survey.getMember().getId() != memberId) {
            throw new BaseException(NOT_MATCH_SURVEY);
        }

        survey.changeStatusInActive();
        PatchSurveyDeleteRes patchSurveyDeleteRes = PatchSurveyDeleteRes.toDto(survey);

        return patchSurveyDeleteRes;
    }

    // ????????? ?????? ??????
    @Transactional(readOnly = false)
    public void forceCloseSurvey(long surveyId, long memberId){
        Survey survey = surveyRepository.findById(surveyId).orElseThrow
                (() -> new BaseException(NOT_EXIST_SURVEY));

        if (survey.getMember().getId() != memberId) {
            throw new BaseException(NOT_MATCH_SURVEY);
        }

        survey.changeStatusExpired();

        flaskService.sendTextResponseToFlaskBySurveyId(survey.getId());
    }


    /**
     * ????????? ?????? ?????????
     */
    // ?????????, ?????? ???, ?????? ?????? ?????? ??????
    private List<PostQuestionRes> getQuestionListEntity(List<PostQuestionReq> postQuestionReqList, Survey survey) {
        List<PostQuestionRes> questionResList = new ArrayList<>();

        if (postQuestionReqList == null)
            return questionResList;

        // Question ??? Survey, QuestionType ??????
        for (int i = 0; i < postQuestionReqList.size(); i++) {
            PostQuestionReq postQuestionReq = postQuestionReqList.get(i);

            long questionTypeId = postQuestionReq.getQuestionTypeId();

            // ????????? ???????????? ???????????? ??? ?????? ??????
            if (questionTypeId == 3 || questionTypeId == 4)
                throw new BaseException(NOT_MATCH_QUESTION_TYPE);

            QuestionType questionType = questionTypeRepository.findById(questionTypeId).orElseThrow(
                    () -> new BaseException(NOT_VALID_QUESTION_TYPE));


            Question question = postQuestionReq.toEntity(survey, questionType);

            questionResList.add(PostQuestionRes.toDto(question));
            questionRepository.save(question);
        }
        return questionResList;
    }

    // ????????? ?????? ??????
    private PostCreateSurveyRes getChoiceQuestionListEntity(List<PostChoiceQuestionReq> postChoiceQuestionReqList, PostCreateSurveyRes surveyResDto, Survey survey) {
        if (postChoiceQuestionReqList == null)
            return surveyResDto;

        List<PostQuestionRes> questionResList = surveyResDto.getQuestions();
        List<PostChoiceRes> choiceResList = new ArrayList<>();

        // ????????? ?????? ??????
        for (int i = 0; i < postChoiceQuestionReqList.size(); i++) {
            PostChoiceQuestionReq choiceQuestionReq = postChoiceQuestionReqList.get(i);
            PostQuestionReq questionReq = choiceQuestionReq.getQuestion();

            long questionTypeId = questionReq.getQuestionTypeId();

            // ????????? ???????????? ???????????? ??? ?????? ??????
            if (questionTypeId != 3 && questionTypeId != 4)
                throw new BaseException(NOT_MATCH_QUESTION_TYPE);

            QuestionType questionType = questionTypeRepository.findById(questionTypeId).orElseThrow(
                    () -> new BaseException(NOT_VALID_QUESTION_TYPE));

            Question question = questionReq.toEntity(survey, questionType);

            questionResList.add(PostQuestionRes.toDto(question));
            questionRepository.save(question);

            // ????????? ?????? ??????
            List<PostChoiceReq> postChoiceReqList = choiceQuestionReq.getChoices();
            checkChoiceNumber(postChoiceReqList);

            for (int j = 0; j < postChoiceReqList.size(); j++) {
                PostChoiceReq postChoiceReq = postChoiceReqList.get(j);

                Choice choice = postChoiceReq.toEntity(question);

                choiceResList.add(PostChoiceRes.toDto(choice));
                choiceRepository.save(choice);
            }
        }
        surveyResDto.setQuestions(questionResList);
        surveyResDto.setChoices(choiceResList);

        return surveyResDto;
    }

    /**
     * ????????? ?????? ?????????
     */
    // ????????? ?????? ?????? ??????
    private PatchSurveyModifyRes modifyQuestions(PatchSurveyModifyRes res, List<PatchQuestionReq> questionDtoList) {
        List<Question> questions = res.getQuestions();
        Survey survey = res.getSurvey();
        for (PatchQuestionReq questionDto : questionDtoList) {
            Question question = questions.stream()
                    .filter(q -> q.getId() == questionDto.getQuestionId()).findFirst().orElseGet(() -> null);

            QuestionType questionType = questionTypeRepository.findById(questionDto.getQuestionTypeId())
                    .orElseThrow(() -> new BaseException(NOT_VALID_QUESTION_TYPE));

            if (question == null) { // ?????? ????????? ???????????? ?????? ??????
                question = questionDto.toEntity(survey, questionType);
                res.addExtraQuestion(question);
                continue;
            }

            // ???????????? ?????? ????????? ????????? ????????????
            if(question.getSurvey().getId() != survey.getId())
                throw new BaseException(NOT_MATCH_QUESTION);

            question.update(questionDto, questionType); // ????????? ?????? ???????????? ????????????
            res.addQuestionNumber(question);
        }

        return res;
    }

    // ????????? ?????? ??????
    private PatchSurveyModifyRes modifyChoiceQuestions(PatchSurveyModifyRes res, List<PatchChoiceQuestionReq> questionDtoList) {
        Survey survey = res.getSurvey();

        // ??????
        List<Question> questions = res.getQuestions();

        for (PatchChoiceQuestionReq choiceQuestionDto : questionDtoList) {
            Question question = questions.stream()
                    .filter(q -> q.getId() == choiceQuestionDto.getQuestionId()).findFirst().orElseGet(() -> null);

            long typeId = choiceQuestionDto.getQuestion().getQuestionTypeId();
            QuestionType type = questionTypeRepository.findById(typeId).orElseThrow(() -> new BaseException(NOT_VALID_QUESTION_TYPE));

            if (question == null) { // ?????? ????????? ???????????? ?????? ??????

                question = choiceQuestionDto.getQuestion().toEntity(survey, type);
                res.addExtraQuestion(question);
            } else {
                // ???????????? ?????? ????????? ????????? ????????????
                if(question.getSurvey().getId() != survey.getId())
                    throw new BaseException(NOT_MATCH_QUESTION);

                question.update(choiceQuestionDto.getQuestion(), type);
                res.addQuestionNumber(question);
            }

            // ?????????
            List<Choice> choices = question.getChoices();

            if (choices != null) {
                choices.forEach(c -> c.changeStatusInActive());

                for (PatchChoiceReq choiceDto : choiceQuestionDto.getChoices()) {
                    Choice choice = choices.stream()
                            .filter(c -> c.getId() == choiceDto.getChoiceId()).findFirst().orElseGet(() -> null);

                    if (choice == null) { // ?????? ????????? ????????? ???????????? ?????? ??????
                        choice = choiceDto.toEntity(question);
                        res.addExtraChoice(choice);
                        continue;
                    }

                    if(choice.getQuestion().getId() != question.getId()) // ???????????? ?????? ????????? ?????? ???
                        throw new BaseException(NOT_MATCH_CHOICE);

                    choice.update(choiceDto);
                    res.addChoiceNumber(choice);
                }
            } else { // ?????? ????????? ????????? ????????? ???????????? ?????? ??????
                Question finalQuestion = question;
                choiceQuestionDto.getChoices().stream()
                        .map(dto -> dto.toEntity(finalQuestion)).forEach(c -> res.addExtraChoice(c));
            }
        }

        return res;
    }

    // ????????? ?????? ?????? ???????????? ??????
    private void checkModifyNumber(PatchSurveyModifyRes res){
        List<Integer> questionNumbers = res.getQuestionNumbers();
        HashMap<Question, ArrayList<Integer>> choiceNumbers = res.getChoiceNumberMap();

        // ?????? ?????? ??????
        Algorithms.checkDuplicateNumber(questionNumbers);

        // ????????? ?????? ??????
        choiceNumbers.keySet().stream()
                .forEach(k -> Algorithms.checkDuplicateNumber(choiceNumbers.get(k)));
    }

    // ?????? ????????? ??????, ????????? ??????
    private void insertNewQuestionsAndChoices(PatchSurveyModifyRes res){
        List<Question> questions = res.getExtraQuestions();
        List<Choice> choices = res.getExtraChoices();

        questionRepository.saveAll(questions);
        choiceRepository.saveAll(choices);
    }
    /* --- Modify End --- */

    // ?????? ?????? ????????? ???????????? ??????
    private void checkQuestionNumber(List<PostQuestionRes> questionList) {
        List<Integer> numberList = new ArrayList<>();
        questionList.forEach(q -> numberList.add(q.getNumber()));

        Algorithms.checkDuplicateNumber(numberList);
    }

    // ????????? ?????? ????????? ???????????? ??????
    private void checkChoiceNumber(List<PostChoiceReq> choiceList) {
        List<Integer> numberList = new ArrayList<>();
        choiceList.forEach(q -> numberList.add(q.getNumber()));

        Algorithms.checkDuplicateNumber(numberList);
    }

    // ????????? ?????? ????????????
    private GetSurveyInfoRes getSurveyInfo(Survey survey) {
        long surveyId = survey.getId();

        List<Question> questions = questionRepository.findQuestionsBySurveyIdAndStatus(surveyId, BaseStatus.ACTIVE);
        List<PostChoiceQuestionReq> postChoiceQuestionReqList = new ArrayList<>();

        Iterator<Question> iter = questions.iterator(); // ConcurrentModificationException ????????? ?????? iterator ??????
        while (iter.hasNext()) {
            Question question = iter.next();
            long questionTypeId = question.getQuestionType().getId();

            if (questionTypeId == 3 || questionTypeId == 4) {
                long questionId = question.getId();
                List<Choice> choiceList = choiceRepository.findChoicesByQuestionIdAndStatus(questionId, BaseStatus.ACTIVE);
                postChoiceQuestionReqList.add(PostChoiceQuestionReq.toVO(question, choiceList));
                iter.remove();
            }
        }

        GetSurveyInfoRes getSurveyInfoRes = GetSurveyInfoRes.toDto(survey, questions);
        getSurveyInfoRes.setChoiceQuestions(postChoiceQuestionReqList);
        return getSurveyInfoRes;
    }
}
