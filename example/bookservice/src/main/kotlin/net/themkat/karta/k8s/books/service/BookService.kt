package net.themkat.karta.k8s.books.service

import net.themkat.karta.k8s.books.model.Book
import net.themkat.karta.k8s.books.repository.BookRepository
import org.springframework.stereotype.Service

@Service
class BookService(val bookRepository: BookRepository) {
    // if you play around with this example, you might replace this with an actual database :P
//    val bookDb = mutableListOf(
//            Book("Clean Code", "Robert C. Martin", "https://www.amazon.com/Clean-Code-Handbook-Software-Craftsmanship/dp/0132350882"),
//            Book("Game Engine Black Book: Wolfenstein 3D", "Fabien Sanglard", "https://www.amazon.com/Game-Engine-Black-Book-Wolfenstein/dp/1539692876"),
//            Book("Programming Kotlin", "Venkat Subramaniam", "https://www.amazon.com/Programming-Kotlin-Expressive-Performant-Applications/dp/1680506358"),
//            Book("Animal Farm", "George Orwell", "https://www.amazon.com/Animal-Farm-George-Orwell/dp/0451526341"))


    // toList because we return an immutable list :)
    fun getAllBooks() = bookRepository.findAll().toList()

    fun addBook(book: Book) {
        bookRepository.save(book)
    }

}