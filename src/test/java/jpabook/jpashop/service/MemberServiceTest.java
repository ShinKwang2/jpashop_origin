package jpabook.jpashop.service;

import jpabook.jpashop.domain.Member;
import jpabook.jpashop.repository.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberServiceTest {

    @Autowired
    MemberService memberService;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    EntityManager em;

    @Test
    @Rollback(false)
    void 회원가입() throws Exception {
        //given
        Member member = new Member();
        member.setName("lee");
        
        //when
        Long savedId = memberService.join(member);

        //then
        em.flush();
        assertThat(member).isEqualTo(memberRepository.findOne(savedId));

     }

     @Test
     void 중복_회원_예외() throws Exception {
         //given
         Member member1 = new Member();
         member1.setName("lee");

         Member member2 = new Member();
         member2.setName("lee");

         Member member3 = new Member();
         member3.setName("kim");
         
         //when
         memberService.join(member1);
         
         //then
         assertThrows(IllegalStateException.class, () -> memberService.join(member2));
      }
}