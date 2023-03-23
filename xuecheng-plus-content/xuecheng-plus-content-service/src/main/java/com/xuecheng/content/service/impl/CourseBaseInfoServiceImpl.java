package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.model.dto.AddCourseDto;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.QueryCourseParamsDto;
import com.xuecheng.content.model.po.CourseBase;
import com.xuecheng.content.model.po.CourseCategory;
import com.xuecheng.content.model.po.CourseMarket;
import com.xuecheng.content.service.CourseBaseInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class CourseBaseInfoServiceImpl implements CourseBaseInfoService {

    @Autowired
    CourseBaseMapper courseBaseMapper;

    @Autowired
    CourseMarketMapper courseMarketMapper;

    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public PageResult<CourseBase> queryCourseBaseList(PageParams pageParams, QueryCourseParamsDto courseParamsDto) {
        // 拼装查询条件
        // 课程名称查询条件
        LambdaQueryWrapper<CourseBase> queryWrapper = new LambdaQueryWrapper<CourseBase>();
        // 根据名称模糊查询
        queryWrapper.like(StringUtils.isNotEmpty(courseParamsDto.getCourseName()), CourseBase::getName, courseParamsDto.getCourseName());
        // 根据课程审核状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getAuditStatus()), CourseBase::getAuditStatus, courseParamsDto.getAuditStatus());
        // 根据课程发布状态查询
        queryWrapper.eq(StringUtils.isNotEmpty(courseParamsDto.getPublishStatus()), CourseBase::getStatus, courseParamsDto.getPublishStatus());

        // page参数
        IPage<CourseBase> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());

        // 开始进行分页查询
        IPage<CourseBase> pageResult = courseBaseMapper.selectPage(page, queryWrapper);
        // 数据列表
        List<CourseBase> items = pageResult.getRecords();
        long total = pageResult.getTotal();
        PageResult<CourseBase> courseBasePageResult = new PageResult<CourseBase>(items, total, pageParams.getPageNo(), pageParams.getPageSize());
        return courseBasePageResult;
    }

    @Transactional
    @Override
    public CourseBaseInfoDto createCourseBase(Long companyId, AddCourseDto dto) {
        // 参数的合法性校验
        if (StringUtils.isBlank(dto.getName())) {
//            throw new RuntimeException("课程名称为空");
            XueChengPlusException.cast("课程名称为空");
        }

        if (StringUtils.isBlank(dto.getMt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getSt())) {
            throw new RuntimeException("课程分类为空");
        }

        if (StringUtils.isBlank(dto.getGrade())) {
            throw new RuntimeException("课程等级为空");
        }

        if (StringUtils.isBlank(dto.getTeachmode())) {
            throw new RuntimeException("教育模式为空");
        }

        if (StringUtils.isBlank(dto.getUsers())) {
            throw new RuntimeException("适应人群为空");
        }

        if (StringUtils.isBlank(dto.getCharge())) {
            throw new RuntimeException("收费规则为空");
        }

        // 向课程基本信息表course_base写入数据
        CourseBase courseBaseNew = new CourseBase();
        // 将页面传入的参数放入 courseBaseNew 对象
        BeanUtils.copyProperties(dto, courseBaseNew);    // 只要属性名称一致就可以拷贝
        courseBaseNew.setCompanyId(companyId);
        courseBaseNew.setCreateDate(LocalDateTime.now());
        // 审核状态默认为未提交
        courseBaseNew.setAuditStatus("202002");
        // 发布状态为未发布
        courseBaseNew.setStatus("203001");
        // 插入数据库
        int insert = courseBaseMapper.insert(courseBaseNew);
        if (insert <= 0) {  // 添加失败
            throw new RuntimeException("添加课程失败");
        }

        // 向课程营销表course_market写入数据
        CourseMarket courseMarketNew = new CourseMarket();
        // 将页面输入的数据拷贝到courseMarketNew
        BeanUtils.copyProperties(dto, courseMarketNew);
        // 主键就是课程的id(要从刚插入的课程信息表中得到，使得两个id一致)
        Long courseId = courseBaseNew.getId();
        courseMarketNew.setId(courseId);
        // 保存营销信息
        saveCourseMarket(courseMarketNew);

        // 从数据库查询课程的详细信息，包括两部分
        CourseBaseInfoDto courseBaseInfo = getCourseBaseInfo(courseId);
        return courseBaseInfo;
    }

    // 单独写一个方法保存营销信息，逻辑：存在就更新，不存在则添加
    private int saveCourseMarket(CourseMarket courseMarketNew) {
        // 参数的合法性校验
        String charge = courseMarketNew.getCharge();
        if (StringUtils.isEmpty(charge)) {
            throw new RuntimeException("收费规则为空");
        }
        // 如果课程收费，价格没有填写也要抛出异常
        if (charge.equals("201001")) {
            if (courseMarketNew.getPrice() == null || courseMarketNew.getPrice() <= 0) {
//                throw new RuntimeException("课程为收费价格不能为空且必须大于0");
                XueChengPlusException.cast("课程为收费价格不能为空且必须大于0");
            }
        }

        // 从数据库查看营销信息，存在就更新，不存在则添加
        Long id = courseMarketNew.getId();
        CourseMarket courseMarket = courseMarketMapper.selectById(id);
        if (courseMarket == null){
            // 插入数据库
            int insert = courseMarketMapper.insert(courseMarketNew);
            return insert;
        }else {
            // 将courseMarketNew拷贝到courseMarket
            BeanUtils.copyProperties(courseMarketNew,courseMarket);
            courseMarket.setId(courseMarketNew.getId());
            // 更新
            int i = courseMarketMapper.updateById(courseMarket);
            return i;
        }
    }

    // 查询课程信息
    public CourseBaseInfoDto getCourseBaseInfo(long courseId){
        // 从课程基本信息表查询
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        if (courseBase == null){
            return null;
        }
        // 从课程营销表查询
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);

        // 结合在一起
        CourseBaseInfoDto courseBaseInfoDto = new CourseBaseInfoDto();
        BeanUtils.copyProperties(courseBase,courseBaseInfoDto);
        if (courseMarket != null){
            BeanUtils.copyProperties(courseMarket,courseBaseInfoDto);
        }
        // 通过 courseCategoryMapper 查出分类信息，将分类名称放在courseBaseInfoDto对象中
        CourseCategory courseCategoryMt = courseCategoryMapper.selectById(courseBase.getMt());
        courseBaseInfoDto.setMtName(courseCategoryMt.getName());

        CourseCategory courseCategorySt = courseCategoryMapper.selectById(courseBase.getSt());
        courseBaseInfoDto.setMtName(courseCategorySt.getName());

        return courseBaseInfoDto;
    }
}
