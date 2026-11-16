package ru.prototype.meetup.book.jpa

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface AuthorEntityRepository : JpaRepository<AuthorEntity, Long> {
    fun findByName(name: String): AuthorEntity?
    fun findAllByNameIn(names: Collection<String>): List<AuthorEntity>
}

interface BookEntityRepository : JpaRepository<BookEntity, Long>, JpaSpecificationExecutor<BookEntity> {

    @EntityGraph(attributePaths = ["author"])
    fun findWithAuthorById(id: Long): BookEntity?

    /** Без @EntityGraph каждая книга на странице догрузит автора отдельным запросом (N+1). */
    @EntityGraph(attributePaths = ["author"])
    override fun findAll(pageable: Pageable): Page<BookEntity>

    @EntityGraph(attributePaths = ["author"])
    override fun findAll(spec: Specification<BookEntity>, pageable: Pageable): Page<BookEntity>
}
