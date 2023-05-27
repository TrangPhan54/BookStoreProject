package com.axonactive.PersonalProject.service;

import com.axonactive.PersonalProject.entity.Status;
import com.axonactive.PersonalProject.service.dto.BookContentDTO;
import com.axonactive.PersonalProject.service.dto.BookDTO;

import java.util.List;

public interface BookService {
    List<BookDTO> getAllBook ();

    BookDTO createBook (BookDTO bookDTO, Long publishingHouseID, Long authorID);

    BookDTO updateBook (Long bookID, BookDTO bookDTO);
    void deleteBookById (Long bookID);

    BookDTO getBookById (Long bookID);
    // 1.Tim ten sach co chua ki tu gan dung
    List<BookDTO> getByBookNameContainingIgnoreCase (String keyword);

    // 2. Tim sach boi trang thai (available or unavailable)
    List<BookDTO> getByStatus (Status status);
    // 3. Tim sach boi ten nha xuat ban

    List<BookDTO> getBookByPublishingHouseName (String publishingHouseName);
    // 4. Tim sach boi ten tac gia
    List<BookDTO> getBookByAuthorFirstName (String authorFirstName);

    // 5. Tim sach boi ho tac gia
    List<BookDTO> getBookByAuthorLastName (String authorLastName);
    // 6. Tim tom tat noi dung sach thong qua ten sach
    BookContentDTO findContentSummaryByBookName (String bookName);
}
