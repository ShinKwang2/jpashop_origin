package jpabook.jpashop.controller;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public String memberList(Model model) {
        List<Member> members = memberService.findMembers();
        model.addAttribute("members", members);
        return "members/memberList";
    }

    @GetMapping("/{memberId}")
    public String member(@PathVariable Long memberId, Model model) {
        Member member = memberService.findOne(memberId);

        // 멤버 폼에 멤버 집어넣기
        MemberForm memberForm = new MemberForm();
        memberToMemberForm(member, memberForm);

        model.addAttribute("memberForm", memberForm);
        return "members/member";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("memberForm", new MemberForm());
        return "members/createMemberForm";
    }

    /**
     * PRG : POST - REDIRECT - GET
     */
    @PostMapping("/new")
    public String create(@Valid @ModelAttribute MemberForm form, BindingResult result, RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "members/createMemberForm";
        }

        Address address = new Address(form.getCity(), form.getStreet(), form.getZipcode());

        Member member = new Member();
        member.setName(form.getName());
        member.setAddress(address);

        Long memberId = memberService.join(member);
        redirectAttributes.addAttribute("memberId", memberId);
        redirectAttributes.addAttribute("status", true);
        return "redirect:/members/{memberId}";
    }

    @GetMapping("/{memberId}/edit")
    public String editForm(@PathVariable Long memberId, Model model) {
        Member member = memberService.findOne(memberId);

        MemberForm memberForm = new MemberForm();
        memberToMemberForm(member, memberForm);

        model.addAttribute("memberForm", memberForm);
        return "members/memberEdit";
    }

    /**
     * 수정해야함... 아직 em 으로 수정하는 방법 모름..
     */
    @ResponseBody
    @PostMapping("/{memberId}/edit")
    public String edit(@PathVariable Long memberId, @ModelAttribute MemberForm memberForm) {
        Member member = memberService.findOne(memberId);

        return "OK";
    }

    private void memberToMemberForm(Member member, MemberForm memberForm) {
        // id
        memberForm.setId(member.getId());
        // 이름
        memberForm.setName(member.getName());
        // 주소
        Address memberAddress = member.getAddress();
        memberForm.setCity(memberAddress.getCity());
        memberForm.setStreet(memberAddress.getStreet());
        memberForm.setZipcode(memberAddress.getZipcode());
    }
}
