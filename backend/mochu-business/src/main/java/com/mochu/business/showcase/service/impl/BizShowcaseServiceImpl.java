package com.mochu.business.showcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mochu.business.project.entity.BizProject;
import com.mochu.business.project.mapper.BizProjectMapper;
import com.mochu.business.showcase.dto.ShowcaseCreateDTO;
import com.mochu.business.showcase.dto.ShowcaseQueryDTO;
import com.mochu.business.showcase.entity.BizShowcase;
import com.mochu.business.showcase.entity.BizShowcaseImage;
import com.mochu.business.showcase.mapper.BizShowcaseImageMapper;
import com.mochu.business.showcase.mapper.BizShowcaseMapper;
import com.mochu.business.showcase.service.BizShowcaseService;
import com.mochu.business.showcase.vo.ShowcaseImageVO;
import com.mochu.business.showcase.vo.ShowcaseVO;
import com.mochu.common.constant.Constants;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BizShowcaseServiceImpl implements BizShowcaseService {

    private final BizShowcaseMapper showcaseMapper;
    private final BizShowcaseImageMapper showcaseImageMapper;
    private final BizProjectMapper projectMapper;
    private final SysUserMapper userMapper;

    private static final Map<Integer, String> SHOWCASE_STATUS = Map.of(
            1, "草稿", 2, "待审批", 3, "已发布", 4, "已驳回"
    );

    private static final Map<Integer, String> VISIBILITY_NAME = Map.of(
            1, "公开", 2, "仅内部"
    );

    // ==================== CREATE ====================

    @Override
    @Transactional
    public Long createShowcase(ShowcaseCreateDTO dto) {
        validateProject(dto.getProjectId());

        BizShowcase showcase = new BizShowcase();
        showcase.setProjectId(dto.getProjectId());
        showcase.setTitle(dto.getTitle());
        showcase.setDescription(dto.getDescription());
        showcase.setCoverUrl(dto.getCoverUrl());
        showcase.setSortOrder(0);
        showcase.setStatus(1); // draft
        showcase.setCreatorId(SecurityUtils.getCurrentUserId());
        showcaseMapper.insert(showcase);

        // Save regular images
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (ShowcaseCreateDTO.ImageItem item : dto.getImages()) {
                BizShowcaseImage image = new BizShowcaseImage();
                image.setShowcaseId(showcase.getId());
                image.setImageUrl(item.getImageUrl());
                image.setCaption(item.getCaption());
                image.setSortOrder(item.getSortOrder());
                image.setCreatedAt(LocalDateTime.now());
                showcaseImageMapper.insert(image);
            }
        }

        // Save videoUrl as special image record
        if (dto.getVideoUrl() != null && !dto.getVideoUrl().isBlank()) {
            BizShowcaseImage videoImage = new BizShowcaseImage();
            videoImage.setShowcaseId(showcase.getId());
            videoImage.setImageUrl(dto.getVideoUrl());
            videoImage.setCaption("VIDEO");
            videoImage.setSortOrder(0);
            videoImage.setCreatedAt(LocalDateTime.now());
            showcaseImageMapper.insert(videoImage);
        }

        // Save panoramaUrl as special image record
        if (dto.getPanoramaUrl() != null && !dto.getPanoramaUrl().isBlank()) {
            BizShowcaseImage panoramaImage = new BizShowcaseImage();
            panoramaImage.setShowcaseId(showcase.getId());
            panoramaImage.setImageUrl(dto.getPanoramaUrl());
            panoramaImage.setCaption("PANORAMA");
            panoramaImage.setSortOrder(0);
            panoramaImage.setCreatedAt(LocalDateTime.now());
            showcaseImageMapper.insert(panoramaImage);
        }

        log.info("Showcase created: id={}, title={}", showcase.getId(), showcase.getTitle());
        return showcase.getId();
    }

    // ==================== APPROVAL WORKFLOW ====================

    @Override
    @Transactional
    public void submitForApproval(Long id) {
        BizShowcase showcase = showcaseMapper.selectById(id);
        if (showcase == null) throw new BusinessException(404, "案例不存在");
        if (showcase.getStatus() != 1) throw new BusinessException("只有草稿状态可以提交审批");

        showcase.setStatus(2);
        showcaseMapper.updateById(showcase);

        log.info("Showcase submitted for approval: id={}", id);
    }

    @Override
    @Transactional
    public void approveShowcase(Long id, String comment) {
        validateApproveComment(comment);
        BizShowcase showcase = showcaseMapper.selectById(id);
        if (showcase == null) throw new BusinessException(404, "案例不存在");
        if (showcase.getStatus() != 2) throw new BusinessException("当前状态不支持审批");

        showcase.setStatus(3);
        showcaseMapper.updateById(showcase);

        log.info("Showcase approved: id={}, comment={}", id, comment);
    }

    @Override
    @Transactional
    public void rejectShowcase(Long id, String comment) {
        validateRejectComment(comment);
        BizShowcase showcase = showcaseMapper.selectById(id);
        if (showcase == null) throw new BusinessException(404, "案例不存在");
        if (showcase.getStatus() != 2) throw new BusinessException("当前状态不支持驳回");

        showcase.setStatus(4);
        showcaseMapper.updateById(showcase);

        log.info("Showcase rejected: id={}, comment={}", id, comment);
    }

    // ==================== VISIBILITY ====================

    @Override
    @Transactional
    public void setVisibility(Long id, Integer visibility) {
        if (visibility == null || (visibility != 1 && visibility != 2)) {
            throw new BusinessException("可见性参数无效，1=公开，2=仅内部");
        }
        BizShowcase showcase = showcaseMapper.selectById(id);
        if (showcase == null) throw new BusinessException(404, "案例不存在");

        // Use sortOrder field to store visibility temporarily
        // (BizShowcase entity does not have a visibility field yet;
        // for now we piggyback on the sortOrder concept -- but ideally
        // a migration adds a visibility column. We'll store it in a
        // special image record with caption="VISIBILITY".)
        // Actually let's just update via a raw wrapper to keep it clean:
        // We need a proper column. For now, we log and skip the actual persist
        // until migration adds the column. But per the requirement, let's
        // store it as described.

        // NOTE: The BizShowcase entity currently lacks a visibility column.
        // We store visibility as a special image record for now.
        LambdaQueryWrapper<BizShowcaseImage> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(BizShowcaseImage::getShowcaseId, id)
                  .eq(BizShowcaseImage::getCaption, "VISIBILITY");
        showcaseImageMapper.delete(delWrapper);

        BizShowcaseImage visibilityRecord = new BizShowcaseImage();
        visibilityRecord.setShowcaseId(id);
        visibilityRecord.setImageUrl(String.valueOf(visibility));
        visibilityRecord.setCaption("VISIBILITY");
        visibilityRecord.setSortOrder(0);
        visibilityRecord.setCreatedAt(LocalDateTime.now());
        showcaseImageMapper.insert(visibilityRecord);

        log.info("Showcase visibility updated: id={}, visibility={}", id, visibility);
    }

    // ==================== QUERY ====================

    @Override
    @Transactional(readOnly = true)
    public ShowcaseVO getShowcaseDetail(Long id) {
        BizShowcase showcase = showcaseMapper.selectById(id);
        if (showcase == null) throw new BusinessException(404, "案例不存在");

        return convertToVO(showcase);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<ShowcaseVO> listShowcases(ShowcaseQueryDTO query) {
        int page = query.getPage() != null ? query.getPage() : Constants.DEFAULT_PAGE;
        int size = Math.min(query.getSize() != null ? query.getSize() : Constants.DEFAULT_SIZE, Constants.MAX_SIZE);

        LambdaQueryWrapper<BizShowcase> wrapper = new LambdaQueryWrapper<>();
        if (query.getStatus() != null) {
            wrapper.eq(BizShowcase::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(BizShowcase::getSortOrder)
               .orderByDesc(BizShowcase::getCreatedAt);

        IPage<BizShowcase> pageResult = showcaseMapper.selectPage(new Page<>(page, size), wrapper);

        List<ShowcaseVO> voList = pageResult.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, pageResult.getTotal(), page, size);
    }

    // ==================== HELPER METHODS ====================

    private ShowcaseVO convertToVO(BizShowcase showcase) {
        ShowcaseVO vo = new ShowcaseVO();
        vo.setId(showcase.getId());
        vo.setProjectId(showcase.getProjectId());
        vo.setProjectName(getProjectName(showcase.getProjectId()));
        vo.setTitle(showcase.getTitle());
        vo.setDescription(showcase.getDescription());
        vo.setCoverUrl(showcase.getCoverUrl());
        vo.setSortOrder(showcase.getSortOrder());
        vo.setStatus(showcase.getStatus());
        vo.setStatusName(SHOWCASE_STATUS.getOrDefault(showcase.getStatus(), "未知"));
        vo.setCreatorName(getUserRealName(showcase.getCreatorId()));
        vo.setCreatedAt(showcase.getCreatedAt());
        vo.setViewCount(0L); // view_count column not yet added

        // Load images
        LambdaQueryWrapper<BizShowcaseImage> imgWrapper = new LambdaQueryWrapper<>();
        imgWrapper.eq(BizShowcaseImage::getShowcaseId, showcase.getId())
                  .orderByAsc(BizShowcaseImage::getSortOrder);
        List<BizShowcaseImage> allImages = showcaseImageMapper.selectList(imgWrapper);

        List<ShowcaseImageVO> imageVOs = new ArrayList<>();
        for (BizShowcaseImage img : allImages) {
            if ("VIDEO".equals(img.getCaption())) {
                vo.setVideoUrl(img.getImageUrl());
            } else if ("PANORAMA".equals(img.getCaption())) {
                vo.setPanoramaUrl(img.getImageUrl());
            } else if ("VISIBILITY".equals(img.getCaption())) {
                try {
                    int vis = Integer.parseInt(img.getImageUrl());
                    vo.setVisibility(vis);
                    vo.setVisibilityName(VISIBILITY_NAME.getOrDefault(vis, "未知"));
                } catch (NumberFormatException e) {
                    vo.setVisibility(1);
                    vo.setVisibilityName(VISIBILITY_NAME.get(1));
                }
            } else {
                ShowcaseImageVO imageVO = new ShowcaseImageVO();
                imageVO.setId(img.getId());
                imageVO.setImageUrl(img.getImageUrl());
                imageVO.setCaption(img.getCaption());
                imageVO.setSortOrder(img.getSortOrder());
                imageVOs.add(imageVO);
            }
        }
        vo.setImages(imageVOs);

        // Default visibility if not set
        if (vo.getVisibility() == null) {
            vo.setVisibility(1);
            vo.setVisibilityName(VISIBILITY_NAME.get(1));
        }

        return vo;
    }

    private void validateProject(Long projectId) {
        BizProject project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(404, "项目不存在");
        }
    }

    private void validateApproveComment(String comment) {
        if (comment == null || comment.length() < 2) {
            throw new BusinessException("审批意见至少2个字符");
        }
    }

    private void validateRejectComment(String comment) {
        if (comment == null || comment.length() < 5) {
            throw new BusinessException("驳回意见至少5个字符");
        }
    }

    private String getProjectName(Long projectId) {
        if (projectId == null) return null;
        BizProject project = projectMapper.selectById(projectId);
        return project != null ? project.getProjectName() : null;
    }

    private String getUserRealName(Long userId) {
        if (userId == null) return null;
        SysUser user = userMapper.selectById(userId);
        return user != null ? user.getRealName() : null;
    }
}
