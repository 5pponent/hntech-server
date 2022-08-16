package hntech.hntechserver.admin

import org.springframework.web.multipart.MultipartFile
import javax.validation.constraints.Email
import javax.validation.constraints.NotEmpty

const val CI = "ci"
const val ORG_CHART = "orgChart"
const val HISTORY = "companyHistory"
const val INTRODUCE = "introduce"

data class LoginForm(
    var password: String,
)

data class IntroduceDto(
    var newIntroduce: String,
)

data class UpdatePasswordForm(
    @field:NotEmpty
    var curPassword: String,

    @field:NotEmpty
    var curPasswordCheck: String,

    @field:NotEmpty
    var newPassword: String,
)

data class PasswordResponse(
    var newPassword: String,
)

data class AdminImageRequest(
    var where: String,
    var file: MultipartFile,
)

data class AdminImageResponse(
    var where: String,
    var updatedServerFilename: String,
)

data class UpdateEmailAccountForm(
    @field:Email
    var email: String,
    var password: String,
)

data class EmailSendingTimeResponse(
    var time: String,
)

data class FooterDto(
    var address: String,
    var afterService: String,
    var phone: String,
    var fax: String,
) {
    constructor(a: Admin) : this(a.address, a.afterService, a.phone, a.fax)
}