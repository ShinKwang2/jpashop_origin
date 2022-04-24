package jpabook.jpashop.domain;

import lombok.Getter;

import javax.persistence.Embeddable;

@Embeddable
@Getter
public class Address {

    // 값 타입은 기본적으로 Immutable로 설계
    // 따라서 생성할 때만 값을 세팅
    private String city;
    private String street;
    private String zipcode;

    protected  Address() {
    }

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }
}
