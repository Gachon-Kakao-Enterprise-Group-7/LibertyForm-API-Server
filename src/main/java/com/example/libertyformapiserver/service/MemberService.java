package com.example.libertyformapiserver.service;

import com.example.libertyformapiserver.config.exception.BaseException;
import com.example.libertyformapiserver.domain.Member;
import com.example.libertyformapiserver.dto.member.kakao.post.PostKakaoRegisterReq;
import com.example.libertyformapiserver.dto.member.post.PostRegisterReq;
import com.example.libertyformapiserver.dto.member.post.PostRegisterRes;
import com.example.libertyformapiserver.repository.MemberRepository;
import com.example.libertyformapiserver.utils.encrypt.SHA256;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.libertyformapiserver.config.response.BaseResponseStatus.ALREADY_EXIST_EMAIL;
import static com.example.libertyformapiserver.config.response.BaseResponseStatus.NOT_MATCH_PASSWORD;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    // 회원 가입
    @Transactional(readOnly = false)
    public PostRegisterRes registerMember(PostRegisterReq dto){
        if(!dto.getCheckPassword().equals(dto.getPassword())){
            throw new BaseException(NOT_MATCH_PASSWORD);
        }
        if(!memberRepository.findMemberByEmail(dto.getEmail()).isEmpty()){
            throw new BaseException(ALREADY_EXIST_EMAIL);
        }

        dto.setPassword(SHA256.encrypt(dto.getPassword()));
        Member member = dto.toEntity(dto.getEmail(), dto.getPassword(), dto.getName());
        memberRepository.save(member);
        PostRegisterRes postRegisterRes = PostRegisterRes.toDto(member);
        return postRegisterRes;
    }

    // 카카오 회원 가입 (Service 에서만 동작)
    @Transactional(readOnly = false)
    public Member registerKakaoMember(PostKakaoRegisterReq dto){
        if(!memberRepository.findMemberByEmail(dto.getEmail()).isEmpty()){
            throw new BaseException(ALREADY_EXIST_EMAIL);
        }

        Member member = dto.toEntity(dto.getEmail(), dto.getName());
        memberRepository.save(member);
        return member;
    }
}
