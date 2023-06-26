package com.axonactive.PersonalProject.service.imple;

import com.axonactive.PersonalProject.service.ResponseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ResponseServiceImplementTest {
    @Autowired
    private ResponseService responseService;

    @Test
    void findResponseOfABookByBookName() {
        String res = responseService.findResponseOfABookByBookName("Gone with the wind");
        System.out.println(res);
    }

    @Test
    void getResponseOfABook() {
        String res = responseService.getResponseOfABook(10L);
        System.out.println(res);
    }
}