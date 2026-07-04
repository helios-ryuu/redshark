package com.helios.redshark.core.error

// File nay chuan hoa loi de cac tang xu ly thong nhat.

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    // --- Auth errors ---

    class AuthException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    class UserCancelledException(message: String = "User cancelled the operation") :
        AppException(message)

    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    class NoCredentialException(message: String = "No credential available") :
        AppException(message)

    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    class UnauthorizedException :
        AppException("Bạn không có quyền thực hiện thao tác này.")

    // --- Network / server ---

    class NetworkException(
        message: String = "Lỗi kết nối mạng.",
        cause: Throwable? = null
    ) : AppException(message, cause) {
        constructor(cause: Throwable) : this("Lỗi kết nối mạng.", cause)
    }

    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    class ServerException(val code: Int, message: String) :
        AppException(message)

    // Khoi code nay tap trung mot nhiem vu cu the de cac noi khac de goi va de doc.
    class RemoteException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    // --- Validation ---

    /**
     * Auth use cases call [ValidationException] with a single message.
     * Content use cases call it with a (field, detail) pair — `field` is
     * preserved for UI mapping and the message is formatted as `[field] detail`.
     */
    class ValidationException(val field: String?, detail: String) :
        AppException(if (field != null) "[$field] $detail" else detail) {
        constructor(message: String) : this(null, message)
    }

    // --- Storage ---

    class StorageException(message: String, cause: Throwable? = null) :
        AppException(message, cause)

    // --- Domain rules (Content) ---

    /** Thrown when a user already has 20 active (OPEN/IN_PROGRESS) issues. */
    class IssueLimitExceededException(limit: Int = 20) :
        AppException("Đạt giới hạn $limit issue active. Không được vượt quá 20.")

    /** Thrown when the requested state transition violates the issue state machine. */
    class InvalidStateTransitionException(from: String, to: String) :
        AppException("Không thể chuyển trạng thái từ $from sang $to.")

    /** Thrown when a resource is not found or has been soft-deleted. TC-C23. */
    class NotFoundException(resource: String) :
        AppException("$resource không tồn tại hoặc đã bị xóa.")

    /** Thrown when trying to create an issue on a non-ACTIVE idea. TC-C08. */
    class IdeaNotActiveException :
        AppException("Không thể tạo issue trên ý tưởng đã đóng hoặc đã hủy.")

    /** Thrown when a unique constraint is violated (e.g., username/email already taken). */
    class ConflictException(message: String, val field: String? = null) : AppException(message)

    // --- Catch-all ---

    class UnknownException(
        message: String = "Đã xảy ra lỗi không xác định.",
        cause: Throwable? = null
    ) : AppException(message, cause) {
        constructor(cause: Throwable) : this("Đã xảy ra lỗi không xác định.", cause)
    }
}
