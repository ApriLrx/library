package org.example.library.biz.favorite;

import org.example.library.common.BusinessException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageResult;
import org.example.library.domain.books.repository.BookRepository;
import org.example.library.domain.favorite.model.Favorite;
import org.example.library.domain.favorite.repository.FavoriteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏应用服务。
 */
@Service
public class FavoriteApplicationService {

    private final FavoriteRepository favoriteRepository;

    private final BookRepository bookRepository;

    public FavoriteApplicationService(FavoriteRepository favoriteRepository, BookRepository bookRepository) {
        this.favoriteRepository = favoriteRepository;
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void add(Long userId, Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "书籍不存在"));
        if (favoriteRepository.exists(userId, bookId)) {
            throw new BusinessException(ErrorCode.RESOURCE_EXISTS, "已收藏该书");
        }
        favoriteRepository.add(Favorite.builder().userId(userId).bookId(bookId).build());
    }

    @Transactional
    public void remove(Long userId, Long bookId) {
        favoriteRepository.remove(userId, bookId);
    }

    public PageResult<Favorite> listMy(Long userId, int page, int pageSize) {
        return favoriteRepository.findByUser(userId, page, pageSize);
    }

    /** 当前用户收藏的书目 id 列表（前端按钮态用）。 */
    public List<Long> myBookIds(Long userId) {
        return favoriteRepository.findBookIdsByUser(userId);
    }
}
