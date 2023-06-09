package com.axonactive.PersonalProject.service.imple;

import com.axonactive.PersonalProject.entity.Author;
import com.axonactive.PersonalProject.entity.Book;
import com.axonactive.PersonalProject.entity.PublishingHouse;
import com.axonactive.PersonalProject.entity.Status;
import com.axonactive.PersonalProject.exception.LibraryException;
import com.axonactive.PersonalProject.repository.AuthorRepository;
import com.axonactive.PersonalProject.repository.BookRepository;
import com.axonactive.PersonalProject.repository.PublishingHouseRepository;
import com.axonactive.PersonalProject.service.BookService;
import com.axonactive.PersonalProject.service.dto.AuthorDTO;
import com.axonactive.PersonalProject.service.dto.BookContentDTO;
import com.axonactive.PersonalProject.service.dto.BookDTO;
import com.axonactive.PersonalProject.service.mapper.BookMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.axonactive.PersonalProject.exception.BooleanMethod.isAlpha;


@Service
@Transactional
@RequiredArgsConstructor
public class BookServiceImplement implements BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final PublishingHouseRepository publishingHouseRepository;
    private final BookMapper bookMapper;

    @Override
    public List<BookDTO> getAllBook() {
        List<Book> books = bookRepository.findAll();
        return bookMapper.toDtos(books);
    }

    @Override
    public BookDTO createBook(BookDTO bookDTO, Long publishingHouseID, Long authorID) {
        Book book = new Book();
        book.setBookName(bookDTO.getBookName());
        book.setContentSummary(bookDTO.getContentSummary());
        book.setBookImage(bookDTO.getBookImage());
        book.setDatePublish(bookDTO.getDatePublish());
        Author author = authorRepository.findById(authorID).orElseThrow();
        PublishingHouse publishingHouse = publishingHouseRepository.findById(publishingHouseID).orElseThrow();
        book.setAuthor(author);
        book.setPublishingHouse(publishingHouse);
        book = bookRepository.save(book);
        return bookMapper.toDto(book);
    }


    @Override
    public BookDTO updateBook(Long bookID, BookDTO bookDTO) {
        Book book = bookRepository.findById(bookID).orElseThrow(LibraryException::BookNotFound);
        book.setBookName(bookDTO.getBookName());
        book.setContentSummary(bookDTO.getContentSummary());
        book.setBookImage(bookDTO.getBookImage());
        book.setDatePublish(bookDTO.getDatePublish());
        book.setPublishingHouse(publishingHouseRepository.findById(bookDTO.getPublishingHouseID()).orElseThrow(LibraryException::PublishingHouseNotFound));
        book.setAuthor(authorRepository.findById(bookDTO.getBookID()).orElseThrow());
        book = bookRepository.save(book);
        return bookMapper.toDto(book);
    }

    @Override
    public void deleteBookById(Long bookID) {
        Book book = bookRepository.findById(bookID).orElseThrow(LibraryException::BookNotFound);
        bookRepository.delete(book);

    }

    @Override
    public BookDTO getBookById(Long bookID) {
        return bookMapper.toDto(bookRepository.findById(bookID).orElseThrow(LibraryException::BookNotFound));
    }
    // 1. Tim sach co chua ki tu gan giong
    @Override
    public List<BookDTO> getByBookNameContainingIgnoreCase(String keyword) {
        return bookMapper.toDtos(bookRepository.findByBookNameContainingIgnoreCase(keyword));
    }
    // 2. Tim sach boi trang thai (available or unavailable)

    public List<BookDTO> getByStatus (Status status){
        return bookMapper.toDtos(bookRepository.findByStatus(status));
    }
    // 3. Tim sach boi ten nha xuat ban
    @Override
    public List<BookDTO> getBookByPublishingHouseName(String publishingHouseName) {
        return bookMapper.toDtos(bookRepository.findBookByPublishingHouseName(publishingHouseName));
    }
    // 4. Tim sach boi ten tac gia
    @Override
    public List<BookDTO> getBookByAuthorFirstName (String authorFirstName) {
        return bookMapper.toDtos(bookRepository.findBookByAuthorFirstName(authorFirstName));
    }
    //5 .Tim sach boi ho tac gia
    @Override
    public List<BookDTO> getBookByAuthorLastName(String authorLastName) {
        return bookMapper.toDtos(bookRepository.findBookByAuthorLastName(authorLastName));
    }

    @Override
    public BookContentDTO findContentSummaryByBookName(String bookName) {
        return bookRepository.findContentSummaryByBookName(bookName);
    }

    private void bookException(BookDTO bookDTO) {
        if (bookDTO.getBookName().isBlank() || !isAlpha(bookDTO.getBookName()))
            throw LibraryException.badRequest("WrongNameOfBookFormat", "Name Of Book Should only contains letters");
        if (bookDTO.getPricePerBook() < 0 || bookDTO.getPricePerBook().isNaN())
            throw LibraryException.badRequest("WrongValue","Price Must Be A Number And More Than 0");

        if (bookDTO.getBookImage().isBlank()){
            throw LibraryException.badRequest("WrongImage","Book Must Have An Image To Describe");
        }
        if (bookDTO.getContentSummary().isBlank()){
            throw LibraryException.badRequest("EmptySummary","Summary Must Have At Least 255 Characters");
        }
        if (bookDTO.getPricePerBook()<0){
            throw LibraryException.badRequest("WrongValue","Price Per Book Must Be More Than 0");
        }
        if (bookDTO.getDatePublish().isAfter(LocalDate.now()))
            throw LibraryException.badRequest("WrongDate","Date Publish Must Be Before Now");

    }
    private void authorException (AuthorDTO authorDTO){
        if (authorDTO.getAuthorLastName().isBlank() || authorDTO.getAuthorFirstName().isBlank()
        || !isAlpha(authorDTO.getAuthorLastName()) || !isAlpha(authorDTO.getAuthorFirstName())){
            throw LibraryException.badRequest("WrongNameFormat","Name Of Author Must Contains Only Letters And Cannot Be Empty");
        }
    }
    public double calculateFine(LocalDate returnDate, LocalDate dueDate) {
        if (returnDate.isBefore(dueDate)) {
            return 0; // No fine if returned before the due date
        }

        long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
        double finePerDay = 0.50; // Define your fine rate per day

        return daysOverdue * finePerDay;
    }
}
