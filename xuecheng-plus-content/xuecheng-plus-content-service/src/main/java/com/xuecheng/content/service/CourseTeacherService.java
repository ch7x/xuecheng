package com.xuecheng.content.service;

import com.xuecheng.content.model.po.CourseTeacher;

import java.util.List;

/**
 * 师资管理相关接口
 */
public interface CourseTeacherService {
    /**
     * 通过课程id查询教师管理
     *
     * @param courseId - 课程id
     * @return - 教师管理页面
     */
    public List<CourseTeacher> getCourseTeacherList(Long courseId);

    /**
     * 新增教师
     *
     * @param courseTeacher - 教师数据
     */
    public void saveCourseTeacher(Long companyId, CourseTeacher courseTeacher);

    /**
     * 删除教师
     * @param companyId - 机构id
     * @param courseId - 课程id
     * @param id - 教师id
     */
    public void deleteCourseTeacher(Long companyId, Long courseId, Long id);
}
