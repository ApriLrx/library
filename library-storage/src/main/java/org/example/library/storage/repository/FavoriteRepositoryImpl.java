package org.example.library.storage.repository;

import org.example.library.common.DomainException;
import org.example.library.common.ErrorCode;
import org.example.library.common.PageQuery;
import org.example.library.common.PageResult;
import org.example.library.domain.favorite.model.Favorite;
import org.example.library.domain.favorite.repository.FavoriteRepository;
import org.example.library.storage.mapper.FavoriteMapper;
import org.example.library.storage.po.FavoritePO;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/** 收藏仓储实现（基础设施层）。 */
@Repository
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final FavoriteMapper favoriteMapper;

    public FavoriteRepositoryImpl(FavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    @Override
    public void add(Favorite favorite) {
        FavoritePO po = new FavoritePO();
        po.setUserId(favorite.getUserId());
        po.setBookId(favorite.getBookId());
        try {
            favoriteMapper.insert(po);
        } catch (DuplicateKeyException e) {
            // 收藏唯一索引（user_id, book_id）冲突
            throw new DomainException(ErrorCode.RESOURCE_EXISTS, "已收藏该书");
        }
    }

    @Override
    public void remove(Long userId, Long bookId) {
        favoriteMapper.delete(userId, bookId);
    }

    @Override
    public boolean exists(Long userId, Long bookId) {
        return favoriteMapper.countByUserAndBook(userId, bookId) > 0;
    }

    @Override
    public PageResult<Favorite> findByUser(Long userId, int page, int pageSize) {
        PageQuery pq = PageQuery.of(page, pageSize);
        long total = favoriteMapper.countByUser(userId);
        List<Favorite> items = favoriteMapper.selectByUser(userId, pq.getOffset(), pq.getEffectivePageSize()).stream()
                .map(this::toDomain).collect(Collectors.toList());
        return PageResult.of(total, pq.getNormalizedPage(), pq.getEffectivePageSize(), items);
    }

    @Override
    public List<Long> findUserIdsByBook(Long bookId) {
        return favoriteMapper.selectUserIdsByBook(bookId);
    }

    @Override
    public List<Long> findBookIdsByUser(Long userId) {
        return favoriteMapper.selectBookIdsByUser(userId);
    }

    private Favorite toDomain(FavoritePO po) {
        return Favorite.builder()
                .id(po.getId())
                .userId(po.getUserId())
                .bookId(po.getBookId())
                .bookName(po.getBookName())
                .author(po.getAuthor())
                .build();
    }
}
