package org.example.library.biz.books;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageResult;
import org.example.library.domain.books.model.Book;
import org.example.library.domain.books.model.BookInfo;
import org.example.library.domain.books.model.Isbn;
import org.example.library.domain.books.repository.BookQuery;
import org.example.library.domain.books.repository.BookRepository;
import org.example.library.domain.books.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 书目应用服务：读者查询 + 管理端写操作。
 */
@Service
public class BookApplicationService {

    private final BookRepository bookRepository;

    private final CategoryRepository categoryRepository;

    public BookApplicationService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public PageResult<Book> search(BookQuery query) {
        return bookRepository.search(query);
    }

    /** 管理端：分页查询全部书目（含下架）。 */
    public PageResult<Book> listAll(String keyword, int page, int pageSize) {
        return bookRepository.findAll(keyword, page, pageSize);
    }

    public Optional<Book> detail(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional
    public Long createBook(String isbn, String name, String author, String publisher,
                           Integer publishYear, String description, String coverUrl,
                           Long categoryId, int totalCount) {
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));
        if (bookRepository.existsByIsbn(isbn)) {
            throw new BusinessException(ErrorCode.RESOURCE_EXISTS, "ISBN 已存在");
        }
        Book book = Book.createNew(
                new Isbn(isbn),
                new BookInfo(name, author, publisher, publishYear, description, coverUrl),
                categoryId, totalCount);
        return bookRepository.create(book);
    }

    @Transactional
    public void updateBook(Long id, String name, String author, String publisher,
                           Integer publishYear, String description, String coverUrl,
                           Long categoryId) {
        Book existing = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "书籍不存在"));
        categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "分类不存在"));
        Book updated = existing.withInfo(
                new BookInfo(name, author, publisher, publishYear, description, coverUrl), categoryId);
        bookRepository.update(updated);
    }

    @Transactional
    public void updateEnabled(Long id, boolean enabled) {
        bookRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "书籍不存在"));
        bookRepository.updateEnabled(id, enabled);
    }
}
