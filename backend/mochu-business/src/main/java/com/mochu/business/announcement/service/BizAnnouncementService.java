package com.mochu.business.announcement.service;

import com.mochu.business.announcement.dto.AnnouncementCreateDTO;
import com.mochu.business.announcement.vo.AnnouncementVO;
import com.mochu.business.announcement.vo.NotificationVO;
import com.mochu.common.result.PageResult;

public interface BizAnnouncementService {
    PageResult<AnnouncementVO> listAnnouncements(Integer type, Integer status, int page, int size);
    Long createAnnouncement(AnnouncementCreateDTO dto);
    void publishAnnouncement(Long id);
    void offlineAnnouncement(Long id);
    PageResult<NotificationVO> listMyNotifications(int page, int size);
    void markNotificationRead(Long id);
    void markAllRead();
    Long getUnreadCount();
}
