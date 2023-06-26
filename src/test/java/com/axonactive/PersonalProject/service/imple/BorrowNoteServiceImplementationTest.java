package com.axonactive.PersonalProject.service.imple;

import com.axonactive.PersonalProject.service.BorrowNoteService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class BorrowNoteServiceImplementationTest {
    @Autowired
    private BorrowNoteService borrowNoteService;

    @Test
    void nameOfCustomerReturnBookLate() {
        List<String> tempList = borrowNoteService.nameOfCustomerReturnBookLate();
        tempList.forEach(System.out::println);
    }

    @Test
    void createBorrowNote() {

    }
}