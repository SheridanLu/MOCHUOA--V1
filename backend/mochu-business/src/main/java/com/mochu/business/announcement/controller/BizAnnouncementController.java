package com.mochu.business.announcement.controller;

import com.mochu.business.announcement.dto.AnnouncementCreateDTO;
import com.mochu.business.announcement.service.BizAnnouncementService;
import com.mochu.business.announcement.vo.AnnouncementVO;
import com.mochu.business.announcement.vo.NotificationVO;
import com.mochu.common.result.PageResult;
import com.mochu.common.result.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/announcements")
@RequiredArgsConstructor
public class BizAnnouncementController {

    private final BizAnnouncementService announcementService;

    @GetMapping
    @PreAuthorize("hasAuthority('announcement:view')")
    public R<PageResult<AnnouncementVO>> listAnnouncements(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(announcementService.listAnnouncements(type, status, page, size));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('announcement:create')")
    public R<Long> createAnnouncement(@Valid @RequestBody AnnouncementCreateDTO dto) {
        return R.ok(announcementService.createAnnouncement(dto));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('announcement:manage')")
    public R<Void> publishAnnouncement(@PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return R.ok();
    }

    @PostMapping("/{id}/offline")
    @PreAuthorize("hasAuthority('announcement:manage')")
    public R<Void> offlineAnnouncement(@PathVariable Long id) {
        announcementService.offlineAnnouncement(id);
        return R.ok();
    }

    // ==================== NOTIFICATIONS ====================

    @GetMapping("/notifications")
    public R<PageResult<NotificationVO>> listMyNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return R.ok(announcementService.listMyNotifications(page, size));
    }

    @PostMapping("/notifications/{id}/read")
    public R<Void> markNotificationRead(@PathVariable Long id) {
        announcementService.markNotificationRead(id);
        return R.ok();
    }

    @PostMapping("/notifications/read-all")
    public R<Void> markAllRead() {
        announcementService.markAllRead();
        return R.ok();
    }

    @GetMapping("/notifications/unread-count")
    public R<Long> getUnreadCount() {
        return R.ok(announcementService.getUnreadCount());
    }
}
