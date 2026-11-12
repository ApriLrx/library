package org.example.library.storage.repository;

import org.example.library.common.PageQuery;
import org.example.library.common.PageResult;
import org.example.library.domain.books.model.Book;
import org.example.library.domain.books.model.BookCopy;
import org.example.library.domain.books.model.BookInfo;
import org.example.library.domain.books.model.Category;
import org.example.library.domain.books.model.CopyStatus;
import org.example.library.domain.books.model.Isbn;
import org.example.library.domain.books.repository.BookQuery;
import org.example.library.domain.books.repository.BookRepository;
import org.example.library.storage.mapper.BookCopyMapper;
import org.example.library.storage.mapper.BookMapper;
import org.example.library.storage.po.BookCopyPO;
import org.example.library.storage.po.BookPO;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** 书目仓储实现（基础设施层）：只做对象映射与哑持久化，不含业务规则。 */
@Repository
public class BookRepositoryImpl implements BookRepository {

    private final BookMapper bookMapper;

    private final BookCopyMapper bookCopyMapper;

    public BookRepositoryImpl(BookMapper bookMapper, BookCopyMapper bookCopyMapper) {
        this.bookMapper = bookMapper;
        this.bookCopyMapper = bookCopyMapper;
    }

    @Override
    public PageResult<Book> search(BookQuery query) {
        boolean other = query.getCategoryId() != null && query.getCategoryId() == Category.OTHER_CATEGORY_ID;
        long total = bookMapper.countSearch(query.getCategoryId(), other, query.getKeyword(), query.getOnlyAvailable());
        List<Book> items = bookMapper.search(
                        query.getCategoryId(), other, query.getKeyword(), query.getOnlyAvailable(),
                        query.getOffset(), query.getEffectivePageSize())
                .stream().map(this::toDomain).collect(Collectors.toList());
        return PageResult.of(total, query.getNormalizedPage(), query.getEffectivePageSize(), items);
    }

    @Override
    public PageResult<Book> findAll(String keyword, int page, int pageSize) {
        PageQuery pq = PageQuery.of(page, pageSize);
        long total = bookMapper.countAll(keyword);
        List<Book> items = bookMapper.selectAll(keyword, pq.getOffset(), pq.getEffectivePageSize()).stream()
                .map(this::toDomain).collect(Collectors.toList());
        return PageResult.of(total, pq.getNormalizedPage(), pq.getEffectivePageSize(), items);
    }

    @Override
    public Optional<Book> findById(Long id) {
        BookPO po = bookMapper.selectById(id);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po, loadCopies(id)));
    }

    @Override
    public Optional<Book> findByIdForUpdate(Long id) {
        BookPO po = bookMapper.selectByIdForUpdate(id);
        if (po == null) {
            return Optional.empty();
        }
        return Optional.of(toDomain(po, loadCopiesForUpdate(id)));
    }

    @Override
    public boolean existsByIsbn(String isbn) {
        return bookMapper.countByIsbn(isbn) > 0;
    }

    @Override
    public boolean existsByIsbnExcludeId(String isbn, Long id) {
        return bookMapper.countByIsbnExcludeId(isbn, id) > 0;
    }

    @Override
    public Long create(Book book) {
        BookPO po = toPO(book);
        bookMapper.insert(po);
        Long bookId = po.getId();
        for (BookCopy copy : book.getCopies()) {
            BookCopyPO copyPO = new BookCopyPO();
            copyPO.setBookId(bookId);
            copyPO.setBarcode(copy.getBarcode());
            copyPO.setStatus(copy.getStatus().name());
            bookCopyMapper.insert(copyPO);
        }
        return bookId;
    }

    @Override
    public void update(Book book) {
        bookMapper.update(toPO(book));
    }

    @Override
    public void updateEnabled(Long id, boolean enabled) {
        bookMapper.updateEnabled(id, enabled ? 1 : 0);
    }

    @Override
    public void updateCopyStatus(Long copyId, CopyStatus status) {
        bookCopyMapper.updateStatus(copyId, status.name());
    }

    @Override
    public void updateAvailableCount(Long bookId, int availableCount) {
        bookMapper.updateAvailableCount(bookId, availableCount);
    }

    private List<BookCopy> loadCopies(Long bookId) {
        return bookCopyMapper.selectByBookId(bookId).stream()
                .map(this::toCopyDomain).collect(Collectors.toList());
    }

    private List<BookCopy> loadCopiesForUpdate(Long bookId) {
        return bookCopyMapper.selectByBookIdForUpdate(bookId).stream()
                .map(this::toCopyDomain).collect(Collectors.toList());
    }

    private BookCopy toCopyDomain(BookCopyPO po) {
        return new BookCopy(po.getId(), po.getBarcode(), CopyStatus.valueOf(po.getStatus()));
    }

    private Book toDomain(BookPO po) {
        return toDomain(po, Collections.emptyList());
    }

    private Book toDomain(BookPO po, List<BookCopy> copies) {
        return Book.builder()
                .id(po.getId())
                .isbn(new Isbn(po.getIsbn()))
                .info(new BookInfo(po.getName(), po.getAuthor(), po.getPublisher(),
                        po.getPublishYear(), po.getDescription(), po.getCoverUrl()))
                .categoryId(po.getCategoryId())
                .totalCount(po.getTotalCount() == null ? 0 : po.getTotalCount())
                .availableCount(po.getAvailableCount() == null ? 0 : po.getAvailableCount())
                .enabled(po.getEnabled() != null && po.getEnabled() == 1)
                .copies(copies)
                .build();
    }

    private BookPO toPO(Book book) {
        BookPO po = new BookPO();
        po.setId(book.getId());
        po.setIsbn(book.getIsbn().getValue());
        po.setName(book.getInfo().getName());
        po.setAuthor(book.getInfo().getAuthor());
        po.setPublisher(book.getInfo().getPublisher());
        po.setPublishYear(book.getInfo().getPublishYear());
        po.setDescription(book.getInfo().getDescription());
        po.setCoverUrl(book.getInfo().getCoverUrl());
        po.setCategoryId(book.getCategoryId());
        po.setTotalCount(book.getTotalCount());
        po.setAvailableCount(book.getAvailableCount());
        po.setEnabled(book.isEnabled() ? 1 : 0);
        return po;
    }
}
