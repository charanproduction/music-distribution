package com.digital.repository;

import com.digital.entity.BankMandate;
import com.digital.entity.User;
import com.digital.enums.MandateStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankMandateRepository extends JpaRepository<BankMandate, Long> {

    Optional<BankMandate> findFirstByUserOrderByUploadedOnDesc(User user);

    List<BankMandate> findByStatus(MandateStatus status);
    List<BankMandate> findByUserOrderByUploadedOnDesc(User user);
   
}
