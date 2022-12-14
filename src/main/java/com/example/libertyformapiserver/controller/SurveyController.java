package com.example.libertyformapiserver.controller;

import com.example.libertyformapiserver.config.response.BaseResponse;
import com.example.libertyformapiserver.config.response.BaseResponseStatus;
import com.example.libertyformapiserver.dto.flask.post.PostEmotionAnalysisDto;
import com.example.libertyformapiserver.dto.jwt.JwtInfo;
import com.example.libertyformapiserver.dto.survey.create.PostCreateSurveyReq;
import com.example.libertyformapiserver.dto.survey.create.PostCreateSurveyRes;
import com.example.libertyformapiserver.dto.survey.get.GetListSurveyRes;
import com.example.libertyformapiserver.dto.survey.get.GetSurveyInfoRes;
import com.example.libertyformapiserver.dto.survey.patch.PatchSurveyDeleteRes;
import com.example.libertyformapiserver.dto.survey.patch.PatchSurveyModifyReq;
import com.example.libertyformapiserver.jwt.NoIntercept;
import com.example.libertyformapiserver.service.FlaskService;
import com.example.libertyformapiserver.service.ObjectStorageService;
import com.example.libertyformapiserver.service.SurveyService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.Validator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.example.libertyformapiserver.config.response.BaseResponseStatus.SURVEY_CLOSE_SUCCESS;

@Log4j2
@RestController
@RequestMapping("/survey")
@RequiredArgsConstructor
public class SurveyController {
    private Validator validator;

    private final SurveyService surveyService;
    private final ObjectStorageService objectStorageService;

    private final FlaskService flaskService;

    // swagger에서 multipartFile 테스트 불가, 해당 테스트 작업 시 postman으로 수행
    @ApiOperation(
            value = "설문지 생성",
            notes = "survey - 설문 내용, questions - 주관식, 감정, 선형대수 문항," +
                    " choiceQuestions - 객관식 문항"
    )
    @ApiResponses({
            @ApiResponse(code = 1000, message = "요청에 성공하였습니다."),
            @ApiResponse(code = 2010, message = "존재하지 않는 유저입니다."),
            @ApiResponse(code = 2011, message = "질문 유형 번호를 다시한번 확인해주시길 바랍니다."),
            @ApiResponse(code = 2012, message = "질문 번호 순서가 올바르지 않습니다. 질문 번호를 다시한번 확인해주시기 바랍니다."),
            @ApiResponse(code = 4001, message = "존재하지 않는 질문 유형입니다."),
            @ApiResponse(code = 4002, message = "파일을 업로드 하는 도중 오류가 발생했습니다.")}
    )
    @PostMapping(value = "/create") // questionImgFiles은 설문 문항 번호로 구분이 됨 ex) 0.jpg, 1.png
    public BaseResponse<PostCreateSurveyRes> createSurvey(@Validated @RequestPart PostCreateSurveyReq surveyReqDto, HttpServletRequest request
            , @RequestParam(value = "thumbnailImg", required = false)MultipartFile thumbnailImgFile, @RequestParam(value = "questionImgs", required = false)List<MultipartFile> questionImgFiles){

        PostCreateSurveyRes postCreateSurveyRes = surveyService.createSurvey(surveyReqDto, JwtInfo.getMemberId(request), thumbnailImgFile, questionImgFiles);
        log.info("Create Survey : {}", postCreateSurveyRes.getSurvey().getCode());

        return new BaseResponse<>(postCreateSurveyRes);
    }

    // 설문지 수정
    @ApiOperation(
            value = "설문지 수정",
            notes = "surveyId - 설문지 아이디, questions - 주관식, 감정, 선형대수 문항," +
                    " choiceQuestions - 객관식 문항"
    )
    @ApiResponses({
            @ApiResponse(code = 1000, message = "요청에 성공하였습니다."),
            @ApiResponse(code = 2010, message = "존재하지 않는 유저입니다."),
            @ApiResponse(code = 2011, message = "질문 유형 번호를 다시한번 확인해주시길 바랍니다."),
            @ApiResponse(code = 2012, message = "질문 번호 순서가 올바르지 않습니다. 질문 번호를 다시한번 확인해주시기 바랍니다."),
            @ApiResponse(code = 2013, message = "존재하지 않는 설문입니다."),
            @ApiResponse(code = 2014, message = "해당 사용자의 설문이 아닙니다."),
            @ApiResponse(code = 2015, message = "존재하지 않는 질문입니다."),
            @ApiResponse(code = 2015, message = "존재하지 않는 선택지입니다."),
            @ApiResponse(code = 2019, message = "해당 사용자의 질문이 아닙니다."),
            @ApiResponse(code = 2500, message = "설문지 수정이 성공적으로 진행되었습니다."),
            @ApiResponse(code = 4001, message = "존재하지 않는 질문 유형입니다."),
            @ApiResponse(code = 4002, message = "파일을 업로드 하는 도중 오류가 발생했습니다.")}
    )
    @PatchMapping("/modify")
    public BaseResponse<String> modifySurvey(@RequestBody PatchSurveyModifyReq surveyModifyReq, HttpServletRequest request){
        surveyService.modifySurvey(surveyModifyReq, JwtInfo.getMemberId(request));
        log.info("Modify Survey Id : {}", surveyModifyReq.getSurvey().getSurveyId());

        return new BaseResponse<>(BaseResponseStatus.SURVEY_MODIFY_SUCCESS);
    }

    // 설문지 모두 불러오기
    @ApiOperation(
            value = "설문지 모두 불러오기",
            notes = "해당 유저에 대한 설문을 모두 불러옵니다. (대시보드 용)"
    )
    @ApiResponses({
            @ApiResponse(code = 1000, message = "요청에 성공하였습니다.")
    })
    @GetMapping
    public BaseResponse<GetListSurveyRes> getAllUserSurvey(HttpServletRequest request){
        GetListSurveyRes getListSurveyRes = surveyService.getAllUserSurvey(JwtInfo.getMemberId(request));
        log.info("getAllUserSurvey : {}", JwtInfo.getMemberId((request)));

        return new BaseResponse<>(surveyService.getAllUserSurvey(JwtInfo.getMemberId(request)));
    }

    // 피설문자 설문지 정보 가져오기
    @ApiOperation(
            value = "피설문자 설문지 정보 가져오기",
            notes = "피설문자가 해당 설문에 대한 정보들을 가져옵니다"
    )
    @ApiResponses({
            @ApiResponse(code = 1000, message = "요청에 성공하였습니다."),
            @ApiResponse(code = 2013, message = "존재하지 않는 설문입니다."),
            @ApiResponse(code = 2014, message = "해당 사용자의 설문이 아닙니다.")
    })
    @NoIntercept
    @GetMapping("{code}")
    public BaseResponse<GetSurveyInfoRes> getSurveyInfo(@PathVariable("code") String code){
        GetSurveyInfoRes getSurveyInfoRes = surveyService.getSurveyInfo(code);
        log.info("Survey Info : {}", getSurveyInfoRes.getSurvey().getCode());

        return new BaseResponse<>(getSurveyInfoRes);
    }


    // 설문지 삭제하기
    @ApiOperation(
            value = "설문지 삭제하기",
            notes = "surveyId를 통해 설문지를 삭제합니다."
    )
    @ApiResponses({
            @ApiResponse(code = 1000, message = "요청에 성공하였습니다."),
            @ApiResponse(code = 2013, message = "존재하지 않는 설문입니다."),
            @ApiResponse(code = 2014, message = "해당 사용자의 설문이 아닙니다.")
    })
    @PatchMapping("/delete/{surveyId}")
    public BaseResponse<PatchSurveyDeleteRes> deleteSurvey(HttpServletRequest request, @PathVariable("surveyId") long surveyId){
        PatchSurveyDeleteRes patchSurveyDeleteRes =  surveyService.deleteSurvey(surveyId, JwtInfo.getMemberId(request));
        log.info("Delete Survey : {}", patchSurveyDeleteRes.getSurveyId());

        return new BaseResponse<>(patchSurveyDeleteRes);
    }

    // 설문지 강제 마감
    @ApiOperation(
            value = "설문지 강제 마감",
            notes = "surveyId를 통해 설문지를 강제로 마감합니다."
    )
    @ApiResponses({
            @ApiResponse(code = 1000, message = "요청에 성공하였습니다."),
            @ApiResponse(code = 2013, message = "존재하지 않는 설문입니다."),
            @ApiResponse(code = 2014, message = "해당 사용자의 설문이 아닙니다.")
    })
    @GetMapping("/close/{surveyId}")
    public BaseResponse<PatchSurveyDeleteRes> forceCloseSurvey(HttpServletRequest request, @PathVariable("surveyId") long surveyId){
        surveyService.forceCloseSurvey(surveyId, JwtInfo.getMemberId(request));
        log.info("Close Survey : {}", surveyId);

        return new BaseResponse<>(SURVEY_CLOSE_SUCCESS);
    }
}
