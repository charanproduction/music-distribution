package com.digital.repository;

import com.digital.entity.User;
import com.digital.entity.UserRightsConsent;
import com.digital.enums.RightsConsentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRightsConsentRepository extends JpaRepository<UserRightsConsent, Long> {

    Optional<UserRightsConsent> findByUser(User user);

    List<UserRightsConsent> findByStatus(RightsConsentStatus status);
    List<UserRightsConsent> findByUserOrderBySubmittedOnDesc(User user);
}
