package com.axonactive.PersonalProject.service.imple;

import com.axonactive.PersonalProject.entity.Book;
import com.axonactive.PersonalProject.entity.Customer;
import com.axonactive.PersonalProject.repository.CustomerRepository;
import com.axonactive.PersonalProject.service.BorrowNoteDetailService;
import com.axonactive.PersonalProject.service.dto.customedDto.CustomerWithNumberOfPhysicalCopiesBorrow;
import com.axonactive.PersonalProject.service.dto.customedDto.ReturnBookByCustomerDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BorrowNoteDetailServiceImplementationTest {
    @Autowired
    private BorrowNoteDetailService borrowNoteDetailService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void getNumberOfBookByCustomerId() {
        Long numberOfBorrowedBooks = borrowNoteDetailService.getNumberOfBookByCustomerId(21L);
        System.out.println(numberOfBorrowedBooks);

//        assertEquals(numberOfBorrowedBooks, 2);
    }

    @Test
    void customerReturnBook() {
        System.out.println(borrowNoteDetailService.customerReturnBook(1L, 2L));
    }


    @Test
    void nameOfBookRemaining() {
        List<Long> ids = List.of(1L,2L);
        List<String> tempList = borrowNoteDetailService.nameOfBookRemaining(1L, ids);
        tempList.forEach(System.out::println);
    }

    @Test
    void returnBookByCustomer() {
        ReturnBookByCustomerDto returnBookByCustomerDto = new ReturnBookByCustomerDto();
        returnBookByCustomerDto.setCustomerId(12L);
        List<Long> physicalBookIds = new ArrayList<>();
        physicalBookIds.add(17L);
        returnBookByCustomerDto.setPhysicalBookIds(physicalBookIds);
        System.out.println(returnBookByCustomerDto.getPhysicalBookIds());
//        List<Long> ids = List.of(17L);
//        borrowNoteDetailService.returnBookByCustomer(12L, ids);
        borrowNoteDetailService.returnBookByCustomer(returnBookByCustomerDto);
//
        Customer customer = customerRepository.findById(12L).get();
//        System.out.println(customer.get);
    }

    @Test
    void getBookNameByBookId() {
        String books = borrowNoteDetailService.getBookNameByBookId(12L);
        System.out.println(books);

    }

    @Test
    void fineFeeForReturningBookLate() {
        ReturnBookByCustomerDto returnBookByCustomerDto = new ReturnBookByCustomerDto();
        returnBookByCustomerDto.setCustomerId(1L);
        List<Long> physicalBookIds = new ArrayList<>();
        physicalBookIds.add(1L);
        physicalBookIds.add(2L);
        physicalBookIds.add(3L);
        returnBookByCustomerDto.setPhysicalBookIds(physicalBookIds);
        borrowNoteDetailService.fineFeeForReturningBookLate(returnBookByCustomerDto);
        System.out.println(returnBookByCustomerDto.getPhysicalBookIds());
//        borrowNoteDetailService.feeFineForReturningBookLate(1L,ids);
    }

    @Test
    void getMaxBorrowBook() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String date1String = "2022/01/01";
        String date2String = "2024/01/01";
        LocalDate date1 = LocalDate.parse(date1String, dateTimeFormatter);
        LocalDate date2 = LocalDate.parse(date2String, dateTimeFormatter);
        Map<Book,Long> re = borrowNoteDetailService.getMaxBorrowBook(date1,date2);
        for (Map.Entry <Book,Long> entry:re.entrySet()){
            Book bk = entry.getKey();
            Long pk = entry.getValue();
            System.out.println(bk.getName());
            System.out.println(pk);
        }

    }

//    @Test
//    void getMaxBorrowCustomer() {
//        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
//        String date1String = "2022/01/01";
//        String date2String = "2024/01/01";
//        LocalDate date1 = LocalDate.parse(date1String, dateTimeFormatter);
//        LocalDate date2 = LocalDate.parse(date2String, dateTimeFormatter);
//        List<CustomerDTO> re = borrowNoteDetailService.getMaxBorrowCustomer(date1,date2);
//        System.out.println(re);
//    }

    @Test
    void getMaxCustomer() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        String date1String = "2022/01/01";
        String date2String = "2024/01/01";
        LocalDate date1 = LocalDate.parse(date1String, dateTimeFormatter);
        LocalDate date2 = LocalDate.parse(date2String, dateTimeFormatter);
        List<CustomerWithNumberOfPhysicalCopiesBorrow> re = borrowNoteDetailService.getMaxCustomer(date1,date2);

//        for (Map.Entry <Customer,Long> entry:re.entrySet()){
//            Customer cus = entry.getKey();
//            Long pk = entry.getValue();
//            System.out.println(cus.getFirstName());
//            System.out.println(cus.getLastName());
//            System.out.println(pk);
//        }

        re.forEach(System.out::println);
    }
}