package com.example.libertyformapiserver.repository;


import com.example.libertyformapiserver.domain.NumericResponse;
import com.example.libertyformapiserver.domain.SingleChoiceResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SingleChoiceRepository extends JpaRepository<SingleChoiceResponse, Long> {
}
