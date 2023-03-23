package com.xuecheng.content;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CourseBaseMapperTests {
    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Test
    void testCourseBaseMapper() {
        // 详细进行分页查询单元测试

        // 拼装查询条件
        QueryCourseParamsDto courseParamsDto = new QueryCourseParamsDto();
        // 课程名称查询条件
        courseParamsDto.setCourseName("java");
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<CourseBase>();
        // 根据名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        // 根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());

        // page参数
        PageParams pageParams = new PageParams();
        // 第一页
        pageParams.setPageNo(1L);
        // 一页两条内容
        pageParams.setPageSize(2L);
        IPage<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        // 开始进行分页查询
        IPage<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 数据列表
        List<CourseBase> items = pageResult.getRecords();
        long total = pageResult.getTotal();
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
        System.out.println(courseBasePageResult);
    }
}
