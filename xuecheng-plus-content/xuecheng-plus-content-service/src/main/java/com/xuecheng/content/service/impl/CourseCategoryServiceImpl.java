package com.xuecheng.content.service.impl;

import com.xuecheng.content.mapper.CourseCategoryMapper;
import com.xuecheng.content.model.dto.CourseCategoryTreeDto;
import com.xuecheng.content.service.CourseCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CourseCategoryServiceImpl implements CourseCategoryService {
    @Autowired
    CourseCategoryMapper courseCategoryMapper;

    @Override
    public List<CourseCategoryTreeDto> queryTreeNodes(String id) {
        // 调用mapper递归查询出分类信息
        List<CourseCategoryTreeDto> courseCategoryTreeDtos = courseCategoryMapper.selectTreeNodes(id);

        // 找到每个节点的子节点，最后封装成List<CourseCategoryTreeDto>对象
        // 先将List转为map，key就是节点的id，value就是CourseCategoryTreeDto对象，目的是为了方便从map获取节点
        // filter(item -> !id.equals(item.getId()))把根节点排除
        Map<String, CourseCategoryTreeDto> mapTemp = courseCategoryTreeDtos.stream()
                .filter(item -> !id.equals(item.getId()))
                .collect(Collectors.toMap(key -> key.getId(), value -> value, (key1, key2) -> key2));

        // 定义一个List作为最终返回的List
        List<CourseCategoryTreeDto> courseCategoryList = new ArrayList<>();

        // 从头遍历List<CourseCategoryTreeDto>，一边遍历一边找子节点放在父节点的childrenTreeNodes
        courseCategoryTreeDtos.stream().filter(item -> !id.equals(item.getId())).forEach(item -> {
            // 向List写入元素
            if (item.getParentid().equals(id)){
                courseCategoryList.add(item);
            }
            // 找到节点的父节点
            CourseCategoryTreeDto courseCategoryParent = mapTemp.get(item.getParentid());
            // 如果该节点的父节点的 childrenTreeNodes 属性为空，则new一个为了使其可以添加
            if (courseCategoryParent != null){
                if (courseCategoryParent.getChildrenTreeNodes() == null){
                    courseCategoryParent.setChildrenTreeNodes(new ArrayList<CourseCategoryTreeDto>());
                }
                // 找到每个节点的子节点放在父节点的 childrenTreeNodes 属性中
                courseCategoryParent.getChildrenTreeNodes().add(item);
            }
        });
        return courseCategoryList;
    }
}
