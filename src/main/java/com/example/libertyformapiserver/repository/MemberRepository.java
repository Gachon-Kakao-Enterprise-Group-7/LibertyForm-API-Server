package com.example.libertyformapiserver.repository;

import com.example.libertyformapiserver.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Integer> {
    Optional<Member> findMemberByEmail(String email);
}