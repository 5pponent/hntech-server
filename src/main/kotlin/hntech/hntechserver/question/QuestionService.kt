package hntech.hntechserver.question

import hntech.hntechserver.question.dto.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class QuestionService(
    private val questionRepository: QuestionRepository,
    private val commentRepository: CommentRepository,
    private val questionManager: QuestionManager
) {
    private fun getQuestion(questionId: Long): Question =
        questionRepository.findById(questionId).orElseThrow { throw QuestionException(QUESTION_NOT_FOUND) }

    private fun getQuestionByIdAndPassword(questionId: Long, password: String): Question =
        questionRepository.findByIdAndPassword(questionId, password).orElseThrow {
            QuestionException(QUESTION_NOT_FOUND)
        }

    @Transactional
    fun createQuestion(form: QuestionCreateForm): Question {
        val question = questionRepository.save(convertEntity(form))
        questionManager.addNewQuestion(question)
        return question
    }
    
    // 전체 문의사항 (메일 테스트용, 이후 오늘 작성된 문의사항만 주도록 변경해야함)
    fun findAllQuestions(): List<Question> =
        questionRepository.findAll()

    // 전체 문의사항 페이징해서 간략 포맷 반환
    fun findAllQuestions(pageable: Pageable): Page<Question> =
        questionRepository.findAll(pageable)

    // 작성한 비밀번호로 해당 문의사항 조회
    fun findQuestionByIdAndPassword(id: Long, password: String): QuestionCompleteResponse {
        val question = getQuestionByIdAndPassword(id, password)
        val comments = commentRepository.findAllByQuestionId(id).map { CommentResponse(it) }
        return QuestionCompleteResponse(question, comments)
    }

    // 문의사항 제목, 내용 수정
    @Transactional
    fun updateQuestion(id: Long, form: QuestionUpdateForm): Question {
        val question = getQuestion(id)
        question.update(form.title, form.content)
        return question
    }
    
    // 문의사항 처리 상태 수정
    @Transactional
    fun updateQuestion(id: Long, form: QuestionStatusUpdateForm): Question {
        val question = getQuestion(id)
        question.update(form.isFAQ)
        return question
    }
    
    // 문의사항 삭제
    @Transactional
    fun deleteQuestion(id: Long) =
        questionRepository.delete(getQuestion(id))
}

@Service
@Transactional
class CommentService(
    private val questionRepository: QuestionRepository,
    private val commentRepository: CommentRepository,
    private val questionManager: QuestionManager
) {
    private fun getQuestion(questionId: Long): Question =
        questionRepository.findById(questionId).orElseThrow { throw QuestionException(QUESTION_NOT_FOUND) }

    fun createComment(questionId: Long, form: CommentCreateForm): Comment {
        val question = getQuestion(questionId)
        val comment = commentRepository.save(convertEntity(form, question))
        question.addComment(comment)
        // 클라이언트가 새 댓글 등록 시 메일로 보낼 문의사항 리스트에 추가
        if (form.writer != "관리자")
            questionManager.addNewCommentQuestion(question)
        return comment
    }

    @Transactional(readOnly = true)
    fun getComment(commentId: Long): Comment =
        commentRepository.findById(commentId).orElseThrow { throw CommentException(COMMENT_NOT_FOUND) }

    fun updateComment(commentId: Long, form: CommentUpdateForm): Comment {
        val comment = getComment(commentId)
        comment.update(form.content)
        return comment
    }

    fun deleteComment(commentId: Long) {
        val comment = getComment(commentId)
        commentRepository.delete(comment)
    }
}