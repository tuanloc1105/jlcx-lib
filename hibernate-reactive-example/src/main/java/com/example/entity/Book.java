package com.example.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Book {
    @Basic(fetch = LAZY)
    public byte[] coverImage;
    @Id
    @GeneratedValue
    private Integer id;
    private String isbn;
    private String title;
    @Basic(fetch = LAZY)
    private LocalDate published;
    @ManyToOne(fetch = LAZY)
    private Author author;

    public Book(String isbn, String title, Author author, LocalDate published) {
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.published = published;
        this.coverImage = ("Cover image for '" + title + "'").getBytes();
    }

}
