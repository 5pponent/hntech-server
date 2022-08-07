package hntech.hntechserver.admin

import hntech.hntechserver.file.File
import hntech.hntechserver.file.FileRepository
import hntech.hntechserver.file.FileService
import hntech.hntechserver.utils.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class AdminService(
    private val adminRepository: AdminRepository,
    private val fileService: FileService,
    private val fileRepository: FileRepository
    )
{
    val log = logger()

    fun getAdmin(): Admin {
        val adminResult = adminRepository.findAll()
        if (adminRepository.findAll().isEmpty()) throw AdminException("관리자 계정 조회 실패")
        else return adminResult[0]
    }

    /**
     * 관리자 생성
     */
    fun createAdmin(password: String): Admin {
        if (adminRepository.findAll().isNotEmpty()) throw AdminException("기존 관리자 존재. 중복 관리자 생성 불가능")
        fileRepository.save(File(originalFilename = "관리자용 더미 파일", serverFilename = ""))
        return adminRepository.save(Admin(password = password))
    }

    /**
     * 관리자 정보 수정
     */
    // 관리자 비밀번호 변경
    fun updatePassword(form: PasswordRequest): String {
        if (form.curPassword != form.curPasswordCheck) throw AdminException(ADMIN_PASSWORD_VALID_FAIL)
        getAdmin().update(newPassword = form.newPassword)
        return getAdmin().password
    }

    // 인사말 수정
    fun updateIntroduce(newIntroduce: String): String {
        val admin = getAdmin()
        admin.update(newIntroduce = newIntroduce)
        return admin.introduce
    }

    private fun updateImage(newImage: MultipartFile, serverFilename: String): String {
        val curImage = fileService.getFile(serverFilename)
        val savedFile = fileService.updateFile(curImage, newImage)
        return savedFile.serverFilename
    }

    // 조직도 수정
    fun updateOrgChart(newImage: MultipartFile): String {
        val admin = getAdmin()
        admin.update(newOrgChartImage = updateImage(newImage, admin.orgChartImage))
        return admin.orgChartImage
    }

    // CI 수정
    fun updateCI(newImage: MultipartFile): String {
        val admin = getAdmin()
        admin.update(newCompInfoImage = updateImage(newImage, admin.compInfoImage))
        return admin.compInfoImage
    }

    // 연혁 수정
    fun updateCompanyHistory(newImage: MultipartFile): String {
        val admin = getAdmin()
        admin.update(newHistoryImage = updateImage(newImage, admin.historyImage))
        return admin.historyImage
    }

    // 하단 (footer) 수정
    fun updateFooter(form: FooterDto): Admin =
        getAdmin().updateFooter(
            newAddress = form.address,
            newAS = form.afterService,
            newPhone = form.phone,
            newFax = form.fax
        )
}