package com.xuecheng.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.mapper.TeachplanMapper;
import com.xuecheng.content.mapper.TeachplanMediaMapper;
import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.po.Teachplan;
import com.xuecheng.content.model.po.TeachplanMedia;
import com.xuecheng.content.service.TeachplanService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeachplanServiceImpl implements TeachplanService {

    @Autowired
    TeachplanMapper teachplanMapper;

    @Autowired
    TeachplanMediaMapper teachplanMediaMapper;

    @Override
    public List<TeachplanDto> findTeachplanTree(Long courseId) {
        List<TeachplanDto> teachplanDtos = teachplanMapper.selectTreeNodes(courseId);
        return teachplanDtos;
    }

    private int getTeachplanCount(Long courseId, Long parentid) {
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper
                .eq(Teachplan::getCourseId, courseId)
                .eq(Teachplan::getParentid, parentid);
        Integer count = teachplanMapper.selectCount(queryWrapper);
        return count + 1;
    }

    @Override
    public void saveTeachplan(SaveTeachplanDto saveTeachplanDto) {
        // 通过课程计划id来判断新增还是修改
        Long id = saveTeachplanDto.getId();
        if (id == null) {
            // 新增
            Teachplan teachplan = new Teachplan();
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            // 确定排序字段，找到同级节点个数，排序字段就是个数加1
            Long courseId = saveTeachplanDto.getCourseId();
            Long parentid = saveTeachplanDto.getParentid();
            int teachplanCount = getTeachplanCount(courseId, parentid);
            teachplan.setOrderby(teachplanCount);
            teachplanMapper.insert(teachplan);
        } else {
            // 修改
            Teachplan teachplan = teachplanMapper.selectById(id);
            BeanUtils.copyProperties(saveTeachplanDto, teachplan);
            teachplanMapper.updateById(teachplan);
        }
    }

    @Override
    public void deleteTeachplan(Long id) {
        // 通过id得到courseId
        Teachplan teachplan = teachplanMapper.selectById(id);
        Long courseId = teachplan.getCourseId();
        Long parentid = teachplan.getParentid();
        if (parentid == 0) {
            // 要删除的是大章节
            int i = getTeachplanCount(courseId, id);
            if (i == 1) {
                // 大章节下边没有小章节
                // 删除课程计划数据
                teachplanMapper.deleteById(id);
            } else {
                // 大章节下边有小章节
                XueChengPlusException.cast("课程计划信息还有子级信息，无法操作");
                return;
            }
        } else {
            // 删除第二级别的小章节的同时需要将teachplan_media表关联的信息也删除。
            teachplanMapper.deleteById(id);
            // 删除媒体相关数据
            LambdaQueryWrapper<TeachplanMedia> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper = queryWrapper.eq(TeachplanMedia::getTeachplanId, id);
            TeachplanMedia teachplanMedia = teachplanMediaMapper.selectOne(queryWrapper);
            if (teachplanMedia != null) {
                teachplanMediaMapper.delete(queryWrapper);
                XueChengPlusException.cast("媒资信息删除失败");
            }
        }
    }

    @Override
    public void moveDown(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        Integer orderby = teachplan.getOrderby();

        // 找到与其parent一样的组
        Long parentid = teachplan.getParentid();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentid);

        List<Teachplan> parents = teachplanMapper.selectList(queryWrapper);

        Teachplan t1 = new Teachplan();

        int i = orderby;
        // 找到最大的orderby作为临时值
        int t = parents.get(parents.size() - 1).getOrderby();
        for (Teachplan parent : parents) {
            // orderby值大于i  并且为所有临时值里最小
            if (parent.getOrderby() > i && parent.getOrderby() <= t) {
                t = parent.getOrderby();
                t1 = parent;
            }
        }
        // 进行orderby交换
        t1.setOrderby(orderby);
        teachplanMapper.updateById(t1);
        teachplan.setOrderby(t);
        teachplanMapper.updateById(teachplan);
    }

    @Override
    public void moveUp(Long id) {
        Teachplan teachplan = teachplanMapper.selectById(id);
        Integer orderby = teachplan.getOrderby();

        // 找到与其parent一样的组
        Long parentid = teachplan.getParentid();
        LambdaQueryWrapper<Teachplan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Teachplan::getParentid, parentid);

        List<Teachplan> parents = teachplanMapper.selectList(queryWrapper);

        Teachplan t1 = new Teachplan();

        int i = orderby;
        // 找到最小的orderby作为临时值
        int t = parents.get(0).getOrderby();
        for (Teachplan parent : parents) {
            // orderby值小于i  并且为所有临时值里最大
            if (parent.getOrderby() < i && parent.getOrderby() >= t) {
                t = parent.getOrderby();
                t1 = parent;
            }
        }
        // 进行orderby交换
        t1.setOrderby(orderby);
        teachplanMapper.updateById(t1);
        teachplan.setOrderby(t);
        teachplanMapper.updateById(teachplan);
    }
}
