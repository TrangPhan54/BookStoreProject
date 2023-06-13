package com.axonactive.PersonalProject.service.imple;

import com.axonactive.PersonalProject.entity.*;
import com.axonactive.PersonalProject.exception.LibraryException;
import com.axonactive.PersonalProject.repository.*;
import com.axonactive.PersonalProject.service.BorrowNoteDetailService;
import com.axonactive.PersonalProject.service.dto.BorrowNoteDetailDTO;
import com.axonactive.PersonalProject.service.dto.CustomerDTO;
import com.axonactive.PersonalProject.service.mapper.BookMapper;
import com.axonactive.PersonalProject.service.mapper.BorrowNoteDetailMapper;
import com.axonactive.PersonalProject.service.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BorrowNoteDetailServiceImplementation implements BorrowNoteDetailService {
    private final BorrowNoteDetailRepository borrowNoteDetailRepository;
    private final PhysicalBookRepository physicalBookRepository;
    private final BorrowNoteRepository borrowNoteRepository;

    private final BorrowNoteDetailMapper borrowNoteDetailMapper;

    private final CustomerRepository customerRepository;

    private final BookMapper bookMapper;
    private final CustomerMapper customerMapper;

    @Override
    public List<BorrowNoteDetailDTO> getAllBorrowNoteDetail() {
        List<BorrowNoteDetail> borrowNoteDetails = borrowNoteDetailRepository.findAll();
        return borrowNoteDetailMapper.toDtos(borrowNoteDetails);

    }

    @Override
    public BorrowNoteDetailDTO createBorrowNoteDetail(BorrowNoteDetailDTO borrowNoteDetailDTO, Long physicalBookID, Long orderID) {
        BorrowNoteDetail borrowNoteDetail = new BorrowNoteDetail();
        PhysicalBook physicalBook = physicalBookRepository.findById(physicalBookID).orElseThrow(LibraryException::PhysicalBookNotFound);
        BorrowNote note = borrowNoteRepository.findById(orderID).orElseThrow(LibraryException::BorrowNoteNotFound);
        borrowNoteDetail.setPhysicalBook(physicalBook);
        borrowNoteDetail.setBorrowNote(note);
        borrowNoteDetail = borrowNoteDetailRepository.save(borrowNoteDetail);


        return borrowNoteDetailMapper.toDto(borrowNoteDetail);
    }


    @Override
    public void deleteBorrowNoteDetailByID(Long borrowNoteDetailID) {
        BorrowNoteDetail borrowNoteDetail = borrowNoteDetailRepository.findById(borrowNoteDetailID).orElseThrow(LibraryException::BorrowNoteDetailNotFound);
        borrowNoteDetailRepository.delete(borrowNoteDetail);
    }

    @Override
    public BorrowNoteDetailDTO getBorrowNoteDetailId(Long borrowNoteDetailId) {
        return borrowNoteDetailMapper.toDto(borrowNoteDetailRepository.findById(borrowNoteDetailId).orElseThrow(LibraryException::BorrowNoteDetailNotFound));
    }
    //1. Danh sách số lượng sách mà khách hàng cụ thể hiện tại đang mượn

    @Override
    public Long getNumberOfBookByCustomerId(Long customerId) {
        List<BorrowNoteDetail> borrowNoteDetailList = borrowNoteDetailRepository.findAll();
        Long numberOfBorrowedBooks = borrowNoteDetailList.stream()
                .filter(brd -> brd.getBorrowNote().getCustomer().getId() == customerId).count();
        return numberOfBorrowedBooks;
    }
    //2. Tính số sách còn mượn sau khi trả của một khách hàng cụ thể

    @Override
    public Long customerReturnBook(Long customerId, Long numberOfBooksReturned) {
        log.info("request the number of return books by customer id {}", customerId);
        List<BorrowNoteDetail> borrowNoteDetailList = borrowNoteDetailRepository.findAll();
        List<BorrowNoteDetail> tempList = borrowNoteDetailList.stream()
                .filter(brd -> brd.getBorrowNote().getCustomer().getId() == customerId)
                .collect(Collectors.toList());
        Long numberOfRemainingBooks = borrowNoteDetailList.stream()
                .filter(brd -> brd.getBorrowNote().getCustomer().getId() == customerId).count() - numberOfBooksReturned;

        tempList.forEach(System.out::println);
        return numberOfRemainingBooks;

    }

    //3. Liệt kê tên các cuốn sách mà 1 khách hàng vẫn còn mượn sau khi trả trước những cuốn đọc rồi.
    @Override
    public List<String> nameOfBookRemaining(Long customerId, List<Long> physicalBookIds) {
        List<BorrowNoteDetail> borrowNoteDetailList = borrowNoteDetailRepository.findAll();
        List<BorrowNoteDetail> bookListOfCustomer = borrowNoteDetailList.stream()
                .filter(brd -> brd.getBorrowNote().getCustomer().getId() == customerId)
                .collect(Collectors.toList());
        for (int i = 0; i < bookListOfCustomer.size(); i++) {
            for (Long j : physicalBookIds) {
                if (bookListOfCustomer.get(i).getPhysicalBook().getId() == j) {
                    borrowNoteDetailRepository.deleteById(bookListOfCustomer.get(i).getId());
                }
            }
        }
        borrowNoteDetailList = borrowNoteDetailRepository.findAll();
        return borrowNoteDetailList.stream()
                .filter(brd -> brd.getBorrowNote().getCustomer().getId() == customerId)
                .map(brd -> brd.getPhysicalBook().getBook().getName())
                .collect(Collectors.toList());
    }

    // 4. Dịch vụ trả sách. Nếu trả trễ từ 5 cuốn trở lên thì tiến hành khóa tài khoản
    @Override
    public void returnBookByCustomer(Long customerId, List<Long> physicalBookIds) {
        List<BorrowNoteDetail> borrowNoteDetailList = borrowNoteDetailRepository.findAll();
        List<BorrowNoteDetail> bookListOfCustomer = borrowNoteDetailList.stream()
                .filter(brd -> brd.getBorrowNote().getCustomer().getId() == customerId)
                .collect(Collectors.toList());
        Customer customer = customerRepository.findById(customerId).orElseThrow(LibraryException::CustomerNotFound);
        for (int i = 0; i < bookListOfCustomer.size(); i++) {
            for (Long j : physicalBookIds) {
                if (bookListOfCustomer.get(i).getPhysicalBook().getId() == j) {
                    if (bookListOfCustomer.get(i).getReturnDate().isAfter(bookListOfCustomer.get(i).getBorrowNote().getDueDate())) {
                        if (customer.getNumberOfTimeReturnLate() < 5) {
                            customer.setNumberOfTimeReturnLate(customer.getNumberOfTimeReturnLate() + 1);
                        }
                        if (customer.getNumberOfTimeReturnLate() >= 5) {
                            customer.setActive(false);
                        }
                    }
                    borrowNoteDetailRepository.deleteById(bookListOfCustomer.get(i).getId());
                }
            }
        }
        if (customer.getNumberOfTimeReturnLate() >= 5) {
            System.out.println("Customer is banned because you have exceeded 5 overdue returns.");
        }
        System.out.println(customer.getNumberOfTimeReturnLate());
    }

    // Tính tiền phạt nếu sách trả trễ so với hạn đã ghi trong phiếu mượn
    @Override
    public void feeFineForReturningBookLate(Long customerId, List<Long> bookIds) {
        List<BorrowNoteDetail> borrowNoteDetailList = borrowNoteDetailRepository.findAll();
        List<BorrowNoteDetail> bookListOfCustomer = borrowNoteDetailList.stream()
                .filter(brd -> brd.getBorrowNote().getCustomer().getId() == customerId)
                .collect(Collectors.toList());
        double totalFee = 0;
        for (int i = 0; i < bookListOfCustomer.size(); i++) {

            double baseFee = 2.0; // Base fee for overdue books
            double finePerDay = 0.5;
            for (Long j : bookIds) {
                if (bookListOfCustomer.get(i).getPhysicalBook().getId() == j) {
                    if (bookListOfCustomer.get(i).getReturnDate().isAfter(bookListOfCustomer.get(i).getBorrowNote().getDueDate())) {
                        Long overdueDays = ChronoUnit.DAYS.between(bookListOfCustomer.get(i).getBorrowNote().getDueDate(), bookListOfCustomer.get(i).getReturnDate());
                        double fine = baseFee + finePerDay * overdueDays;
                        if (overdueDays <= 7) {
                            bookListOfCustomer.get(i).setFineFee(fine);
                        } else {
                            double penaltyFactor = Math.pow(1.02, overdueDays - 7);
                            fine *= penaltyFactor;
                            bookListOfCustomer.get(i).setFineFee(fine);
                        }
                    }
                }
//                totalFee += bookListOfCustomer.get(i).getFineFee();
            }
            totalFee += bookListOfCustomer.get(i).getFineFee();
//            borrowNoteDetailRepository.deleteById(bookListOfCustomer.get(i).getId());
        }
        System.out.println("You have to pay $" + totalFee + " for returning book late.");
    }

    @Override
    public Long countNumberMaxBorrowBook(Long bookId) {
        return borrowNoteDetailRepository.countNumberMaxBorrowBook(bookId);
    }

    public String getBookNameByBookId(Long bookId) {
        Optional<BorrowNoteDetail> borrowNoteDetailListOfCustomer = borrowNoteDetailRepository.findAll().stream()
                .filter(brd -> brd.getPhysicalBook().getBook().getId() == bookId).findFirst();
        List<Book> bookOfCustomer = borrowNoteDetailListOfCustomer
                .stream().map(BorrowNoteDetail::getPhysicalBook).map(PhysicalBook::getBook)
                .collect(Collectors.toList());
        String bookName = bookOfCustomer.stream()
                .map(Book::getName)
                .collect(Collectors.joining(","));
        return bookName;
    }

    // Tim id nhung sach duoc muon nhieu nhat
    @Override
    public List<Long> maxBorrowBook() {
        return borrowNoteDetailRepository.maxBorrowBook();
    }
    // Tim ten nhung cuon sach duoc muon nhieu nhat

    @Override
    public List<Book> nameOfMaxBorrowBook() {
        return borrowNoteDetailRepository.nameOfMaxBorrowBook(maxBorrowBook());
    }

    // Tim id nhung sach duoc muon it nhat
    @Override
    public List<Long> minBorrowBook() {
        return borrowNoteDetailRepository.minBorrowBook();
    }

    // Tim ten nhung cuon sach duoc muon it nhat
    @Override
    public List<Book> nameOfMinBorrowBook() {
        return borrowNoteDetailRepository.nameOfMinBorrowBook(minBorrowBook());
    }


    @Override
    public Map<Book, Long> getMaxBorrowBook(LocalDate date1, LocalDate date2) {
        List<BorrowNoteDetail> brdListBetweenDates = borrowNoteDetailRepository.findByBorrowNoteBorrowDateBetween(date1, date2);
        List<Book> bookList = brdListBetweenDates.stream().map(BorrowNoteDetail::getPhysicalBook).map(PhysicalBook::getBook).collect(Collectors.toList());
        Map<Book, Long> booksWithPhysicalCopiedBorrowed = new HashMap<>();
        for (Book book : bookList) {
            booksWithPhysicalCopiedBorrowed.put(book, 0L);
        }
        for (Map.Entry<Book, Long> entry : booksWithPhysicalCopiedBorrowed.entrySet()) {
            Book key = entry.getKey();
            Long value = entry.getValue();
            for (Book book : bookList) {
                if (Objects.equals(book.getId(), key.getId())) {
                    value++;
                    entry.setValue(value);
                }
            }
        }
        Map<Book, Long> result = booksWithPhysicalCopiedBorrowed.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        return result;
    }

    @Override
    public Map<Customer, Long> getMaxCustomer(LocalDate date1, LocalDate date2) {
        List<BorrowNoteDetail> borrowNoteDetailList = borrowNoteDetailRepository.findByBorrowNoteBorrowDateBetween(date1,date2);
        List<Customer> customers = borrowNoteDetailList.stream().map(BorrowNoteDetail::getBorrowNote).map(BorrowNote::getCustomer).collect(Collectors.toList());
        Map<Customer,Long> customersWithNumOfTimeBorrow = new HashMap<>();
        for (Customer customer: customers){
            customersWithNumOfTimeBorrow.put(customer,0L);
        }
        for (Map.Entry<Customer,Long> entry: customersWithNumOfTimeBorrow.entrySet()){
            Customer key = entry.getKey();
            Long value = entry.getValue();
            for (Customer customer: customers){
                if (Objects.equals(customer.getId(), key.getId())){
                    value++;
                    entry.setValue(value);
                }
            }
        }
        Map<Customer,Long> result = customersWithNumOfTimeBorrow.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry:: getKey, Map.Entry::getValue,
                        (a, b) -> a,LinkedHashMap::new));
        return result;

    }




}
