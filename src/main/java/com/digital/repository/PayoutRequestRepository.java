package com.digital.repository;

import com.digital.entity.PayoutRequest;
import com.digital.entity.User;
import com.digital.enums.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PayoutRequestRepository extends JpaRepository<PayoutRequest, Long> {

    List<PayoutRequest> findByUserOrderByRequestedOnDesc(User user);

    List<PayoutRequest> findByStatusOrderByRequestedOnAsc(PayoutStatus status);
}
