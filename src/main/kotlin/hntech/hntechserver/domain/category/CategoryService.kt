package hntech.hntechserver.domain.category

import hntech.hntechserver.config.ARCHIVE
import hntech.hntechserver.config.MAX_MAIN_CATEGORY_COUNT
import hntech.hntechserver.config.PRODUCT
import hntech.hntechserver.domain.file.FileService
import hntech.hntechserver.utils.logging.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CategoryService(
    private val categoryRepository: CategoryRepository,
    private val fileService: FileService
) {
    val log = logger()

    // 카테고리명 중복 체크
    private fun checkCategoryName(name: String) {
        if (categoryRepository.existsByCategoryName(name)) throw CategoryException(DUPLICATE_CATEGORY_NAME)
    }

    // 메인에 등록된 카테고리 개수 체크
    private fun checkMainCategoryCount() {
        if (categoryRepository.countMainCategories() >= MAX_MAIN_CATEGORY_COUNT)
            throw CategoryException(MAXIMUM_NUMBER_OF_CATEGORIES)
    }
    
    // 마지막 순서의 카테고리 조회
    private fun getLastCategory(): Category? = categoryRepository.findFirstByOrderBySequenceDesc()

    // 카테고리 생성
    @Transactional
    fun createCategory(form: CreateCategoryForm): Category {
        checkCategoryName(form.categoryName)
        if (form.showInMain == "true") checkMainCategoryCount()

        return categoryRepository.save(
            Category(
                categoryName = form.categoryName,
                sequence = getLastCategory()?.let { it.sequence + 1 } ?: run { 1 },
                file = form.imageFileId?.let { fileService.getFile(it) },
                type = form.type,
                showInMain = form.showInMain
            )
        )
    }

    /**
     * 카테고리 조회
     */
    // 카테고리 전체 조회
    fun getAllCategories(): List<Category> {
        var result: MutableList<Category> = mutableListOf()
        result.addAll(getAllByType(ARCHIVE))
        result.addAll(getAllByType(PRODUCT))
        return result
    }

    // 타입으로 카테고리 전체 조회
    fun getAllByType(type: String): List<Category> = categoryRepository.findAllByType(type)

    // 카테고리 이름 전체 조회 (셀렉트 박스용)
    fun getAllNames(): List<String> =
        categoryRepository.findAllByType(PRODUCT)
            .union(categoryRepository.findAllByType(ARCHIVE))
            .map { it.categoryName }
            .toList()


    // 메인에 표시될 카테고리만 조회
    fun getMainCategories(): List<Category> = categoryRepository.findAllByShowInMain()
    
    // 카테고리 ID로 조회
    fun getCategory(id: Long): Category =
        categoryRepository.findById(id).orElseThrow { throw CategoryException(CATEGORY_NOT_FOUND) }
    
    // 카테고리 이름으로 조회
    fun getCategory(categoryName: String): Category =
        categoryRepository.findByCategoryName(categoryName) ?: throw CategoryException(CATEGORY_NOT_FOUND)

    /**
     * 카테고리 수정
     */
    // 카테고리 수정
    @Transactional
    fun updateCategory(categoryId: Long, form: UpdateCategoryForm): List<Category> {
        checkMainCategoryCount()

        val category: Category = getCategory(categoryId)

        // 수정하려는 이름이 현재 이름과 같지 않으면 이름 중복 체크
        if (category.categoryName != form.categoryName) checkCategoryName(form.categoryName)

        category.file?.let {
            fileService.deleteFile(category.file!!)
            category.file = null
        }

        category.update(
            categoryName = form.categoryName,
            showInMain = form.showInMain,
            file = form.imageServerFilename?.let { fileService.getFile(form.imageServerFilename!!) }
        )
        return getAllCategories()
    }
    
    /**
     * 카테고리 순서 변경
     * 바꿀 카테고리를 목표 카테고리의 앞에 위치시킨다.
     */
    @Transactional
    fun updateCategorySequence(form: UpdateCategorySequenceForm): List<Category> {
        val currentSequence: Int = getCategory(form.currentCategoryId).sequence
        var targetSequence: Int = when(form.targetCategoryId) {
            // 타겟 id가 0이면 맨 뒤로 보냄
            0L -> getLastCategory()!!.sequence + 1
            else -> getCategory(form.targetCategoryId).sequence
        }
        /**
         * 순서 변경 전 sequence 조정
         * 좌측으로 바꿀 경우 target의 우측 카테고리들의 sequence + 1
         * 우측으로 바꿀 경우 target의 좌측 카테고리들의 sequence - 1
         */
        if (currentSequence > targetSequence)
            categoryRepository.adjustSequenceToRight(targetSequence, currentSequence)
        else
            categoryRepository.adjustSequenceToLeft(currentSequence, targetSequence)

        if (form.targetCategoryId == 0L || currentSequence < targetSequence)
            targetSequence -= 1
        // 바꿀 카테고리의 sequence를 기존 targetCategory의 sequence로 변경
        getCategory(form.currentCategoryId).update(sequence = targetSequence)

        return getAllCategories()
    }

    // 카테고리 삭제
    @Transactional
    fun deleteCategory(categoryId: Long): Boolean {
        val findCategory = getCategory(categoryId)
        
        // 카테고리에 물려있는 파일들 삭제
        findCategory.file?.let { fileService.deleteFile(it) }
//        findCategory.archives.forEach { fileService.deleteAllFiles(it.files) }
        findCategory.products.forEach { fileService.deleteAllFiles(it.files) }

        // 카테고리 순서 조정
        categoryRepository.adjustSequenceToLeftAll(findCategory.sequence)

        categoryRepository.deleteById(categoryId)
        return true
    }
}