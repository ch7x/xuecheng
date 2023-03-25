package com.xuecheng.content.service;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;

import java.util.List;

/**
 * 课程计划管理相关接口
 */
public interface TeachplanService {
    /**
     * 根据课程id查询课程计划
     * @param courseId - 课程id
     * @return 课程计划
     */
    public List<TeachplanDto> findTeachplanTree(Long courseId);

    /**
     * 新增/修改/保存课程计划
     * @param saveTeachplanDto
     */
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto);

    /**
     * 删除课程计划
     * @param id - 删除课程计划的id
     */
    public void deleteTeachplan(Long id);

    /**
     * 向下移动
     */
    public void moveDown(Long id);

    /**
     * 向上移动
     */
    public void moveUp(Long id);
}
