package hntech.hntechserver.comment

import hntech.hntechserver.question.Question
import hntech.hntechserver.utils.BaseTimeEntity
import javax.persistence.*

@Entity
class Comment (
    @Id @GeneratedValue
    @Column(name = "comment_id")
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    var question: Question,

    var writer: String = "",
    var sequence: Int = 0,

    // 중복되는 부분
    var content: String = "",
) : BaseTimeEntity() {

    fun update(content: String) {
        this.content = content
    }
}