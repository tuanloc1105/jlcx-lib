package com.example.mapper;

import com.example.entity.Book;
import com.example.http.request.CreateBookRequest;
import vn.com.lcx.common.annotation.mapper.MapperClass;

@MapperClass
public interface BookMapper {

    Book map(CreateBookRequest request);

}
