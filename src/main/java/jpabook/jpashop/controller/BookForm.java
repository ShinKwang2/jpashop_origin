package jpabook.jpashop.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter @Setter
public class BookForm {

    private Long id;

    /**
     * 상품 공통 속성
     */
    @NotEmpty(message = "이름은 필수입니다.")
    private String name;

    private int price;
    private int stockQuantity;

    /**
     * Book 속성
     */
    private String author;
    private String isbn;

}
