package jpabook.jpashop.api;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.repository.OrderRepository;
import jpabook.jpashop.repository.OrderSearch;
import jpabook.jpashop.repository.order.query.OrderFlatDto;
import jpabook.jpashop.repository.order.query.OrderItemQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryDto;
import jpabook.jpashop.repository.order.query.OrderQueryRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * V1. 엔티티 직접 노출
 *  - 엔티티가 변하면 API 스펙이 변함
 *  - 트랜잭션 안에서 지연 로딩 필요
 *  - 양방향 연관관계 문제
 *
 * V2. 엔티티를 조회해서 DTO 로 변환(fetch join 사용 X)
 *  - 트랜잭션 안에서 지연 로딩 필요
 *
 * V3. 엔티티를 조회해서 DTO 로 변환(fetch join 사용 O)
 *  - 기본적으로 collection fetch join 을 사용할 경우, 페이징 불가!
 *  - 페이징 시에는 N 부분을 포기해야 함(대신에 batch fetch size? 옵션 주면 N -> 1 쿼리로 변경 가능)
 *
 * V4. JPA 에서 DTO 로 바로 조회, 컬렉션 N 조회 (1 + N Query)
 *  - 페이징 가능
 * V5. JPA 에서 DTO 로 바로 조회, 컬렉션 1 조회 최적화 버전 (1 + 1 Query)
 *  - 페이징 가능
 * V6. JPA 에서 DTO로 바로 조회, 플랫 데이터(1Query) (1 Query)
 *  - 페이징 불가능
 */
@RequiredArgsConstructor
@RestController
public class OrderApiController {

    private final OrderRepository orderRepository;
    private final OrderQueryRepository orderQueryRepository;

    /**
     * V1. 엔티티 직접 노출
     *  - Hibernate5Module 모듈 등록, Lazy로 인한 proxy 클래스 null 처리
     *  - 양방향 관계 문제 발생 -> @JsonIgnore
     */
    @GetMapping("/api/v1/orders")
    public List<Order> ordersV1() {
        List<Order> all = orderRepository.findAllByString(new OrderSearch());
        for (Order order : all) {
            order.getMember().getName();    //Lazy 강제 초기화
            order.getDelivery().getAddress();   //Lazy 강제 초기화
            List<OrderItem> orderItems = order.getOrderItems(); //Lazy 강제 초기화
            orderItems.stream()
                    .forEach(o -> o.getItem().getName());   //Lazy 강제 초기화
        }
        return all;
    }

    @GetMapping("/api/v2/orders")
    public Result ordersV2() {
        List<Order> orders = orderRepository.findAllByString(new OrderSearch());
        List<OrderDto> collect = orders.stream()
                .map(OrderDto::new)
                .collect(Collectors.toList());

        return new Result(collect);
    }

    @GetMapping("/api/v3/orders")
    public Result ordersV3() {
        List<Order> orders = orderRepository.findAllWithItem();

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    /**
     * V3.1 엔티티를 조회해서 DTO 로 변환 페이징 고려
     *  - ToOne 관계만 우선 모두 페치 조인으로 최적화
     *  - 컬렉션 관게는 hibernate.default_batch_fetch_size, @BatchSize 로 최적화
     */
    @GetMapping("/api/v3.1/orders")
    public Result ordersV3_page(@RequestParam(value = "offset", defaultValue = "0") int offset,
                                @RequestParam(value = "limit", defaultValue = "100") int limit) {
        List<Order> orders = orderRepository.findAllWithMemberDelivery(offset, limit);

        List<OrderDto> collect = orders.stream()
                .map(o -> new OrderDto(o))
                .collect(Collectors.toList());
        return new Result(collect);
    }

    @GetMapping("/api/v4/orders")
    public Result ordersV4() {
        List<OrderQueryDto> orderQueryDtos = orderQueryRepository.findOrderQueryDtos();
        return new Result(orderQueryDtos);
    }

    @GetMapping("/api/v5/orders")
    public Result ordersV5() {
        List<OrderQueryDto> orderQueryDtos = orderQueryRepository.findAllByDto_optimization();
        return new Result(orderQueryDtos);
    }

    /**
     * 쿼리 : 딱 1번
     *  - 쿼리는 한 번이지만 조인으로 인해 DB에서 애플리케이션에 전달하는 데이터에 중복 데이터 추가
     *  - 페이징 불가능
     */
    @GetMapping("/api/v6/orders")
    public Result ordersV6() {
        List<OrderFlatDto> flats = orderQueryRepository.findAllByDto_flat();

        Comparator<OrderQueryDto> comparator = new Comparator<OrderQueryDto>() {
            @Override
            public int compare(OrderQueryDto o1, OrderQueryDto o2) {
                if (o1.getOrderId() > o2.getOrderId())
                    return 1;
                else if (o1.getOrderId() < o2.getOrderId())
                    return -1;
                else
                    return 0;
            }
        };

        List<OrderQueryDto> collect = flats.stream()
                .collect(Collectors.groupingBy(o -> new OrderQueryDto(
                                o.getOrderId(), o.getName(), o.getOrderDate(), o.getOrderStatus(), o.getAddress()),
                        Collectors.mapping(o -> new OrderItemQueryDto(o.getOrderId(), o.getItemName(), o.getOrderPrice(), o.getCount()),
                                Collectors.toList()))).entrySet().stream()
                .map(e -> new OrderQueryDto(
                        e.getKey().getOrderId(), e.getKey().getName(), e.getKey().getOrderDate(), e.getKey().getOrderStatus(), e.getKey().getAddress(), e.getValue()))
                .collect(Collectors.toList());

        collect.sort(comparator);



        return new Result(collect);
    }

    @Getter
    static class OrderDto {

        private Long orderId;
        private String name;
        private LocalDateTime orderDate;
        private OrderStatus orderStatus;
        private Address address;
        private List<OrderItemDto> orderItemDtos;

        public OrderDto(Order order) {
            orderId = order.getId();
            name = order.getMember().getName();
            orderDate = order.getOrderDate();
            orderStatus = order.getStatus();
            address = order.getDelivery().getAddress();

            orderItemDtos = order.getOrderItems().stream()
                    .map(orderItem -> new OrderItemDto(orderItem))
                    .collect(Collectors.toList());
        }
    }

    @Getter
    static class OrderItemDto {

        private String itemName;    //상품 명
        private int orderPrice;     //주문 가격
        private int count;          //주문 수량

        public OrderItemDto(OrderItem orderItem) {
            itemName = orderItem.getItem().getName();
            orderPrice = orderItem.getOrderPrice();
            count = orderItem.getCount();
        }
    }

    @AllArgsConstructor
    @Getter
    static class Result<T> {
        private T data;
    }
}
