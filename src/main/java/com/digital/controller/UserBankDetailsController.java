package com.digital.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.digital.entity.BankDetails;
import com.digital.entity.User;
import com.digital.repository.BankDetailsRepository;
import com.digital.repository.UserRepository;
import com.digital.service.BankMandateService;
import com.digital.service.MandatePDFGenerator;
import com.digital.service.UserBankService;
import com.digital.util.MandateFormGenerator;
import com.lowagie.text.Document;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;
@Controller
@RequestMapping("/user")	
public class UserBankDetailsController {
	@Autowired
    private UserBankService bankService;
	@Autowired
	  private UserRepository userRepo;
	@Autowired
	private BankDetailsRepository bankRepo;
	@Autowired
	private MandateFormGenerator mandateformService;
	@Autowired
	private BankMandateService mandateService;
	@Autowired
	private MandatePDFGenerator mandatePdfService;
	
	@GetMapping("/bank")
    public String bankDetailsPage(Model model, Principal principal) {
        model.addAttribute("bankList", bankService.getBankDetails(principal.getName()));
        model.addAttribute("bank", new BankDetails());
        return "user/bank-details";
    }

    @PostMapping("/bank")
    public String addBankDetails(@ModelAttribute("bank") BankDetails bank,
                                 Principal principal) {
        bankService.saveBankDetails(principal.getName(), bank);
        return "redirect:/user/bank";
    }

    @PostMapping("/bank/delete/{id}")
    public String deleteBank(@PathVariable Long id, Principal principal) {
        bankService.deleteBankAccount(id, principal.getName());
        return "redirect:/user/bank";
    }
    
//    @GetMapping("/bank/mandate/download")
//    public void downloadMandateForm(HttpServletResponse response, Principal principal) throws IOException {
//
//    	response.setContentType("application/pdf");
//        response.setHeader("Content-Disposition", "attachment; filename=Account-Mandate-Form.pdf");
//
//        OutputStream out = response.getOutputStream();
//
//        Document document = new Document();
//        PdfWriter.getInstance(document, out);
//
//        document.open();
//
//        String username = principal.getName();
//
//        document.add(new Paragraph("Account Mandate Form", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
//        document.add(new Paragraph("\n"));
//
//        document.add(new Paragraph("Account Holder Name: " + username));
//        document.add(new Paragraph("Date: " + LocalDate.now()));
//        document.add(new Paragraph("\n\n"));
//        document.add(new Paragraph("This form authorizes the distribution company to remit payments to the registered bank account."));
//        document.add(new Paragraph("\n\n\n"));
//        document.add(new Paragraph("Signature: __________________________"));
//
//        document.close();
//    }
    
    @GetMapping("/bank/mandate/download")
    public ResponseEntity<byte[]> downloadMandate(Principal principal) throws Exception {

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        BankDetails bank = bankRepo.findByUser_IdAndPrimaryBank(user.getId(),Boolean.TRUE)
                .orElseThrow(() -> new RuntimeException("Bank details missing"));

        byte[] pdf = mandateformService.generate(user, bank);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Account-Mandate-Form.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    
    @GetMapping("/compliance/mandate/download")
    public ResponseEntity<byte[]> downloadBankMandate(Principal principal) {

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        byte[] pdf = mandatePdfService.generateMandatePDF(user);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=Bank-Mandate-Form.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    
    @PostMapping("/mandate")
    public String uploadMandate(@RequestParam("mandateFile") MultipartFile mandateFile,
                                @RequestParam("kycFile") MultipartFile kycFile,
                                Principal principal,
                                RedirectAttributes ra) {
        try {
        	mandateService.uploadMandate(principal.getName(), mandateFile);
            ra.addFlashAttribute("success", "Mandate submitted! Awaiting admin review.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Upload failed: " + e.getMessage());
        }
        return "redirect:/user/compliance/mandate/download";
    }
    
//    @GetMapping("/bank")
//    public String bankDetails(Model model, Principal principal) {
//        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
//        model.addAttribute("user", user);
//        return "user/bank-details";
//    }

//    @PostMapping("/bank")
//    public String updateBank(@ModelAttribute("bank") BankDetails bankDto,
//                             @RequestParam Long bankId,
//                             Principal principal,
//                             Model model) {
//
//        String username = principal.getName();
//        bankService.updateBankDetails(username, bankDto, bankId);
//
//        model.addAttribute("success", "Bank details updated!");
//
//        // Refresh bank list
//        List<BankDetails> bankList = bankService.getBankDetails(username);
//        model.addAttribute("banks", bankList);
//
//        return "user/bank-details";
//    }
    
    @PostMapping("/bank/setPrimary/{id}")
    public String setPrimary(@PathVariable Long id, Principal principal) {

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        List<BankDetails> banks = bankRepo.findByUser_Id(user.getId());

        banks.forEach(b -> {
            b.setPrimaryBank(b.getId().equals(id));
            bankRepo.save(b);
        });

        return "redirect:/user/bank";
    }
    
}
