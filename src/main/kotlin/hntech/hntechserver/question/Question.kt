package hntech.hntechserver.question

import hntech.hntechserver.comment.Comment
import hntech.hntechserver.utils.BaseTimeEntity
import javax.persistence.*

@Entity
@SequenceGenerator(
    name = "QUESTION_PK_GENERATOR",
    sequenceName = "QUESTION_SEQ",
    initialValue = 1,
    allocationSize = 50
)
class Question(
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "QUESTION_PK_GENERATOR")
    @Column(name = "question_id")
    var id: Long? = null,

    var writer: String = "",
    var password: String = "",
    var FAQ: String = "",
    var status: String = "대기중", // 대기중, 처리중, 답변완료

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL])
    var comments: MutableList<Comment> = mutableListOf(),

    var title: String = "",
    var content: String = "",
) : BaseTimeEntity() {

    fun update(
        title: String? = null,
        content: String? = null,
        status: String? = null,
        FAQ: String? = null,
    ) {
        title?.let { this.title = title }
        content?.let { this.content = content }
        status?.let { this.status = status }
        FAQ?.let { this.FAQ = FAQ }
    }

    fun addComment(comment: Comment) { this.comments.add(comment) }
}




