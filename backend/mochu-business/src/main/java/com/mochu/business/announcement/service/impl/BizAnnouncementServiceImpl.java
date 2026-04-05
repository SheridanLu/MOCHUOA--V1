package com.mochu.business.announcement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.announcement.dto.AnnouncementCreateDTO;
import com.mochu.business.announcement.entity.BizMessageNotification;
import com.mochu.business.announcement.entity.SysAnnouncement;
import com.mochu.business.announcement.mapper.BizMessageNotificationMapper;
import com.mochu.business.announcement.mapper.SysAnnouncementMapper;
import com.mochu.business.announcement.service.BizAnnouncementService;
import com.mochu.business.announcement.vo.AnnouncementVO;
import com.mochu.business.announcement.vo.NotificationVO;
import com.mochu.common.exception.BusinessException;
import com.mochu.common.result.PageResult;
import com.mochu.common.utils.SecurityUtils;
import com.mochu.system.entity.SysUser;
import com.mochu.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizAnnouncementServiceImpl implements BizAnnouncementService {

    private final SysAnnouncementMapper announcementMapper;
    private final BizMessageNotificationMapper notificationMapper;
    private final SysUserMapper userMapper;

    private static final Map<Integer, String> TYPE_MAP = Map.of(
            1, "通知", 2, "公告", 3, "制度文件"
    );

    private static final Map<Integer, String> STATUS_MAP = Map.of(
            1, "草稿", 2, "已发布", 3, "已下线"
    );

    @Override
    @Transactional(readOnly = true)
    public PageResult<AnnouncementVO> listAnnouncements(Integer type, Integer status, int page, int size) {
        LambdaQueryWrapper<SysAnnouncement> wrapper = new LambdaQueryWrapper<>();
        if (type != null) {
            wrapper.eq(SysAnnouncement::getType, type);
        }
        if (status != null) {
            wrapper.eq(SysAnnouncement::getDeleted, status == 3 ? 1 : 0);
            if (status == 1) {
                wrapper.isNull(SysAnnouncement::getPublishAt)
                       .or(w -> w.gt(SysAnnouncement::getPublishAt, LocalDateTime.now()));
            } else if (status == 2) {
                wrapper.le(SysAnnouncement::getPublishAt, LocalDateTime.now());
            }
        } else {
            wrapper.eq(SysAnnouncement::getDeleted, 0);
        }
        wrapper.orderByDesc(SysAnnouncement::getCreatedAt);

        Page<SysAnnouncement> pageParam = new Page<>(page, size);
        Page<SysAnnouncement> result = announcementMapper.selectPage(pageParam, wrapper);

        List<AnnouncementVO> records = result.getRecords().stream()
                .map(this::toAnnouncementVO)
                .toList();

        return new PageResult<>(records, result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public Long createAnnouncement(AnnouncementCreateDTO dto) {
        SysAnnouncement announcement = new SysAnnouncement();
        announcement.setTitle(dto.getTitle());
        announcement.setContent(dto.getContent());
        announcement.setType(dto.getType());
        announcement.setPinned(dto.getPinned() != null ? dto.getPinned() : 0);
        announcement.setPublishAt(dto.getPublishAt());
        announcement.setCreatorId(SecurityUtils.getCurrentUserId());
        announcementMapper.insert(announcement);

        log.info("Announcement created: id={}, title={}", announcement.getId(), announcement.getTitle());
        return announcement.getId();
    }

    @Override
    @Transactional
    public void publishAnnouncement(Long id) {
        SysAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }

        // Set publishAt to now if not already set
        if (announcement.getPublishAt() == null) {
            announcement.setPublishAt(LocalDateTime.now());
        }
        announcementMapper.updateById(announcement);

        // Generate notifications for target users
        generateNotifications(announcement);

        log.info("Announcement published: id={}, title={}", id, announcement.getTitle());
    }

    @Override
    @Transactional
    public void offlineAnnouncement(Long id) {
        SysAnnouncement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(404, "公告不存在");
        }

        // Soft delete via logical deletion
        announcementMapper.deleteById(id);

        log.info("Announcement offline: id={}, title={}", id, announcement.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<NotificationVO> listMyNotifications(int page, int size) {
        Long userId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<BizMessageNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMessageNotification::getUserId, userId)
               .orderByDesc(BizMessageNotification::getCreatedAt);

        Page<BizMessageNotification> pageParam = new Page<>(page, size);
        Page<BizMessageNotification> result = notificationMapper.selectPage(pageParam, wrapper);

        List<NotificationVO> records = result.getRecords().stream()
                .map(this::toNotificationVO)
                .toList();

        return new PageResult<>(records, result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void markNotificationRead(Long id) {
        BizMessageNotification notification = notificationMapper.selectById(id);
        if (notification == null) {
            throw new BusinessException(404, "通知不存在");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此通知");
        }

        notification.setReadFlag(1);
        notificationMapper.updateById(notification);
    }

    @Override
    @Transactional
    public void markAllRead() {
        Long userId = SecurityUtils.getCurrentUserId();

        LambdaUpdateWrapper<BizMessageNotification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(BizMessageNotification::getUserId, userId)
               .eq(BizMessageNotification::getReadFlag, 0)
               .set(BizMessageNotification::getReadFlag, 1);
        notificationMapper.update(null, wrapper);

        log.info("All notifications marked as read for user: {}", userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount() {
        Long userId = SecurityUtils.getCurrentUserId();

        LambdaQueryWrapper<BizMessageNotification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BizMessageNotification::getUserId, userId)
               .eq(BizMessageNotification::getReadFlag, 0);

        return notificationMapper.selectCount(wrapper);
    }

    // ==================== HELPER METHODS ====================

    private void generateNotifications(SysAnnouncement announcement) {
        // Query all active users (deleted=0, status=1)
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getDeleted, 0)
                   .eq(SysUser::getStatus, 1);
        List<SysUser> users = userMapper.selectList(userWrapper);

        String typeName = TYPE_MAP.getOrDefault(announcement.getType(), "通知");
        LocalDateTime now = LocalDateTime.now();

        for (SysUser user : users) {
            BizMessageNotification notification = new BizMessageNotification();
            notification.setUserId(user.getId());
            notification.setTitle(announcement.getTitle());
            notification.setContent("您有一条新的" + typeName + "，请查看");
            notification.setModule("announcement");
            notification.setTargetId(announcement.getId());
            notification.setReadFlag(0);
            notification.setCreatedAt(now);
            notificationMapper.insert(notification);
        }

        log.info("Generated {} notifications for announcement: {}", users.size(), announcement.getId());
    }

    private AnnouncementVO toAnnouncementVO(SysAnnouncement entity) {
        AnnouncementVO vo = new AnnouncementVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setType(entity.getType());
        vo.setTypeName(TYPE_MAP.getOrDefault(entity.getType(), "未知"));
        vo.setPinned(entity.getPinned());
        vo.setPublishAt(entity.getPublishAt());
        vo.setCreatorName(getUserRealName(entity.getCreatorId()));
        vo.setCreatedAt(entity.getCreatedAt());

        // Determine status
        if (entity.getDeleted() != null && entity.getDeleted() == 1) {
            vo.setStatus(3);
        } else if (entity.getPublishAt() != null && !entity.getPublishAt().isAfter(LocalDateTime.now())) {
            vo.setStatus(2);
        } else {
            vo.setStatus(1);
        }
        vo.setStatusName(STATUS_MAP.getOrDefault(vo.getStatus(), "未知"));

        return vo;
    }

    private NotificationVO toNotificationVO(BizMessageNotification entity) {
        NotificationVO vo = new NotificationVO();
        vo.setId(entity.getId());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setModule(entity.getModule());
        vo.setTargetId(entity.getTargetId());
        vo.setReadFlag(entity.getReadFlag());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }

    private String getUserRealName(Long userId) {
        if (userId == null) return null;
        SysUser user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : null;
    }
}
