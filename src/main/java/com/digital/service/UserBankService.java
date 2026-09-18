package com.digital.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.digital.entity.BankDetails;
import com.digital.entity.User;
import com.digital.repository.BankDetailsRepository;
import com.digital.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserBankService {

	private final BankDetailsRepository bankRepo;
	private final UserRepository userRepo;

	public List<BankDetails> getBankDetails(String username) {
		User user = userRepo.findByUsername(username).orElseThrow();
		return bankRepo.findByUser_Id(user.getId());
	}

	public void saveBankDetails(String username, BankDetails bankDetails) {
		User user = userRepo.findByUsername(username).orElseThrow();

		// If primary set true, remove primary from existing ones
		if (bankDetails.isPrimaryBank()) {
			bankRepo.findByUser_Id(user.getId()).forEach(b -> {
				b.setPrimaryBank(false);
				bankRepo.save(b);
			});
		}

		bankDetails.setUser(user);
		bankRepo.save(bankDetails);
	}

	public void deleteBankAccount(Long id, String username) {
		User user = userRepo.findByUsername(username).orElseThrow();
		bankRepo.findById(id).ifPresent(b -> {
			if (b.getUser().getId().equals(user.getId())) {
				bankRepo.delete(b);
			}
		});
	}

	@Transactional
	public void updateBankDetails(String username, BankDetails updatedDetails, Long bankId) {

		User user = userRepo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

		BankDetails bank = bankRepo.findById(bankId).orElseThrow(() -> new RuntimeException("Bank record not found"));

		if (!bank.getUser().equals(user)) {
			throw new RuntimeException("Access denied");
		}

		bank.setBankName(updatedDetails.getBankName());
		bank.setAccountNumber(updatedDetails.getAccountNumber());
		bank.setIfsc(updatedDetails.getIfsc());
		bank.setUpi(updatedDetails.getUpi());

		bankRepo.save(bank);
	}
}
