package org.example.library.domain.books.service;

import org.example.library.domain.books.model.Book;
import org.example.library.domain.books.model.BookCopy;
import org.example.library.domain.books.model.CopyStatus;
import org.example.library.domain.books.repository.BookRepository;

/**
 * 书目库存领域服务：封装「借出 / 归还副本」的聚合状态流转与持久化。
 * 库存不变式由 {@link Book} 聚合保证，本服务只负责把聚合变更落库。
 */
public class BookStockService {

    private final BookRepository bookRepository;

    public BookStockService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    /** 借出副本：从聚合中选中 AVAILABLE 副本置 BORROWED，同步可借数量，返回借出副本。 */
    public BookCopy lendCopy(Book book) {
        BookCopy copy = book.lend();
        bookRepository.updateCopyStatus(copy.getId(), CopyStatus.BORROWED);
        bookRepository.updateAvailableCount(book.getId(), book.getAvailableCount());
        return copy;
    }

    /** 归还副本：副本恢复 AVAILABLE，同步可借数量。 */
    public void returnCopy(Book book, Long copyId) {
        book.giveBack(copyId);
        bookRepository.updateCopyStatus(copyId, CopyStatus.AVAILABLE);
        bookRepository.updateAvailableCount(book.getId(), book.getAvailableCount());
    }
}
