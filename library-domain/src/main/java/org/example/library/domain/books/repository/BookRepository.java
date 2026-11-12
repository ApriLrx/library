package org.example.library.domain.books.repository;

import org.example.library.common.PageResult;
import org.example.library.domain.books.model.Book;
import org.example.library.domain.books.model.CopyStatus;

import java.util.Optional;

/** 书目仓储接口（领域层定义，基础设施层实现）。只提供简单持久化原语，不含业务规则。 */
public interface BookRepository {

    PageResult<Book> search(BookQuery query);

    /** 管理端：分页查询全部书目（含下架）。 */
    PageResult<Book> findAll(String keyword, int page, int pageSize);

    /** 按 id 载入聚合（含副本）。 */
    Optional<Book> findById(Long id);

    /** 锁定书目与其副本行（FOR UPDATE），供借阅/归还并发使用。 */
    Optional<Book> findByIdForUpdate(Long id);

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnExcludeId(String isbn, Long id);

    /** 新增书目（含副本），纯持久化，返回自增 id。 */
    Long create(Book book);

    /** 更新书目基本信息（不含库存/上下架）。 */
    void update(Book book);

    /** 上下架。 */
    void updateEnabled(Long id, boolean enabled);

    /** 更新副本状态（哑 SET）。 */
    void updateCopyStatus(Long copyId, CopyStatus status);

    /** 覆盖可借数量为领域计算值（哑 SET）。 */
    void updateAvailableCount(Long bookId, int availableCount);
}
