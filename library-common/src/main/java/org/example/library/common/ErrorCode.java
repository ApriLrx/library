package org.example.library.common;

/**
 * 统一错误码（见 readme「响应设计」）。code=0 表示成功，其余为业务/系统错误码，
 * 并与 HTTP 状态码对应（用于全局异常处理时返回正确的 HTTP 状态）。
 */
public enum ErrorCode {

    SUCCESS(0, "ok", 200),
    PARAM_ERROR(40001, "参数校验失败", 400),
    UNAUTHORIZED(40100, "未登录或令牌失效", 401),
    FORBIDDEN(40300, "无操作权限", 403),
    NOT_FOUND(40400, "资源不存在", 404),
    NO_STOCK(40901, "暂无可借库存", 400),
    ALREADY_RETURNED(40902, "借阅记录已归还，请勿重复操作", 400),
    USERNAME_EXISTS(40903, "用户名已存在", 400),
    RESOURCE_EXISTS(40904, "资源已存在", 400),
    SYSTEM_ERROR(50000, "服务异常", 500);

    private final int code;

    private final String message;

    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}
