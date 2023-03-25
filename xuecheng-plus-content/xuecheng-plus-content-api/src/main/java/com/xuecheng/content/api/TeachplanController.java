package com.xuecheng.content.api;

import com.xuecheng.content.model.dto.SaveTeachplanDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.service.TeachplanService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 课程计划管理相关的接口
 */

@Api(value = "课程计划编辑接口", tags = "课程计划编辑接口")
@RestController
public class TeachplanController {

    @Autowired
    TeachplanService teachplanService;

    @ApiOperation("查询课程计划树形结构")
    // 查询课程计划
    @GetMapping("/teachplan/{courseId}/tree-nodes")
    public List<TeachplanDto> getTreeNodes(@PathVariable Long courseId) {
        List<TeachplanDto> teachplanTree = teachplanService.findTeachplanTree(courseId);
        return teachplanTree;
    }

    @ApiOperation("课程计划创建或修改")
    @PostMapping("/teachplan")
    public void saveTeachplan(@RequestBody SaveTeachplanDto teachplan) {
        teachplanService.saveTeachplan(teachplan);
    }

    @ApiOperation("课程计划删除")
    @DeleteMapping("/teachplan/{id}")
    public void deleteTeachplan(@PathVariable Long id){
        teachplanService.deleteTeachplan(id);
    }

    /**
     * Request URL: http://localhost:8601/api/content/teachplan/movedown/43
     * Request Method: POST
     * 参数1：movedown  为 移动类型，表示向下移动
     * 参数2：43为课程计划id
     */
    @ApiOperation("向下移动")
    @PostMapping("/teachplan/movedown/{id}")
    public void moveDown(@PathVariable Long id){
        teachplanService.moveDown(id);
    }

    @ApiOperation("向上移动")
    @PostMapping("/teachplan/moveup/{id}")
    public void moveUp(@PathVariable Long id){
        teachplanService.moveUp(id);
    }
}
