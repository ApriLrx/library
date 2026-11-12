package org.example.library.web.controller;

import org.example.library.domain.user.model.Role;
import org.example.library.infra.auth.RequireRole;
import org.example.library.common.PageResult;
import org.example.library.common.Result;
import org.example.library.domain.favorite.model.Favorite;
import org.example.library.infra.auth.UserContext;
import org.example.library.biz.favorite.FavoriteApplicationService;
import org.example.library.web.dto.FavoriteRequest;
import org.example.library.web.vo.FavoriteVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/** 收藏接口（READER）。 */
@RequireRole(Role.READER)
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    private final FavoriteApplicationService favoriteApplicationService;

    public FavoriteController(FavoriteApplicationService favoriteApplicationService) {
        this.favoriteApplicationService = favoriteApplicationService;
    }

    @PostMapping
    public Result<Void> add(@RequestBody @Valid FavoriteRequest request) {
        favoriteApplicationService.add(UserContext.getUserId(), request.getBookId());
        return Result.success();
    }

    @DeleteMapping("/{bookId}")
    public Result<Void> remove(@PathVariable Long bookId) {
        favoriteApplicationService.remove(UserContext.getUserId(), bookId);
        return Result.success();
    }

    @GetMapping
    public Result<PageResult<FavoriteVO>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int pageSize) {
        PageResult<Favorite> favorites = favoriteApplicationService.listMy(UserContext.getUserId(), page, pageSize);
        return Result.success(PageResult.of(favorites.getTotal(), favorites.getPage(), favorites.getPageSize(),
                favorites.getItems().stream().map(FavoriteVO::from).collect(Collectors.toList())));
    }

    @GetMapping("/book-ids")
    public Result<List<Long>> myBookIds() {
        return Result.success(favoriteApplicationService.myBookIds(UserContext.getUserId()));
    }
}
