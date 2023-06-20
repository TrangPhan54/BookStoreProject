package com.axonactive.PersonalProject.service.imple;

import com.axonactive.PersonalProject.entity.*;
import com.axonactive.PersonalProject.exception.LibraryException;
import com.axonactive.PersonalProject.repository.CustomerRepository;
import com.axonactive.PersonalProject.repository.PhysicalBookRepository;
import com.axonactive.PersonalProject.repository.ReceiptRepository;
import com.axonactive.PersonalProject.service.ReceiptService;
import com.axonactive.PersonalProject.service.dto.CreateReceiptDTO;
import com.axonactive.PersonalProject.service.dto.ReceiptDTO;
import com.axonactive.PersonalProject.service.mapper.ReceiptMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
@Service
@Transactional
@RequiredArgsConstructor

public class ReceiptServiceImplementation implements ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final CustomerRepository customerRepository;

    private final ReceiptMapper receiptMapper;
    private final PhysicalBookRepository physicalBookRepository;
    private static final double LIQUIDATION_COEFFICIENT = 0.2;
    @Override
    public List<ReceiptDTO> getAllReceipt() {
        return receiptMapper.toDtos(receiptRepository.findAll());
    }

    @Override
    public ReceiptDTO createReceipt(CreateReceiptDTO createReceiptDTO) {
        Customer customer = customerRepository.findById(createReceiptDTO.getCustomerID()).orElseThrow(LibraryException::CustomerNotFound);
        Receipt receipt = Receipt.builder().customer(customer).build();
        double totalFee = 0.0;
        List<ReceiptDetail> receiptDetailList = new ArrayList<>();
        for (Long physicalBookId : createReceiptDTO.getPhysicalBookIdList()) {
            PhysicalBook physicalBook = physicalBookRepository.findById(physicalBookId).get();
            ReceiptDetail receiptDetail = new ReceiptDetail();
            receiptDetail.setReceipt(receipt);
            receiptDetail.setLiquidationFee(physicalBook.getImportPrice() * LIQUIDATION_COEFFICIENT);
            totalFee += receiptDetail.getLiquidationFee();
            receiptDetail.setPhysicalBook(physicalBook);
            receiptDetailList.add(receiptDetail);
        }
        receipt.setReceiptDetailList(receiptDetailList);
        receipt = receiptRepository.save(receipt);
        ReceiptDTO receiptDTO = new ReceiptDTO();
        receiptDTO.setId(receipt.getId());
        receiptDTO.setCustomerID(receipt.getCustomer().getId());
        receiptDTO.setLiquidationFee(totalFee);
        receiptDTO.setPhysicalBookIdList(createReceiptDTO.getPhysicalBookIdList());
        return receiptDTO;
    }

}
