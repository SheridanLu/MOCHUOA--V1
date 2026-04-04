package com.mochu.system.vo;

import lombok.Data;
import java.util.List;

@Data
public class HomeVO {
    private List<ShortcutVO> shortcuts;
    private List<TodoVO> todoList;
    private int todoCount;
    private List<AnnouncementVO> announcements;

    @Data
    public static class ShortcutVO {
        private String code;
        private String name;
        private String icon;
        private String path;
    }

    @Data
    public static class TodoVO {
        private Long id;
        private String title;
        private String module;
        private Long targetId;
        private String targetType;
        private String createdAt;
    }

    @Data
    public static class AnnouncementVO {
        private Long id;
        private String title;
        private String content;
        private boolean pinned;
        private String publishAt;
    }
}
