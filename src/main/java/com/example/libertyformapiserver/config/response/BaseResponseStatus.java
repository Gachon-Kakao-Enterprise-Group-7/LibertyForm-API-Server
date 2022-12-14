package com.example.libertyformapiserver.config.response;

import lombok.Getter;

/**
 * 상태 코드 관리
 */
@Getter
public enum BaseResponseStatus {
    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true, 1000, "요청에 성공하였습니다."),

    /**
     * 2000 : Request 오류
     */
    EMPTY_JWT(false, 2001, "header에 JWT가 없습니다."),
    INVALID_JWT(false, 2002, "유효하지 않은 JWT입니다. 재로그인 바랍니다."),
    EXPIRED_JWT(false, 2003,"만료기간이 지난 JWT입니다. 재로그인 바랍니다."),
    NOT_MATCH_PASSWORD(false, 2004, "비밀번호 확인란이 일치하지 않습니다."),
    ALREADY_EXIST_EMAIL(false, 2005, "중복된 이메일 주소입니다."),
    NOT_EXIST_EMAIL(false, 2006, "존재하지 않는 이메일 입니다."),
    INVALID_PASSWORD(false, 2007, "비밀번호가 일치하지 않습니다."),
    INACTIVE_STATUS(false, 2008, "삭제된 데이터입니다."),
    KAKAO_TOKEN_ERROR(false, 2009, "카카오 토큰을 받아오는 수행 작업 중에 오류가 발생했습니다."),
    KAKAO_LOGIN_ERROR(false, 2009, "카카오 로그인을 시도하는 중에 오류가 발생했습니다."),
    INVALID_MEMBER(false, 2010, "존재하지 않는 유저입니다."),
    NOT_MATCH_QUESTION_TYPE(false, 2011, "질문 유형 번호를 다시한번 확인해주시길 바랍니다."),
    NOT_SEQUENCE_QUESTION_NUMBER(false, 2012, "질문 번호 순서가 올바르지 않습니다. 질문 번호를 다시한번 확인해주시기 바랍니다."),
    NOT_EXIST_SURVEY(false, 2013, "존재하지 않는 설문입니다."),
    NOT_MATCH_SURVEY(false, 2014, "해당 사용자의 설문이 아닙니다."),
    NOT_EXIST_QUESTION(false, 2015, "존재하지 않는 질문입니다."),
    NOT_EXIST_CHOICE(false, 2016, "존재하지 않는 선택지 입니다"),
    NOT_ALLOW_EMAIL(false, 2017, "자기 자신의 이메일은 추가하실 수 없습니다."),
    ALREADY_REGISTER_EMAIL(false, 2018, "이미 연락처에 등록된 이메일입니다."),
    NOT_EXIST_CONTACT(false, 2018, "연락처가 존재하지 않습니다."),
    NOT_MATCH_QUESTION(false, 2019, "해당 사용자의 질문이 아닙니다."),
    NOT_EXIST_CODE(false, 2020, "유효한 코드 번호가 아닙니다."),
    NOT_MATCH_CHOICE(false, 2021, "선택지의 질문 번호를 확인해주세요."),
    NOT_EXIST_RESPONSE(false, 2022, "설문 응답 데이터가 존재하지 않습니다."),

    /**
     * 2500 : Request 성공
     */
    SURVEY_MODIFY_SUCCESS(true, 2500, "설문지 수정이 성공적으로 진행되었습니다."),
    SURVEY_MANAGEMENT_CREATED(true, 2501, "해당 사용자들에게 설문이 발송이 정상적으로 수행되었습니다."),
    CONTACT_DELETE_SUCCESS(true, 2502, "해당 이메일이 연락처에서 제거되었습니다."),
    SURVEY_MANAGEMENT_SUBMIT(true, 2503, "해당 설문지에 대해 설문 제출 응답처리가 되었습니다."),
    WORD_CLOUD_SUCCESS(true, 2504, "워드 클라우드 이미지가 성공적으로 업로드 되었습니다."),
    EMOTION_ANALYSIS_SUCCESS(true, 2505, "감정 분석이 성공적으로 업데이트 되었습니다."),
    SURVEY_CLOSE_SUCCESS(true, 2505, "설문지 강제 마감이 성공적으로 진행되었습니다."),

    /**
     * 3000 : Response 오류
     */
    VALIDATED_ERROR(false, 3000, "VALIDATED_ERROR"), // @Valid 예외 처리

    /**
     * 3500 : Response 성공
     */
    IMG_UPLOAD_SUCCESS(true, 3500, "이미지 업로드에 성공하였습니다."),
    SEND_SURVEY_SUCCESS(true, 3501, "설문지 발송에 성공하였습니다."),

    /**
     * 4000 : Database, Server 오류
     */
    INTERNAL_SERVER_ERROR(false, 4000, "서버 오류입니다"),
    NOT_VALID_QUESTION_TYPE(false, 4001, "존재하지 않는 질문 유형입니다."),
    FILE_UPLOAD_ERROR(false, 4002, "파일 업로드를 하는 도중에 오류가 발생했습니다."),
    THREAD_OVER_REQUEST(false, 4003, "잠시 후에 다시 시도해주세요"),
    NOT_EXIST_PAGE(false, 4004, "존재하지 않는 페이지입니다.");

    private final boolean isSuccess;
    private final int code;
    private final String message;

    BaseResponseStatus(boolean isSuccess, int code, String message){
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
    }
}
