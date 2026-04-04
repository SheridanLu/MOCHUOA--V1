package com.mochu.system.service.impl;

import com.mochu.common.utils.SecurityUtils;
import com.mochu.system.service.HomeService;
import com.mochu.system.vo.HomeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    @Override
    public HomeVO getHomeData() {
        HomeVO vo = new HomeVO();
        vo.setShortcuts(getDefaultShortcuts());
        vo.setTodoList(new ArrayList<>());
        vo.setTodoCount(0);
        vo.setAnnouncements(new ArrayList<>());
        return vo;
    }

    private List<HomeVO.ShortcutVO> getDefaultShortcuts() {
        List<HomeVO.ShortcutVO> shortcuts = new ArrayList<>();
        addShortcut(shortcuts, "project", "项目管理", "Folder", "/project");
        addShortcut(shortcuts, "contract", "合同管理", "Document", "/contract");
        addShortcut(shortcuts, "material", "物资管理", "Box", "/material");
        addShortcut(shortcuts, "finance", "财务管理", "Money", "/finance");
        return shortcuts;
    }

    private void addShortcut(List<HomeVO.ShortcutVO> list, String code, String name, String icon, String path) {
        HomeVO.ShortcutVO s = new HomeVO.ShortcutVO();
        s.setCode(code); s.setName(name); s.setIcon(icon); s.setPath(path);
        list.add(s);
    }
}
