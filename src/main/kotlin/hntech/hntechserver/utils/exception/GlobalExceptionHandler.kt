package hntech.hntechserver.utils.exception

import hntech.hntechserver.admin.AdminException
import hntech.hntechserver.category.CategoryException
import hntech.hntechserver.file.FileException
import hntech.hntechserver.product.ProductException
import hntech.hntechserver.question.CommentException
import hntech.hntechserver.question.QuestionException
import hntech.hntechserver.utils.badRequest
import hntech.hntechserver.utils.unauthorized
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import javax.security.auth.login.LoginException

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun globalErrorHandle(ex: Exception) = badRequest(ex)

    @ExceptionHandler(FileException::class)
    fun fileUploadErrorHandle(ex: FileException) = badRequest(ex)

    @ExceptionHandler(ValidationException::class)
    fun validationErrorHandle(ex: ValidationException) = badRequest(ex.bindingResult)

    @ExceptionHandler(QuestionException::class)
    fun questionExceptionHandler(ex: QuestionException) = badRequest(ex)

    @ExceptionHandler(CommentException::class)
    fun commentExceptionHandler(ex: CommentException) = badRequest(ex)

    @ExceptionHandler(CategoryException::class)
    fun categoryExceptionHandler(ex: CategoryException) = badRequest(ex)

    @ExceptionHandler(ProductException::class)
    fun productExceptionHandler(ex: ProductException) = badRequest(ex)

    @ExceptionHandler(AdminException::class)
    fun adminExceptionHandle(ex: AdminException) = badRequest(ex)

    @ExceptionHandler(LoginException::class)
    fun loginExceptionHandle(ex: LoginException) = unauthorized(ex)

}