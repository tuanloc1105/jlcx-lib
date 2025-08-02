package com.example.controller;

import com.example.http.request.CreateBookRequest;
import com.example.service.BookService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import vn.com.lcx.common.annotation.Component;
import vn.com.lcx.vertx.base.annotation.process.Controller;
import vn.com.lcx.vertx.base.annotation.process.Post;
import vn.com.lcx.vertx.base.controller.ReactiveController;
import vn.com.lcx.vertx.base.http.response.CommonResponse;

@Component
@Controller(path = "/api")
@RequiredArgsConstructor
public class APIController extends ReactiveController {

    private final Gson gson;
    private final BookService bookService;

    @Post(path = "/create_book")
    public void createBook(RoutingContext ctx) {
        try {
            CreateBookRequest req = handleRequest(ctx, gson, new TypeToken<>() {
            });
            bookService.createBook(req).onSuccess(user -> {
                handleResponse(ctx, gson, new CommonResponse());
            }).onFailure(err -> {
                handleError(ctx, gson, err);
            });
        } catch (Throwable t) {
            handleError(ctx, gson, t);
        }
    }

}
