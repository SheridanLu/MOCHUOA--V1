package com.mochu.business.contact.controller;

import com.mochu.business.contact.service.BizContactService;
import com.mochu.business.contact.vo.ContactVO;
import com.mochu.common.result.R;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class BizContactController {

    private final BizContactService contactService;

    @GetMapping
    @PreAuthorize("hasAuthority('contact:view')")
    public R<List<ContactVO>> list(@RequestParam(required = false) Long deptId,
                                   @RequestParam(required = false) String keyword) {
        return R.ok(contactService.listByDept(deptId, keyword));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('contact:view')")
    public R<List<ContactVO>> search(@RequestParam String keyword) {
        return R.ok(contactService.search(keyword));
    }

    @PostMapping("/sync/{userId}")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<Void> syncFromUser(@PathVariable Long userId) {
        contactService.syncFromUser(userId);
        return R.ok();
    }

    @PostMapping("/hide/{userId}")
    @PreAuthorize("hasAuthority('hr:view')")
    public R<Void> hideByUser(@PathVariable Long userId) {
        contactService.hideByUser(userId);
        return R.ok();
    }
}
