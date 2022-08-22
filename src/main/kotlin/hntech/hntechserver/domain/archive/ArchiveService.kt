package hntech.hntechserver.domain.archive

import com.querydsl.jpa.impl.JPAQueryFactory
import hntech.hntechserver.domain.archive.QArchive.archive
import hntech.hntechserver.domain.category.CategoryService
import hntech.hntechserver.config.MAX_NOTICE_NUM
import hntech.hntechserver.domain.file.FileService
import hntech.hntechserver.utils.logging.logger
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ArchiveService(
    private val archiveRepository: ArchiveRepository,
    private val categoryService: CategoryService,
    private val fileService: FileService,
    private val archiveFileRepository: ArchiveFileRepository,
    private val query: JPAQueryFactory
    ) {
    val log = logger()

    private fun checkNoticeable() {
        if (archiveRepository.countNotice() > MAX_NOTICE_NUM)
            throw ArchiveException(OVER_NOTICE_MAX_NUM)
    }

    /**
     * 자료실 글 생성
     */
    @Transactional
    fun createArchive(form: ArchiveForm): Archive {
        // 카테고리 가져오기
        val category = categoryService.getCategory(form.categoryName)

        // 공지사항 최대 개수 체크
        if (form.notice == "true") checkNoticeable()

        // 자료실 글 생성
        val archive = archiveRepository.save(Archive(
            title = form.title,
            notice = form.notice,
            content = form.content,
            category = category
        ))

        // 파일 지정하기 (파일이 없으면 수행 X)
        if (form.files.isNotEmpty()) {
            val files = form.files.map {
                val savedFile = fileService.getFile(it)
                archiveFileRepository.save(ArchiveFile(archive = archive, file = savedFile))
            }.toMutableList()
            archive.update(files = files)
        }

        return archive
    }

    /**
     * 자료 조회
     */
    // id 로 자료실 하나 조회
    fun getArchive(id: Long): Archive =
        archiveRepository.findById(id).orElseThrow { ArchiveException(ARCHIVE_NOT_FOUND) }

    // 동적 쿼리 사용 함수
    fun keywordContains(keyword: String?) =
        keyword?.let { archive.title.contains(it).or(archive.content.contains(it)) }
    fun categoryNameEq(categoryName: String?) =
        categoryName?.let { archive.category.categoryName.eq(it) }

    // 자료 목록 조회 (페이징)
    fun getArchives(pageable: Pageable, categoryName: String? = null, keyword: String? = null): PageImpl<Archive> {
        val result: List<Archive> = query
            .selectFrom(archive)
            .where(
                categoryNameEq(categoryName),
                keywordContains(keyword),
                archive.notice.eq("false")
            )
            .orderBy(archive.id.desc())
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val totalCount = query
            .select(archive.count())
            .from(archive)
            .where(
                categoryNameEq(categoryName),
                keywordContains(keyword)
            )
            .fetchOne()

        return PageImpl<Archive>(result, pageable, totalCount!!)
    }

    // 공지사항 모두 조회 (페이징)
    fun getAllNotice(): List<Archive> =
        archiveRepository.findAllNotice()

    /**
     * 자료 수정
     */
    @Transactional
    fun updateArchive(id: Long, form: ArchiveForm): Archive {
        val archive = getArchive(id)

        // 카테고리 가져오기
        val category = categoryService.getCategory(form.categoryName)

        // 공지사항 최대 개수 체크
        checkNoticeable()

        // 기존 오래된 파일들은 삭제
        fileService.deleteAllFiles(archive.files.map { it.file }.toMutableList())
        archiveFileRepository.deleteAll(archive.files)

        val files = form.files.map {
            val savedFile = fileService.getFile(it)
            archiveFileRepository.save(ArchiveFile(archive = archive, file = savedFile))
        }.toMutableList()

        archive.update(
            notice = form.notice,
            title = form.title,
            content = form.content,
            category = category,
            files = files
        )

        return archive
    }

    /**
     * 자료 삭제
     */
    @Transactional
    fun deleteArchive(id: Long): Boolean {
        val targetArchive = getArchive(id)
        fileService.deleteAllFiles(targetArchive.files.map { it.file }.toMutableList())
        archiveRepository.deleteById(id)
        return true
    }
}