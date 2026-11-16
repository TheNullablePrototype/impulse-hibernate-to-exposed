package ru.prototype.meetup.book.jpa

import jakarta.persistence.*
import ru.prototype.meetup.book.Genre

@Entity
@Table(name = "author")
class AuthorEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
)

@Entity
@Table(name = "book")
class BookEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var title: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    var author: AuthorEntity,
    @Enumerated(EnumType.STRING)
    var genre: Genre,
    var publishedYear: Int,
)
