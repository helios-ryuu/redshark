package com.helios.redshark.core

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    // --- Business rule violations ---

    /** Thrown when a user already has 20 active (OPEN/IN_PROGRESS) issues. */
    class IssueLimitExceededException(limit: Int = 20) :
        AppException("Đạt giới hạn $limit issue active. Không được vượt quá 20.")

    /** Thrown when the requested state transition violates the issue state machine. */
    class InvalidStateTransitionException(from: String, to: String) :
        AppException("Không thể chuyển trạng thái từ $from sang $to.")

    /** Thrown when input data fails domain validation rules. */
    class ValidationException(val field: String, detail: String) :
        AppException("[$field] $detail")

    /** Thrown when a resource is not found or has been soft-deleted. TC-C23 */
    class NotFoundException(resource: String) :
        AppException("$resource không tồn tại hoặc đã bị xóa.")

    /** Thrown when trying to create an issue on a non-ACTIVE idea. TC-C08 */
    class IdeaNotActiveException :
        AppException("Không thể tạo issue trên ý tưởng đã đóng hoặc đã hủy.")

    // --- Auth ---

    class UnauthorizedException :
        AppException("Bạn không có quyền thực hiện thao tác này.")

    // --- Network / remote ---

    class NetworkException(cause: Throwable? = null) :
        AppException("Lỗi kết nối mạng.", cause)

    class RemoteException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    // --- Catch-all ---

    class UnknownException(cause: Throwable? = null) :
        AppException("Đã xảy ra lỗi không xác định.", cause)
}