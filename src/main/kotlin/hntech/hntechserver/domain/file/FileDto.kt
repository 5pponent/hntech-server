package hntech.hntechserver.domain.file

data class FileResponse(
    var id: Long,
    var originalFilename: String,
    var serverFilename: String,
    var savedPath: String,
) {
    constructor(file: File): this(
        id = file.id!!,
        originalFilename = file.originalFilename,
        serverFilename = file.serverFilename,
        savedPath = file.savedPath,
    )
}

data class FileListResponse(
    var uploadedFiles: List<FileResponse>,
)

