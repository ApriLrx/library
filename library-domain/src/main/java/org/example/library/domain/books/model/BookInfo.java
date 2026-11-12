package org.example.library.domain.books.model;

import lombok.Value;

import java.io.Serializable;

/** 书目信息值对象：书名、作者、出版社、出版年份、简介、封面。 */
@Value
public class BookInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    String name;
    String author;
    String publisher;
    Integer publishYear;
    String description;
    String coverUrl;
}
