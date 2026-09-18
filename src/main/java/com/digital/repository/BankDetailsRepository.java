package com.digital.repository;

import com.digital.entity.BankDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankDetailsRepository extends JpaRepository<BankDetails, Long> {

    List<BankDetails> findByUser_Id(Long userId);
    
   BankDetails findByUser_IdAndPrimaryBankTrue(Long userId);
    Optional<BankDetails> findByUser_IdAndPrimaryBank(Long userId,Boolean primaryBank);
}

