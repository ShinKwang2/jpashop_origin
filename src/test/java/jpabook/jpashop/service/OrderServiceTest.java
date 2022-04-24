package jpabook.jpashop.service;

import jpabook.jpashop.domain.*;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;


    @Test
    void 상품주문() throws Exception {
        //given
        Member member = createMember("회원1", new Address("서울", "증산로", "123-123"));

        Item book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.ORDER).as("상품 주문시 상태는 ORDER");
        assertThat(getOrder.getOrderItems().size()).isEqualTo(1).as("주문한 상품 종류 수가 정확해야 한다");
        assertThat(getOrder.getTotalPrice()).isEqualTo(10000 * orderCount).as("주문 가격은 가격 * 수량이다.");
        assertThat(book.getStockQuantity()).isEqualTo(8).as("주문 수량만큼 재고가 줄어야 한다.");
    }


    @Test
    void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember("회원1", new Address("서울", "증산로", "123-123"));
        Item book = createBook("시골 JPA", 10000, 10);


        //when
        int orderCount = 11;

        //then
        /**
         * assertJ의 오류 검증 테스트
         */
        assertThatExceptionOfType(NotEnoughStockException.class).isThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount));
        assertThatThrownBy(() -> orderService.order(member.getId(), book.getId(), orderCount)).isInstanceOf(NotEnoughStockException.class);

        /**
         * junit 의 오류 검증 테스트
         */
        org.junit.jupiter.api.Assertions.assertThrows(NotEnoughStockException.class, () -> orderService.order(member.getId(), book.getId(), orderCount));



    }

    @Test
    void 주문취소() throws Exception {
        //given
        Member member = createMember("회원1", new Address("서울", "증산로", "123-123"));
        Item book = createBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), book.getId(), orderCount);

        //when
        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(getOrder.getStatus()).isEqualTo(OrderStatus.CANCEL).as("주문 취소시 상태는 CANCEL 이다.");
        assertThat(book.getStockQuantity()).isEqualTo(10).as("주문의 취소디ㅗㄴ 상품은 그만큼 재고가 증가해야 한다.");

    }

    private Item createBook(String name, int price, int stockQuantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember(String memberName, Address address) {
        Member member = new Member();
        member.setName(memberName);
        member.setAddress(address);
        em.persist(member);
        return member;
    }
}