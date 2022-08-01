package hntech.hntechserver.category

import org.springframework.web.multipart.MultipartFile
import javax.validation.constraints.NotBlank

data class CategoryRequest(
    @field:NotBlank
    var categoryName: String,
    var image: MultipartFile,
)

data class CategoryResponse(
    var id: Long,
    var categoryName: String,
    var categoryImagePath: String
)

fun convertDto(c: Category) =
    CategoryResponse(
        id = c.id!!,
        categoryName = c.categoryName,
        categoryImagePath = c.categoryImagePath
    )
