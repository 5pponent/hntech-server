package hntech.hntechserver.question

import com.fasterxml.jackson.annotation.JsonIdentityInfo
import com.fasterxml.jackson.annotation.ObjectIdGenerators.*
import hntech.hntechserver.utils.BaseTimeEntity
import javax.persistence.*

@Entity
@JsonIdentityInfo(generator = IntSequenceGenerator::class, property = "key")
class Question(
    @Id @GeneratedValue
    @Column(name = "question_id")
    var id: Long? = null,

    var writer: String = "",
    var password: String = "",
    var status: String = "", // 대기, 진행중, 완료

    @OneToMany(mappedBy = "question", cascade = [CascadeType.ALL])
    var comments: MutableList<Comment> = mutableListOf(),

    var title: String = "",
    var content: String = "",
) : BaseTimeEntity() {

    fun update(title: String, content: String) {
        this.title = title
        this.content = content
    }
    fun update(status: String) {
        this.status = status
    }
    fun addComment(comment: Comment) {
        this.comments.add(comment)
    }
}

@Entity
@JsonIdentityInfo(generator = IntSequenceGenerator::class, property = "key")
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


